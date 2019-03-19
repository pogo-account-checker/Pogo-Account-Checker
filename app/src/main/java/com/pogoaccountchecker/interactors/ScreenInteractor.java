package com.pogoaccountchecker.interactors;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.pogoaccountchecker.ocr.TextInImageRecognizer;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

import java.io.File;

import androidx.annotation.NonNull;

public class ScreenInteractor {
    private Context mContext;
    private TextInImageRecognizer mTextRecognizer;
    private int realWidth, realHeight, virtualWidth, virtualHeight;
    private boolean mScreenshotTaken;
    private boolean mInterrupted;
    private final String APP_PATH;
    private final String POGO_PACKAGE;
    private final String LOG_TAG = getClass().getSimpleName();

    public ScreenInteractor(Context context) {
        mContext = context;
        mTextRecognizer = new TextInImageRecognizer(mContext);
        realWidth = getWidth();
        realHeight = getHeight();
        APP_PATH = Environment.getExternalStorageDirectory().getPath() + "/PogoAccountChecker";
        POGO_PACKAGE = "com.nianticlabs.pokemongo";
    }

    public void tap(int x, int y) {
        if (!mInterrupted) {
            Shell.runSuCommand("input tap " + x + " " + y);
            Log.d(LOG_TAG, "Tapped screen at: " + Float.toString(x) + "," + Float.toString(y));
        }
    }

    public void tapRandom(int x, int y, int offsetX, int offsetY) {
        int randomOffsetX = Utils.randomWithRange(-offsetX / 2, offsetX / 2);
        int randomOffsetY = Utils.randomWithRange(-offsetY / 2, offsetY / 2);
        tap(x + randomOffsetX, y + randomOffsetY);
    }

    public void insertText(@NonNull String text) {
        Shell.runSuCommand("input text '" + text + "'");
    }

    private Uri takeScreenshot() {
        Shell.runSuCommand("screencap -p " + APP_PATH + "/screenshot.png");
        mScreenshotTaken = true;
        return Uri.fromFile(new File(APP_PATH + "/screenshot.png"));
    }

    public FirebaseVisionText getVisionText() {
        Uri screenshotUri = takeScreenshot();
        if (screenshotUri != null) {
            return mTextRecognizer.detectText(screenshotUri);
        } else {
            return null;
        }
    }

    public void showBars() {
        Shell.runSuCommand("settings put global policy_control null*");
    }

    public void hideBars() {
        Shell.runSuCommand("settings put global policy_control immersive.full=" + POGO_PACKAGE);
    }

    private void resize(int newWidth, int newHeight) {
        Shell.runSuCommand("wm size " + newWidth + "x" + newHeight);
        virtualWidth = newWidth;
        virtualHeight = newHeight;
    }

    public int resizeTo16x9() {
        if (!((realWidth % 9) == 0 && (realHeight % 16) == 0) && !(realWidth / 9 == realHeight / 16)) {
            // Screen is not 16x9, resize it.
            if (realWidth >= 2160 && realHeight >= 3840) {
                resize(2160, 3840);
            } else if (realWidth >= 1440 && realHeight >= 2560) {
                resize(1440, 2560);
            } else if (realWidth >= 1080 && realHeight >= 1920) {
                resize(1080, 1920);
            } else if (realWidth >= 720 && realHeight >= 1280) {
                resize(720, 1280);
            } else if (realWidth >= 540 && realHeight >= 960) {
                resize(540, 960);
            } else if (realWidth >= 360 && realHeight >= 640) {
                resize(360, 640);
            }
            return virtualWidth;
        } else {
            // Screen was not resized.
            return realWidth;
        }
    }

    private int getHeight() {
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

    private int getWidth() {
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

    public void interrupt() {
        mInterrupted = true;
    }

    public void resume() {
        mInterrupted = false;
    }

    public void cleanUp() {
        mTextRecognizer.close();
        showBars();
        if (virtualWidth != 0 || virtualHeight != 0) {
            // Screen was resized, resize back to real resolution.
            resize(realWidth, realHeight);
        }
        if (mScreenshotTaken) Shell.runSuCommand("rm " + APP_PATH + "/screenshot.png");
    }
}
