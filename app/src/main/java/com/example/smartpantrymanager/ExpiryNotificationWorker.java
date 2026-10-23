
package com.example.smartpantrymanager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpiryNotificationWorker extends Worker {

    public static final String KEY_MANUAL_TEST =
            "manual_test";

    private static final String CHANNEL_ID =
            "pantry_expiry_alerts";

    private static final int NOTIFICATION_ID = 1001;

    private static final String PREFS_NAME =
            "smart_pantry_settings";

    private static final String KEY_LAST_SIGNATURE =
            "last_expiry_notification_signature";

    private static final String KEY_LAST_DATE =
            "last_expiry_notification_date";

    public ExpiryNotificationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters parameters) {

        super(context, parameters);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context context = getApplicationContext();

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        boolean enabled = preferences.getBoolean(
                "expiry_warnings_enabled",
                true
        );

        if (!enabled) {
            cancelExpiryNotification(context);
            return Result.success();
        }

        if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
            return Result.success();
        }

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager == null ||
                !manager.areNotificationsEnabled()) {
            return Result.success();
        }

        boolean manualTest = getInputData().getBoolean(
                KEY_MANUAL_TEST,
                false
        );

        int warningDays = preferences.getInt(
                "expiry_warning_days",
                7
        );

        warningDays = Math.max(
                1,
                Math.min(30, warningDays)
        );

        DatabaseHelper databaseHelper =
                new DatabaseHelper(context);

        List<PantryItem> pantryItems;

        try {
            pantryItems =
                    databaseHelper.getAllPantryItems();
        } finally {
            databaseHelper.close();
        }

        List<String> messages = new ArrayList<>();
        List<String> signatures = new ArrayList<>();

        int expiredCount = 0;
        int todayCount = 0;
        int soonCount = 0;

        Calendar today = Calendar.getInstance();
        resetTime(today);

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.ROOT
                );

        formatter.setLenient(false);

        for (PantryItem item : pantryItems) {

            String expiryDate = item.getExpiryDate();

            if (expiryDate == null ||
                    expiryDate.trim().isEmpty() ||
                    expiryDate.trim().equalsIgnoreCase(
                            "Not specified"
                    )) {
                continue;
            }

            try {
                Date parsedDate =
                        formatter.parse(expiryDate.trim());

                if (parsedDate == null) {
                    continue;
                }

                Calendar expiry = Calendar.getInstance();
                expiry.setTime(parsedDate);
                resetTime(expiry);

                long difference =
                        calculateDaysDifference(
                                today,
                                expiry
                        );

                String name = item.getName();

                if (name == null ||
                        name.trim().isEmpty()) {
                    name = "Unnamed ingredient";
                }

                String message;
                String status;

                if (difference < 0) {

                    long daysExpired = -difference;

                    message = name + (
                            daysExpired == 1
                                    ? " - Expired yesterday"
                                    : " - Expired "
                                    + daysExpired
                                    + " days ago"
                    );

                    status = "expired";
                    expiredCount++;

                } else if (difference == 0) {

                    message = name + " - Expires today";
                    status = "today";
                    todayCount++;

                } else if (difference <= warningDays) {

                    message = name + (
                            difference == 1
                                    ? " - Expires tomorrow"
                                    : " - Expires in "
                                    + difference
                                    + " days"
                    );

                    status = "soon";
                    soonCount++;

                } else {
                    continue;
                }

                messages.add(message);

                signatures.add(
                        item.getId()
                                + "|"
                                + name
                                + "|"
                                + expiryDate.trim()
                                + "|"
                                + status
                );

            } catch (ParseException ignored) {
            }
        }

        if (messages.isEmpty()) {
            cancelExpiryNotification(context);

            preferences.edit()
                    .remove(KEY_LAST_SIGNATURE)
                    .remove(KEY_LAST_DATE)
                    .apply();

            return Result.success();
        }

        Collections.sort(signatures);

        StringBuilder signatureBuilder =
                new StringBuilder();

        for (String signaturePart : signatures) {
            signatureBuilder.append(signaturePart)
                    .append("\n");
        }

        String currentSignature =
                signatureBuilder.toString();

        String currentDate = formatter.format(
                today.getTime()
        );

        String lastSignature =
                preferences.getString(
                        KEY_LAST_SIGNATURE,
                        ""
                );

        String lastDate =
                preferences.getString(
                        KEY_LAST_DATE,
                        ""
                );

        if (!manualTest &&
                currentDate.equals(lastDate) &&
                currentSignature.equals(lastSignature)) {

            return Result.success();
        }

        createNotificationChannel(context);

        String summary =
                expiredCount + " expired, "
                        + todayCount + " expiring today, "
                        + soonCount + " expiring soon.";

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        int displayCount = Math.min(
                messages.size(),
                6
        );

        for (int i = 0; i < displayCount; i++) {
            inboxStyle.addLine(messages.get(i));
        }

        if (messages.size() > displayCount) {
            inboxStyle.addLine(
                    "And "
                            + (messages.size() - displayCount)
                            + " more ingredients..."
            );
        }

        inboxStyle.setSummaryText(summary);

        Intent intent = new Intent(
                context,
                PantryActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_alert
                        )
                        .setContentTitle(
                                "Pantry Expiry Reminder"
                        )
                        .setContentText(messages.get(0))
                        .setStyle(inboxStyle)
                        .setPriority(
                                NotificationCompat.PRIORITY_DEFAULT
                        )
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setNumber(messages.size());

        manager.notify(
                NOTIFICATION_ID,
                builder.build()
        );

        if (!manualTest) {
            preferences.edit()
                    .putString(
                            KEY_LAST_SIGNATURE,
                            currentSignature
                    )
                    .putString(
                            KEY_LAST_DATE,
                            currentDate
                    )
                    .apply();
        }

        return Result.success();
    }

    private void cancelExpiryNotification(
            Context context) {

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
        }
    }

    private long calculateDaysDifference(
            Calendar today,
            Calendar expiry) {

        Calendar cursor =
                (Calendar) today.clone();

        long days = 0;

        if (cursor.before(expiry)) {

            while (cursor.before(expiry)) {
                cursor.add(
                        Calendar.DAY_OF_YEAR,
                        1
                );
                days++;
            }

        } else if (cursor.after(expiry)) {

            while (cursor.after(expiry)) {
                cursor.add(
                        Calendar.DAY_OF_YEAR,
                        -1
                );
                days--;
            }
        }

        return days;
    }

    private void resetTime(Calendar calendar) {

        calendar.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        calendar.set(
                Calendar.MINUTE,
                0
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );
    }

    private void createNotificationChannel(
            Context context) {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Pantry Expiry Alerts",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );

            channel.setDescription(
                    "Notifications about expired "
                            + "and expiring pantry ingredients"
            );

            NotificationManager manager =
                    context.getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(
                        channel
                );
            }
        }
    }
}
