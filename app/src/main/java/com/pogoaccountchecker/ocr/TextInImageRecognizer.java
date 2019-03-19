package com.pogoaccountchecker.ocr;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.pogoaccountchecker.utils.Utils;

import java.io.IOException;

import androidx.annotation.NonNull;

public class TextInImageRecognizer {
    private Context mContext;
    private FirebaseVisionTextRecognizer mDetector;
    private final String LOG_TAG = getClass().getSimpleName();

    public TextInImageRecognizer(Context context) {
        mContext = context;
    }

    public FirebaseVisionText detectText(Uri imageUri) {
        if (mDetector == null) mDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        FirebaseVisionImage image;
        try {
            image = FirebaseVisionImage.fromFilePath(mContext, imageUri);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception occurred when opening file.");
            e.printStackTrace();
            return null;
        }

        mDetector.processImage(image);
        Task<FirebaseVisionText> result =
                mDetector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                Log.i(LOG_TAG, "Text recognition completed successfully. Detected: " + firebaseVisionText.getText());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(LOG_TAG, "Failure during text recognition.");
                                e.printStackTrace();
                            }
                        });

        int count = 0;
        while (count != 1000) {
            Utils.sleep(10);
            if (result.isComplete()) {
                if (result.isSuccessful()) {
                     return result.getResult();
                }
                return null;
            }
            count++;
        }
        Log.e(LOG_TAG, "Text recognition API didn't respond in time.");
        return null;
    }

    public void close() {
        try {
            if (mDetector != null) mDetector.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception while closing detector.");
            e.printStackTrace();
        }
    }
}
