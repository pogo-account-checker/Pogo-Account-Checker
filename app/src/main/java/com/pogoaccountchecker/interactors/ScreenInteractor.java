package com.pogoaccountchecker.interactors;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.pogoaccountchecker.ocr.TextInImageRecognizer;
import com.pogoaccountchecker.utils.Shell;
import com.pogoaccountchecker.utils.Utils;

import java.io.File;

import androidx.annotation.NonNull;

public class ScreenInteractor {
    private TextInImageRecognizer mTextRecognizer;
    private final String APP_PATH;
    private final String LOG_TAG = getClass().getSimpleName();

    public ScreenInteractor(Context context) {
        mTextRecognizer = new TextInImageRecognizer(context);
        APP_PATH = Environment.getExternalStorageDirectory().getPath() + "/PogoAccountChecker";
    }

    public void tap(int x, int y) {
        Shell.runSuCommand("input tap " + x + " " + y);
        Log.d(LOG_TAG, "Tapped screen at: " + Float.toString(x) + "," + Float.toString(y));
    }

    public void tapRandom(int x, int y, int offsetX, int offsetY) {
        int randomOffsetX = Utils.randomWithRange(-offsetX / 2, offsetX / 2);
        int randomOffsetY = Utils.randomWithRange(-offsetY / 2, offsetY / 2);
        tap(x + randomOffsetX, y + randomOffsetY);
    }

    public void insertText(@NonNull String text) {
        Shell.runSuCommand("input text '" + text + "'");
    }

    private void takeScreenshot() {
        Shell.runSuCommand("screencap -p " + APP_PATH + "/screenshot.png");
    }

    public FirebaseVisionText getVisionText() {
        takeScreenshot();
        Uri screenshotUri = Uri.fromFile(new File(APP_PATH + "/screenshot.png"));
        return mTextRecognizer.detectText(screenshotUri);
    }

    public Point[] getElementCornerPoints(FirebaseVisionText visionText, String elementText) {
        elementText = elementText.toLowerCase();

        for (FirebaseVisionText.TextBlock block: visionText.getTextBlocks()) {
            if (block.getText().toLowerCase().contains(elementText)) {
                for (FirebaseVisionText.Line line: block.getLines()) {
                    if (line.getText().toLowerCase().contains(elementText)) {
                        for (FirebaseVisionText.Element element: line.getElements()) {
                            if (element.getText().toLowerCase().equals(elementText)) {
                                return element.getCornerPoints();
                            }
                        }    
                    }
                    
                }
            }
        }

        return null;
    }

    public Point[] getLineCornerPoints(FirebaseVisionText visionText, String lineText) {
        lineText = lineText.toLowerCase();

        for (FirebaseVisionText.TextBlock block: visionText.getTextBlocks()) {
            if (block.getText().toLowerCase().contains(lineText)) {
                for (FirebaseVisionText.Line line: block.getLines()) {
                    if (line.getText().toLowerCase().equals(lineText)) {
                        return line.getCornerPoints();
                    }

                }
            }
        }

        return null;
    }

    public void cleanUp() {
        mTextRecognizer.close();
        Shell.runSuCommand("rm " + APP_PATH + "/screenshot.png");
    }
}
