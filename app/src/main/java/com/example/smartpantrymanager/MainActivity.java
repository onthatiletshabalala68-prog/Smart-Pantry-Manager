
package com.example.smartpantrymanager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        FrameLayout backgroundContainer = new FrameLayout(this);

        backgroundContainer.setBackgroundResource(
                R.drawable.home_background
        );

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout contentWrapper = new LinearLayout(this);
        contentWrapper.setOrientation(LinearLayout.VERTICAL);
        contentWrapper.setGravity(Gravity.CENTER);
        contentWrapper.setPadding(
                dp(20), dp(24), dp(20), dp(24)
        );

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER);
        contentLayout.setPadding(
                dp(20), dp(28), dp(20), dp(28)
        );

        GradientDrawable panelBackground =
                new GradientDrawable();

        panelBackground.setColor(
                Color.argb(225, 255, 250, 242)
        );
        panelBackground.setCornerRadius(dp(24));

        contentLayout.setBackground(panelBackground);
        contentLayout.setElevation(dp(5));

        TextView title = new TextView(this);
        title.setText("Smart Pantry Manager");
        title.setTextSize(27);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.rgb(32, 91, 46));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(16));

        contentLayout.addView(title);

        TextView description = new TextView(this);
        description.setText(
                "Manage your ingredients, discover recipes "
                        + "and keep track of your shopping."
        );
        description.setTextSize(15);
        description.setTextColor(Color.rgb(65, 65, 65));
        description.setGravity(Gravity.CENTER);
        description.setPadding(0, 0, 0, dp(26));

        contentLayout.addView(description);

        Button pantryButton = createHomeButton(
                "My Pantry",
                Color.rgb(67, 145, 75)
        );

        pantryButton.setOnClickListener(view ->
                openActivity(PantryActivity.class)
        );

        contentLayout.addView(pantryButton);

        Button recipesButton = createHomeButton(
                "Suggested Recipes",
                Color.rgb(224, 129, 38)
        );

        recipesButton.setOnClickListener(view ->
                openActivity(RecipeActivity.class)
        );

        contentLayout.addView(recipesButton);

        Button shoppingButton = createHomeButton(
                "Shopping List",
                Color.rgb(60, 121, 182)
        );

        shoppingButton.setOnClickListener(view ->
                openActivity(ShoppingActivity.class)
        );

        contentLayout.addView(shoppingButton);

        Button settingsButton = createHomeButton(
                "Settings",
                Color.rgb(126, 83, 159)
        );

        settingsButton.setOnClickListener(view ->
                openActivity(SettingsActivity.class)
        );

        contentLayout.addView(settingsButton);

        contentWrapper.addView(
                contentLayout,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        scrollView.addView(contentWrapper);

        backgroundContainer.addView(
                scrollView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        rootLayout.addView(
                backgroundContainer,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        LinearLayout bottomNavigation =
                BottomNavigationHelper.create(
                        this,
                        MainActivity.class
                );

        rootLayout.addView(
                bottomNavigation,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(rootLayout);

        startExpiryNotifications();
    }

    private Button createHomeButton(
            String text,
            int backgroundColor) {

        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setTextColor(Color.WHITE);
        button.setTypeface(null, Typeface.BOLD);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);

        GradientDrawable buttonBackground =
                new GradientDrawable();

        buttonBackground.setColor(backgroundColor);
        buttonBackground.setCornerRadius(dp(14));

        button.setBackground(buttonBackground);
        button.setElevation(dp(3));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(56)
                );

        params.setMargins(0, dp(7), 0, dp(7));
        button.setLayoutParams(params);

        return button;
    }

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density
        );
    }

    private void startExpiryNotifications() {

        ExpiryNotificationScheduler.schedule(this);

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_PERMISSION_REQUEST
                );
            }
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

            if (grantResults.length > 0
                    && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

                ExpiryNotificationScheduler.schedule(this);
            }
        }
    }

    private void openActivity(
            Class<? extends Activity> activityClass) {

        Intent intent = new Intent(
                this,
                activityClass
        );

        startActivity(intent);
    }
}
