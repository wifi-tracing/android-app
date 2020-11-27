package com.example.prj_android_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.skyfishjy.library.RippleBackground;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 100);
        checkPermission(Manifest.permission.ACCESS_WIFI_STATE, 101);
        checkPermission(Manifest.permission.CHANGE_WIFI_STATE, 102);

        startWifiService();

    }
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
                permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            MainActivity.this,
                            new String[] { permission },
                            requestCode);
        }
    }

    public void startWifiService() {
        Intent serviceIntent = new Intent(this, WifiScanningService.class);
        serviceIntent.putExtra("inputExtra", "Wifi Scanning Service");
        ContextCompat.startForegroundService(this, serviceIntent);


        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.content);
        rippleBackground.startRippleAnimation();
    }
    public void stopWifiService() {
        Intent serviceIntent = new Intent(this, WifiScanningService.class);
        stopService(serviceIntent);
    }
}