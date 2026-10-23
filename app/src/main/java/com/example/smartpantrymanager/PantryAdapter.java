package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends ArrayAdapter<PantryItem> {

    private static final String PREFS_NAME = "smart_pantry_settings";
    private static final String KEY_WARNING_DAYS = "expiry_warning_days";
    private static final String KEY_WARNINGS_ENABLED =
            "expiry_warnings_enabled";

    public PantryAdapter(Context context, List<PantryItem> items) {
        super(context, android.R.layout.simple_list_item_2,
                android.R.id.text1, items);
    }

    @Override
    public View getView(int position, View convertView,
                        ViewGroup parent) {

        View view = super.getView(position, convertView, parent);

        PantryItem item = getItem(position);

        TextView nameView = view.findViewById(android.R.id.text1);
        TextView detailsView = view.findViewById(android.R.id.text2);

        if (item != null) {
            nameView.setText(item.getName());
            nameView.setTypeface(null, Typeface.BOLD);

            String expiry = item.getExpiryDate();
            String status = getConfiguredExpiryStatus(
                    getContext(), expiry);

            String expiryText;

            if (expiry == null || expiry.trim().isEmpty()
                    || expiry.equalsIgnoreCase("Not specified")) {
                expiryText = "Not specified";
            } else {
                expiryText = expiry;
            }

            detailsView.setText(String.format(
                    Locale.getDefault(),
                    "%s %s | Expiry: %s | %s",
                    formatQuantity(item.getQuantity()),
                    item.getUnit(),
                    expiryText,
                    status
            ));

            int backgroundColor;

            switch (status) {
                case "EXPIRED":
                    backgroundColor = Color.rgb(255, 220, 220);
                    break;

                case "Expiring today":
                case "Expiring soon":
                    backgroundColor = Color.rgb(255, 235, 200);
                    break;

                case "Fresh":
                    backgroundColor = Color.rgb(220, 245, 220);
                    break;

                default:
                    backgroundColor = Color.WHITE;
                    break;
            }

            view.setBackgroundColor(backgroundColor);

            nameView.setTextColor(Color.rgb(40, 40, 40));
            detailsView.setTextColor(Color.rgb(40, 40, 40));
        }

        return view;
    }

    public static String getConfiguredExpiryStatus(
            Context context, String expiryDate) {

        if (expiryDate == null
                || expiryDate.trim().isEmpty()
                || expiryDate.equalsIgnoreCase("Not specified")) {
            return "No expiry date";
        }

        try {
            LocalDate expiry = LocalDate.parse(expiryDate.trim());
            LocalDate today = LocalDate.now();

            long daysRemaining =
                    ChronoUnit.DAYS.between(today, expiry);

            if (daysRemaining < 0) {
                return "EXPIRED";
            }

            SharedPreferences preferences =
                    context.getSharedPreferences(
                            PREFS_NAME, Context.MODE_PRIVATE);

            boolean warningsEnabled =
                    preferences.getBoolean(
                            KEY_WARNINGS_ENABLED, true);

            int warningDays =
                    preferences.getInt(KEY_WARNING_DAYS, 7);

            if (!warningsEnabled) {
                return "Fresh";
            }

            if (daysRemaining == 0) {
                return "Expiring today";
            }

            if (daysRemaining <= warningDays) {
                return "Expiring soon";
            }

            return "Fresh";

        } catch (Exception e) {
            return "Invalid expiry date";
        }
    }

    public static String getExpiryStatus(String expiryDate) {
        if (expiryDate == null
                || expiryDate.trim().isEmpty()
                || expiryDate.equalsIgnoreCase("Not specified")) {
            return "No expiry date";
        }

        try {
            LocalDate expiry = LocalDate.parse(expiryDate.trim());
            LocalDate today = LocalDate.now();

            long daysRemaining =
                    ChronoUnit.DAYS.between(today, expiry);

            if (daysRemaining < 0) {
                return "EXPIRED";
            } else if (daysRemaining == 0) {
                return "Expiring today";
            } else if (daysRemaining <= 7) {
                return "Expiring soon";
            } else {
                return "Fresh";
            }

        } catch (Exception e) {
            return "Invalid expiry date";
        }
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.format(
                    Locale.getDefault(), "%.0f", quantity);
        }

        return String.format(
                Locale.getDefault(), "%s", quantity);
    }
}