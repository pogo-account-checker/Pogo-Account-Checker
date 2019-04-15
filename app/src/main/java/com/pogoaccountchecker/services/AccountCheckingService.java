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
import android.widget.Toast;

import com.pogoaccountchecker.R;
import com.pogoaccountchecker.activities.MainActivity;
import com.pogoaccountchecker.activities.ResultActivity;
import com.pogoaccountchecker.interactors.PogoInteractor;
import com.pogoaccountchecker.interactors.PogoInteractor.Screen;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;
import com.pogoaccountchecker.websocket.MadWebSocket;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import static com.pogoaccountchecker.App.NOTIFICATION_CHANNEL_ID;

public class AccountCheckingService extends Service implements MadWebSocket.OnWebSocketEventListener {
    private PogoInteractor mPogoInteractor;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilderChecking;
    private MadWebSocket mWebSocket;
    private char mDelimiter;
    private volatile boolean mChecking, mPaused, mStopped;
    private int mAccountCount;
    private int mNotBannedCount, mBannedCount, mNewCount, mWrongCredentialsCount, mNotActivatedCount, mLockedCount, mErrorCount;
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

    @Override
    public void onConnected() {
        // Make sure service is not killed when client unbinds.
        initialize();
    }

    @Override
    public void onNotConnected() {
        Toast.makeText(AccountCheckingService.this, "Couldn't connect to MAD server.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisconnected(boolean closedByServer) {

    }

    @Override
    public void onMessageReceived(final String message) {
        if (message.contains("account_count") && !message.contains(String.valueOf(mDelimiter))) {
            mAccountCount = Integer.parseInt(message.substring("account_count ".length()));
            updateCheckingNotificationText("Checked: 0/" + mAccountCount);
        } else if (message.contains("finished") && !message.contains(String.valueOf(mDelimiter))) {
            mChecking = false;
            stopForeground(true);
            showFinishedNotification("Account checking finished", getStats());

            if (message.contains("true")) {
                startResultActivity();
                mPogoInteractor.cleanUp(true);
            } else if (message.contains("false")) {
                mPogoInteractor.cleanUp(false);
            }
        } else if (message.contains(String.valueOf(mDelimiter))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AccountStatus status = checkAccount(message, mDelimiter);
                    switch (status) {
                        case NOT_BANNED:
                            mWebSocket.sendMessage("not_banned");
                            break;
                        case BANNED:
                            mWebSocket.sendMessage("banned");
                            break;
                        case NEW:
                            mWebSocket.sendMessage("new");
                            break;
                        case NOT_ACTIVATED:
                            mWebSocket.sendMessage("not_activated");
                            break;
                        case LOCKED:
                            mWebSocket.sendMessage("locked");
                            break;
                        case WRONG_CREDENTIALS:
                            mWebSocket.sendMessage("wrong_credentials");
                            break;
                        default:
                            mWebSocket.sendMessage("not_checked");
                    }
                }
            }).start();
        } else {
            Shell.runSuCommand("echo '" + message + "' >> " + PATHNAME + "/error.txt");
        }
    }

    public enum AccountStatus {
        NOT_BANNED, BANNED, NEW, NOT_ACTIVATED, LOCKED, WRONG_CREDENTIALS, NOT_CHECKED
    }

    private AccountStatus checkAccount(String account, char delimiter) {
        int index = account.indexOf(delimiter);
        String username = account.substring(0, index);
        String password = account.substring(index + 1);

        int wrongScreenCount = 0;
        Screen currentScreen = Screen.UNKNOWN;

        // Clear app data.
        mPogoInteractor.clearAppData();

        int errorCount = 0;
        while (errorCount != 10) {
            if (!mStopped) {
                while (mPaused) {
                    Utils.sleep(2000);
                }
                mPogoInteractor.stopPogo();
            } else {
                return AccountStatus.NOT_CHECKED;
            }

            if (errorCount > 0) mPogoInteractor.clearAppData();

            // Start Pogo.
            mPogoInteractor.startPogo();

            while (wrongScreenCount < 20 && !mPaused && !mStopped) {
                currentScreen = mPogoInteractor.currentScreen();
                if (currentScreen == Screen.DATE_OF_BIRTH || currentScreen == Screen.PLAYER_SELECTION) {
                    wrongScreenCount = 0;
                    break;
                } else {
                    wrongScreenCount++;
                }
            }

            if (mPaused || mStopped) continue;

            if (currentScreen == Screen.DATE_OF_BIRTH) {
                mPogoInteractor.selectDateOfBirth();

                if (mPaused || mStopped) continue;

                // Wait while pogo transitions to the returning/new player selection screen.
                Utils.sleep(Utils.randomWithRange(450, 550));

                mPogoInteractor.selectReturningPlayer();
            } else if (currentScreen == Screen.PLAYER_SELECTION) {
                mPogoInteractor.selectReturningPlayer();
            } else {
                errorCount++;
                mPogoInteractor.stopPogo();
                continue;
            }

            if (mPaused || mStopped) continue;

            // Wait while pogo transitions to the account type selection screen.
            Utils.sleep(Utils.randomWithRange(450, 550));

            // Select PTC.
            mPogoInteractor.selectPTC();

            if (mPaused || mStopped) continue;

            // Wait while pogo transitions to login screen.
            Utils.sleep(Utils.randomWithRange(450, 550));

            // Login.
            mPogoInteractor.login(username, password);

            if (mPaused || mStopped) continue;

            while (wrongScreenCount < 20 && !mPaused && !mStopped) {
                currentScreen = mPogoInteractor.currentScreen();
                if (currentScreen != Screen.LOGIN && currentScreen != Screen.UNKNOWN) {
                    wrongScreenCount = 0;
                    break;
                } else {
                    wrongScreenCount++;
                }
            }

            if (mPaused || mStopped) continue;

            switch(currentScreen) {
                case LOADING:
                    Utils.sleep(Utils.randomWithRange(450, 550));

                    // Check another time, because the loading screen is shortly visible before the banned screen.
                    currentScreen = mPogoInteractor.currentScreen();
                    if (currentScreen == Screen.LOADING) {
                        mNotBannedCount++;
                        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_banned.txt");
                        Log.i(LOG_TAG, "Account " + account + " is not banned.");
                        return AccountStatus.NOT_BANNED;
                    }
                case ACCOUNT_BANNED:
                    mBannedCount++;
                    Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/banned.txt");
                    Log.i(LOG_TAG, "Account " + account + " is banned.");
                    return AccountStatus.BANNED;
                case ACCOUNT_NEW:
                    mNewCount++;
                    Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/new.txt");
                    Log.i(LOG_TAG, "Account " + account + " is a new account.");
                    return AccountStatus.NEW;
                case ACCOUNT_NOT_ACTIVATED:
                    mNotActivatedCount++;
                    Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_activated.txt");
                    Log.i(LOG_TAG, "Account " + account + " is not activated.");
                    return AccountStatus.NOT_ACTIVATED;
                case ACCOUNT_LOCKED:
                    mLockedCount++;
                    Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/locked.txt");
                    Log.i(LOG_TAG, "Account " + account + " is locked.");
                    return AccountStatus.LOCKED;
                case ACCOUNT_WRONG_CREDENTIALS:
                    mWrongCredentialsCount++;
                    Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/wrong_credentials.txt");
                    Log.i(LOG_TAG, "Account " + account + " does not exist or its credentials are wrong.");
                    return AccountStatus.WRONG_CREDENTIALS;
                default:
                    errorCount++;
                    mPogoInteractor.stopPogo();
                    Log.e(LOG_TAG, "Couldn't detect status of account " + account + ".");
            }
        }

        mErrorCount++;
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/error.txt");
        Log.e(LOG_TAG, "Error limit reached for account " + account + ".");
        return AccountStatus.NOT_CHECKED;
    }

    private void initialize() {
        mPogoInteractor.resume();

        mPaused = mStopped = false;
        mChecking = true;

        mNotBannedCount = mBannedCount = mNewCount = mNotActivatedCount = mLockedCount = mWrongCredentialsCount = mErrorCount = 0;

        // Make sure service is not killed when client unbinds.
        Intent intent = new Intent(this, AccountCheckingService.class);
        startService(intent);

        startForeground(CHECKING_NOTIFICATION_ID, mNotificationBuilderChecking.build());
        updateCheckingNotificationText("Checked: 0/" + mAccountCount);

        // Create PogoAccountChecker folder.
        Shell.runSuCommand("mkdir " + PATHNAME);
    }

    public void checkAccounts(final List<String> accounts, final char delimiter) {
        if (mChecking) return;

        mAccountCount = accounts.size();
        initialize();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<mAccountCount; i++) {
                    checkAccount(accounts.get(i), delimiter);

                    if (mStopped) return;

                    updateCheckingNotificationText("Checked: " + getCheckedCount() + "/" + mAccountCount);
                }

                mChecking = false;

                stopForeground(true);

                startResultActivity();
                showFinishedNotification("Account checking finished", getStats());

                mPogoInteractor.cleanUp(true);

                Log.i(LOG_TAG, "Account checking finished. " + getStats());
            }
        }).start();
    }

    public void checkAccountsWithMAD(final String webSocketUri, final char delimiter) {
        if (mChecking) return;

        mDelimiter = delimiter;

        mWebSocket = new MadWebSocket(webSocketUri);
        mWebSocket.start(this);
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
        stopForeground(true);
        showFinishedNotification("Account checking stopped", getStats());
        startResultActivity();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mPogoInteractor.cleanUp(true);
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
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("accountCount", mAccountCount);
        intent.putExtra("notBannedCount", mNotBannedCount);
        intent.putExtra("bannedCount", mBannedCount);
        intent.putExtra("newCount", mNewCount);
        intent.putExtra("wrongCredentialsCount", mWrongCredentialsCount);
        intent.putExtra("notActivatedCount", mNotActivatedCount);
        intent.putExtra("lockedCount", mLockedCount);
        intent.putExtra("errorCount", mErrorCount);
        intent.putExtra("stopped", mStopped);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
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
        intent.putExtra("newCount", mNewCount);
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
        return mNotBannedCount + mBannedCount + mNewCount + mWrongCredentialsCount + mNotActivatedCount + mLockedCount + mErrorCount;
    }

    private String getStats() {
        String stats = "";

        if (mNotBannedCount > 0) {
            stats += "not banned: " + mNotBannedCount + ", ";
        }

        if (mBannedCount > 0) {
            stats += "banned: " + mBannedCount + ", ";
        }

        if (mNewCount > 0) {
            stats += "new: " + mNewCount + ", ";
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

        int checkedCount = getCheckedCount();

        if (stats.isEmpty()) {
            return "Checked: " + checkedCount + "/" + mAccountCount;
        } else {
            return "Checked: " + checkedCount + "/" + mAccountCount + ", " + stats.substring(0, stats.length()-2);
        }
    }
}
