package com.prj.app.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.prj.app.R;
import com.prj.app.managers.NotificationManager;
import com.prj.app.services.WifiScanningService;
import com.prj.app.workers.DummyUploadWorker;
import com.prj.app.workers.ExposureWorker;
import com.skyfishjy.library.RippleBackground;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressLint("StaticFieldLeak")
public class MainActivity extends AppCompatActivity {
    private static Intent wifiScanningIntent;
    private static RippleBackground rippleBackground;
    private static TextView scanningStatusTextView;
    final String welcomeScreenShownPref = "WelcomeScreenShown";
    SharedPreferences mPrefs;

    public static void updateScanningUI(@NotNull Context context) {
        NotificationManager notificationManager = NotificationManager.getInstance(context);
        boolean isScanning = notificationManager.getIsScanningVisible();
        if (scanningStatusTextView != null) {
            if (isScanning) {
                scanningStatusTextView.setText(R.string.scanning);
                scanningStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.primary));
            } else {
                scanningStatusTextView.setText(R.string.notscanning);
                scanningStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.secondary));
            }
        }
        if (rippleBackground != null) {
            if (isScanning && !rippleBackground.isRippleAnimationRunning()) {
                rippleBackground.startRippleAnimation();
            } else if (!isScanning && rippleBackground.isRippleAnimationRunning()) {
                rippleBackground.stopRippleAnimation();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);
    }

    private void initialiseActivity() {
        if (wifiScanningIntent == null) {
            wifiScanningIntent = new Intent(this, WifiScanningService.class);
            wifiScanningIntent.putExtra("inputExtra", "Wifi Scanning Service");
            NotificationManager.getInstance(getApplicationContext()).setIsScanningVisible(true);
            ContextCompat.startForegroundService(this, wifiScanningIntent);
        }
        updateScanningUI(getApplicationContext());
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
        //show welcome screen on startup
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean welcomeScreenShown = mPrefs.getBoolean(welcomeScreenShownPref, false);
        if (!welcomeScreenShown) {
            startActivity(new Intent(this, WelcomeActivity.class));
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(welcomeScreenShownPref, true);
            editor.apply();
            return;
        }
        rippleBackground = findViewById(R.id.rippleBackground);
        scanningStatusTextView = findViewById(R.id.scanningStatusTextView);
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat
                    .requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            100);
        } else {
            initialiseActivity();
        }
        updateScanningUI(getApplicationContext());
    }

    public void onRequestPermissionsResult(int requestCode, String @NotNull [] permissions, int @NotNull [] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            NotificationManager.getInstance(getApplicationContext()).setIsScanningVisible(false);
            updateScanningUI(getApplicationContext());
        } else {
            initialiseActivity();
        }
    }


    public void openDevUtils(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}