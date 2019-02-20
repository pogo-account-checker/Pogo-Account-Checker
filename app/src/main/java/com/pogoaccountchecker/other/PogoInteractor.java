package com.pogoaccountchecker.other;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.pogoaccountchecker.R;
import com.pogoaccountchecker.utils.TextInImageRecognizer;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class PogoInteractor {
    private Context mContext;
    private TextInImageRecognizer mTextInImageRecognizer;
    private final String POGO_PACKAGE_NAME = "com.nianticlabs.pokemongo";
    private final String POGO_MAIN_ACTIVITY_NAME = "com.nianticproject.holoholo.libholoholo.unity.UnityMainActivity";
    private final String PATHNAME;
    private int screenHeight;
    private int screenWidth;
    private float percentage2019Width;
    private float percentage2019Height;
    private float percentage2010Width;
    private float percentage2010Height;
    private float percentageSubmitWidth;
    private float percentageSubmitHeight;
    private boolean mInterrupted;
    private final String LOG_TAG = getClass().getSimpleName();

    public PogoInteractor(Context context) {
        mContext = context;
        mTextInImageRecognizer = new TextInImageRecognizer(mContext);
        PATHNAME = Environment.getExternalStorageDirectory().getPath() + "/PogoAccountChecker";
        mInterrupted = false;

        screenHeight = getScreenHeight();
        screenWidth  = getScreenWidth();

        if (!((screenWidth % 9) == 0 && (screenHeight % 16) == 0) && !(screenWidth / 9 == screenHeight / 16)) {
            // Screen is not 16x9, resize.
            if (screenWidth >= 2160 && screenHeight >= 3840) {
                resizeScreen(2160, 3840);
            } else if (screenWidth >= 1440 && screenHeight >= 2560) {
                resizeScreen(1440, 2560);
            } else if (screenWidth >= 1080 && screenHeight >= 1920) {
                resizeScreen(1080, 1920);
            } else if (screenWidth >= 720 && screenHeight >= 1280) {
                resizeScreen(720, 1280);
            } else if (screenWidth >= 540 && screenHeight >= 960) {
                resizeScreen(540, 960);
            } else if (screenWidth >= 360 && screenHeight >= 640) {
                resizeScreen(360, 640);
            }
        }

        percentage2019Width = (float) mContext.getResources().getInteger(R.integer.width_2019) / 1000000;
        percentage2019Height = (float) mContext.getResources().getInteger(R.integer.height_2019) / 1000000;
        percentage2010Width = (float) mContext.getResources().getInteger(R.integer.width_2010) / 1000000;
        percentage2010Height = (float) mContext.getResources().getInteger(R.integer.height_2010) / 1000000;
        percentageSubmitWidth = (float) mContext.getResources().getInteger(R.integer.width_submit) / 1000000;
        percentageSubmitHeight = (float) mContext.getResources().getInteger(R.integer.height_submit) / 1000000;

        hideControlBar();
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

    public boolean selectDateOfBirth(int numAttempts) {
        if (numAttempts < 1) {
            numAttempts = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numAttempts) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return false;
            String text = visionText.getText().toLowerCase();
            if (text.contains("2019") && text.contains("submit")) {
                Log.i(LOG_TAG, "On DOB screen.");

                // Open year selector.
                if (!tapRandomWithPercentage(percentage2019Width, percentage2019Height, 0)) return false;

                // Wait for animation.
                Utils.sleep(500);

                // Select year of birth.
                if(!tapRandomWithPercentage(percentage2010Width, percentage2010Height, 0)) return false;

                // Wait for animation.
                Utils.sleep(500);

                // Submit date of birth.
                if (tapRandomWithPercentage(percentageSubmitWidth, percentageSubmitHeight, 0)) {
                    Log.i(LOG_TAG, "Year of birth submitted.");
                    return true;
                }
                return false;
            }
            Log.w(LOG_TAG, "Not on DOB screen.");
            onWrongScreenCount++;
        }
        Log.e(LOG_TAG, "DOB screen not detected after " + Integer.toString(numAttempts) + " attempts.");
        return false;
    }

    public boolean selectReturningPlayer(int numAttempts) {
        if (numAttempts < 1) {
            numAttempts = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numAttempts) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return false;
            if (visionText.getText().toLowerCase().contains("returning")) {
                Log.i(LOG_TAG, "On returning/new player selection screen.");
                return tapText(visionText, "returning");
            }
            Log.w(LOG_TAG, "Not on returning/new player selection screen.");
            onWrongScreenCount++;
        }
        Log.e(LOG_TAG, "Returning/new player selection screen not detected after " + Integer.toString(numAttempts) + " attempts.");
        return false;
    }

    public boolean selectPTC(int numAttempts) {
        if (numAttempts < 1) {
            numAttempts = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numAttempts) {
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
        Log.e(LOG_TAG, "Account type selection screen not detected after " + Integer.toString(numAttempts) + " attempts.");
        return false;
    }

    public boolean login(String username, String password, int numAttempts) {
        if (numAttempts < 1) {
            numAttempts = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numAttempts) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return false;
            List<FirebaseVisionText.Line> usernameLines = getLines(visionText.getTextBlocks(), "username", 2);
            List<FirebaseVisionText.Line> passwordLines = getLines(visionText.getTextBlocks(), "password", 2);
            if (usernameLines.size() == 2 && passwordLines.size() == 2 && visionText.getText().toLowerCase().contains("sign")) {
                Log.i(LOG_TAG, "On login screen.");

                int statusBarHeight = getStatusBarHeight();

                Point[] cornerPoints;

                // Get corner points from the line that contains only "username".
                String textLine0 = usernameLines.get(0).getText().toLowerCase();
                if (!(textLine0.contains("forgot") && textLine0.contains("your") && textLine0.contains("?"))) {
                    // Get corner points from the first line.
                    cornerPoints = getCornerPoints(usernameLines.get(0), "username");

                } else {
                    // Get corner points from the second line.
                    cornerPoints = getCornerPoints(usernameLines.get(1), "username");
                }
                // Tap "username".
                if (cornerPoints == null || !tapRandomInBox(cornerPoints)) return false;
                if (!insertText(username)) return false;
                // Tap to hide keyboard.
                if (!tapScreen(Utils.randomWithRange(10, 100), statusBarHeight + Utils.randomWithRange(10, 20))) return false;

                // Wait for keyboard to disappear.
                Utils.sleep(500);

                // Get corner points from the line that contains only "password".
                textLine0 = passwordLines.get(0).getText().toLowerCase();
                if (!(textLine0.contains("forgot") && textLine0.contains("your") && textLine0.contains("?"))) {
                    // Get corner points from the first line.
                    cornerPoints = getCornerPoints(passwordLines.get(0), "password");

                } else {
                    // Get corner points from the second line.
                    cornerPoints = getCornerPoints(passwordLines.get(1), "password");
                }
                // Tap "password".
                if (cornerPoints == null || !tapRandomInBox(cornerPoints)) return false;
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
        Log.e(LOG_TAG, "Login screen not detected after " + Integer.toString(numAttempts) + " attempts.");
        return false;
    }

    public enum LoginResult {
        ACCOUNT_NOT_BANNED, ACCOUNT_BANNED, ACCOUNT_NOT_EXIST, ERROR
    }

    public LoginResult getLoginResult(int numAttempts) {
        if (numAttempts < 1) {
            numAttempts = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numAttempts) {
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
                Utils.sleep(500);
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
        Log.e(LOG_TAG, "Login result not detected after " + Integer.toString(numAttempts) + " attempts.");
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

    private boolean tapRandomWithPercentage(float widthRatio, float heightRatio, int offset) {
        int x = (int) (widthRatio * screenWidth);
        int y = (int) (heightRatio * screenHeight);
        int tapX = Utils.randomWithRange(x - offset, x + offset);
        int tapY = Utils.randomWithRange(y - offset, y + offset);
        return tapScreen(tapX, tapY);
    }

    private boolean tapRandomInBox(@NonNull Point[] boxCornerPoints) {
        int tapX = Utils.randomWithRange(boxCornerPoints[0].x, boxCornerPoints[2].x);
        int tapY = Utils.randomWithRange(boxCornerPoints[0].y, boxCornerPoints[2].y);
        return tapScreen(tapX, tapY);
    }

    /**
     * Taps the first occurrence of a {@code String} in a {@link FirebaseVisionText}.
     *
     * @param visionText The {@link FirebaseVisionText} that possibly contains the {@code String}.
     * @param text The {@code String} that needs to be tapped.
     * @return true when the text was successfully tapped and false when not.
     */
    private boolean tapText(@NonNull FirebaseVisionText visionText, @NonNull String text) {
        List<FirebaseVisionText.Line> lines = getLines(visionText.getTextBlocks(), text, 1);
        if (lines.isEmpty()) return false;
        Point[] cornerPoints = getCornerPoints(lines.get(0), text);
        return tapRandomInBox(cornerPoints);
    }

    private boolean insertText(@NonNull String text) {
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

    private Point[] getCornerPoints(FirebaseVisionText.Line line, String text) {
        for (FirebaseVisionText.Element element : line.getElements()) {
            if (element.getText().toLowerCase().contains(text)) {
                return element.getCornerPoints();
            }
        }
        Log.d(LOG_TAG, "Couldn't find text in line.");
        return null;
    }

    private List<FirebaseVisionText.Line> getLines(@NonNull List<FirebaseVisionText.TextBlock> textBlocks, @NonNull String text, int count) {
        List<FirebaseVisionText.Line> lines = new ArrayList<>(count);
        if (count < 1) count = 1;
        int foundCount = 0;
        text = text.toLowerCase();
        for (FirebaseVisionText.TextBlock textBlock : textBlocks) {
            if (textBlock.getText().toLowerCase().contains(text)) {
                for (FirebaseVisionText.Line line: textBlock.getLines()) {
                    if (line.getText().toLowerCase().contains(text)) {
                        lines.add(line);
                        foundCount++;
                        if (count == foundCount) return lines;
                    }
                }
            }
        }
        Log.d(LOG_TAG, "Couldn't find text in textBlock.");
        return lines;
    }

    private int getScreenHeight() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        if (size.y > size.x) {
            return size.y;
        } else {
            return size.x;
        }
    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        if (size.x < size.y) {
            return size.x;
        } else {
            return size.y;
        }
    }

    private int getStatusBarHeight() {
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId != 0) {
            return mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private boolean resizeScreen(int newWidth, int newHeight) {
        try {
            Shell.runSuCommand("wm size " + newWidth + "x" + newHeight);
            screenWidth = newWidth;
            screenHeight = newHeight;
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hideControlBar() {
        try {
            Shell.runSuCommand("settings put global policy_control immersive.full=com.nianticlabs.pokemongo");
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
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
