package com.pogoaccountchecker.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.pogoaccountchecker.R;
import com.pogoaccountchecker.activities.MainActivity;
import com.pogoaccountchecker.activities.ResultActivity;
import com.pogoaccountchecker.interactors.PogoInteractor;
import com.pogoaccountchecker.interactors.PogoInteractor.LoginResult;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import static com.pogoaccountchecker.App.NOTIFICATION_CHANNEL_ID;

public class AccountCheckingService extends Service {
    private PogoInteractor mPogoInteractor;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilderChecking;
    private volatile boolean mChecking, mPaused, mStopped;
    private int mAccountCount;
    private int mNotBannedCount, mBannedCount, mWrongCredentialsCount, mNotActivatedCount, mLockedCount, mErrorCount;
    private final String PATHNAME = Environment.getExternalStorageDirectory().getPath() + "/PogoAccountChecker";
    private final IBinder binder = new AccountCheckingServiceBinder();
    private final String LOG_TAG = getClass().getSimpleName();
    public final int CHECKING_NOTIFICATION_ID = 1;
    public final int FINISHED_NOTIFICATION_ID = 2;

    public class AccountCheckingServiceBinder extends Binder {
        public AccountCheckingService getService() {
            return AccountCheckingService.this;
        }
    }

    @Override
    public void onCreate() {
        mPogoInteractor = new PogoInteractor(this);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        mNotificationBuilderChecking = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setSmallIcon(R.drawable.ic_outline_running_24px)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true);
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
        if (mChecking) stop();
    }

    private LoginResult checkAccount(String account, char delimiter) {
        int index = account.indexOf(delimiter);
        String username = account.substring(0, index);
        String password = account.substring(index + 1);

        boolean dateOfBirthEntered = false;
        int errorCount = 0;

        while (errorCount != 10) {
            while (mPaused && !mStopped) {
                Utils.sleep(2000);
            }

            if (!dateOfBirthEntered) {
                // Clear app data.
                if (!mPogoInteractor.clearAppData()) {
                    if (mStopped) return LoginResult.INTERRUPTED;
                    if (mPaused) continue;
                    errorCount++;
                    continue;
                }

                // Start Pogo.
                if (!mPogoInteractor.startPogo()) {
                    if (mStopped) return LoginResult.INTERRUPTED;
                    if (mPaused) continue;
                    errorCount++;
                    continue;
                }

                // Check if we are on the DOB screen.
                if (!mPogoInteractor.isOnDateOfBirthScreen(20)) {
                    if (mStopped) return LoginResult.INTERRUPTED;
                    if (mPaused) continue;
                    errorCount++;
                    continue;
                }

                // Select date of birth.
                if (!mPogoInteractor.selectDateOfBirth()) {
                    if (mStopped) return LoginResult.INTERRUPTED;
                    if (mPaused) continue;
                    errorCount++;
                    continue;
                } else {
                    dateOfBirthEntered = true;
                }

                // Wait while pogo transitions to the returning/new player selection screen.
                Utils.sleep(Utils.randomWithRange(450, 550));

                // Select returning player.
                if (!mPogoInteractor.selectReturningPlayer()) {
                    if (mStopped) return LoginResult.INTERRUPTED;
                    if (mPaused) continue;
                    mPogoInteractor.stopPogo();
                    errorCount++;
                    continue;
                }
            } else {
                // Start Pogo.
                if (!mPogoInteractor.startPogo()) {
                    if (mStopped) return LoginResult.INTERRUPTED;
                    if (mPaused) continue;
                    errorCount++;
                    continue;
                }

                // Check if we are on the returning player selection screen.
                if (!mPogoInteractor.isOnReturningPlayerSelection(20)) {
                    if (mStopped) return LoginResult.INTERRUPTED;
                    if (mPaused) continue;
                    dateOfBirthEntered = false; // DOB might not be entered.
                    mPogoInteractor.stopPogo();
                    errorCount++;
                    continue;
                }

                // Select returning player.
                if (!mPogoInteractor.selectReturningPlayer()) {
                    if (mStopped) return LoginResult.INTERRUPTED;
                    if (mPaused) continue;
                    mPogoInteractor.stopPogo();
                    errorCount++;
                    continue;
                }
            }

            // Wait while pogo transitions to the account type selection screen.
            Utils.sleep(Utils.randomWithRange(450, 550));

            // Select PTC.
            if (!mPogoInteractor.selectPTC()) {
                if (mStopped) return LoginResult.INTERRUPTED;
                if (mPaused) continue;
                mPogoInteractor.stopPogo();
                errorCount++;
                continue;
            }

            // Wait while pogo transitions to login screen.
            Utils.sleep(Utils.randomWithRange(450, 550));

            // Login.
            if (!mPogoInteractor.login(username, password)) {
                if (mStopped) return LoginResult.INTERRUPTED;
                if (mPaused) continue;
                mPogoInteractor.stopPogo();
                errorCount++;
                continue;
            }

            LoginResult loginResult = mPogoInteractor.getLoginResult(20);

            if (loginResult == LoginResult.NOT_BANNED) {
                Log.i(LOG_TAG, "Account " + account + " is not banned.");
                mNotBannedCount++;
                Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_banned.txt");
                return LoginResult.NOT_BANNED;
            } else if (loginResult == LoginResult.BANNED) {
                Log.i(LOG_TAG, "Account " + account + " is banned.");
                mBannedCount++;
                Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/banned.txt");
                return LoginResult.BANNED;
            } else if (loginResult == LoginResult.WRONG_CREDENTIALS) {
                Log.i(LOG_TAG, "Account " + account + " does not exist or the credentials are wrong.");
                mWrongCredentialsCount++;
                Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/wrong_credentials.txt");
                return LoginResult.WRONG_CREDENTIALS;
            } else if (loginResult == LoginResult.NOT_ACTIVATED) {
                Log.i(LOG_TAG, "Account " + account + " is not activated.");
                mNotActivatedCount++;
                Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_activated.txt");
                return LoginResult.NOT_ACTIVATED;
            } else if (loginResult == LoginResult.LOCKED) {
                Log.i(LOG_TAG, "Account " + account + " is locked.");
                mLockedCount++;
                Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/locked.txt");
                return LoginResult.LOCKED;
            } else if (loginResult == LoginResult.ERROR) {
                Log.e(LOG_TAG, "Error when trying to detect login result for account " + account + ".");
                mPogoInteractor.stopPogo();
                errorCount++;
            } else if (loginResult == LoginResult.INTERRUPTED) {
                if (mStopped) return LoginResult.INTERRUPTED;
            }
        }

        mErrorCount++;
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/error.txt");
        Log.e(LOG_TAG, "Error limit reached. Wrote account " + account + " to error.txt.");
        return LoginResult.ERROR;
    }

    public void checkAccounts(final List<String> accounts, final char delimiter) {
        if (mChecking) return;

        // Make sure service is not killed when clients unbind.
        Intent intent = new Intent(this, AccountCheckingService.class);
        startService(intent);

        mPaused = mStopped = false;
        mChecking = true;
        mNotBannedCount = mBannedCount = mWrongCredentialsCount = mNotActivatedCount = mLockedCount = mErrorCount = 0;
        mPogoInteractor.resume();

        mAccountCount = accounts.size();

        startForeground(CHECKING_NOTIFICATION_ID, mNotificationBuilderChecking.build());
        updateCheckingNotificationText("Checked: 0/" + mAccountCount);

        if (!Shell.runSuCommand("mkdir " + PATHNAME)) {
            Log.e(LOG_TAG, "Couldn't create folder, aborting program!");
            System.exit(0);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<mAccountCount; i++) {
                    LoginResult result = checkAccount(accounts.get(i), delimiter);
                    Log.d(LOG_TAG, result + "");
                    updateCheckingNotificationText("Checked: " + getCheckedCount() + "/" + mAccountCount);
                }

                mChecking = false;
                stopForeground(true);
                startResultActivity();
                showFinishedNotification("Account checking finished", getStats());
                mPogoInteractor.cleanUp();
                Log.i(LOG_TAG, "Account checking finished. " + getStats());
            }
        }).start();
    }

    public void pause() {
        mPaused = true;
        mPogoInteractor.interrupt();
        updateCheckingNotificationTitle("Account checking paused");
    }

    public void resume() {
        mPaused = false;
        mPogoInteractor.resume();
        updateCheckingNotificationTitle("Checking accounts…");
    }

    public void stop() {
        if (!mChecking) return;

        mChecking = false;
        mStopped = true;
        mPogoInteractor.interrupt();
        showFinishedNotification("Account checking stopped", getStats());
        startResultActivity();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mPogoInteractor.cleanUp();
            }
        }).start();
    }

    public boolean isChecking() {
        return mChecking;
    }

    public boolean isPaused() {
        return mPaused;
    }

    private void updateCheckingNotificationTitle(String text) {
        mNotificationBuilderChecking.setContentTitle(text);
        Notification notification = mNotificationBuilderChecking.build();
        mNotificationManager.notify(CHECKING_NOTIFICATION_ID, notification);
    }

    private void updateCheckingNotificationText(String text) {
        mNotificationBuilderChecking.setContentText(text);
        Notification notification = mNotificationBuilderChecking.build();
        mNotificationManager.notify(CHECKING_NOTIFICATION_ID, notification);
    }

    private void showFinishedNotification(String title, String text) {
        Intent resultIntent = new Intent(this, ResultActivity.class);
        resultIntent.putExtra("accountCount", mAccountCount);
        resultIntent.putExtra("notBannedCount", mNotBannedCount);
        resultIntent.putExtra("bannedCount", mBannedCount);
        resultIntent.putExtra("wrongCredentialsCount", mWrongCredentialsCount);
        resultIntent.putExtra("notActivatedCount", mNotActivatedCount);
        resultIntent.putExtra("lockedCount", mLockedCount);
        resultIntent.putExtra("errorCount", mErrorCount);
        resultIntent.putExtra("stopped", mStopped);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_outline_done_24px)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        mNotificationManager.notify(FINISHED_NOTIFICATION_ID, builder.build());
    }

    private void startResultActivity() {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("accountCount", mAccountCount);
        intent.putExtra("notBannedCount", mNotBannedCount);
        intent.putExtra("bannedCount", mBannedCount);
        intent.putExtra("wrongCredentialsCount", mWrongCredentialsCount);
        intent.putExtra("notActivatedCount", mNotActivatedCount);
        intent.putExtra("lockedCount", mLockedCount);
        intent.putExtra("errorCount", mErrorCount);
        intent.putExtra("stopped", mStopped);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);

        startActivities(stackBuilder.getIntents());
    }

    private int getCheckedCount() {
        return mNotBannedCount + mBannedCount + mWrongCredentialsCount + mNotActivatedCount + mLockedCount + mErrorCount;
    }

    private String getStats() {
        String stats = "";
        if (mNotBannedCount > 0) {
            stats += "not banned: " + mNotBannedCount + ", ";
        }
        if (mBannedCount > 0) {
            stats += "banned: " + mBannedCount + ", ";
        }
        if (mWrongCredentialsCount > 0) {
            stats += "wrong un/pass: " + mWrongCredentialsCount + ", ";
        }
        if (mNotActivatedCount > 0) {
            stats += "not activated: " + mNotActivatedCount + ", ";
        }
        if (mLockedCount > 0) {
            stats += "locked: " + mLockedCount + ", ";
        }
        if (mErrorCount > 0) {
            stats += "couldn't be checked: " + mErrorCount + "  ";
        }

        int checkedCount = mNotBannedCount + mBannedCount + mWrongCredentialsCount + mNotActivatedCount + mLockedCount + mErrorCount;

        if (stats.isEmpty()) {
            return "Checked: " + checkedCount + "/" + mAccountCount;
        } else {
            return "Checked: " + checkedCount + "/" + mAccountCount + ", " + stats.substring(0, stats.length()-2);
        }
    }
}
