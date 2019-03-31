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
    private final String LOG_TAG = getClass().getSimpleName();

    public PogoInteractor(Context context) {
        mContext = context;
        mScreenInteractor = new ScreenInteractor(mContext);
        POGO_PACKAGE = "com.nianticlabs.pokemongo";
    }

    public void startPogo() {
        Shell.runSuCommand("am start -n " + POGO_PACKAGE + "/com.nianticproject.holoholo.libholoholo.unity.UnityMainActivity");
        Log.i(LOG_TAG, "Pogo started.");
    }

    public void stopPogo() {
        Shell.runSuCommand("am force-stop " + POGO_PACKAGE);
        Log.i(LOG_TAG, "Pogo stopped.");
    }

    public void clearAppData() {
        Shell.runSuCommand("pm clear " + POGO_PACKAGE);
        Log.i(LOG_TAG, "App data cleared.");
    }

    public enum Screen {
        DATE_OF_BIRTH, PLAYER_SELECTION, LOGIN, LOADING, ACCOUNT_BANNED, ACCOUNT_NEW, ACCOUNT_WRONG_CREDENTIALS, ACCOUNT_NOT_ACTIVATED, ACCOUNT_LOCKED, NOT_AUTHENTICATE, UNKNOWN
    }

    public Screen currentScreen() {
        FirebaseVisionText visionText = mScreenInteractor.getVisionText();
        if (visionText == null) return Screen.UNKNOWN;
        String text = visionText.getText().toLowerCase();

        if (text.contains("username") && text.contains("sign") && text.contains("forgot")) {
            return Screen.LOGIN;
        }

        if (text.contains("check") || text.contains("exists") || text.contains("correctly")) {
            return Screen.ACCOUNT_NEW;
        }

        if (text.contains("returning") || text.contains("player") || text.contains("new")) {
            return Screen.PLAYER_SELECTION;
        }

        if (text.contains("date") || text.contains("birth") || text.contains("submit")) {
            return Screen.DATE_OF_BIRTH;
        }

        if (text.contains("termination") || text.contains("permanently") || text.contains("violating")) {
            return Screen.ACCOUNT_BANNED;
        }

        if (text.contains("incorrect") || text.contains("before") || text.contains("minutes")) {
            return Screen.ACCOUNT_WRONG_CREDENTIALS;
        }

        if (text.contains("remember") || text.contains("alert") || text.contains("surroundings")) {
            return Screen.LOADING;
        }

        if (text.contains("authenticate") || text.contains("again")) {
            return Screen.NOT_AUTHENTICATE;
        }

        if (text.contains("activate") || text.contains("order") || text.contains("play")) {
            return Screen.ACCOUNT_NOT_ACTIVATED;
        }

        if (text.contains("security") || text.contains("regain") || text.contains("questions")) {
            return Screen.ACCOUNT_LOCKED;
        }

        return Screen.UNKNOWN;
    }

    public void selectDateOfBirth() {
        if (!mScreenReady) prepareScreen();

        // Open year selector.
        mScreenInteractor.tapRandom(xYearSelector, yYearSelector, widthYearSelector, heightYearSelector);

        // Wait for animation.
        Utils.sleep(Utils.randomWithRange(450, 550));

        // Select year of birth.
        mScreenInteractor.tapRandom(x2010, y2010, width2010, height2010);

        // Wait for animation.
        Utils.sleep(Utils.randomWithRange(450, 550));

        // Submit date of birth.
        mScreenInteractor.tapRandom(xSubmit, ySubmit, widthSubmit, heightSubmit);

        Log.i(LOG_TAG, "Year of birth submitted.");
    }

    public void selectReturningPlayer() {
        if (!mScreenReady) prepareScreen();

        mScreenInteractor.tapRandom(xReturningPlayer, yReturningPlayer, widthReturningPlayer, heightReturningPlayer);
    }

    public void selectPTC() {
        mScreenInteractor.tapRandom(xPtc, yPtc, widthPtc, heightPtc);
    }

    public void login(String username, String password) {
        // Tap in username box.
        mScreenInteractor.tapRandom(xUsername, yUsername, widthUsername, heightUsername);

        // Type the username.
        mScreenInteractor.insertText(username);

        // Tap to hide keyboard.
        mScreenInteractor.tapRandom(xPokemon, yPokemon, widthPokemon, heightPokemon);

        // Wait for keyboard to disappear.
        Utils.sleep(Utils.randomWithRange(450, 550));

        // Tap in password box.
        mScreenInteractor.tapRandom(xPassword, yPassword, widthPassword, heightPassword);

        // Type the username.
        mScreenInteractor.insertText(password);

        // Tap to hide keyboard.
        mScreenInteractor.tapRandom(xPokemon, yPokemon, widthPokemon, heightPokemon);

        // Wait for keyboard to disappear.
        Utils.sleep(Utils.randomWithRange(450, 550));

        mScreenInteractor.tapRandom(xSignIn, ySignIn, widthSignIn, heightSignIn);
    }

    private void prepareScreen() {
        mScreenInteractor.hideBars();

        int virtualWidth = mScreenInteractor.resizeTo16x9();

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
        mScreenInteractor.interrupt();
    }

    public void resume() {
        mScreenInteractor.resume();
    }

    public void cleanUp(boolean clearAppData) {
        clearAppData();
        mScreenReady = false;
        mScreenInteractor.cleanUp();
    }
}
