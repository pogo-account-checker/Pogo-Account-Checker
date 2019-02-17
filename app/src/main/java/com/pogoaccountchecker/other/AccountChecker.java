package com.pogoaccountchecker.other;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.pogoaccountchecker.R;
import com.pogoaccountchecker.activities.MainActivity;
import com.pogoaccountchecker.activities.ResultActivity;
import com.pogoaccountchecker.other.PogoInteractor.LoginResult;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

import java.io.IOException;
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
    private int mNotBannedCount, mBannedCount, mNotExistCount, mErrorCount;
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
        mNotBannedCount = mBannedCount = mNotExistCount = mErrorCount = 0;
        mInterrupted = false;
    }

    public interface OnAccountCheckingFinishedListener {
        void onAccountCheckingFinished();
    }

    public void start() {
        try {
            Shell.runSuCommand("mkdir " + PATHNAME);
        } catch (IOException | InterruptedException e) {
            Log.e(LOG_TAG, "Exception when creating directory.");
            e.printStackTrace();
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

                    // Wait while pogo starts.
                    Utils.sleep(5000);

                    // Select date of birth.
                    if (!mPogoInteractor.selectDateOfBirth(20)) {
                        if (mInterrupted) return;
                        errorCount++;
                        continue;
                    }
                    dateOfBirthEntered = true;

                    // Wait while pogo transitions to the returning/new player selection screen.
                    Utils.sleep(500);

                    // Select returning player.
                    if (!mPogoInteractor.selectReturningPlayer(5)) {
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

                    // Wait while pogo starts.
                    Utils.sleep(4000);

                    // Select returning player.
                    if (!mPogoInteractor.selectReturningPlayer(20)) {
                        if (mInterrupted) return;
                        mPogoInteractor.stopPogo();
                        errorCount++;
                        continue;
                    }
                }

                // Wait while pogo transitions to the account type selection screen.
                Utils.sleep(500);

                // Select PTC.
                if (!mPogoInteractor.selectPTC(5)) {
                    if (mInterrupted) return;
                    mPogoInteractor.stopPogo();
                    errorCount++;
                    continue;
                }

                // Wait while pogo transitions to login screen.
                Utils.sleep(500);

                // Login.
                if (!mPogoInteractor.login(username, password, 5)) {
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
                try {
                    if (loginResult == LoginResult.ACCOUNT_NOT_BANNED) {
                        Log.i(LOG_TAG, "Account " + account + " is not banned.");
                        mNotBannedCount++;
                        Shell.runSuCommand("echo \"" + account + "\" >> " + PATHNAME + "/not_banned.txt");
                        break;
                    } else if (loginResult == LoginResult.ACCOUNT_BANNED) {
                        Log.i(LOG_TAG, "Account " + account + " is banned.");
                        mBannedCount++;
                        Shell.runSuCommand("echo \"" + account + "\" >> " + PATHNAME + "/banned.txt");
                        break;
                    } else if (loginResult == LoginResult.ACCOUNT_NOT_EXIST) {
                        Log.i(LOG_TAG, "Account " + account + " does not exist.");
                        mNotExistCount++;
                        Shell.runSuCommand("echo \"" + account + "\" >> " + PATHNAME + "/not_exist.txt");
                        break;
                    }
                } catch (IOException | InterruptedException e) {
                    if (loginResult == LoginResult.ACCOUNT_NOT_BANNED) {
                        Log.e(LOG_TAG, "Exception when writing account " + account + " to not_banned.txt.");
                    } else if (loginResult == LoginResult.ACCOUNT_BANNED) {
                        Log.e(LOG_TAG, "Exception when writing account " + account + " to banned.txt.");
                    } else if (loginResult == LoginResult.ACCOUNT_NOT_EXIST) {
                        Log.e(LOG_TAG, "Exception when writing account " + account + " to not_exist.txt.");
                    }
                    e.printStackTrace();
                    errorCount++;
                }
            }
            if (errorCount == 10) {
                mErrorCount++;
                try {
                    Shell.runSuCommand("echo \"" + account + "\" " + PATHNAME + "/error.txt");
                    Log.e(LOG_TAG, "Error limit reached. Wrote account " + account + " to error.txt.");
                } catch (IOException | InterruptedException e) {
                    Log.e(LOG_TAG, "Exception when writing account " + account + " to error.txt.");
                    e.printStackTrace();
                }
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
        resultIntent.putExtra("notExistCount", mNotExistCount);
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
        return mNotBannedCount + " are not banned, " + mBannedCount + " are banned, " + mNotExistCount + " don't exist, " + mErrorCount + " couldn't be checked.";
    }

    public void stop() {
        mInterrupted = true;
        mPogoInteractor.stopPogo();
        mPogoInteractor.close();
        showNotification("Account checking stopped", getStats());
    }
}
