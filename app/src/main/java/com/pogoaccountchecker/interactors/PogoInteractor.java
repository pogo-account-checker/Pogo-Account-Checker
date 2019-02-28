package com.pogoaccountchecker.interactors;

import android.content.Context;
import android.util.Log;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.pogoaccountchecker.R;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

public class PogoInteractor {
    private Context mContext;
    private final String POGO_PACKAGE;
    private ScreenInteractor mScreenInteractor;
    private volatile boolean mScreenReady;
    private int xYearSelector, yYearSelector, widthYearSelector, heightYearSelector;
    private int x2010, y2010, width2010, height2010;
    private int xSubmit, ySubmit, widthSubmit, heightSubmit;
    private int xReturningPlayer, yReturningPlayer, widthReturningPlayer, heightReturningPlayer;
    private int xPtc, yPtc, widthPtc, heightPtc;
    private int xUsername, yUsername, widthUsername, heightUsername;
    private int xPassword, yPassword, widthPassword, heightPassword;
    private int xSignIn, ySignIn, widthSignIn, heightSignIn;
    private int xPokemon, yPokemon, widthPokemon, heightPokemon;
    private volatile boolean mInterrupted;
    private final String LOG_TAG = getClass().getSimpleName();

    public PogoInteractor(Context context) {
        mContext = context;
        mScreenInteractor = new ScreenInteractor(mContext);
        POGO_PACKAGE = "com.nianticlabs.pokemongo";
    }

    public boolean startPogo() {
        if (mInterrupted) return false;
        if (Shell.runSuCommand("am start -n " + POGO_PACKAGE + "/com.nianticproject.holoholo.libholoholo.unity.UnityMainActivity")) {
            Log.i(LOG_TAG, "Pogo started.");
            return true;
        } else {
            return false;
        }
    }

    public boolean stopPogo() {
        if (mInterrupted) return false;
        if (Shell.runSuCommand("am force-stop " + POGO_PACKAGE)) {
            Log.i(LOG_TAG, "Pogo stopped.");
            return true;
        } else {
            return false;
        }
    }

    public boolean clearAppData() {
        if (mInterrupted) return false;
        if (Shell.runSuCommand("pm clear " + POGO_PACKAGE)) {
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
            if (mInterrupted) return false;
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
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
        if (mInterrupted) return false;
        if (!mScreenReady) prepareScreen();

        // Open year selector.
        if (!mScreenInteractor.tapRandom(xYearSelector, yYearSelector, widthYearSelector, heightYearSelector)) return false;

        // Wait for animation.
        Utils.sleep(Utils.randomWithRange(450, 550));
        if (mInterrupted) return false;

        // Select year of birth.
        if (!mScreenInteractor.tapRandom(x2010, y2010, width2010, height2010)) return false;

        // Wait for animation.
        Utils.sleep(Utils.randomWithRange(450, 550));
        if (mInterrupted) return false;

        // Submit date of birth.
        if (mScreenInteractor.tapRandom(xSubmit, ySubmit, widthSubmit, heightSubmit)) {
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
            if (mInterrupted) return false;
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
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
        if (mInterrupted) return false;
        if (!mScreenReady) prepareScreen();

        return mScreenInteractor.tapRandom(xReturningPlayer, yReturningPlayer, widthReturningPlayer, heightReturningPlayer);
    }

    public boolean selectPTC() {
        if (mInterrupted) return false;
        return mScreenInteractor.tapRandom(xPtc, yPtc, widthPtc, heightPtc);
    }

    public boolean login(String username, String password) {
        if (mInterrupted) return false;

        // Tap in username box.
        if (!mScreenInteractor.tapRandom(xUsername, yUsername, widthUsername, heightUsername)) return false;

        // Type the username.
        if (!mScreenInteractor.insertText(username)) return false;

        // Tap to hide keyboard.
        if (!mScreenInteractor.tapRandom(xPokemon, yPokemon, widthPokemon, heightPokemon)) return false;

        // Wait for keyboard to disappear.
        Utils.sleep(Utils.randomWithRange(450, 550));
        if (mInterrupted) return false;

        // Tap in password box.
        if (!mScreenInteractor.tapRandom(xPassword, yPassword, widthPassword, heightPassword)) return false;

        // Type the username.
        if (!mScreenInteractor.insertText(password)) return false;

        // Tap to hide keyboard.
        if (!mScreenInteractor.tapRandom(xPokemon, yPokemon, widthPokemon, heightPokemon)) return false;

        // Wait for keyboard to disappear.
        Utils.sleep(Utils.randomWithRange(450, 550));
        if (mInterrupted) return false;

        return mScreenInteractor.tapRandom(xSignIn, ySignIn, widthSignIn, heightSignIn);
    }

    public enum LoginResult {
        NOT_BANNED, BANNED, WRONG_CREDENTIALS, NOT_ACTIVATED, LOCKED, ERROR, INTERRUPTED
    }

    public LoginResult getLoginResult(int numAttempts) {
        if (numAttempts < 1) {
            numAttempts = 1;
        }
        int onWrongScreenCount = 0;
        while (onWrongScreenCount != numAttempts) {
            if (mInterrupted) return LoginResult.INTERRUPTED;
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            if (visionText == null) return LoginResult.ERROR;
            String text = visionText.getText().toLowerCase();
            if (text.contains("username") && text.contains("sign") && text.contains("forgot")) {
                // Still on login screen.
                Log.w(LOG_TAG, "Still on login screen.");
                onWrongScreenCount++;
                continue;
            }
            if (text.contains("remember") || text.contains("alert") || text.contains("surroundings")) {
                Utils.sleep(500);
                // Recheck if account is not banned, because the loading screen is shortly visible before the ban screen appears.
                visionText = mScreenInteractor.getVisionText();
                if (visionText == null) return LoginResult.ERROR;
                text = visionText.getText().toLowerCase();
                if (text.contains("termination") || text.contains("permanently") || text.contains("violating")) {
                    // Account is banned. :/
                    return LoginResult.BANNED;
                }
                // Account is not banned, yay.
                return LoginResult.NOT_BANNED;
            }
            if (text.contains("termination") || text.contains("permanently") || text.contains("violating")) {
                // Account is banned. :/
                return LoginResult.BANNED;
            }
            if (text.contains("incorrect") || text.contains("before") || text.contains("minutes")) {
                // Account does not exist or username/password is incorrect.
                return LoginResult.WRONG_CREDENTIALS;
            }
            if (text.contains("activate") || text.contains("order") || text.contains("play")) {
                // Account is not activated.
                return LoginResult.NOT_ACTIVATED;
            }
            if (text.contains("security") || text.contains("regain") || text.contains("questions")) {
                // Account is locked.
                return LoginResult.LOCKED;
            }
            if (text.contains("authenticate") || text.contains("again")) {
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

    private void prepareScreen() {
        if (!mScreenInteractor.hideBars()) {
            Log.e(LOG_TAG, "Couldn't hide bars, aborting program!");
            System.exit(0);
        }
        int virtualWidth = mScreenInteractor.resizeTo16x9();
        if (virtualWidth == -1) {
            Log.e(LOG_TAG, "Couldn't resize screen, aborting program!");
            System.exit(0);
        }

        // Set center coords of buttons/text views.
        xYearSelector = scale(mContext.getResources().getInteger(R.integer.x_year_selector), virtualWidth);
        yYearSelector = scale(mContext.getResources().getInteger(R.integer.y_year_selector), virtualWidth);
        x2010 = scale(mContext.getResources().getInteger(R.integer.x_2010), virtualWidth);
        y2010 = scale(mContext.getResources().getInteger(R.integer.y_2010), virtualWidth);
        xSubmit = scale(mContext.getResources().getInteger(R.integer.x_submit), virtualWidth);
        ySubmit = scale(mContext.getResources().getInteger(R.integer.y_submit), virtualWidth);
        xReturningPlayer = scale(mContext.getResources().getInteger(R.integer.x_returning_player), virtualWidth);
        yReturningPlayer = scale(mContext.getResources().getInteger(R.integer.y_returning_player), virtualWidth);
        xPtc = scale(mContext.getResources().getInteger(R.integer.x_ptc), virtualWidth);
        yPtc = scale(mContext.getResources().getInteger(R.integer.y_ptc), virtualWidth);
        xUsername = scale(mContext.getResources().getInteger(R.integer.x_username), virtualWidth);
        yUsername = scale(mContext.getResources().getInteger(R.integer.y_username), virtualWidth);
        xPassword = scale(mContext.getResources().getInteger(R.integer.x_password), virtualWidth);
        yPassword = scale(mContext.getResources().getInteger(R.integer.y_password), virtualWidth);
        xSignIn = scale(mContext.getResources().getInteger(R.integer.x_sign_in), virtualWidth);
        ySignIn = scale(mContext.getResources().getInteger(R.integer.y_sign_in), virtualWidth);

        // Set width and height of buttons/text views.
        widthYearSelector = scale(mContext.getResources().getInteger(R.integer.width_year_selector), virtualWidth);
        heightYearSelector = scale(mContext.getResources().getInteger(R.integer.height_year_selector), virtualWidth);
        width2010 = scale(mContext.getResources().getInteger(R.integer.width_2010), virtualWidth);
        height2010 = scale(mContext.getResources().getInteger(R.integer.height_2010), virtualWidth);
        widthSubmit = scale(mContext.getResources().getInteger(R.integer.width_submit), virtualWidth);
        heightSubmit = scale(mContext.getResources().getInteger(R.integer.height_submit), virtualWidth);
        widthReturningPlayer = scale(mContext.getResources().getInteger(R.integer.width_returning_player), virtualWidth);
        heightReturningPlayer = scale(mContext.getResources().getInteger(R.integer.height_returning_player), virtualWidth);
        widthPtc = scale(mContext.getResources().getInteger(R.integer.width_ptc), virtualWidth);
        heightPtc = scale(mContext.getResources().getInteger(R.integer.height_ptc), virtualWidth);
        widthUsername = scale(mContext.getResources().getInteger(R.integer.width_username), virtualWidth);
        heightUsername = scale(mContext.getResources().getInteger(R.integer.height_username), virtualWidth);
        widthPassword = scale(mContext.getResources().getInteger(R.integer.width_password), virtualWidth);
        heightPassword = scale(mContext.getResources().getInteger(R.integer.height_password), virtualWidth);
        widthSignIn = scale(mContext.getResources().getInteger(R.integer.width_sign_in), virtualWidth);
        heightSignIn = scale(mContext.getResources().getInteger(R.integer.height_sign_in), virtualWidth);

        // Set width, height, and center coords of Pokemon banner.
        xPokemon = scale(mContext.getResources().getInteger(R.integer.x_pokemon), virtualWidth);
        yPokemon = scale(mContext.getResources().getInteger(R.integer.y_pokemon), virtualWidth);
        widthPokemon = scale(mContext.getResources().getInteger(R.integer.width_pokemon), virtualWidth);
        heightPokemon = scale(mContext.getResources().getInteger(R.integer.height_pokemon), virtualWidth);

        mScreenReady = true;
    }

    private int scale(int number, int virtualWidth) {
        float scale = (float) virtualWidth / 1080; // 1080 is standard.
        return (int) (scale * number);
    }

    public void interrupt() {
        mInterrupted = true;
    }

    public void resume() {
        mInterrupted = false;
    }

    public void cleanUp() {
        clearAppData();
        mInterrupted = true;
        mScreenReady = false;
        mScreenInteractor.cleanUp();
    }
}
