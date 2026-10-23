
package com.example.smartpantrymanager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class SettingsActivity extends Activity {

    private static final String PREFS_NAME =
            "smart_pantry_settings";

    private static final String KEY_EXPIRY_DAYS =
            "expiry_warning_days";

    private static final String KEY_EXPIRY_ENABLED =
            "expiry_warnings_enabled";

    private static final int NOTIFICATION_PERMISSION_REQUEST =
            102;

    private SharedPreferences preferences;
    private SeekBar expirySeekBar;
    private TextView expiryDaysText;
    private CheckBox expiryWarningsCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        ScrollView scrollView = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);

        TextView title = new TextView(this);
        title.setText("Settings");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);

        TextView description = new TextView(this);
        description.setText(
                "Customise how Smart Pantry Manager works."
        );
        description.setTextSize(16);
        description.setPadding(0, 0, 0, 24);
        layout.addView(description);

        Button profileButton = new Button(this);
        profileButton.setText("My Profile");
        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    SettingsActivity.this,
                    ProfileActivity.class
            );
            startActivity(intent);
        });
        layout.addView(profileButton);

        TextView expiryHeading = new TextView(this);
        expiryHeading.setText("Expiry Warning Period");
        expiryHeading.setTextSize(18);
        expiryHeading.setPadding(0, 24, 0, 8);
        layout.addView(expiryHeading);

        TextView expiryDescription = new TextView(this);
        expiryDescription.setText(
                "Choose how many days before an ingredient's "
                        + "expiry date it should be flagged."
        );
        expiryDescription.setTextSize(14);
        layout.addView(expiryDescription);

        expiryDaysText = new TextView(this);
        expiryDaysText.setTextSize(16);
        expiryDaysText.setPadding(0, 16, 0, 4);
        layout.addView(expiryDaysText);

        expirySeekBar = new SeekBar(this);
        expirySeekBar.setMax(29);

        int savedDays = preferences.getInt(
                KEY_EXPIRY_DAYS,
                7
        );

        savedDays = Math.max(
                1,
                Math.min(30, savedDays)
        );

        expirySeekBar.setProgress(savedDays - 1);

        expirySeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser) {

                        updateExpiryDaysText(progress + 1);
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar) {
                    }
                }
        );

        layout.addView(expirySeekBar);

        TextView rangeText = new TextView(this);
        rangeText.setText(
                "Choose between 1 and 30 days."
        );
        rangeText.setPadding(0, 4, 0, 16);
        layout.addView(rangeText);

        expiryWarningsCheckBox = new CheckBox(this);
        expiryWarningsCheckBox.setText(
                "Enable expiry warnings"
        );

        expiryWarningsCheckBox.setChecked(
                preferences.getBoolean(
                        KEY_EXPIRY_ENABLED,
                        true
                )
        );

        layout.addView(expiryWarningsCheckBox);

        TextView notificationHeading =
                new TextView(this);

        notificationHeading.setText(
                "Expiry Notifications"
        );

        notificationHeading.setTextSize(18);
        notificationHeading.setPadding(0, 24, 0, 8);
        layout.addView(notificationHeading);

        TextView notificationDescription =
                new TextView(this);

        notificationDescription.setText(
                "Check your pantry for expired "
                        + "or soon-to-expire ingredients."
        );

        notificationDescription.setTextSize(14);
        notificationDescription.setPadding(
                0, 0, 0, 12
        );

        layout.addView(notificationDescription);

        Button testNotificationButton =
                new Button(this);

        testNotificationButton.setText(
                "Test Expiry Notifications"
        );

        testNotificationButton.setOnClickListener(
                view -> testExpiryNotifications()
        );

        layout.addView(testNotificationButton);

        TextView aboutHeading = new TextView(this);
        aboutHeading.setText("About");
        aboutHeading.setTextSize(18);
        aboutHeading.setPadding(0, 24, 0, 8);
        layout.addView(aboutHeading);

        TextView aboutText = new TextView(this);
        aboutText.setText(
                "Smart Pantry Manager\n"
                        + "Manage pantry ingredients, track expiry "
                        + "dates, discover recipes and maintain "
                        + "a shopping list."
        );
        layout.addView(aboutText);

        Button saveButton = new Button(this);
        saveButton.setText("Save Settings");
        saveButton.setOnClickListener(
                view -> saveSettings()
        );
        layout.addView(saveButton);

        Button backButton = new Button(this);
        backButton.setText("Back to Home");
        backButton.setOnClickListener(view -> finish());
        layout.addView(backButton);

        scrollView.addView(layout);

        rootLayout.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        LinearLayout bottomNavigation =
                BottomNavigationHelper.create(
                        this,
                        SettingsActivity.class
                );

        rootLayout.addView(
                bottomNavigation,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(rootLayout);

        updateExpiryDaysText(
                expirySeekBar.getProgress() + 1
        );
    }

    private void updateExpiryDaysText(int days) {

        expiryDaysText.setText(
                "Warn me " + days
                        + (days == 1 ? " day" : " days")
                        + " before expiry"
        );
    }

    private void saveSettings() {

        if (saveSettingsToPreferences()) {

            if (expiryWarningsCheckBox.isChecked()) {
                ExpiryNotificationScheduler.checkNow(this);
            } else {
                cancelCurrentExpiryNotification();
            }

            Toast.makeText(
                    this,
                    "Settings saved successfully",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Could not save settings",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private boolean saveSettingsToPreferences() {

        return preferences.edit()
                .putInt(
                        KEY_EXPIRY_DAYS,
                        expirySeekBar.getProgress() + 1
                )
                .putBoolean(
                        KEY_EXPIRY_ENABLED,
                        expiryWarningsCheckBox.isChecked()
                )
                .commit();
    }

    private void testExpiryNotifications() {

        if (!saveSettingsToPreferences()) {

            Toast.makeText(
                    this,
                    "Could not save settings for the test",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!expiryWarningsCheckBox.isChecked()) {

            cancelCurrentExpiryNotification();

            Toast.makeText(
                    this,
                    "Enable expiry warnings before testing",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    NOTIFICATION_PERMISSION_REQUEST
            );

            return;
        }

        runNotificationTest();
    }

    private void runNotificationTest() {

        Data inputData =
                new Data.Builder()
                        .putBoolean(
                                ExpiryNotificationWorker.KEY_MANUAL_TEST,
                                true
                        )
                        .build();

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(
                        ExpiryNotificationWorker.class
                )
                        .setInputData(inputData)
                        .build();

        WorkManager.getInstance(this)
                .enqueue(request);

        Toast.makeText(
                this,
                "Expiry notification test scheduled",
                Toast.LENGTH_LONG
        ).show();
    }

    private void cancelCurrentExpiryNotification() {

        android.app.NotificationManager manager =
                (android.app.NotificationManager)
                        getSystemService(
                                NOTIFICATION_SERVICE
                        );

        if (manager != null) {
            manager.cancel(1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode ==
                NOTIFICATION_PERMISSION_REQUEST) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                runNotificationTest();

            } else {

                Toast.makeText(
                        this,
                        "Notification permission is required",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
