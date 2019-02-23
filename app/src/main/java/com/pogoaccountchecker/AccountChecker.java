package com.pogoaccountchecker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.pogoaccountchecker.activities.ResultActivity;
import com.pogoaccountchecker.pogo.PogoInteractor;
import com.pogoaccountchecker.pogo.PogoInteractor.LoginResult;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

import java.util.ArrayList;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import static com.pogoaccountchecker.App.NOTIFICATION_CHANNEL_ID;

public class AccountChecker {
    private Context mContext;
    private ArrayList<String> mAccounts;
    private char mSeparator;
    private OnAccountCheckingFinishedListener mCallback;
    private PogoInteractor mPogoInteractor;
    private final String PATHNAME;
    private int mNotBannedCount, mBannedCount, mWrongCredentialsCount, mNotActivatedCount, mLockedCount, mErrorCount;
    private boolean mInterrupted;
    private final int NOTIFICATION_ID = 2;
    private final String LOG_TAG = getClass().getSimpleName();

    public AccountChecker(Context context, ArrayList<String> accounts, char separator, OnAccountCheckingFinishedListener listener) {
        mContext = context;
        mAccounts = accounts;
        mSeparator = separator;
        mCallback = listener;
        mPogoInteractor = new PogoInteractor(mContext);
        PATHNAME = Environment.getExternalStorageDirectory().getPath() + "/PogoAccountChecker";
        mNotBannedCount = mBannedCount = mWrongCredentialsCount = mNotActivatedCount = mLockedCount = mErrorCount = 0;
        mInterrupted = false;
    }

    public interface OnAccountCheckingFinishedListener {
        void onAccountCheckingFinished();
    }

    public void start() {
        if (!Shell.runSuCommand("mkdir " + PATHNAME)) {
            Log.e(LOG_TAG, "Couldn't create folder, aborting program!");
            System.exit(0);
        }

        for (String account : mAccounts) {
            int index = account.indexOf(mSeparator);
            String username = account.substring(0, index);
            String password = account.substring(index + 1);

            boolean dateOfBirthEntered = false;
            int errorCount = 0;
            while (errorCount != 10) {
                if (!dateOfBirthEntered) {
                    // Clear app data.
                    if (!mPogoInteractor.clearAppData()) {
                        if (mInterrupted) return;
                        errorCount++;
                        continue;
                    }

                    // Start Pogo.
                    if (!mPogoInteractor.startPogo()) {
                        if (mInterrupted) return;
                        errorCount++;
                        continue;
                    }

                    // Check if we are on the DOB screen.
                    if (!mPogoInteractor.isOnDateOfBirthScreen(20)) {
                        if (mInterrupted) return;
                        errorCount++;
                        continue;
                    }

                    // Select date of birth.
                    if (!mPogoInteractor.selectDateOfBirth()) {
                        if (mInterrupted) return;
                        errorCount++;
                        continue;
                    } else {
                        dateOfBirthEntered = true;
                    }

                    // Wait while pogo transitions to the returning/new player selection screen.
                    Utils.sleep(Utils.randomWithRange(450, 550));

                    // Select returning player.
                    if (!mPogoInteractor.selectReturningPlayer()) {
                        if (mInterrupted) return;
                        mPogoInteractor.stopPogo();
                        errorCount++;
                        continue;
                    }
                } else {
                    // Start Pogo.
                    if (!mPogoInteractor.startPogo()) {
                        if (mInterrupted) return;
                        errorCount++;
                        continue;
                    }

                    // Check if we are on the returning player selection screen.
                    if (!mPogoInteractor.isOnReturningPlayerSelection(20)) {
                        if (mInterrupted) return;
                        mPogoInteractor.stopPogo();
                        errorCount++;
                        continue;
                    }

                    // Select returning player.
                    if (!mPogoInteractor.selectReturningPlayer()) {
                        if (mInterrupted) return;
                        mPogoInteractor.stopPogo();
                        errorCount++;
                        continue;
                    }
                }

                // Wait while pogo transitions to the account type selection screen.
                Utils.sleep(Utils.randomWithRange(450, 550));

                // Select PTC.
                if (!mPogoInteractor.selectPTC()) {
                    if (mInterrupted) return;
                    mPogoInteractor.stopPogo();
                    errorCount++;
                    continue;
                }

                // Wait while pogo transitions to login screen.
                Utils.sleep(Utils.randomWithRange(450, 550));

                // Login.
                if (!mPogoInteractor.login(username, password)) {
                    if (mInterrupted) return;
                    mPogoInteractor.stopPogo();
                    errorCount++;
                    continue;
                }

                LoginResult loginResult = mPogoInteractor.getLoginResult(20);
                if (loginResult == LoginResult.ERROR) {
                    if (mInterrupted) return;
                    Log.e(LOG_TAG, "Error when trying to detect login result for account " + account + ".");
                    mPogoInteractor.stopPogo();
                    errorCount++;
                    continue;
                }


                if (loginResult == LoginResult.NOT_BANNED) {
                    Log.i(LOG_TAG, "Account " + account + " is not banned.");
                    mNotBannedCount++;
                    Shell.runSuCommand("echo \"" + account + "\" >> " + PATHNAME + "/not_banned.txt");
                    break;
                } else if (loginResult == LoginResult.BANNED) {
                    Log.i(LOG_TAG, "Account " + account + " is banned.");
                    mBannedCount++;
                    Shell.runSuCommand("echo \"" + account + "\" >> " + PATHNAME + "/banned.txt");
                    break;
                } else if (loginResult == LoginResult.WRONG_CREDENTIALS) {
                    Log.i(LOG_TAG, "Account " + account + " does not exist or the credentials are wrong.");
                    mWrongCredentialsCount++;
                    Shell.runSuCommand("echo \"" + account + "\" >> " + PATHNAME + "/wrong_credentials.txt");
                    break;
                } else if (loginResult == LoginResult.NOT_ACTIVATED) {
                    Log.i(LOG_TAG, "Account " + account + " is not activated.");
                    mNotActivatedCount++;
                    Shell.runSuCommand("echo \"" + account + "\" >> " + PATHNAME + "/not_activated.txt");
                    break;
                } else if (loginResult == LoginResult.LOCKED) {
                    Log.i(LOG_TAG, "Account " + account + " is locked.");
                    mLockedCount++;
                    Shell.runSuCommand("echo \"" + account + "\" >> " + PATHNAME + "/locked.txt");
                    break;
                }
            }
            if (errorCount == 10) {
                mErrorCount++;
                Shell.runSuCommand("echo \"" + account + "\" >> " + PATHNAME + "/error.txt");
                Log.e(LOG_TAG, "Error limit reached. Wrote account " + account + " to error.txt.");
            }
        }

        Log.i(LOG_TAG, "Account checking finished. " + getStats());
        showNotification("Account checking finished", getStats());
        mPogoInteractor.stopPogo();
        mPogoInteractor.close();
        mCallback.onAccountCheckingFinished();
    }

    private void showNotification(String title, String body) {
        Intent resultIntent = new Intent(mContext, ResultActivity.class);
        resultIntent.putExtra("numAccounts", mAccounts.size());
        resultIntent.putExtra("notBannedCount", mNotBannedCount);
        resultIntent.putExtra("bannedCount", mBannedCount);
        resultIntent.putExtra("wrongCredentialsCount", mWrongCredentialsCount);
        resultIntent.putExtra("notActivatedCount", mNotActivatedCount);
        resultIntent.putExtra("lockedCount", mLockedCount);
        resultIntent.putExtra("errorCount", mErrorCount);
        resultIntent.putExtra("interrupted", mInterrupted);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_outline_done_24px)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
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
            stats += "couldn't be checked: " + mErrorCount + ", ";
        }

        // Make first letter uppercase and remove final two characters.
        return stats.substring(0, 1).toUpperCase() + stats.substring(1, stats.length() - 2);
    }

    public void stop() {
        mInterrupted = true;
        mPogoInteractor.stopPogo();
        mPogoInteractor.close();
        showNotification("Account checking stopped", getStats());
    }
}
