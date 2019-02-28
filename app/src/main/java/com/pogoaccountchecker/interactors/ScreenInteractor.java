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

    public boolean tap(int x, int y) {
        if (Shell.runSuCommand("input tap " + x + " " + y)) {
            Log.d(LOG_TAG, "Tapped screen at: " + Float.toString(x) + "," + Float.toString(y));
            return true;
        } else {
            return false;
        }
    }

    public boolean tapRandom(int x, int y, int offsetX, int offsetY) {
        int randomOffsetX = Utils.randomWithRange(-offsetX / 2, offsetX / 2);
        int randomOffsetY = Utils.randomWithRange(-offsetY / 2, offsetY / 2);
        return tap(x + randomOffsetX, y + randomOffsetY);
    }

    public boolean insertText(@NonNull String text) {
        return Shell.runSuCommand("input text '" + text + "'");
    }

    private Uri takeScreenshot() {
        if (Shell.runSuCommand("screencap -p " + APP_PATH + "/screenshot.png")) {
            mScreenshotTaken = true;
            return Uri.fromFile(new File(APP_PATH + "/screenshot.png"));
        } else {
            return null;
        }
    }

    public FirebaseVisionText getVisionText() {
        Uri screenshotUri = takeScreenshot();
        if (screenshotUri != null) {
            return mTextRecognizer.detectText(screenshotUri);
        } else {
            return null;
        }
    }

    public boolean showBars() {
        return Shell.runSuCommand("settings put global policy_control null*");
    }

    public boolean hideBars() {
        return Shell.runSuCommand("settings put global policy_control immersive.full=" + POGO_PACKAGE);
    }

    private boolean resize(int newWidth, int newHeight) {
        if (Shell.runSuCommand("wm size " + newWidth + "x" + newHeight)) {
            virtualWidth = newWidth;
            virtualHeight = newHeight;
            return true;
        } else {
            return false;
        }
    }

    public int resizeTo16x9() {

        if (!((realWidth % 9) == 0 && (realHeight % 16) == 0) && !(realWidth / 9 == realHeight / 16)) {
            // Screen is not 16x9, resize it.
            boolean success = true;
            if (realWidth >= 2160 && realHeight >= 3840) {
                success = resize(2160, 3840);
            } else if (realWidth >= 1440 && realHeight >= 2560) {
                success = resize(1440, 2560);
            } else if (realWidth >= 1080 && realHeight >= 1920) {
                success = resize(1080, 1920);
            } else if (realWidth >= 720 && realHeight >= 1280) {
                success = resize(720, 1280);
            } else if (realWidth >= 540 && realHeight >= 960) {
                success = resize(540, 960);
            } else if (realWidth >= 360 && realHeight >= 640) {
                success = resize(360, 640);
            }
            if (success) {
                return virtualWidth;
            } else {
                return -1;
            }
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
