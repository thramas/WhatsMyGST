// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.pukingminion.whatsmygst;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;

import com.google.firebase.ml.vision.label.FirebaseVisionLabel;

import java.util.Comparator;
import java.util.List;

/**
 * Graphic instance for rendering a label within an associated graphic overlay view.
 */
public class LabelGraphic extends GraphicOverlay.Graphic {

    private final Paint textPaint;
    private final GraphicOverlay overlay;

    private final List<FirebaseVisionLabel> labels;

    LabelGraphic(GraphicOverlay overlay, List<FirebaseVisionLabel> labels) {
        super(overlay);
        this.overlay = overlay;
        this.labels = labels;
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30.0f);
        postInvalidate();
    }

    @Override
    public synchronized void draw(Canvas canvas) {
        float x = overlay.getWidth() / 4.0f;
        float y = overlay.getHeight() / 2.0f;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            labels.sort(new Comparator<FirebaseVisionLabel>() {
                @Override
                public int compare(FirebaseVisionLabel o1, FirebaseVisionLabel o2) {
                    return o1.getConfidence() > o2.getConfidence() ? 1 : -1;
                }
            });
        }

        for (FirebaseVisionLabel label : labels) {
            if (label.getConfidence() < 0.5) continue;
            canvas.drawText(label.getLabel(), x, y, textPaint);
            Rect bounds = new Rect();
            textPaint.getTextBounds(label.getLabel(), 0, label.getLabel().length(), bounds);
            int width = bounds.width();
            String confidence = String.valueOf((Math.round(label.getConfidence() * 100.0) / 100.0) * 100);
            canvas.drawText(confidence + "% ", x + width + 10, y, textPaint);
            y = y - 32.0f;
        }
    }

    private String getGSTRateForLabel(String label) {
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences("GST", Context.MODE_PRIVATE);
        return sharedPreferences.getString(label, "Unknown");
    }
}
