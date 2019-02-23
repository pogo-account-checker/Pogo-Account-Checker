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

import androidx.annotation.NonNull;

public class PogoInteractor {
    private Context mContext;
    private TextInImageRecognizer mTextInImageRecognizer;
    private final String POGO_PACKAGE_NAME = "com.nianticlabs.pokemongo";
    private final String PATHNAME;
    private int resizedScreenWidth, resizedScreenHeight, realScreenWidth, realScreenHeight;
    private int xYearSelector, yYearSelector, widthYearSelector, heightYearSelector;
    private int x2010, y2010, width2010, height2010;
    private int xSubmit, ySubmit, widthSubmit, heightSubmit;
    private int xReturningPlayer, yReturningPlayer, widthReturningPlayer, heightReturningPlayer;
    private int xPtc, yPtc, widthPtc, heightPtc;
    private int xUsername, yUsername, widthUsername, heightUsername;
    private int xPassword, yPassword, widthPassword, heightPassword;
    private int xSignIn, ySignIn, widthSignIn, heightSignIn;
    private int xPokemon, yPokemon, widthPokemon, heightPokemon;
    private boolean mInterrupted;
    private final String LOG_TAG = getClass().getSimpleName();

    public PogoInteractor(Context context) {
        mContext = context;
        mTextInImageRecognizer = new TextInImageRecognizer(mContext);
        PATHNAME = Environment.getExternalStorageDirectory().getPath() + "/PogoAccountChecker";
        mInterrupted = false;

        hideBars();

        realScreenWidth = resizedScreenWidth = getScreenWidth();
        realScreenHeight = resizedScreenHeight = getScreenHeight();

        if (!((realScreenWidth % 9) == 0 && (realScreenHeight % 16) == 0) && !(realScreenWidth / 9 == realScreenHeight / 16)) {
            // Screen is not 16x9, resize it.
            if (realScreenWidth >= 2160 && realScreenHeight >= 3840) {
                resizeScreen(2160, 3840);
            } else if (realScreenWidth >= 1440 && realScreenHeight >= 2560) {
                resizeScreen(1440, 2560);
            } else if (realScreenWidth >= 1080 && realScreenHeight >= 1920) {
                resizeScreen(1080, 1920);
            } else if (realScreenWidth >= 720 && realScreenHeight >= 1280) {
                resizeScreen(720, 1280);
            } else if (realScreenWidth >= 540 && realScreenHeight >= 960) {
                resizeScreen(540, 960);
            } else if (realScreenWidth >= 360 && realScreenHeight >= 640) {
                resizeScreen(360, 640);
            }
        }

        // Set center coords of buttons/text views.
        xYearSelector = scale(mContext.getResources().getInteger(R.integer.x_year_selector));
        yYearSelector = scale(mContext.getResources().getInteger(R.integer.y_year_selector));
        x2010 = scale(mContext.getResources().getInteger(R.integer.x_2010));
        y2010 = scale(mContext.getResources().getInteger(R.integer.y_2010));
        xSubmit = scale(mContext.getResources().getInteger(R.integer.x_submit));
        ySubmit = scale(mContext.getResources().getInteger(R.integer.y_submit));
        xReturningPlayer = scale(mContext.getResources().getInteger(R.integer.x_returning_player));
        yReturningPlayer = scale(mContext.getResources().getInteger(R.integer.y_returning_player));
        xPtc = scale(mContext.getResources().getInteger(R.integer.x_ptc));
        yPtc = scale(mContext.getResources().getInteger(R.integer.y_ptc));
        xUsername = scale(mContext.getResources().getInteger(R.integer.x_username));
        yUsername = scale(mContext.getResources().getInteger(R.integer.y_username));
        xPassword = scale(mContext.getResources().getInteger(R.integer.x_password));
        yPassword = scale(mContext.getResources().getInteger(R.integer.y_password));
        xSignIn = scale(mContext.getResources().getInteger(R.integer.x_sign_in));
        ySignIn = scale(mContext.getResources().getInteger(R.integer.y_sign_in));

        // Set width and height of buttons/text views.
        widthYearSelector = scale(mContext.getResources().getInteger(R.integer.width_year_selector));
        heightYearSelector = scale(mContext.getResources().getInteger(R.integer.height_year_selector));
        width2010 = scale(mContext.getResources().getInteger(R.integer.width_2010));
        height2010 = scale(mContext.getResources().getInteger(R.integer.height_2010));
        widthSubmit = scale(mContext.getResources().getInteger(R.integer.width_submit));
        heightSubmit = scale(mContext.getResources().getInteger(R.integer.height_submit));
        widthReturningPlayer = scale(mContext.getResources().getInteger(R.integer.width_returning_player));
        heightReturningPlayer = scale(mContext.getResources().getInteger(R.integer.height_returning_player));
        widthPtc = scale(mContext.getResources().getInteger(R.integer.width_ptc));
        heightPtc = scale(mContext.getResources().getInteger(R.integer.height_ptc));
        widthUsername = scale(mContext.getResources().getInteger(R.integer.width_username));
        heightUsername = scale(mContext.getResources().getInteger(R.integer.height_username));
        widthPassword = scale(mContext.getResources().getInteger(R.integer.width_password));
        heightPassword = scale(mContext.getResources().getInteger(R.integer.height_password));
        widthSignIn = scale(mContext.getResources().getInteger(R.integer.width_sign_in));
        heightSignIn = scale(mContext.getResources().getInteger(R.integer.height_sign_in));

        // Set width, height, and center coords of Pokemon banner.
        xPokemon = scale(mContext.getResources().getInteger(R.integer.x_pokemon));
        yPokemon = scale(mContext.getResources().getInteger(R.integer.y_pokemon));
        widthPokemon = scale(mContext.getResources().getInteger(R.integer.width_pokemon));
        heightPokemon = scale(mContext.getResources().getInteger(R.integer.height_pokemon));
    }

    public boolean startPogo() {
        if (!mInterrupted && Shell.runSuCommand("am start -n " + POGO_PACKAGE_NAME + "/com.nianticproject.holoholo.libholoholo.unity.UnityMainActivity")) {
            Log.i(LOG_TAG, "Pogo started.");
            return true;
        } else {
            return false;
        }
    }

    public boolean stopPogo() {
        if (!mInterrupted && Shell.runSuCommand("am force-stop " + POGO_PACKAGE_NAME)) {
            Log.i(LOG_TAG, "Pogo stopped.");
            return true;
        } else {
            return false;
        }
    }

    public boolean clearAppData() {
        if (!mInterrupted && Shell.runSuCommand("pm clear " + POGO_PACKAGE_NAME)) {
            Log.i(LOG_TAG, "App data cleared.");
            return true;
        } else {
            return false;
        }
    }

    public boolean isOnDateOfBirthScreen(int numAttempts) {
        if (numAttempts < 1) {
            numAttempts = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numAttempts) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return false;
            String text = visionText.getText().toLowerCase();
            if (text.contains("date") || text.contains("birth") || text.contains("submit")) {
                Log.i(LOG_TAG, "On DOB screen.");
                return true;
            } else {
                Log.w(LOG_TAG, "Not on DOB screen.");
                onWrongScreenCount++;
            }
        }
        Log.e(LOG_TAG, "DOB screen not detected after " + Integer.toString(numAttempts) + " attempts.");
        return false;
    }

    public boolean selectDateOfBirth() {
        // Open year selector.
        if (!tapScreenRandom(xYearSelector, yYearSelector, widthYearSelector, heightYearSelector)) return false;

        // Wait for animation.
        Utils.sleep(Utils.randomWithRange(450, 550));

        // Select year of birth.
        if (!tapScreenRandom(x2010, y2010, width2010, height2010)) return false;

        // Wait for animation.
        Utils.sleep(Utils.randomWithRange(450, 550));

        // Submit date of birth.
        if (tapScreenRandom(xSubmit, ySubmit, widthSubmit, heightSubmit)) {
            Log.i(LOG_TAG, "Year of birth submitted.");
            return true;
        } else {
            return false;
        }
    }

    public boolean isOnReturningPlayerSelection(int numAttempts) {
        if (numAttempts < 1) {
            numAttempts = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numAttempts) {
            FirebaseVisionText visionText = getVisionTextInCurrentScreen();
            if (visionText == null) return false;
            String text = visionText.getText().toLowerCase();
            if (text.contains("returning") || text.contains("player") || text.contains("new")) {
                Log.i(LOG_TAG, "On returning player selection screen.");
                return true;
            } else {
                Log.w(LOG_TAG, "Not on returning player selection screen.");
                onWrongScreenCount++;
            }
        }
        Log.e(LOG_TAG, "Returning player selection screen not detected after " + Integer.toString(numAttempts) + " attempts.");
        return false;
    }

    public boolean selectReturningPlayer() {
        return tapScreenRandom(xReturningPlayer, yReturningPlayer, widthReturningPlayer, heightReturningPlayer);
    }

    public boolean selectPTC() {
        return tapScreenRandom(xPtc, yPtc, widthPtc, heightPtc);
    }

    public boolean login(String username, String password) {
        // Tap in username box.
        if (!tapScreenRandom(xUsername, yUsername, widthUsername, heightUsername)) return false;

        // Type the username.
        if (!insertText(username)) return false;

        // Tap to hide keyboard.
        if (!tapScreenRandom(xPokemon, yPokemon, widthPokemon, heightPokemon)) return false;

        // Wait for keyboard to disappear.
        Utils.sleep(Utils.randomWithRange(450, 550));

        // Tap in password box.
        if (!tapScreenRandom(xPassword, yPassword, widthPassword, heightPassword)) return false;

        // Type the username.
        if (!insertText(password)) return false;

        // Tap to hide keyboard.
        if (!tapScreenRandom(xPokemon, yPokemon, widthPokemon, heightPokemon)) return false;

        // Wait for keyboard to disappear.
        Utils.sleep(Utils.randomWithRange(450, 550));

        return tapScreenRandom(xSignIn, ySignIn, widthSignIn, heightSignIn);
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
        if (!mInterrupted && Shell.runSuCommand("input tap " + x + " " + y)) {
            Log.d(LOG_TAG, "Tapped screen at: " + Float.toString(x) + "," + Float.toString(y));
            return true;
        } else {
            return false;
        }
    }

    private boolean tapScreenRandom(int x, int y, int offsetX, int offsetY) {
        int randomOffsetX = Utils.randomWithRange(-offsetX / 2, offsetX / 2);
        int randomOffsetY = Utils.randomWithRange(-offsetY / 2, offsetY / 2);
        return tapScreen(x + randomOffsetX, y + randomOffsetY);
    }

    private boolean insertText(@NonNull String text) {
        return !mInterrupted && Shell.runSuCommand("input text '" + text + "'");
    }

    private Uri takeScreenshot() {
        if (!mInterrupted && Shell.runSuCommand("screencap -p " + PATHNAME + "/screenshot.png")) {
            return Uri.fromFile(new File(PATHNAME + "/screenshot.png"));
        } else {
            return null;
        }
    }

    private FirebaseVisionText getVisionTextInCurrentScreen() {
        Uri screenshotUri = takeScreenshot();
        if (screenshotUri != null) {
            return mTextInImageRecognizer.detectText(screenshotUri);
        } else {
            return null;
        }
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

    private void resizeScreen(int newWidth, int newHeight) {
        if (Shell.runSuCommand("wm size " + newWidth + "x" + newHeight)) {
            resizedScreenWidth = newWidth;
            resizedScreenHeight = newHeight;
        } else {
            Log.e(LOG_TAG, "Couldn't resize screen, aborting program!");
            System.exit(0);
        }
    }

    private int scale(int number) {
        float scale = (float) resizedScreenWidth / 1080; // 1080 is standard.
        return (int) (scale * number);
    }

    private boolean showBars() {
        return Shell.runSuCommand("settings put global policy_control null*");
    }

    private boolean hideBars() {
        return Shell.runSuCommand("settings put global policy_control immersive.full=com.nianticlabs.pokemongo");
    }

    public void close() {
        mInterrupted = true;
        mTextInImageRecognizer.close();
        showBars();
        if (realScreenWidth != resizedScreenWidth || realScreenHeight != resizedScreenHeight) resizeScreen(realScreenWidth, realScreenHeight);
        Shell.runSuCommand("rm " + PATHNAME + "/screenshot.png");
    }
}
