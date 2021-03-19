package com.prj.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.skyfishjy.library.RippleBackground;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static boolean isScanning = false;
    private static Intent wifiScanningIntent;
    private boolean startScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);
        handlePermission(Manifest.permission.ACCESS_FINE_LOCATION, 100);
        handlePermission(Manifest.permission.ACCESS_WIFI_STATE, 101);
        handlePermission(Manifest.permission.CHANGE_WIFI_STATE, 102);
        if (wifiScanningIntent == null) {
            wifiScanningIntent = new Intent(this, WifiScanningService.class);
            wifiScanningIntent.putExtra("inputExtra", "Wifi Scanning Service");
        }
        startExposureWorker();
        startDummyUploadsWorker();
    }

    /**
     * Enqueue a unique request for dummy uploads
     */
    private void startDummyUploadsWorker() {
        Random random = new Random();

        OneTimeWorkRequest.Builder myWorkBuilder =
                new OneTimeWorkRequest.Builder(DummyUploadWorker.class).setInitialDelay(random.nextInt(7), TimeUnit.MINUTES); //between 0 and 128 minutes from now
        OneTimeWorkRequest uploadRequest = myWorkBuilder.build();
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork("DUMMY_UPLOAD", ExistingWorkPolicy.KEEP, uploadRequest);
    }

    /**
     * Enqueue a unique request for exposure checks
     */
    private void startExposureWorker() {
        PeriodicWorkRequest.Builder myWorkBuilder =
                new PeriodicWorkRequest.Builder(ExposureWorker.class, 12, TimeUnit.HOURS); //run every 12 hours
        PeriodicWorkRequest exposureRequest = myWorkBuilder.build();
        WorkManager.getInstance(getApplicationContext())
                //If there is existing pending (uncompleted) work with the same unique name, do nothing
                .enqueueUniquePeriodicWork("EXPOSURE_CHECK", ExistingPeriodicWorkPolicy.KEEP, exposureRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isScanning && !startScanning) {
            stopService(wifiScanningIntent);
            isScanning = false;
        } else if (startScanning && !isScanning) {
            Log.d("wifi", "Started Scanning.");
            ContextCompat.startForegroundService(this, wifiScanningIntent);
            isScanning = true;
        }

        RippleBackground rippleBackground = findViewById(R.id.rippleBackground);

        if (rippleBackground != null) {
            if (isScanning && !rippleBackground.isRippleAnimationRunning()) {
                rippleBackground.startRippleAnimation();
            } else if (!isScanning && rippleBackground.isRippleAnimationRunning()) {
                rippleBackground.stopRippleAnimation();
            }
        }
    }

    public void handlePermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat
                    .requestPermissions(
                            MainActivity.this,
                            new String[]{permission},
                            requestCode);
        } else {
            startScanning = true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String @NotNull [] permissions, int @NotNull [] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            startScanning = false;
        }
    }

    public void openDevUtils(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}