package com.pogoaccountchecker.other;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.pogoaccountchecker.utils.TextInImageRecognizer;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;

public class PogoInteractor {
    private Context mContext;
    private TextInImageRecognizer mTextInImageRecognizer;
    private final String POGO_PACKAGE_NAME = "com.nianticlabs.pokemongo";
    private final String POGO_MAIN_ACTIVITY_NAME = "com.nianticproject.holoholo.libholoholo.unity.UnityMainActivity";
    private final String PATHNAME;
    private boolean mInterrupted;
    private final String LOG_TAG = getClass().getSimpleName();

    public PogoInteractor(Context context) {
        mContext = context;
        mTextInImageRecognizer = new TextInImageRecognizer(mContext);
        PATHNAME = Environment.getExternalStorageDirectory().getPath() + "/PogoAccountChecker";
        mInterrupted = false;
    }

    public boolean startPogo() {
        if (mInterrupted) return false;
        try {
            Shell.runSuCommand("am start -n " + POGO_PACKAGE_NAME + "/" + POGO_MAIN_ACTIVITY_NAME);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception when starting Pogo.");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Exception when starting Pogo.");
            e.printStackTrace();
            return false;
        }
        Log.i(LOG_TAG, "Pogo started.");
        return true;
    }

    public boolean stopPogo() {
        if (mInterrupted) return false;
        try {
            Shell.runSuCommand("am force-stop " + POGO_PACKAGE_NAME);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception when stopping Pogo.");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        Log.i(LOG_TAG, "Pogo stopped.");
        return true;
    }

    public boolean clearAppData() {
        if (mInterrupted) return false;
        try {
            Shell.runSuCommand("pm clear " + POGO_PACKAGE_NAME);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception when clearing app data.");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Exception when clearing app data.");
            e.printStackTrace();
            return false;
        }
        Log.i(LOG_TAG, "App data cleared.");
        return true;
    }

    public boolean selectDateOfBirth(int numRetries) {
        if (numRetries < 1) {
            numRetries = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numRetries) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return false;
            String text = visionText.getText().toLowerCase();
            if ((text.contains("date") || text.contains("birth")) && text.contains("submit")) {
                Log.i(LOG_TAG, "On DOB screen.");

                // Open year selector.
                if (!tapText(visionText, "2019")) return false;

                // Wait for animation.
                Utils.sleep(500);

                // Select year of birth.
                if (!selectYearOfBirth(5)) return false;

                // Wait for animation.
                Utils.sleep(500);

                // Submit date of birth.
                if (!submitDateOfBirth(visionText)) return false;

                Log.i(LOG_TAG, "Year of birth submitted.");
                return true;
            }
            Log.w(LOG_TAG, "Not on DOB screen.");
            onWrongScreenCount++;
        }
        Log.e(LOG_TAG, "DOB screen not detected after " + Integer.toString(numRetries) + " retries.");
        return false;
    }

    private boolean selectYearOfBirth(int numRetries) {
        if (numRetries < 1) {
            numRetries = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numRetries) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return false;
            if (visionText.getText().toLowerCase().contains("2018")) {
                Log.i(LOG_TAG, "Year selector detected.");
                return tapText(visionText, "2018");
            }
            Log.w(LOG_TAG, "Year selector not detected.");
            onWrongScreenCount++;
        }
        Log.e(LOG_TAG, "Year selector not detected after " + Integer.toString(numRetries) + " retries.");
        return false;
    }

    private boolean submitDateOfBirth(FirebaseVisionText visionText) {
        if (visionText.getText().toLowerCase().contains("submit")) {
            Log.i(LOG_TAG, "Submit button detected.");
            return tapText(visionText, "submit");
        }
        Log.e(LOG_TAG, "Couldn't detect submit button.");
        return false;
    }

    public boolean selectReturningPlayer(int numRetries) {
        if (numRetries < 1) {
            numRetries = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numRetries) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return false;
            if (visionText.getText().toLowerCase().contains("returning")) {
                Log.i(LOG_TAG, "On returning/new player selection screen.");
                return tapText(visionText, "returning");
            }
            Log.w(LOG_TAG, "Not on returning/new player selection screen.");
            onWrongScreenCount++;
        }
        Log.e(LOG_TAG, "Returning/new player selection screen not detected after " + Integer.toString(numRetries) + " retries.");
        return false;
    }

    public boolean selectPTC(int numRetries) {
        if (numRetries < 1) {
            numRetries = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numRetries) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return false;
            String text = visionText.getText().toLowerCase();
            if (text.contains("trainer")) {
                Log.i(LOG_TAG, "On account type selection screen.");
                return tapText(visionText, "trainer");
            } else if(text.contains("club")) {
                Log.i(LOG_TAG, "On account type selection screen.");
                return tapText(visionText, "club");
            }
            Log.w(LOG_TAG, "Not on account type selection screen.");
            onWrongScreenCount++;
        }
        Log.e(LOG_TAG, "Account type selection screen not detected after " + Integer.toString(numRetries) + " retries.");
        return false;
    }

    public boolean login(String username, String password, int numRetries) {
        if (numRetries < 1) {
            numRetries = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numRetries) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return false;
            String text = visionText.getText().toLowerCase();
            if (text.contains("username") && text.contains("password") && text.contains("sign")) {
                Log.i(LOG_TAG, "On login screen.");

                int statusBarHeight = getStatusBarHeigth();

                if (!tapText(visionText, "username")) return false;
                if (!insertText(username)) return false;
                // Tap to hide keyboard.
                if (!tapScreen(Utils.randomWithRange(10, 100), statusBarHeight + Utils.randomWithRange(10, 20))) return false;

                // Wait for keyboard to disappear.
                Utils.sleep(500);

                if (!tapText(visionText, "password")) return false;
                if (!insertText(password)) return false;
                // Tap to hide keyboard.
                if (!tapScreen(Utils.randomWithRange(10, 100), statusBarHeight + Utils.randomWithRange(10, 20))) return false;

                // Wait for keyboard to disappear.
                Utils.sleep(500);

                return tapText(visionText, "sign");
            }
            Log.w(LOG_TAG, "Not on login screen.");
            onWrongScreenCount++;
        }
        Log.e(LOG_TAG, "Login screen not detected after " + Integer.toString(numRetries) + " retries.");
        return false;
    }

    public enum LoginResult {
        ACCOUNT_NOT_BANNED, ACCOUNT_BANNED, ACCOUNT_NOT_EXIST, ERROR
    }

    public LoginResult getLoginResult(int numRetries) {
        if (numRetries < 1) {
            numRetries = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numRetries) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return LoginResult.ERROR;
            String text = visionText.getText().toLowerCase();
            if (text.contains("forgot")) {
                // Still on login screen.
                Log.w(LOG_TAG, "Still on login screen.");
                onWrongScreenCount++;
                continue;
            }
            if (text.contains("remember") || text.contains("surroundings")) {
                Utils.sleep(2000);
                // Recheck if account is not banned, because the loading screen is shortly visible before the ban screen appears.
                visionText = getVisionTextInCurrentScreen();
                if (visionText == null) return LoginResult.ERROR;
                text = visionText.getText().toLowerCase();
                if (text.contains("termination") || text.contains("service")) {
                    // Account is banned. :/
                    return LoginResult.ACCOUNT_BANNED;
                }
                // Account is not banned, yay.
                return LoginResult.ACCOUNT_NOT_BANNED;
            }
            if (text.contains("termination") || text.contains("service")) {
                // Account is banned. :/
                return LoginResult.ACCOUNT_BANNED;
            }
            if (text.contains("incorrect") || text.contains("minutes")) {
                // Account does not exist or username/password is incorrect.
                return LoginResult.ACCOUNT_NOT_EXIST;
            }
            if (text.contains("unable") || text.contains("again")) {
                // Unable to authenticate.
                Log.e(LOG_TAG, "Pogo was unable to authenticate.");
                return LoginResult.ERROR;
            }
            Log.e(LOG_TAG, "Detected none of the login keywords.");
            onWrongScreenCount++;
        }
        Log.e(LOG_TAG, "Login result not detected after " + Integer.toString(numRetries) + " retries.");
        return LoginResult.ERROR;
    }

    private boolean tapScreen(int x, int y) {
        if (mInterrupted) return false;
        try {
            Shell.runSuCommand("input tap " + x + " " + y);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception when tapping screen.");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        Log.d(LOG_TAG, "Tapped screen at: " + Integer.toString(x) + "," + Integer.toString(y));
        return true;
    }

    private boolean tapRandomInBox(@NonNull Point[] boxCornerPoints) {
        int tapX = Utils.randomWithRange(boxCornerPoints[0].x, boxCornerPoints[2].x);
        int tapY = Utils.randomWithRange(boxCornerPoints[0].y, boxCornerPoints[2].y);
        return tapScreen(tapX, tapY);
    }

    private boolean tapText(@NonNull FirebaseVisionText visionText, @NonNull String text) {
        Point[] cornerPoints = getBlockCornerPoints(visionText.getTextBlocks(), text);
        if (cornerPoints == null) return false;
        return tapRandomInBox(cornerPoints);
    }

    private boolean insertText(String text) {
        if (mInterrupted) return false;
        try {
            Shell.runSuCommand("input text '" + text + "'");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception when inserting text.");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Uri takeScreenshot() {
        if (mInterrupted) return null;
        try {
            Shell.runSuCommand("screencap -p " + PATHNAME + "/screenshot.png");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception when taking screenshot.");
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        Log.d(LOG_TAG, "Screenshot taken successfully.");
        return Uri.fromFile(new File(PATHNAME + "/screenshot.png"));
    }

    private FirebaseVisionText getVisionTextInCurrentScreen() {
        Uri screenshotUri = takeScreenshot();
        if (screenshotUri == null) return null;
        return mTextInImageRecognizer.detectText(screenshotUri);
    }

    private Point[] getBlockCornerPoints(List<FirebaseVisionText.TextBlock> textBlocks, String text) {
        text = text.toLowerCase();
        for (FirebaseVisionText.TextBlock textBlock : textBlocks) {
            if (textBlock.getText().toLowerCase().contains(text)) {
                for (FirebaseVisionText.Line line: textBlock.getLines()) {
                    if (line.getText().toLowerCase().contains(text)) {
                        for (FirebaseVisionText.Element element : line.getElements()) {
                            if (element.getText().toLowerCase().contains(text) && !element.getText().contains("?")) {
                                return element.getCornerPoints();
                            }
                        }
                    }
                }
            }
        }
        Log.d(LOG_TAG, "Couldn't find text in textBlock.");
        return null;
    }

    private int getStatusBarHeigth() {
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId != 0) {
            return mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public void close() {
        mInterrupted = true;
        mTextInImageRecognizer.close();
        try {
            Shell.runSuCommand("rm " + PATHNAME + "/screenshot.png");
        } catch (IOException | InterruptedException e) {
            Log.e(LOG_TAG, "Exception when deleting screenshot.png.");
            e.printStackTrace();
        }
    }
}
