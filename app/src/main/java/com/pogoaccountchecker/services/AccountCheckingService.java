package com.pogoaccountchecker.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.pogoaccountchecker.AccountChecker;
import com.pogoaccountchecker.R;
import com.pogoaccountchecker.activities.MainActivity;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.pogoaccountchecker.App.NOTIFICATION_CHANNEL_ID;

public class AccountCheckingService extends Service implements AccountChecker.OnAccountCheckingFinishedListener {
    private static final int NOTIFICATION_ID = 1;
    private AccountChecker mAccountChecker;
    private boolean mRunning;

    @Override
    public void onCreate() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_outline_running_24px)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mRunning) {
            ArrayList<String> accounts = intent.getStringArrayListExtra("accounts");
            mAccountChecker = new AccountChecker(this, accounts, ',', this);
            // Run account checking on different thread so that the UI thread does not get blocked.
            Thread workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    mAccountChecker.start();
                }
            });
            workerThread.start();
            mRunning = true;
        } else {
            Toast.makeText(this, "Account check is already running!", Toast.LENGTH_LONG).show();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onAccountCheckingFinished() {
        mRunning = false;
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mRunning) mAccountChecker.stop();
    }
}
