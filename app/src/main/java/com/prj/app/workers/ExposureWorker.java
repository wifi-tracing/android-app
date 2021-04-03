package com.prj.app.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.prj.app.logic.RiskAnalyser;
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
        RiskAnalyser riskAnalyser = new RiskAnalyser(DatabaseManager.getInstance(getApplicationContext()), null, getApplicationContext());
        riskAnalyser.getMatchingBSSIDs(); //this will take care of sending a notification
        return Result.success();
    }
}