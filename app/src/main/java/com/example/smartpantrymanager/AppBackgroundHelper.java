
package com.example.smartpantrymanager;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

public class AppBackgroundHelper {

    public static void apply(View view) {

        int[] colors = {
                Color.rgb(224, 242, 220),
                Color.rgb(245, 250, 238),
                Color.rgb(255, 248, 235)
        };

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                colors
        );

        gradient.setGradientType(
                GradientDrawable.LINEAR_GRADIENT
        );

        view.setBackground(gradient);
    }
}
