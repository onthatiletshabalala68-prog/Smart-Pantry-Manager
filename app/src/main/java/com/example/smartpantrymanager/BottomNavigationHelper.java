
package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class BottomNavigationHelper {

    public static LinearLayout create(
            Activity activity,
            Class<? extends Activity> currentActivity) {

        LinearLayout navigationBar =
                new LinearLayout(activity);

        navigationBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        navigationBar.setGravity(Gravity.CENTER);

        navigationBar.setPadding(4, 8, 4, 8);

        navigationBar.setBackgroundColor(
                Color.rgb(245, 247, 250)
        );

        addNavigationButton(
                navigationBar,
                activity,
                "Home",
                MainActivity.class,
                currentActivity
        );

        addNavigationButton(
                navigationBar,
                activity,
                "Pantry",
                PantryActivity.class,
                currentActivity
        );

        addNavigationButton(
                navigationBar,
                activity,
                "Recipes",
                RecipeActivity.class,
                currentActivity
        );

        addNavigationButton(
                navigationBar,
                activity,
                "Shopping",
                ShoppingActivity.class,
                currentActivity
        );

        addNavigationButton(
                navigationBar,
                activity,
                "Settings",
                SettingsActivity.class,
                currentActivity
        );

        return navigationBar;
    }

    private static void addNavigationButton(
            LinearLayout navigationBar,
            Activity activity,
            String label,
            Class<? extends Activity> targetActivity,
            Class<? extends Activity> currentActivity) {

        Button button = new Button(activity);

        button.setText(label);
        button.setTextSize(10);
        button.setAllCaps(false);

        boolean selected =
                targetActivity.equals(currentActivity);

        button.setTypeface(
                null,
                selected
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setCornerRadius(18);

        if (selected) {
            background.setColor(
                    Color.rgb(46, 125, 50)
            );
            button.setTextColor(Color.WHITE);
        } else {
            background.setColor(
                    Color.TRANSPARENT
            );
            button.setTextColor(
                    Color.rgb(45, 55, 65)
            );
        }

        button.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(activity, 48),
                        1
                );

        params.setMargins(2, 0, 2, 0);

        navigationBar.addView(button, params);

        button.setOnClickListener(view -> {

            if (selected) {
                return;
            }

            Intent intent = new Intent(
                    activity,
                    targetActivity
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            activity.startActivity(intent);

            if (!(activity instanceof MainActivity)) {
                activity.finish();
            }
        });
    }

    private static int dp(
            Activity activity,
            int value) {

        return Math.round(
                value * activity.getResources()
                        .getDisplayMetrics().density
        );
    }
}
