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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.annotation.KeepName;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Demo app showing the various features of ML Kit for Firebase. This class is used to
 * set up continuous frame processing on frames from a camera source.
 */
@KeepName
public final class MainActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback,
        OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener {
    private static final String IMAGE_LABEL_DETECTION = "Label Detection";
    private static final String TAG = "LivePreviewActivity";
    private static final int PERMISSION_REQUESTS = 1;

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = IMAGE_LABEL_DETECTION;
    private TextView gstTv;
    private TextView gstTv2;
    private HashMap<String, GSTItem> gstMap = new HashMap<>();
    private TextView itemTv;
    private TextView itemTv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_live_preview);
        preview = (CameraSourcePreview) findViewById(R.id.firePreview);
        gstTv = (TextView) findViewById(R.id.gst_tv);
        itemTv = (TextView) findViewById(R.id.item_tv);
        gstTv2 = (TextView) findViewById(R.id.gst_tv_2);
        itemTv2 = (TextView) findViewById(R.id.item_tv_2);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = (GraphicOverlay) findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        } else {
            getRuntimePermissions();
        }

        prepareGSTMap();
    }

    private void prepareGSTMap() {
        gstMap.clear();
        InputStream is = getResources().openRawResource(R.raw.gst_rates);
        BufferedReader lineParser = null;
        try {
            lineParser = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String line;
            while ((line = lineParser.readLine()) != null) {
                String[] data = line.split(",");
                gstMap.put(data[0].toLowerCase(), new GSTItem(data));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "Selected model: " + selectedModel);
        preview.stop();
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
            startCameraSource();
        } else {
            getRuntimePermissions();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }

    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        switch (model) {
            case IMAGE_LABEL_DETECTION:
                Log.i(TAG, "Using Image Label Detector Processor");
                cameraSource.setMachineLearningFrameProcessor(new ImageLabelingProcessor(new OnClassifiedListener() {
                    @Override
                    public void onImageClassified(@NonNull List<FirebaseVisionLabel> label) {
                        setTextValue(label, 0, 0);
                    }
                }));
                break;
            default:
                Log.e(TAG, "Unknown model: " + model);
        }
    }

    private void setSecondaryTextValue(final List<FirebaseVisionLabel> label) {
        final String secondLabel = label.get(1).getLabel();
        fetchGSTForItem(secondLabel, new BasicCallback() {
            @Override
            public void onSuccess(GSTItem item) {
                String itemText = "Category: Unknown";
                String gstText = "Item not found in database";
                if (item != null) {
                    itemText = "Second Prediction: " + secondLabel;
                    gstText = "CGST:" + item.getCgst()
                            + "% SGST:" + item.getSgst()
                            + "% IGST:" + item.getIgst() + "%";
                    if (gstTv2 != null) {
                        gstTv2.setText(gstText);
                        gstTv2.setVisibility(View.VISIBLE);
                    }
                    if (itemTv2 != null) {
                        itemTv2.setText(itemText);
                        itemTv2.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (gstTv2 != null) {
                        gstTv2.setVisibility(View.GONE);
                    }
                    if (itemTv2 != null) {
                        itemTv2.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void setTextValue(final List<FirebaseVisionLabel> label, final int index, final int itemCount) {
        if (index > 1) {
            return;
        }
        final String labelStr = label.get(index).getLabel();
        fetchGSTForItem(labelStr, new BasicCallback() {
            @Override
            public void onSuccess(GSTItem item) {
                String itemText;
                String gstText;
                TextView gstTvSample = itemCount > 0 ? gstTv2 : gstTv;
                TextView itemTvSample = itemCount > 0 ? itemTv2 : itemTv;
                if (item != null) {
                    String prefix = itemCount > 0 ? "Second Prediction : " : "First Prediction : ";
                    itemText = prefix + labelStr;
                    gstText = "CGST:" + item.getCgst()
                            + "% SGST:" + item.getSgst()
                            + "% IGST:" + item.getIgst() + "%";
                    if (gstTvSample != null) {
                        gstTvSample.setText(gstText);
                        gstTvSample.setVisibility(View.VISIBLE);
                    }
                    if (itemTvSample != null) {
                        itemTvSample.setText(itemText);
                        itemTvSample.setVisibility(View.VISIBLE);
                    }
                    if (label.size() > 1) {
                        setTextValue(label, index + 1, itemCount + 1);
                    }
                } else {
                    if (gstTvSample != null) {
                        gstTvSample.setVisibility(View.GONE);
                    }
                    if (itemTvSample != null) {
                        itemTvSample.setVisibility(View.GONE);
                    }
                    if (label.size() > 1) {
                        setTextValue(label, index + 1, itemCount);
                    }
                }
            }
        });
    }

    //todo This method needs improvement, a lot of improvement.
    private void fetchGSTForItem(String label, BasicCallback callback) {
        if (label.contains(" ")) {
            label = label.substring(label.lastIndexOf(" ") + 1);
        }
        boolean itemFound = false;
        for (Object o : gstMap.entrySet()) {

            HashMap.Entry pair = (HashMap.Entry) o;
            GSTItem item = (GSTItem) pair.getValue();

            if (item.getDesc().contains(label.toLowerCase())) {
                if (callback != null) {
                    callback.onSuccess(item);
                    itemFound = true;
                    break;
                }
            }
        }

        if (callback != null && !itemFound) {
            callback.onSuccess(null);
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
}
