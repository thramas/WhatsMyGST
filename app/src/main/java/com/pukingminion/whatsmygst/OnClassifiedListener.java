package com.pukingminion.whatsmygst;

import com.google.firebase.ml.vision.label.FirebaseVisionLabel;

/**
 * Created by Samarth on 23/06/18.
 */

public interface OnClassifiedListener {
    void onImageClassified(FirebaseVisionLabel label);
}
