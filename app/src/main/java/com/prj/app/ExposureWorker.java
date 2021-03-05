package com.prj.app;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

public class ExposureWorker extends Worker {
    public ExposureWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public @NotNull Result doWork() {
        // Do the work here--in this case, upload the images.
        Log.d("wifi", "Starting Exposure Check");
        BSSIDMatcher bssidMatcher = new BSSIDMatcher(new DatabaseManager(getApplicationContext()), null, getApplicationContext());
        bssidMatcher.getMatchingBSSIDs(); //this will take care of sending a notification
        return Result.success();
    }
}