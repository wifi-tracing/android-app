package com.prj.app.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.prj.app.logic.BSSIDMatcher;
import com.prj.app.managers.DatabaseManager;

import org.jetbrains.annotations.NotNull;

public class ExposureWorker extends Worker {
    public ExposureWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public @NotNull Result doWork() {
        BSSIDMatcher bssidMatcher = new BSSIDMatcher(DatabaseManager.getInstance(getApplicationContext()), null, getApplicationContext());
        bssidMatcher.getMatchingBSSIDs(); //this will take care of sending a notification
        return Result.success();
    }
}