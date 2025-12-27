package com.LeoNet.leonetscaner;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

public class FrontCameraCaptureActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content
        setContentView(R.layout.activity_front_camera_capture);

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get barcode view
        barcodeView = findViewById(R.id.zxing_barcode_scanner);

        if (barcodeView != null) {
            CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
            settings.setRequestedCameraId(1); // FRONT camera
            settings.setAutoFocusEnabled(true);
            barcodeView.getBarcodeView().setCameraSettings(settings);

            // Flip the preview to avoid mirror effect
            barcodeView.getBarcodeView().setScaleX(-1f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) barcodeView.pause();
    }
}
