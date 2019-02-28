package com.pogoaccountchecker.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.pogoaccountchecker.AccountChecker;
import com.pogoaccountchecker.R;
import com.pogoaccountchecker.activities.MainActivity;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.pogoaccountchecker.App.FOREGROUND_NOTIFICATION_ID;
import static com.pogoaccountchecker.App.NOTIFICATION_CHANNEL_ID;

public class AccountCheckingService extends Service {
    private AccountChecker mAccountChecker;
    private NotificationCompat.Builder mNotificationBuilder;
    private volatile boolean mIsChecking, mIsPaused;
    private final IBinder binder = new AccountCheckingServiceBinder();

    public class AccountCheckingServiceBinder extends Binder {
        public AccountCheckingService getService() {
            return AccountCheckingService.this;
        }
    }

    @Override
    public void onCreate() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        mNotificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setSmallIcon(R.drawable.ic_outline_running_24px)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true);

        mAccountChecker = new AccountChecker(this, mNotificationBuilder);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        if (mIsChecking) mAccountChecker.stop();
    }

    public void checkAccounts(final List<String> accounts, final char delimiter) {
        if (!mIsChecking) {
            // Run account checking on different thread so that the UI thread does not get blocked.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mAccountChecker.checkAccounts(accounts, delimiter);
                    stopForeground(true);
                    mIsChecking = false;
                }
            }).start();

            // Make sure service is not killed when clients unbind.
            Intent intent = new Intent(this, AccountCheckingService.class);
            startService(intent);

            Notification notification = mNotificationBuilder.build();
            startForeground(FOREGROUND_NOTIFICATION_ID, notification);

            mIsChecking = true;
        }
    }

    public void pauseChecking() {
        if (mIsChecking) {
            mIsPaused = true;
            mAccountChecker.pause();
        }
    }

    public void continueChecking() {
        if (mIsChecking) {
            mIsPaused = false;
            mAccountChecker.resume();
        }
    }

    public void stopChecking() {
        if (mIsChecking) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mAccountChecker.stop();
                }
            }).start();
            stopForeground(true);
            mIsChecking = false;
        }
    }

    public boolean isChecking() {
        return mIsChecking;
    }

    public boolean isPaused() {
        return mIsPaused;
    }
}
