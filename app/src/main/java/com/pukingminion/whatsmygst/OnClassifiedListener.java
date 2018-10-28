package com.pukingminion.whatsmygst;

import android.support.annotation.NonNull;

import com.google.firebase.ml.vision.label.FirebaseVisionLabel;

import java.util.List;

/**
 * Created by Samarth on 23/06/18.
 */

public interface OnClassifiedListener {
    void onImageClassified(@NonNull List<FirebaseVisionLabel> label);
}
