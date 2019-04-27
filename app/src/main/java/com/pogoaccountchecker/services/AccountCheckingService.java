package com.pogoaccountchecker.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.preference.PreferenceManager;

import static com.pogoaccountchecker.App.NOTIFICATION_CHANNEL_ID;

public class AccountCheckingService extends Service implements MadWebSocket.OnWebSocketEventListener {
    private SharedPreferences mSharedPreferences;
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

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

    private void initialize() {
        mPogoInteractor.resume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String delimiter = sharedPreferences.getString(getString(R.string.delimiter_pref_key), ":");
        mDelimiter = delimiter.charAt(0);

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

    private Screen getScreenAfterBoot() {
        int wrongScreenCount = 0;
        while (wrongScreenCount < 20 && !mPaused && !mStopped) {
            Screen currentScreen = mPogoInteractor.currentScreen();
            if (currentScreen == Screen.DATE_OF_BIRTH || currentScreen == Screen.PLAYER_SELECTION) {
                return currentScreen;
            } else {
                Log.i(LOG_TAG, "Not on date of birth or new/existing account screen.");
                wrongScreenCount++;
            }
        }

        return Screen.UNKNOWN;
    }

    private Screen getScreenAfterLogin() {
        int wrongScreenCount = 0;
        while (wrongScreenCount < 20 && !mPaused && !mStopped) {
            Screen currentScreen = mPogoInteractor.currentScreen();
            if (currentScreen != Screen.LOGIN && currentScreen != Screen.UNKNOWN) {
                return currentScreen;
            } else {
                Log.i(LOG_TAG, "Account is not yet logged in.");
                wrongScreenCount++;
            }
        }

        return Screen.UNKNOWN;
    }

    private Screen getScreenAfterLoading() {
        int wrongScreenCount = 0;
        while (wrongScreenCount < 20 && !mPaused && !mStopped) {
            Screen currentScreen = mPogoInteractor.currentScreen();
            if (currentScreen == Screen.SAFETY_WARNING) {
                return currentScreen;
            } else {
                Log.i(LOG_TAG, "Still on loading screen.");
                wrongScreenCount++;
            }
        }

        return Screen.UNKNOWN;
    }

    private void closeNotificationPopupAndOpenPlayerProfile() {
        mPogoInteractor.closeNotificationPopup();
        if (mPaused || mStopped) return;
        Utils.sleepRandom(450, 550);
        if (mPaused || mStopped) return;

        mPogoInteractor.openPlayerProfile();
        if (mPaused || mStopped) return;
        Utils.sleepRandom(1950, 2050);
    }


    private int detectAccountLevel() {
        Screen currentScreen = getScreenAfterLoading();
        if (currentScreen == Screen.SAFETY_WARNING) {
            mPogoInteractor.closeSafetyWarning();
            if (mPaused || mStopped) return -1;
            Utils.sleepRandom(950, 1050);
            if (mPaused || mStopped) return -1;

            closeNotificationPopupAndOpenPlayerProfile();
            if (mPaused || mStopped) return -1;

            int wrongScreenCount = 0;
            while (wrongScreenCount < 20 && !mPaused && !mStopped) {
                currentScreen = mPogoInteractor.currentScreen();
                if (currentScreen == Screen.PLAYER_PROFILE) {
                    return mPogoInteractor.getAccountLevel();
                } else if (currentScreen == Screen.CHEATING_WARNING_1) {
                    mPogoInteractor.closeCheatingWarning1();
                    if (mPaused || mStopped) break;
                    Utils.sleepRandom(450, 550);
                    if (mPaused || mStopped) break;

                    closeNotificationPopupAndOpenPlayerProfile();
                } else if (currentScreen == Screen.CHEATING_WARNING_2) {
                    mPogoInteractor.closeCheatingWarning2();
                    if (mPaused || mStopped) break;
                    Utils.sleepRandom(450, 550);
                    if (mPaused || mStopped) break;

                    closeNotificationPopupAndOpenPlayerProfile();
                } else if (currentScreen == Screen.CHEATING_WARNING_3) {
                    mPogoInteractor.closeCheatingWarning3();
                    if (mPaused || mStopped) break;
                    Utils.sleepRandom(450, 550);
                    if (mPaused || mStopped) break;

                    closeNotificationPopupAndOpenPlayerProfile();
                } else if (currentScreen == Screen.SUSPENSION_WARNING) {
                    mPogoInteractor.closeSuspensionsWarning();
                    if (mPaused || mStopped) break;
                    Utils.sleepRandom(450, 550);
                    if (mPaused || mStopped) break;

                    closeNotificationPopupAndOpenPlayerProfile();
                } else if (currentScreen == Screen.NOTIFICATION_POPUP) {
                    mPogoInteractor.closeNotificationPopup();
                } else if (currentScreen == Screen.TUTORIAL_CATCH_POKEMON) {
                    return -2;
                } else if (currentScreen == Screen.SAFETY_WARNING) {
                    mPogoInteractor.closeSafetyWarning();
                    if (mPaused || mStopped) break;
                    Utils.sleepRandom(950, 1050);
                    if (mPaused || mStopped) break;

                    closeNotificationPopupAndOpenPlayerProfile();
                } else {
                    Log.e(LOG_TAG, "Not on player profile screen.");
                    wrongScreenCount++;
                    // Assume that we are on the overworld screen.
                    mPogoInteractor.openPlayerProfile();
                    if (mPaused || mStopped) break;
                    Utils.sleepRandom(1950, 2050);
                }
            }
        }

        return -1;
    }

    public enum AccountStatus {
        BANNED, NOT_BANNED, WRONG_CREDENTIALS, NEW, NOT_ACTIVATED, LOCKED, NOT_CHECKED
    }

    private AccountStatus checkAccount(String account, char delimiter) {
        int index = account.indexOf(delimiter);
        String username = account.substring(0, index);
        String password = account.substring(index + 1);
        Log.i(LOG_TAG, "Checking account " + username + ".");

        mPogoInteractor.clearAppData();

        int errorCount = 0;
        while (errorCount != 10) {
            if (mStopped) {
                return AccountStatus.NOT_CHECKED;
            } else {
                if (mPaused) {
                    while (mPaused) {
                        Utils.sleep(2000);
                    }
                    mPogoInteractor.clearAppData();
                }
            }

            mPogoInteractor.startPogo();
            if (mPaused || mStopped) continue;

            Screen currentScreen = getScreenAfterBoot();
            if (mPaused || mStopped) continue;
            if (currentScreen == Screen.DATE_OF_BIRTH) {
                mPogoInteractor.selectDateOfBirth();
                if (mPaused || mStopped) continue;
                // Wait while pogo transitions to the new/existing account screen.
                Utils.sleepRandom(450, 550);
                if (mPaused || mStopped) continue;
            } else if (currentScreen != Screen.PLAYER_SELECTION) {
                Log.e(LOG_TAG, "Not on date of birth or new/existing account screen after 20 tries.");
                errorCount++;
                continue;
            }

            mPogoInteractor.selectReturningPlayer();
            if (mPaused || mStopped) continue;
            // Wait while pogo transitions to the new/existing account screen.
            Utils.sleepRandom(450, 550);
            if (mPaused || mStopped) continue;

            mPogoInteractor.selectPtc();
            if (mPaused || mStopped) continue;
            // Wait while pogo transitions to login screen.
            Utils.sleepRandom(450, 550);
            if (mPaused || mStopped) continue;

            mPogoInteractor.login(username, password);
            if (mPaused || mStopped) continue;

            currentScreen = getScreenAfterLogin();
            if (mPaused || mStopped) continue;
            switch (currentScreen) {
                case LOADING:
                    boolean detectLevel = mSharedPreferences.getBoolean(getString(R.string.detect_account_level_pref_key), false);
                    if (detectLevel) {
                        mPogoInteractor.grantLocationPermission();
                        mPogoInteractor.grantCameraPermission();

                        int accountLevel = detectAccountLevel();
                        if (accountLevel >= 0) {
                            Log.i(LOG_TAG, "Account " + username + " (L" + accountLevel + ") is not banned.");
                            Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_banned_L" + accountLevel + ".txt.");
                            mNotBannedCount++;
                            return AccountStatus.NOT_BANNED;
                        } else if (accountLevel == -1) {
                            if (mPaused || mStopped) continue;
                            if (++errorCount == 10) {
                                Log.e(LOG_TAG, "Couldn't detect level of account " + username + " 10 times in a row.");
                                Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_banned.txt.");
                                mNotBannedCount++;
                                return AccountStatus.NOT_BANNED;
                            }
                            Log.e(LOG_TAG, "Couldn't detect level of account " + username + ". Trying again.");
                            mPogoInteractor.clearAppData();
                            break;
                        } else if (accountLevel == -2) {
                            Log.i(LOG_TAG, "Account " + username + " is not banned but needs to catch the tutorial starter pokemon.");
                            Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_banned_tutorial.txt.");
                            mNotBannedCount++;
                            return AccountStatus.NOT_BANNED;
                        }
                    } else {
                        Utils.sleepRandom(950, 1050);
                        if (mPaused || mStopped) continue;

                        // Check another time, because the loading screen is shortly visible before the banned screen.
                        currentScreen = mPogoInteractor.currentScreen();
                        if (currentScreen == Screen.LOADING) {
                            Log.i(LOG_TAG, "Account " + username + " is not banned.");
                            Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_banned.txt.");
                            mNotBannedCount++;
                            return AccountStatus.NOT_BANNED;
                        }
                    }
                case ACCOUNT_BANNED:
                    Log.i(LOG_TAG, "Account " + username + " is banned.");
                    Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/banned.txt.");
                    mBannedCount++;
                    return AccountStatus.BANNED;
                case ACCOUNT_WRONG_CREDENTIALS:
                    Log.i(LOG_TAG, "Account " + username + " does not exist or its credentials are wrong.");
                    Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/wrong_credentials.txt.");
                    mWrongCredentialsCount++;
                    return AccountStatus.WRONG_CREDENTIALS;
                case ACCOUNT_NEW:
                    Log.i(LOG_TAG, "Account " + username + " is a new account.");
                    Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/new.txt.");
                    mNewCount++;
                    return AccountStatus.NEW;
                case ACCOUNT_NOT_ACTIVATED:
                    Log.i(LOG_TAG, "Account " + username + " is not activated.");
                    Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_activated.txt.");
                    mNotActivatedCount++;
                    return AccountStatus.NOT_ACTIVATED;
                case ACCOUNT_LOCKED:
                    Log.i(LOG_TAG, "Account " + username + " is locked.");
                    Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/locked.txt.");
                    mLockedCount++;
                    return AccountStatus.LOCKED;
                default:
                    Log.e(LOG_TAG, "Couldn't detect status of account " + username + ". Trying again.");
                    errorCount++;
                    mPogoInteractor.stopPogo();
            }
        }

        Log.e(LOG_TAG, "Checking for account " + username + " failed 10 times in a row.");
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/error.txt");
        mErrorCount++;
        return AccountStatus.NOT_CHECKED;
    }

    public void checkAccounts(final List<String> accounts) {
        if (mChecking) return;

        mAccountCount = accounts.size();
        initialize();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<mAccountCount; i++) {
                    checkAccount(accounts.get(i), mDelimiter);
                    if (mStopped) return;
                    updateCheckingNotificationText("Checked: " + getCheckedCount() + "/" + mAccountCount);
                }

                Log.i(LOG_TAG, "Account checking finished. " + getStats() + ".");
                startResultActivity();
                showFinishedNotification("Account checking finished", getStats());
                stopForeground(true);
                mPogoInteractor.cleanUp(true);
                mChecking = false;
            }
        }).start();
    }

    public void checkAccountsWithMAD() {
        if (mChecking) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String webSocketUri = sharedPreferences.getString(getString(R.string.webSocket_uri_pref_key), "");
        mWebSocket = new MadWebSocket(webSocketUri);
        mWebSocket.start(this);
    }

    public void pause() {
        Log.i(LOG_TAG, "Account checking paused.");
        mPaused = true;
        mPogoInteractor.interrupt();
        updateCheckingNotificationTitle("Account checking paused");
    }

    public void resume() {
        Log.i(LOG_TAG, "Account checking resumed.");
        mPaused = false;
        mPogoInteractor.resume();
        updateCheckingNotificationTitle("Checking accounts…");
    }

    public void stop() {
        if (!mChecking) return;

        Log.i(LOG_TAG, "Account checking stopped. " + getStats());
        mChecking = false;
        mStopped = true;
        mPogoInteractor.interrupt();
        showFinishedNotification("Account checking stopped", getStats());
        startResultActivity();
        stopForeground(true);
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
