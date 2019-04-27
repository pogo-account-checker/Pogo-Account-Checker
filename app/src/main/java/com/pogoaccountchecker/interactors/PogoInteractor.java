package com.pogoaccountchecker.interactors;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.Log;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.pogoaccountchecker.R;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

import androidx.preference.PreferenceManager;

public class PogoInteractor {
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private ScreenInteractor mScreenInteractor;
    private volatile boolean mInterrupted;
    private int mAccountLevel;
    private final String POGO_PACKAGE = "com.nianticlabs.pokemongo";
    private final String LOG_TAG = getClass().getSimpleName();

    public PogoInteractor(Context context) {
        mContext = context;
        mScreenInteractor = new ScreenInteractor(mContext);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void startPogo() {
        Shell.runSuCommand("am start -n " + POGO_PACKAGE + "/com.nianticproject.holoholo.libholoholo.unity.UnityMainActivity");
        Log.i(LOG_TAG, "Pogo started.");
    }

    public void stopPogo() {
        mAccountLevel = -1;
        Shell.runSuCommand("am force-stop " + POGO_PACKAGE);
        Log.i(LOG_TAG, "Pogo stopped.");
    }

    public void clearAppData() {
        mAccountLevel = -1;
        Shell.runSuCommand("pm clear " + POGO_PACKAGE);
        Log.i(LOG_TAG, "Pogo app data cleared.");
    }

    public void grantLocationPermission() {
        Shell.runSuCommand("pm grant " + POGO_PACKAGE + " android.permission.ACCESS_FINE_LOCATION");
        Log.i(LOG_TAG, "Location permission granted.");
    }

    public void grantCameraPermission() {
        Shell.runSuCommand("pm grant " + POGO_PACKAGE + " android.permission.CAMERA");
        Log.i(LOG_TAG, "Camera permission granted.");
    }

    public enum Screen {
        LOGIN_FAILED, DATE_OF_BIRTH, PLAYER_SELECTION, LOGIN, LOADING, SAFETY_WARNING, LOCATION_PERMISSION, CAMERA_PERMISSION, NOTIFICATION_POPUP, CHEATING_WARNING_1, CHEATING_WARNING_2,
        CHEATING_WARNING_3, SUSPENSION_WARNING, TUTORIAL_CATCH_POKEMON, PLAYER_PROFILE, ACCOUNT_BANNED, ACCOUNT_WRONG_CREDENTIALS, ACCOUNT_NEW, ACCOUNT_NOT_ACTIVATED, ACCOUNT_LOCKED, NOT_AUTHENTICATE,
        UNKNOWN
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

        if (text.contains("allow") && text.contains("access") && text.contains("location")) {
            return Screen.LOCATION_PERMISSION;
        }

        if (text.contains("allow") && text.contains("pictures") && text.contains("record")) {
            return Screen.CAMERA_PERMISSION;
        }

        if (text.contains("remember") && text.contains("alert") && text.contains("surroundings")) {
            return Screen.LOADING;
        }

        if ((text.contains("play") && text.contains("while") && text.contains("driving"))
                || (text.contains("trespass") && text.contains("while") && text.contains("playing"))
                || (text.contains("enter") && text.contains("dangerous") && text.contains("areas"))) {
            return Screen.SAFETY_WARNING;
        }

        if (text.contains("see") && text.contains("details") && text.contains("dismiss")) {
            return Screen.NOTIFICATION_POPUP;
        }

        if (text.contains("suggests") && text.contains("accesses") && text.contains("compromised")) {
            return Screen.CHEATING_WARNING_1;
        }

        if (text.contains("modified") && text.contains("degraded") && text.contains("transgressions")) {
            return Screen.CHEATING_WARNING_2;
        }

        if (text.contains("modified") && text.contains("strike") && text.contains("transgressions")) {
            return Screen.CHEATING_WARNING_3;
        }

        if (text.contains("indicated") && text.contains("opportunity") && text.contains("permanently")) {
            return Screen.SUSPENSION_WARNING;
        }

        if (text.contains("catch") && (text.contains("pokémon") || text.contains("pokemon"))) {
            return Screen.TUTORIAL_CATCH_POKEMON;
        }

        if (text.contains("level") && text.contains("buddy") && text.contains("style")) {
            // Store account level, since it will most likely be needed later on.
            setAccountLevelFromVisionText(visionText);
            return Screen.PLAYER_PROFILE;
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
        int yearSelectorX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.year_selector_x_pref_key), "0"));
        int yearSelectorY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.year_selector_y_pref_key), "0"));
        int yearSelectorWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.year_selector_width_pref_key), "0"));
        int yearSelectorHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.year_selector_height_pref_key), "0"));

        int yob2010X = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.yob_2010_x_pref_key), "0"));
        int yob2010Y = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.yob_2010_y_pref_key), "0"));
        int yob2010Width = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.yob_2010_width_pref_key), "0"));
        int yob2010Height = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.yob_2010_height_pref_key), "0"));

        int submitX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.submit_dob_button_x_pref_key), "0"));
        int submitY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.submit_dob_button_y_pref_key), "0"));
        int submitWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.submit_dob_button_width_pref_key), "0"));
        int submitHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.submit_dob_button_height_pref_key), "0"));

        FirebaseVisionText visionText = null;
        if (yearSelectorX == 0 || yearSelectorY == 0 || yearSelectorWidth == 0 || yearSelectorHeight == 0) {
            visionText = mScreenInteractor.getVisionText();
            Point[] yearSelectorCornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "2019");
            if (yearSelectorCornerPoints == null) return;

            yearSelectorX = (yearSelectorCornerPoints[0].x + yearSelectorCornerPoints[1].x) / 2;
            yearSelectorY = (yearSelectorCornerPoints[0].y + yearSelectorCornerPoints[2].y) / 2;
            yearSelectorWidth = yearSelectorCornerPoints[1].x - yearSelectorCornerPoints[0].x;
            yearSelectorHeight = yearSelectorCornerPoints[2].y - yearSelectorCornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.year_selector_x_pref_key), Integer.toString(yearSelectorX));
            editor.putString(mContext.getString(R.string.year_selector_y_pref_key), Integer.toString(yearSelectorY));
            editor.putString(mContext.getString(R.string.year_selector_width_pref_key), Integer.toString(yearSelectorWidth));
            editor.putString(mContext.getString(R.string.year_selector_height_pref_key), Integer.toString(yearSelectorHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        if (submitX == 0 || submitY == 0 || submitWidth == 0 || submitHeight == 0) {
            if (visionText == null) visionText = mScreenInteractor.getVisionText();
            Point[] dateOfBirthSubmitCornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "submit");
            if (dateOfBirthSubmitCornerPoints == null) return;

            submitX = (dateOfBirthSubmitCornerPoints[0].x + dateOfBirthSubmitCornerPoints[1].x) / 2;
            submitY = (dateOfBirthSubmitCornerPoints[0].y + dateOfBirthSubmitCornerPoints[2].y) / 2;
            submitWidth = dateOfBirthSubmitCornerPoints[1].x - dateOfBirthSubmitCornerPoints[0].x;
            submitHeight = dateOfBirthSubmitCornerPoints[2].y - dateOfBirthSubmitCornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.submit_dob_button_x_pref_key), Integer.toString(submitX));
            editor.putString(mContext.getString(R.string.submit_dob_button_y_pref_key), Integer.toString(submitY));
            editor.putString(mContext.getString(R.string.submit_dob_button_width_pref_key), Integer.toString(submitWidth));
            editor.putString(mContext.getString(R.string.submit_dob_button_height_pref_key), Integer.toString(submitHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        // Open year selector.
        mScreenInteractor.tapRandom(yearSelectorX, yearSelectorY, yearSelectorWidth, yearSelectorHeight);
        if (mInterrupted) return;

        // Wait for animation.
        Utils.sleepRandom(450, 550);
        if (mInterrupted) return;

        if (yob2010X == 0 || yob2010Y == 0 || yob2010Width == 0 || yob2010Height == 0 ) {
            visionText = mScreenInteractor.getVisionText();
            Point[] yearOfBirth2010CornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "2010");
            if (yearOfBirth2010CornerPoints == null) return;

            yob2010X = (yearOfBirth2010CornerPoints[0].x + yearOfBirth2010CornerPoints[1].x) / 2;
            yob2010Y = (yearOfBirth2010CornerPoints[0].y + yearOfBirth2010CornerPoints[2].y) / 2;
            yob2010Width = yearOfBirth2010CornerPoints[1].x - yearOfBirth2010CornerPoints[0].x;
            yob2010Height = yearOfBirth2010CornerPoints[2].y - yearOfBirth2010CornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.yob_2010_x_pref_key), Integer.toString(yob2010X));
            editor.putString(mContext.getString(R.string.yob_2010_y_pref_key), Integer.toString(yob2010Y));
            editor.putString(mContext.getString(R.string.yob_2010_width_pref_key), Integer.toString(yob2010Width));
            editor.putString(mContext.getString(R.string.yob_2010_height_pref_key), Integer.toString(yob2010Height));
            editor.apply();

            if (mInterrupted) return;
        }

        // Select year of birth.
        mScreenInteractor.tapRandom(yob2010X, yob2010Y, yob2010Width, yob2010Height);
        if (mInterrupted) return;

        // Wait for animation.
        Utils.sleepRandom(450, 550);
        if (mInterrupted) return;

        // Submit date of birth.
        mScreenInteractor.tapRandom(submitX, submitY, submitWidth, submitHeight);
        Log.i(LOG_TAG, "Date of birth submitted.");
    }

    public void selectReturningPlayer() {
        int returningPlayerX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.returning_player_button_x_pref_key), "0"));
        int returningPlayerY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.returning_player_button_y_pref_key), "0"));
        int returningPlayerWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.returning_player_button_width_pref_key), "0"));
        int returningPlayerHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.returning_player_button_height_pref_key), "0"));

        if (returningPlayerX == 0 || returningPlayerY == 0 || returningPlayerWidth == 0 || returningPlayerHeight == 0) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            Point[] cornerPoints = mScreenInteractor.getLineCornerPoints(visionText, "returning player");
            if (cornerPoints == null) return;

            returningPlayerX = (cornerPoints[0].x + cornerPoints[1].x) / 2;
            returningPlayerY = (cornerPoints[0].y + cornerPoints[2].y) / 2;
            returningPlayerWidth = cornerPoints[1].x - cornerPoints[0].x;
            returningPlayerHeight = cornerPoints[2].y - cornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.returning_player_button_x_pref_key), Integer.toString(returningPlayerX));
            editor.putString(mContext.getString(R.string.returning_player_button_y_pref_key), Integer.toString(returningPlayerY));
            editor.putString(mContext.getString(R.string.returning_player_button_width_pref_key), Integer.toString(returningPlayerWidth));
            editor.putString(mContext.getString(R.string.returning_player_button_height_pref_key), Integer.toString(returningPlayerHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        mScreenInteractor.tapRandom(returningPlayerX, returningPlayerY, returningPlayerWidth, returningPlayerHeight);
        Log.i(LOG_TAG, "Returning player selected.");
    }

    public void selectPtc() {
        int ptcX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.ptc_button_x_pref_key), "0"));
        int ptcY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.ptc_button_y_pref_key), "0"));
        int ptcWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.ptc_button_width_pref_key), "0"));
        int ptcHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.ptc_button_height_pref_key), "0"));

        if (ptcX == 0 || ptcY == 0 || ptcWidth == 0 || ptcHeight == 0) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            String ptcText;
            if (visionText.getText().contains("pokémon")) {
                ptcText = "pokémon trainer club";
            } else {
                ptcText = "pokemon trainer club";
            }
            Point[] cornerPoints = mScreenInteractor.getLineCornerPoints(visionText, ptcText);
            if (cornerPoints == null) return;

            ptcX = (cornerPoints[0].x + cornerPoints[1].x) / 2;
            ptcY = (cornerPoints[0].y + cornerPoints[2].y) / 2;
            ptcWidth = cornerPoints[1].x - cornerPoints[0].x;
            ptcHeight = cornerPoints[2].y - cornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.ptc_button_x_pref_key), Integer.toString(ptcX));
            editor.putString(mContext.getString(R.string.ptc_button_y_pref_key), Integer.toString(ptcY));
            editor.putString(mContext.getString(R.string.ptc_button_width_pref_key), Integer.toString(ptcWidth));
            editor.putString(mContext.getString(R.string.ptc_button_height_pref_key), Integer.toString(ptcHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        mScreenInteractor.tapRandom(ptcX, ptcY, ptcWidth, ptcHeight);
        Log.i(LOG_TAG, "Pokémon trainer club selected.");
    }

    public void login(String username, String password) {
        int usernameX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.username_text_field_x_pref_key), "0"));
        int usernameY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.username_text_field_y_pref_key), "0"));
        int usernameWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.username_text_field_width_pref_key), "0"));
        int usernameHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.username_text_field_height_pref_key), "0"));

        int passwordX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.password_text_field_x_pref_key), "0"));
        int passwordY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.password_text_field_y_pref_key), "0"));
        int passwordWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.password_text_field_width_pref_key), "0"));
        int passwordHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.password_text_field_height_pref_key), "0"));

        int signInX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.sign_in_button_x_pref_key), "0"));
        int signInY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.sign_in_button_y_pref_key), "0"));
        int signInWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.sign_in_button_width_pref_key), "0"));
        int signInHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.sign_in_button_height_pref_key), "0"));

        FirebaseVisionText visionText = null;
        if (usernameX == 0 || usernameY == 0 || usernameWidth == 0 || usernameHeight == 0) {
            visionText = mScreenInteractor.getVisionText();
            Point[] usernameCornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "username");
            if (usernameCornerPoints == null) return;

            usernameX = (usernameCornerPoints[0].x + usernameCornerPoints[1].x) / 2;
            usernameY = (usernameCornerPoints[0].y + usernameCornerPoints[2].y) / 2;
            usernameWidth = usernameCornerPoints[1].x - usernameCornerPoints[0].x;
            usernameHeight = usernameCornerPoints[2].y - usernameCornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.username_text_field_x_pref_key), Integer.toString(usernameX));
            editor.putString(mContext.getString(R.string.username_text_field_y_pref_key), Integer.toString(usernameY));
            editor.putString(mContext.getString(R.string.username_text_field_width_pref_key), Integer.toString(usernameWidth));
            editor.putString(mContext.getString(R.string.username_text_field_height_pref_key), Integer.toString(usernameHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        if (passwordX == 0 || passwordY == 0 || passwordWidth == 0 || passwordHeight == 0) {
            if (visionText == null) visionText = mScreenInteractor.getVisionText();
            Point[] passwordCornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "password");
            if (passwordCornerPoints == null) return;

            passwordX = (passwordCornerPoints[0].x + passwordCornerPoints[1].x) / 2;
            passwordY = (passwordCornerPoints[0].y + passwordCornerPoints[2].y) / 2;
            passwordWidth = passwordCornerPoints[1].x - passwordCornerPoints[0].x;
            passwordHeight = passwordCornerPoints[2].y - passwordCornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.password_text_field_x_pref_key), Integer.toString(passwordX));
            editor.putString(mContext.getString(R.string.password_text_field_y_pref_key), Integer.toString(passwordY));
            editor.putString(mContext.getString(R.string.password_text_field_width_pref_key), Integer.toString(passwordWidth));
            editor.putString(mContext.getString(R.string.password_text_field_height_pref_key), Integer.toString(passwordHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        if (signInX == 0 || signInY == 0 || signInWidth == 0 || signInHeight == 0) {
            if (visionText == null) visionText = mScreenInteractor.getVisionText();
            Point[] signInCornerPoints;
            if (visionText.getText().contains("sign in")) {
                signInCornerPoints = mScreenInteractor.getLineCornerPoints(visionText, "sign in");
            } else {
                signInCornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "sign");
            }
            if (signInCornerPoints == null) return;

            signInX = (signInCornerPoints[0].x + signInCornerPoints[1].x) / 2;
            signInY = (signInCornerPoints[0].y + signInCornerPoints[2].y) / 2;
            signInWidth = signInCornerPoints[1].x - signInCornerPoints[0].x;
            signInHeight = signInCornerPoints[2].y - signInCornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.sign_in_button_x_pref_key), Integer.toString(signInX));
            editor.putString(mContext.getString(R.string.sign_in_button_y_pref_key), Integer.toString(signInY));
            editor.putString(mContext.getString(R.string.sign_in_button_width_pref_key), Integer.toString(signInWidth));
            editor.putString(mContext.getString(R.string.sign_in_button_height_pref_key), Integer.toString(signInHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        // Tap in username box.
        mScreenInteractor.tapRandom(usernameX, usernameY, usernameWidth, usernameHeight);
        if (mInterrupted) return;

        // Type the username.
        mScreenInteractor.insertText(username);
        if (mInterrupted) return;

        // Tap to hide keyboard.
        mScreenInteractor.tap(Utils.randomWithRange(0, 50), Utils.randomWithRange(100, 150));
        if (mInterrupted) return;
        // Wait for keyboard to disappear.
        Utils.sleepRandom(450, 550);
        if (mInterrupted) return;

        // Tap in password box.
        mScreenInteractor.tapRandom(passwordX, passwordY, passwordWidth, passwordHeight);
        if (mInterrupted) return;

        // Type the username.
        mScreenInteractor.insertText(password);
        if (mInterrupted) return;

        // Tap to hide keyboard.
        mScreenInteractor.tap(Utils.randomWithRange(0, 50), Utils.randomWithRange(100, 150));
        if (mInterrupted) return;
        // Wait for keyboard to disappear.
        Utils.sleepRandom(450, 550);
        if (mInterrupted) return;

        mScreenInteractor.tapRandom(signInX, signInY, signInWidth, signInHeight);
        Log.i(LOG_TAG, "Account signed in.");
    }

    public void closeSafetyWarning() {
        int closeWarningX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_safety_warning_button_x_pref_key), "0"));
        int closeWarningY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_safety_warning_button_y_pref_key), "0"));
        int closeWarningWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_safety_warning_button_width_pref_key), "0"));
        int closeWarningHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_safety_warning_button_height_pref_key), "0"));

        if (closeWarningX == 0 || closeWarningY == 0 || closeWarningWidth == 0 || closeWarningHeight == 0) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            Point[] cornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "ok");
            if (cornerPoints == null) return;

            closeWarningX = (cornerPoints[0].x + cornerPoints[1].x) / 2;
            closeWarningY = (cornerPoints[0].y + cornerPoints[2].y) / 2;
            closeWarningWidth = cornerPoints[1].x - cornerPoints[0].x;
            closeWarningHeight = cornerPoints[2].y - cornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.close_safety_warning_button_x_pref_key), Integer.toString(closeWarningX));
            editor.putString(mContext.getString(R.string.close_safety_warning_button_y_pref_key), Integer.toString(closeWarningY));
            editor.putString(mContext.getString(R.string.close_safety_warning_button_width_pref_key), Integer.toString(closeWarningWidth));
            editor.putString(mContext.getString(R.string.close_safety_warning_button_height_pref_key), Integer.toString(closeWarningHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        mScreenInteractor.tapRandom(closeWarningX, closeWarningY, closeWarningWidth, closeWarningHeight);
        Log.i(LOG_TAG, "Safety warning closed.");
    }

    public void closeNotificationPopup() {
        int screenHeight = mScreenInteractor.getScreenHeight();
        mScreenInteractor.tap(0, Utils.randomWithRange(screenHeight - 50, screenHeight + 50));
        Log.i(LOG_TAG, "Notification popup closed.");
    }

    public void closeCheatingWarning1() {
        int closeWarningX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_1_button_x_pref_key), "0"));
        int closeWarningY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_1_button_y_pref_key), "0"));
        int closeWarningWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_1_button_width_pref_key), "0"));
        int closeWarningHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_1_button_height_pref_key), "0"));

        if (closeWarningX == 0 || closeWarningY == 0 || closeWarningWidth == 0 || closeWarningHeight == 0) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            Point[] cornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "got it");
            if (cornerPoints == null) return;

            closeWarningX = (cornerPoints[0].x + cornerPoints[1].x) / 2;
            closeWarningY = (cornerPoints[0].y + cornerPoints[2].y) / 2;
            closeWarningWidth = cornerPoints[1].x - cornerPoints[0].x;
            closeWarningHeight = cornerPoints[2].y - cornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.close_cheating_warning_1_button_x_pref_key), Integer.toString(closeWarningX));
            editor.putString(mContext.getString(R.string.close_cheating_warning_1_button_y_pref_key), Integer.toString(closeWarningY));
            editor.putString(mContext.getString(R.string.close_cheating_warning_1_button_width_pref_key), Integer.toString(closeWarningWidth));
            editor.putString(mContext.getString(R.string.close_cheating_warning_1_button_height_pref_key), Integer.toString(closeWarningHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        mScreenInteractor.tapRandom(closeWarningX, closeWarningY, closeWarningWidth, closeWarningHeight);
        Log.i(LOG_TAG, "Cheating warning 1 closed.");
    }

    public void closeCheatingWarning2() {
        int closeWarningX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_2_button_x_pref_key), "0"));
        int closeWarningY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_2_button_y_pref_key), "0"));
        int closeWarningWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_2_button_width_pref_key), "0"));
        int closeWarningHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_2_button_height_pref_key), "0"));

        if (closeWarningX == 0 || closeWarningY == 0 || closeWarningWidth == 0 || closeWarningHeight == 0) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            Point[] cornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "got it");
            if (cornerPoints == null) return;

            closeWarningX = (cornerPoints[0].x + cornerPoints[1].x) / 2;
            closeWarningY = (cornerPoints[0].y + cornerPoints[2].y) / 2;
            closeWarningWidth = cornerPoints[1].x - cornerPoints[0].x;
            closeWarningHeight = cornerPoints[2].y - cornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.close_cheating_warning_2_button_x_pref_key), Integer.toString(closeWarningX));
            editor.putString(mContext.getString(R.string.close_cheating_warning_2_button_y_pref_key), Integer.toString(closeWarningY));
            editor.putString(mContext.getString(R.string.close_cheating_warning_2_button_width_pref_key), Integer.toString(closeWarningWidth));
            editor.putString(mContext.getString(R.string.close_cheating_warning_2_button_height_pref_key), Integer.toString(closeWarningHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        mScreenInteractor.tapRandom(closeWarningX, closeWarningY, closeWarningWidth, closeWarningHeight);
        Log.i(LOG_TAG, "Cheating warning 2 closed.");
    }

    public void closeCheatingWarning3() {
        int closeWarningX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_3_button_x_pref_key), "0"));
        int closeWarningY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_3_button_y_pref_key), "0"));
        int closeWarningWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_3_button_width_pref_key), "0"));
        int closeWarningHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_cheating_warning_3_button_height_pref_key), "0"));

        if (closeWarningX == 0 || closeWarningY == 0 || closeWarningWidth == 0 || closeWarningHeight == 0) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            Point[] cornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "got it");
            if (cornerPoints == null) return;

            closeWarningX = (cornerPoints[0].x + cornerPoints[1].x) / 2;
            closeWarningY = (cornerPoints[0].y + cornerPoints[2].y) / 2;
            closeWarningWidth = cornerPoints[1].x - cornerPoints[0].x;
            closeWarningHeight = cornerPoints[2].y - cornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.close_cheating_warning_3_button_x_pref_key), Integer.toString(closeWarningX));
            editor.putString(mContext.getString(R.string.close_cheating_warning_3_button_y_pref_key), Integer.toString(closeWarningY));
            editor.putString(mContext.getString(R.string.close_cheating_warning_3_button_width_pref_key), Integer.toString(closeWarningWidth));
            editor.putString(mContext.getString(R.string.close_cheating_warning_3_button_height_pref_key), Integer.toString(closeWarningHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        mScreenInteractor.tapRandom(closeWarningX, closeWarningY, closeWarningWidth, closeWarningHeight);
        Log.i(LOG_TAG, "Cheating warning 3 closed.");
    }

    public void closeSuspensionsWarning() {
        int closeWarningX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_suspension_warning_button_x_pref_key), "0"));
        int closeWarningY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_suspension_warning_button_y_pref_key), "0"));
        int closeWarningWidth = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_suspension_warning_button_width_pref_key), "0"));
        int closeWarningHeight = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.close_suspension_warning_button_height_pref_key), "0"));

        if (closeWarningX == 0 || closeWarningY == 0 || closeWarningWidth == 0 || closeWarningHeight == 0) {
            FirebaseVisionText visionText = mScreenInteractor.getVisionText();
            Point[] cornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "got it");
            if (cornerPoints == null) return;

            closeWarningX = (cornerPoints[0].x + cornerPoints[1].x) / 2;
            closeWarningY = (cornerPoints[0].y + cornerPoints[2].y) / 2;
            closeWarningWidth = cornerPoints[1].x - cornerPoints[0].x;
            closeWarningHeight = cornerPoints[2].y - cornerPoints[0].y;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mContext.getString(R.string.close_suspension_warning_button_x_pref_key), Integer.toString(closeWarningX));
            editor.putString(mContext.getString(R.string.close_suspension_warning_button_y_pref_key), Integer.toString(closeWarningY));
            editor.putString(mContext.getString(R.string.close_suspension_warning_button_width_pref_key), Integer.toString(closeWarningWidth));
            editor.putString(mContext.getString(R.string.close_suspension_warning_button_height_pref_key), Integer.toString(closeWarningHeight));
            editor.apply();

            if (mInterrupted) return;
        }

        mScreenInteractor.tapRandom(closeWarningX, closeWarningY, closeWarningWidth, closeWarningHeight);
        Log.i(LOG_TAG, "Suspension warning closed");
    }

    public void openPlayerProfile() {
        int playerProfileX = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.player_profile_button_x_pref_key), "0"));
        int playerProfileY = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.player_profile_button_y_pref_key), "0"));
        mScreenInteractor.tap(playerProfileX, playerProfileY);
        Log.i(LOG_TAG, "Player profile opened.");
    }

    private void setAccountLevelFromVisionText(FirebaseVisionText visionText) {
        Point[] levelCornerPoints = mScreenInteractor.getElementCornerPoints(visionText, "level");
        if (levelCornerPoints == null) return;

        for (FirebaseVisionText.TextBlock block: visionText.getTextBlocks()) {
            for (FirebaseVisionText.Line line: block.getLines()) {
                for (FirebaseVisionText.Element element: line.getElements()) {
                    Point[] levelValueCornerPoints = element.getCornerPoints();
                    if (levelValueCornerPoints[0].y < levelCornerPoints[0].y && levelValueCornerPoints[0].x < mScreenInteractor.getScreenWidth() / 2 &&
                            levelValueCornerPoints[0].y > mScreenInteractor.getScreenHeight() / 2) {
                        mAccountLevel = Integer.parseInt(element.getText());
                    }
                }
            }
        }
    }

    public int getAccountLevel() {
        if (mAccountLevel == -1) {
            setAccountLevelFromVisionText(mScreenInteractor.getVisionText());
        }
        return mAccountLevel;
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
