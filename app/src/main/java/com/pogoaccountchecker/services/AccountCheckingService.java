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

import com.pogoaccountchecker.R;
import com.pogoaccountchecker.activities.MainActivity;
import com.pogoaccountchecker.activities.ResultActivity;
import com.pogoaccountchecker.interactors.PogoInteractor;
import com.pogoaccountchecker.interactors.PogoInteractor.Screen;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.preference.PreferenceManager;

import java.util.List;

import static com.pogoaccountchecker.App.NOTIFICATION_CHANNEL_ID;

public class AccountCheckingService extends Service {
    private SharedPreferences mSharedPreferences;
    private PogoInteractor mPogoInteractor;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilderChecking;
    private char mDelimiter;
    private volatile boolean mChecking, mPaused, mStopped;
    private int mAccountCount;
    private int mNotBannedCount, mBannedCount, mNewCount, mWrongCredentialsCount, mNotActivatedCount, mLockedCount, mErrorCount;
    private final String PATHNAME = Environment.getExternalStorageDirectory().getPath() + "/PogoAccountChecker";
    private final IBinder binder = new AccountCheckingServiceBinder();
    private final String LOG_TAG = getClass().getSimpleName();
    public final int CHECKING_NOTIFICATION_ID = 1;
    public final int FINISHED_NOTIFICATION_ID = 2;

    public enum AccountStatus {
        NOT_BANNED, NOT_BANNED_TUTORIAL, BANNED, WRONG_CREDENTIALS, NEW, NOT_ACTIVATED, LOCKED, NOT_CHECKED
    }

    public class AccountCheckingServiceBinder extends Binder {
        public AccountCheckingService getService() {
            return AccountCheckingService.this;
        }
    }

    @Override
    public void onCreate() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
        while (wrongScreenCount < 20 && !isInterrupted()) {
            Screen currentScreen = mPogoInteractor.getCurrentScreen();
            if (currentScreen != Screen.BOOT && currentScreen != Screen.UNKNOWN) {
                return currentScreen;
            } else {
                if (currentScreen == Screen.BOOT) {
                    Log.i(LOG_TAG, "Still on boot screen.");
                } else {
                    Log.e(LOG_TAG, "Could not recognize current screen.");
                }
                wrongScreenCount++;
            }
        }

        return Screen.UNKNOWN;
    }

    private Screen getScreenAfterLogin() {
        int wrongScreenCount = 0;
        while (wrongScreenCount < 20 && !isInterrupted()) {
            Screen currentScreen = mPogoInteractor.getCurrentScreen();
            if (currentScreen != Screen.LOGIN && currentScreen != Screen.UNKNOWN) {
                return currentScreen;
            } else {
                if (currentScreen == Screen.LOGIN) {
                    Log.i(LOG_TAG, "Still on login screen.");
                } else {
                    Log.e(LOG_TAG, "Could not recognize current screen.");
                }
                wrongScreenCount++;
            }
        }

        return Screen.UNKNOWN;
    }

    private Screen getScreenAfterLoading() {
        int wrongScreenCount = 0;
        while (wrongScreenCount < 20 && !isInterrupted()) {
            Screen currentScreen = mPogoInteractor.getCurrentScreen();
            if (currentScreen != Screen.LOADING && currentScreen != Screen.UNKNOWN) {
                return currentScreen;
            } else {
                if (currentScreen == Screen.LOADING) {
                    Log.i(LOG_TAG, "Still on loading screen.");
                } else {
                    Log.e(LOG_TAG, "Could not recognize current screen.");
                }
                wrongScreenCount++;
            }
        }

        return Screen.UNKNOWN;
    }

    private Screen getScreenAfterTos() {
        int wrongScreenCount = 0;
        while (wrongScreenCount < 5 && !isInterrupted()) {
            Screen currentScreen = mPogoInteractor.getCurrentScreen();
            if (currentScreen != Screen.TERMS_OF_SERVICE && currentScreen != Screen.UNKNOWN) {
                return currentScreen;
            } else {
                if (currentScreen == Screen.TERMS_OF_SERVICE) {
                    Log.i(LOG_TAG, "Still on terms of service screen.");
                } else {
                    Log.e(LOG_TAG, "Could not recognize current screen.");
                }
                wrongScreenCount++;
            }
        }

        return Screen.UNKNOWN;
    }

    private Screen getScreenAfterPrivacyPolicy() {
        int wrongScreenCount = 0;
        while (wrongScreenCount < 5 && !isInterrupted()) {
            Screen currentScreen = mPogoInteractor.getCurrentScreen();
            if (currentScreen != Screen.PRIVACY_POLICY && currentScreen != Screen.UNKNOWN) {
                return currentScreen;
            } else {
                if (currentScreen == Screen.PRIVACY_POLICY) {
                    Log.i(LOG_TAG, "Still on privacy policy screen.");
                } else {
                    Log.e(LOG_TAG, "Could not recognize current screen.");
                }
                wrongScreenCount++;
            }
        }

        return Screen.UNKNOWN;
    }

    private void closeNotificationOpenPlayerProfile() {
        mPogoInteractor.closeNotificationPopup();
        if (isInterrupted()) return;
        Utils.sleep(Integer.parseInt(mSharedPreferences.getString(getString(R.string.notification_popup_delay_pref_key), "500")));
        if (isInterrupted()) return;

        mPogoInteractor.openPlayerProfile();
        if (isInterrupted()) return;
        Utils.sleep(Integer.parseInt(mSharedPreferences.getString(getString(R.string.player_profile_delay_pref_key), "2000")));
    }

    private boolean getOnPlayerProfile() {
        // Close notification popup before opening player profile, since most accounts that need to be checked will probably have unread messages.
        closeNotificationOpenPlayerProfile();

        int wrongScreenCount = 0;
        while (wrongScreenCount < 10 && !isInterrupted()) {
            Screen currentScreen = mPogoInteractor.getCurrentScreen();
            switch (currentScreen) {
                case PLAYER_PROFILE:
                    return true;
                case NOTIFICATION_POPUP:
                    mPogoInteractor.closeNotificationPopup();
                    if (isInterrupted()) return false;
                    Utils.sleep(Integer.parseInt(mSharedPreferences.getString(getString(R.string.notification_popup_delay_pref_key), "500")));
                    break;
                case TERMS_OF_SERVICE:
                    mPogoInteractor.acceptTermsOfService();
                    if (isInterrupted()) return false;
                    Utils.sleepRandom(450, 550);
                    break;
                case PRIVACY_POLICY:
                    mPogoInteractor.closePrivacyPolicy();
                    if (isInterrupted()) return false;
                    Utils.sleepRandom(450, 550);
                    break;
                case CHEATING_WARNING_1:
                    mPogoInteractor.closeCheatingWarning1();
                    if (isInterrupted()) return false;
                    Utils.sleepRandom(450, 550);
                    if (isInterrupted()) return false;

                    closeNotificationOpenPlayerProfile();
                    break;
                case CHEATING_WARNING_2:
                    mPogoInteractor.closeCheatingWarning2();
                    if (isInterrupted()) return false;
                    Utils.sleepRandom(450, 550);
                    if (isInterrupted()) return false;

                    closeNotificationOpenPlayerProfile();
                    break;
                case CHEATING_WARNING_3:
                    mPogoInteractor.closeCheatingWarning3();
                    if (isInterrupted()) return false;
                    Utils.sleepRandom(450, 550);
                    if (isInterrupted()) return false;

                    closeNotificationOpenPlayerProfile();
                    break;
                case SUSPENSION_WARNING:
                    mPogoInteractor.closeSuspensionWarning();
                    if (isInterrupted()) return false;
                    Utils.sleepRandom(450, 550);
                    if (isInterrupted()) return false;

                    closeNotificationOpenPlayerProfile();
                    break;
                default:
                    Log.e(LOG_TAG, "Not on player profile screen.");
                    wrongScreenCount++;
                    // Assume that we are on the overworld screen.
                    mPogoInteractor.openPlayerProfile();
                    if (isInterrupted()) return false;
                    Utils.sleep(Integer.parseInt(mSharedPreferences.getString(getString(R.string.player_profile_delay_pref_key), "2000")));
            }
        }

        return false;
    }

    private void processNotBannedAccount(String account, String username) {
        Log.i(LOG_TAG, "Account " + username + " is not banned.");
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_banned.txt");
        mNotBannedCount++;
    }

    private void processNotBannedLevelAccount(String account, String username, int level) {
        Log.i(LOG_TAG, "Account " + username + " (L" + level + ") is not banned.");
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_banned_L" + level + ".txt");
        mNotBannedCount++;
    }

    private void processNotBannedTutorialAccount(String account, String username) {
        Log.i(LOG_TAG, "Account " + username + " is not banned but needs to complete the tutorial.");
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_banned_tutorial.txt");
        mNotBannedCount++;
    }

    private void processBannedAccount(String account, String username) {
        Log.i(LOG_TAG, "Account " + username + " is banned.");
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/banned.txt");
        mBannedCount++;
    }

    private void processWrongCredentialsAccount(String account, String username) {
        Log.i(LOG_TAG, "Account " + username + " does not exist or its credentials are wrong.");
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/wrong_credentials.txt");
        mWrongCredentialsCount++;
    }

    private void processNewAccount(String account, String username) {
        Log.i(LOG_TAG, "Account " + username + " is a new account.");
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/new.txt");
        mNewCount++;
    }

    private void processNotActivatedAccount(String account, String username) {
        Log.i(LOG_TAG, "Account " + username + " is not activated.");
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/not_activated.txt");
        mNotActivatedCount++;
    }

    private void processLockedAccount(String account, String username) {
        Log.i(LOG_TAG, "Account " + username + " is locked.");
        Shell.runSuCommand("echo '" + account + "' >> " + PATHNAME + "/locked.txt");
        mLockedCount++;
    }

    private AccountStatus checkAccount(String account, char delimiter) {
        int index = account.indexOf(delimiter);
        String username = account.substring(0, index);
        String password = account.substring(index + 1);
        Log.i(LOG_TAG, "Checking account " + username + ".");

        mPogoInteractor.clearAppData();
        boolean permissionsGranted = false;

        int errorCount = 0;
        while (errorCount != 10) {
            if (mStopped) {
                return AccountStatus.NOT_CHECKED;
            } else if (mPaused) {
                while (mPaused) {
                    Utils.sleep(2000);
                }
                mPogoInteractor.stopPogo();
                if (isInterrupted()) continue;
            }

            mPogoInteractor.startPogo();
            if (isInterrupted()) continue;

            boolean detectLevel = mSharedPreferences.getBoolean(getString(R.string.detect_account_level_pref_key), false);
            boolean checkTutorial = mSharedPreferences.getBoolean(getString(R.string.check_tutorial_pref_key), false);
            if (!permissionsGranted && (detectLevel || checkTutorial)) {
                mPogoInteractor.grantLocationPermission();
                if (isInterrupted()) continue;
                mPogoInteractor.grantCameraPermission();
                if (isInterrupted()) continue;
                permissionsGranted = true;
            }

            Screen currentScreen = getScreenAfterBoot();
            if (isInterrupted()) continue;
            if (currentScreen == Screen.DATE_OF_BIRTH || currentScreen == Screen.NEW_EXISTING_ACCOUNT) {
                if (currentScreen == Screen.DATE_OF_BIRTH) {
                    Log.i(LOG_TAG, "On date of birth screen.");
                    mPogoInteractor.selectDateOfBirth();
                    if (isInterrupted()) continue;
                    // Wait while pogo transitions to the new/existing account screen.
                    Utils.sleep(Integer.parseInt(mSharedPreferences.getString(getString(R.string.submit_dob_delay_pref_key), "500")));
                    if (isInterrupted()) continue;
                } else {
                    Log.i(LOG_TAG, "On new/existing account screen.");
                }

                mPogoInteractor.selectReturningPlayer();
                if (isInterrupted()) continue;
                // Wait while pogo transitions to the new/existing account screen.
                Utils.sleep(Integer.parseInt(mSharedPreferences.getString(getString(R.string.returning_player_delay_pref_key), "500")));
                if (isInterrupted()) continue;

                mPogoInteractor.selectPtc();
                if (isInterrupted()) continue;
                // Wait while pogo transitions to login screen.
                Utils.sleep(Integer.parseInt(mSharedPreferences.getString(getString(R.string.ptc_delay_pref_key), "500")));
                if (isInterrupted()) continue;

                mPogoInteractor.login(username, password);
                if (isInterrupted()) continue;

                currentScreen = getScreenAfterLogin();
                if (isInterrupted()) continue;
            }

            if (currentScreen == Screen.LOADING) {
                // Check another time, because the loading screen is shortly visible before the banned and login failed screen.
                currentScreen = mPogoInteractor.getCurrentScreen();
                if (isInterrupted()) continue;
            }

            switch (currentScreen) {
                case LOADING:
                    Log.i(LOG_TAG, "On loading screen.");
                    if (detectLevel || checkTutorial) {
                        currentScreen = getScreenAfterLoading();
                        if (isInterrupted()) continue;
                        if (currentScreen == Screen.TERMS_OF_SERVICE) {
                            mPogoInteractor.acceptTermsOfService();
                            if (isInterrupted()) continue;
                            Utils.sleepRandom(450, 550);
                            if (isInterrupted()) continue;

                            currentScreen = getScreenAfterTos();
                            if (isInterrupted()) continue;
                        }

                        if (currentScreen == Screen.PRIVACY_POLICY) {
                            mPogoInteractor.closePrivacyPolicy();
                            if (isInterrupted()) continue;
                            Utils.sleepRandom(450, 550);
                            if (isInterrupted()) continue;

                            currentScreen = getScreenAfterPrivacyPolicy();
                            if (isInterrupted()) continue;
                        }

                        if (currentScreen == Screen.TUTORIAL_GREETING || currentScreen == Screen.TUTORIAL_CATCH_POKEMON || currentScreen == Screen.TUTORIAL_FIRST_POKEMON
                                || currentScreen == Screen.TUTORIAL_POKESTOPS) {
                            processNotBannedTutorialAccount(account, username);
                            return AccountStatus.NOT_BANNED_TUTORIAL;
                        }

                        if (currentScreen == Screen.SAFETY_WARNING_SMALL || currentScreen == Screen.SAFETY_WARNING_LONG) {
                            if (detectLevel) {
                                mPogoInteractor.closeSafetyWarning(currentScreen);
                                if (isInterrupted()) continue;
                                Utils.sleep(Integer.parseInt(mSharedPreferences.getString(getString(R.string.safety_warning_delay_pref_key), "1000")));
                                if (isInterrupted()) continue;

                                if (getOnPlayerProfile()) {
                                    int level = mPogoInteractor.getAccountLevel();
                                    if (level == -1) {
                                        processBannedAccount(account, username);
                                    } else {
                                        processNotBannedLevelAccount(account, username, level);
                                    }
                                    return AccountStatus.NOT_BANNED;
                                } else if (isInterrupted()) continue;
                            } else {
                                processNotBannedAccount(account, username);
                                return AccountStatus.NOT_BANNED;
                            }
                        }
                    } else {
                        processNotBannedAccount(account, username);
                        return AccountStatus.NOT_BANNED;
                    }
                    break;
                case ACCOUNT_BANNED:
                    Log.i(LOG_TAG, "On account banned screen.");
                    processBannedAccount(account, username);
                    return AccountStatus.BANNED;
                case ACCOUNT_WRONG_CREDENTIALS:
                    Log.i(LOG_TAG, "On account wrong credentials screen.");
                    processWrongCredentialsAccount(account, username);
                    return AccountStatus.WRONG_CREDENTIALS;
                case ACCOUNT_NEW:
                    Log.i(LOG_TAG, "On account new screen.");
                    processNewAccount(account, username);
                    return AccountStatus.NEW;
                case ACCOUNT_NOT_ACTIVATED:
                    Log.i(LOG_TAG, "On account not activated screen.");
                    processNotActivatedAccount(account, username);
                    return AccountStatus.NOT_ACTIVATED;
                case ACCOUNT_LOCKED:
                    Log.i(LOG_TAG, "On account locked screen.");
                    processLockedAccount(account, username);
                    return AccountStatus.LOCKED;
                case LOGIN_FAILED:
                    Log.e(LOG_TAG, "On login failed screen. Clearing app data and trying again.");
                    mPogoInteractor.clearAppData();
                    permissionsGranted = false;
                    continue;
            }

            Log.e(LOG_TAG, "Couldn't detect status of account " + username + ". Trying again.");
            errorCount++;
            mPogoInteractor.stopPogo();
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
                for (int i = 0; i < mAccountCount; i++) {
                    checkAccount(accounts.get(i), mDelimiter);
                    if (mStopped) return;
                    updateCheckingNotificationText("Checked: " + getCheckedCount() + "/" + mAccountCount);
                }

                Log.i(LOG_TAG, "Account checking finished. " + getStats() + ".");
                mChecking = false;
                startResultActivity();
                showFinishedNotification("Account checking finished", getStats());
                stopForeground(true);
                mPogoInteractor.cleanUp(true);
            }
        }).start();
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
        mStopped = true;
        mChecking = false;
        mPogoInteractor.interrupt();
        startResultActivity();
        showFinishedNotification("Account checking stopped", getStats());
        stopForeground(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mPogoInteractor.cleanUp(true);
            }
        }).start();
    }

    private boolean isInterrupted() {
        return mPaused || mStopped;
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
            return "Checked: " + checkedCount + "/" + mAccountCount + ", " + stats.substring(0, stats.length() - 2);
        }
    }
}
