package com.pogoaccountchecker.interactors;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

public class PogoInteractor {
    private ScreenInteractor mScreenInteractor;
    private boolean mItemsLocated;
    private volatile boolean mInterrupted;
    private int xYearSelector, yYearSelector, widthYearSelector, heightYearSelector;
    private int x2010, y2010, width2010, height2010;
    private int xSubmit, ySubmit, widthSubmit, heightSubmit;
    private int xReturningPlayer, yReturningPlayer, widthReturningPlayer, heightReturningPlayer;
    private int xPtc, yPtc, widthPtc, heightPtc;
    private int xUsername, yUsername, widthUsername, heightUsername;
    private int xPassword, yPassword, widthPassword, heightPassword;
    private int xSignIn, ySignIn, widthSignIn, heightSignIn;
    private final String POGO_PACKAGE = "com.nianticlabs.pokemongo";
    private final String LOG_TAG = getClass().getSimpleName();

    public PogoInteractor(Context context) {
        mScreenInteractor = new ScreenInteractor(context);
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
        Log.i(LOG_TAG, "Pogo app data cleared.");
    }

    public enum Screen {
        LOGIN_FAILED, DATE_OF_BIRTH, PLAYER_SELECTION, LOGIN, LOADING, ACCOUNT_BANNED, ACCOUNT_WRONG_CREDENTIALS, ACCOUNT_NEW, ACCOUNT_NOT_ACTIVATED, ACCOUNT_LOCKED, NOT_AUTHENTICATE, UNKNOWN
    }

    public Screen currentScreen() {
        FirebaseVisionText visionText = mScreenInteractor.getVisionText();
        if (visionText == null) return Screen.UNKNOWN;
        String text = visionText.getText().toLowerCase();

        if (text.contains("failed") && text.contains("retry") && text.contains("different")) {
            return Screen.LOGIN_FAILED;
        }

        if (text.contains("date") && text.contains("birth") && text.contains("submit")) {
            return Screen.DATE_OF_BIRTH;
        }

        if (text.contains("returning player") && text.contains("new player")) {
            return Screen.PLAYER_SELECTION;
        }

        if (text.contains("username") && text.contains("sign") && text.contains("forgot")) {
            return Screen.LOGIN;
        }

        if (text.contains("remember") && text.contains("alert") && text.contains("surroundings")) {
            return Screen.LOADING;
        }

        if ((text.contains("termination") && text.contains("permanently") && text.contains("violating"))
                || (text.contains("failed") && text.contains("game") && text.contains("server"))) {
            return Screen.ACCOUNT_BANNED;
        }

        if (text.contains("incorrect") && text.contains("before") && text.contains("minutes")) {
            return Screen.ACCOUNT_WRONG_CREDENTIALS;
        }

        if (text.contains("check") && text.contains("exists") && text.contains("correctly")) {
            return Screen.ACCOUNT_NEW;
        }

        if (text.contains("activate") && text.contains("order") && text.contains("play")) {
            return Screen.ACCOUNT_NOT_ACTIVATED;
        }

        if ((text.contains("security") && text.contains("regain") && text.contains("questions"))
                || (text.contains("measure") && text.contains("failed") && text.contains("back"))) {
            return Screen.ACCOUNT_LOCKED;
        }

        if (text.contains("unable") && text.contains("authenticate") && text.contains("again")) {
            return Screen.NOT_AUTHENTICATE;
        }

        return Screen.UNKNOWN;
    }

    public void selectDateOfBirth() {
        if (!mItemsLocated) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();

            Point[] cornerPoints2019 = mScreenInteractor.getElementCornerPoints(visionText, "2019");
            if (cornerPoints2019 == null) {
                return;
            }
            xYearSelector = (cornerPoints2019[0].x + cornerPoints2019[1].x) / 2;
            yYearSelector = (cornerPoints2019[0].y + cornerPoints2019[2].y) / 2;
            widthYearSelector = cornerPoints2019[1].x - cornerPoints2019[0].x;
            heightYearSelector = cornerPoints2019[2].y - cornerPoints2019[0].y;

            Point[] cornerPointsSubmit = mScreenInteractor.getElementCornerPoints(visionText, "submit");
            if (cornerPointsSubmit == null) {
                return;
            }
            xSubmit = (cornerPointsSubmit[0].x + cornerPointsSubmit[1].x) / 2;
            ySubmit = (cornerPointsSubmit[0].y + cornerPointsSubmit[2].y) / 2;
            widthSubmit = cornerPointsSubmit[1].x - cornerPointsSubmit[0].x;
            heightSubmit = cornerPointsSubmit[2].y - cornerPointsSubmit[0].y;

            if (mInterrupted) return;
        }

        // Open year selector.
        mScreenInteractor.tapRandom(xYearSelector, yYearSelector, widthYearSelector, heightYearSelector);
        if (mInterrupted) return;

        // Wait for animation.
        Utils.sleepRandom(450, 550);
        if (mInterrupted) return;

        if (!mItemsLocated) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            Point[] cornerPoints2010 = mScreenInteractor.getElementCornerPoints(visionText, "2010");
            if (cornerPoints2010 == null) {
                return;
            }
            x2010 = (cornerPoints2010[0].x + cornerPoints2010[1].x) / 2;
            y2010 = (cornerPoints2010[0].y + cornerPoints2010[2].y) / 2;
            width2010 = cornerPoints2010[1].x - cornerPoints2010[0].x;
            height2010 = cornerPoints2010[2].y - cornerPoints2010[0].y;

            if (mInterrupted) return;
        }

        // Select year of birth.
        mScreenInteractor.tapRandom(x2010, y2010, width2010, height2010);
        if (mInterrupted) return;

        // Wait for animation.
        Utils.sleepRandom(450, 550);
        if (mInterrupted) return;

        // Submit date of birth.
        mScreenInteractor.tapRandom(xSubmit, ySubmit, widthSubmit, heightSubmit);
        Log.i(LOG_TAG, "Date of birth submitted.");
    }

    public void selectReturningPlayer() {
        if (!mItemsLocated) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            Point[] cornerPoints = mScreenInteractor.getLineCornerPoints(visionText, "returning player");
            if (cornerPoints == null) {
                return;
            }
            xReturningPlayer = (cornerPoints[0].x + cornerPoints[1].x) / 2;
            yReturningPlayer = (cornerPoints[0].y + cornerPoints[2].y) / 2;
            widthReturningPlayer = cornerPoints[1].x - cornerPoints[0].x;
            heightReturningPlayer = cornerPoints[2].y - cornerPoints[0].y;

            if (mInterrupted) return;
        }

        mScreenInteractor.tapRandom(xReturningPlayer, yReturningPlayer, widthReturningPlayer, heightReturningPlayer);
        Log.i(LOG_TAG, "Returning player selected.");
    }

    public void selectPTC() {
        if (!mItemsLocated) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            Point[] cornerPoints;
            if (visionText.getText().toLowerCase().contains("pokémon")) {
                cornerPoints = mScreenInteractor.getLineCornerPoints(visionText, "pokémon trainer club");
            } else {
                cornerPoints = mScreenInteractor.getLineCornerPoints(visionText, "pokemon trainer club");
            }
            if (cornerPoints == null) {
                return;
            }
            xPtc = (cornerPoints[0].x + cornerPoints[1].x) / 2;
            yPtc = (cornerPoints[0].y + cornerPoints[2].y) / 2;
            widthPtc = cornerPoints[1].x - cornerPoints[0].x;
            heightPtc = cornerPoints[2].y - cornerPoints[0].y;

            if (mInterrupted) return;
        }

        mScreenInteractor.tapRandom(xPtc, yPtc, widthPtc, heightPtc);
        Log.i(LOG_TAG, "PTC selected.");
    }

    public void login(String username, String password) {
        if (!mItemsLocated) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();

            Point[] cornerPointsUsername = mScreenInteractor.getElementCornerPoints(visionText, "username");
            if (cornerPointsUsername == null) {
                return;
            }
            xUsername = (cornerPointsUsername[0].x + cornerPointsUsername[1].x) / 2;
            yUsername = (cornerPointsUsername[0].y + cornerPointsUsername[2].y) / 2;
            widthUsername = cornerPointsUsername[1].x - cornerPointsUsername[0].x;
            heightUsername = cornerPointsUsername[2].y - cornerPointsUsername[0].y;

            Point[] cornerPointsPassword = mScreenInteractor.getElementCornerPoints(visionText, "password");
            if (cornerPointsPassword == null) {
                return;
            }
            xPassword = (cornerPointsPassword[0].x + cornerPointsPassword[1].x) / 2;
            yPassword = (cornerPointsPassword[0].y + cornerPointsPassword[2].y) / 2;
            widthPassword = cornerPointsPassword[1].x - cornerPointsPassword[0].x;
            heightPassword = cornerPointsPassword[2].y - cornerPointsPassword[0].y;

            Point[] cornerPointsSignIn = mScreenInteractor.getElementCornerPoints(visionText, "sign");
            if (cornerPointsSignIn == null) {
                return;
            }
            xSignIn = (cornerPointsSignIn[0].x + cornerPointsSignIn[1].x) / 2;
            ySignIn = (cornerPointsSignIn[0].y + cornerPointsSignIn[2].y) / 2;
            widthSignIn = cornerPointsSignIn[1].x - cornerPointsSignIn[0].x;
            heightSignIn = cornerPointsSignIn[2].y - cornerPointsSignIn[0].y;

            mItemsLocated = true;

            if (mInterrupted) return;
        }

        // Tap in username box.
        mScreenInteractor.tapRandom(xUsername, yUsername, widthUsername, heightUsername);
        if (mInterrupted) return;

        // Type the username.
        mScreenInteractor.insertText(username);
        if (mInterrupted) return;

        // Tap to hide keyboard.
        mScreenInteractor.tap(Utils.randomWithRange(50, 100), Utils.randomWithRange(100, 150));
        if (mInterrupted) return;

        // Wait for keyboard to disappear.
        Utils.sleepRandom(450, 550);
        if (mInterrupted) return;

        // Tap in password box.
        mScreenInteractor.tapRandom(xPassword, yPassword, widthPassword, heightPassword);
        if (mInterrupted) return;

        // Type the username.
        mScreenInteractor.insertText(password);
        if (mInterrupted) return;

        // Tap to hide keyboard.
        mScreenInteractor.tap(Utils.randomWithRange(50, 100), Utils.randomWithRange(100, 150));
        if (mInterrupted) return;

        // Wait for keyboard to disappear.
        Utils.sleepRandom(450, 550);
        if (mInterrupted) return;

        mScreenInteractor.tapRandom(xSignIn, ySignIn, widthSignIn, heightSignIn);
        Log.i(LOG_TAG, "Account signed in.");
    }

    public void interrupt() {
        mInterrupted = true;
    }

    public void resume() {
        mInterrupted = false;
    }

    public void cleanUp(boolean clearAppData) {
        if (clearAppData) clearAppData();
        mScreenInteractor.cleanUp();
    }
}
