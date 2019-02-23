package com.pogoaccountchecker.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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

public class AccountCheckingService extends Service implements AccountChecker.OnAccountCheckingStatusChangedListener {
    private static final int NOTIFICATION_ID = 1;
    private AccountChecker mAccountChecker;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private boolean mRunning;

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

         mNotificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setSmallIcon(R.drawable.ic_outline_running_24px)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true);

        Notification notification = mNotificationBuilder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mRunning) {
            ArrayList<String> accounts = intent.getStringArrayListExtra("accounts");
            mAccountChecker = new AccountChecker(this, accounts, ',', this);

            String notificationText = "0/" + accounts.size() + " have been checked.";
            updateNotificationText(notificationText);

            // Run account checking on different thread so that the UI thread does not get blocked.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mAccountChecker.start();
                }
            }).start();
            mRunning = true;
        } else {
            Toast.makeText(this, "Account check is already running!", Toast.LENGTH_LONG).show();
        }

        return START_NOT_STICKY;
    }

    private void updateNotificationText(String text) {
        mNotificationBuilder.setContentText(text);
        Notification notification = mNotificationBuilder.build();
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onFinished() {
        mRunning = false;
        stopSelf();
    }

    @Override
    public void onProgressChanged(int numChecked, int numAccounts) {
        String newText = numChecked + "/" + numAccounts + " have been checked.";
        updateNotificationText(newText);
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
