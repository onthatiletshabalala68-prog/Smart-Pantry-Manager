
package com.example.smartpantrymanager;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ExpiryNotificationScheduler {

    private static final String PERIODIC_WORK_NAME =
            "smart_pantry_expiry_check";

    private static final String IMMEDIATE_WORK_NAME =
            "smart_pantry_immediate_expiry_check";

    public static void schedule(Context context) {

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        ExpiryNotificationWorker.class,
                        24,
                        TimeUnit.HOURS
                ).build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        PERIODIC_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        request
                );
    }

    public static void checkNow(Context context) {

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(
                        ExpiryNotificationWorker.class
                ).build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        IMMEDIATE_WORK_NAME,
                        ExistingWorkPolicy.KEEP,
                        request
                );
    }
}
