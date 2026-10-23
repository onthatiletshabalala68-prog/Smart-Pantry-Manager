
package com.example.smartpantrymanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PantryActivity extends Activity {

    private DatabaseHelper databaseHelper;
    private PantryAdapter pantryAdapter;
    private ListView pantryListView;
    private TextView emptyMessage;
    private EditText searchField;
    private Spinner filterSpinner;

    private boolean expiryWarningsEnabled = true;
    private int expiryWarningDays = 7;

    private final List<PantryItem> allPantryItems =
            new ArrayList<>();

    private final String[] metricUnits = {
            "g", "kg", "mL", "L", "mg",
            "item", "piece", "clove", "slice",
            "packets", "cans", "bottles"
    };

    private final String[] filterOptions = {
            "All Ingredients",
            "Expired",
            "Expiring Soon",
            "Fresh"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);
        loadExpirySettings();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("My Pantry");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 12);

        searchField = new EditText(this);
        searchField.setHint("Search ingredients...");
        searchField.setSingleLine(true);
        searchField.setInputType(InputType.TYPE_CLASS_TEXT);

        filterSpinner = new Spinner(this);

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filterOptions
        );

        filterAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        filterSpinner.setAdapter(filterAdapter);

        Button addButton = new Button(this);
        addButton.setText("Add Ingredient");

        emptyMessage = new TextView(this);
        emptyMessage.setText(
                "Your pantry is empty. Add an ingredient."
        );
        emptyMessage.setGravity(Gravity.CENTER);
        emptyMessage.setPadding(0, 20, 0, 20);

        pantryListView = new ListView(this);

        layout.addView(title);
        layout.addView(searchField);
        layout.addView(filterSpinner);
        layout.addView(addButton);
        layout.addView(emptyMessage);

        layout.addView(
                pantryListView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        AppBackgroundHelper.apply(rootLayout);

        rootLayout.addView(
                layout,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        LinearLayout bottomNavigation =
                BottomNavigationHelper.create(
                        this,
                        PantryActivity.class
                );

        rootLayout.addView(
                bottomNavigation,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(rootLayout);

        pantryAdapter = new PantryAdapter(
                this,
                new ArrayList<>()
        );

        pantryListView.setAdapter(pantryAdapter);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {
                applySearchAndFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        filterSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
                        applySearchAndFilter();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );

        addButton.setOnClickListener(
                view -> showIngredientDialog(null)
        );

        pantryListView.setOnItemClickListener(
                (parent, view, position, id) -> {
                    PantryItem item = pantryAdapter.getItem(position);

                    if (item != null) {
                        showIngredientDialog(item);
                    }
                }
        );

        pantryListView.setOnItemLongClickListener(
                (parent, view, position, id) -> {
                    PantryItem item = pantryAdapter.getItem(position);

                    if (item != null) {
                        showIngredientOptions(item);
                    }

                    return true;
                }
        );

        updatePantryList();
    }

    private void applySearchAndFilter() {
        if (pantryAdapter == null) {
            return;
        }

        String searchText = searchField.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        String selectedFilter =
                filterSpinner.getSelectedItem() == null
                        ? "All Ingredients"
                        : filterSpinner.getSelectedItem().toString();

        List<PantryItem> filteredItems = new ArrayList<>();

        for (PantryItem item : allPantryItems) {
            if (item == null || item.getName() == null) {
                continue;
            }

            boolean matchesSearch = item.getName()
                    .toLowerCase(Locale.ROOT)
                    .contains(searchText);

            String status = getConfiguredExpiryStatus(
                    item.getExpiryDate()
            );

            boolean matchesFilter;

            switch (selectedFilter) {
                case "Expired":
                    matchesFilter = status.equals("EXPIRED");
                    break;

                case "Expiring Soon":
                    matchesFilter =
                            status.equals("Expiring today")
                                    || status.equals("Expiring soon");
                    break;

                case "Fresh":
                    matchesFilter = status.equals("Fresh");
                    break;

                default:
                    matchesFilter = true;
                    break;
            }

            if (matchesSearch && matchesFilter) {
                filteredItems.add(item);
            }
        }

        pantryAdapter.clear();
        pantryAdapter.addAll(filteredItems);
        pantryAdapter.notifyDataSetChanged();

        if (allPantryItems.isEmpty()) {
            emptyMessage.setText(
                    "Your pantry is empty. Add an ingredient."
            );
            emptyMessage.setVisibility(View.VISIBLE);
        } else if (filteredItems.isEmpty()) {
            emptyMessage.setText(
                    "No ingredients match your search or filter."
            );
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.GONE);
        }
    }

    private void loadExpirySettings() {
        SharedPreferences preferences = getSharedPreferences(
                "smart_pantry_settings",
                MODE_PRIVATE
        );

        expiryWarningsEnabled = preferences.getBoolean(
                "expiry_warnings_enabled",
                true
        );

        expiryWarningDays = preferences.getInt(
                "expiry_warning_days",
                7
        );

        expiryWarningDays = Math.max(
                1,
                Math.min(30, expiryWarningDays)
        );
    }

    private String getConfiguredExpiryStatus(String expiryDate) {
        if (expiryDate == null
                || expiryDate.trim().isEmpty()
                || expiryDate.equalsIgnoreCase("Not specified")) {
            return "Fresh";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.ROOT
        );

        dateFormat.setLenient(false);

        try {
            Date expiry = dateFormat.parse(expiryDate.trim());

            if (expiry == null) {
                return "Fresh";
            }

            Calendar expiryCalendar = Calendar.getInstance();
            expiryCalendar.setTime(expiry);

            Calendar todayCalendar = Calendar.getInstance();

            long daysUntilExpiry =
                    getCalendarDayNumber(expiryCalendar)
                            - getCalendarDayNumber(todayCalendar);

            if (daysUntilExpiry < 0) {
                return "EXPIRED";
            }

            if (!expiryWarningsEnabled) {
                return "Fresh";
            }

            if (daysUntilExpiry == 0) {
                return "Expiring today";
            }

            if (daysUntilExpiry <= expiryWarningDays) {
                return "Expiring soon";
            }

            return "Fresh";

        } catch (ParseException e) {
            return "Fresh";
        }
    }

    private long getCalendarDayNumber(Calendar date) {
        Calendar utcDate = Calendar.getInstance(
                TimeZone.getTimeZone("UTC"),
                Locale.ROOT
        );

        utcDate.clear();

        utcDate.set(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
        );

        return utcDate.getTimeInMillis() / 86400000L;
    }

    private void updatePantryList() {
        allPantryItems.clear();

        allPantryItems.addAll(
                databaseHelper.getAllPantryItems()
        );

        applySearchAndFilter();
    }

    private void showIngredientOptions(PantryItem item) {
        String[] options = {
                "Edit Ingredient",
                "Delete Ingredient"
        };

        new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showIngredientDialog(item);
                    } else {
                        confirmDelete(item);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage(
                        "Are you sure you want to delete "
                                + item.getName() + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = databaseHelper.deletePantryItem(
                            item.getId()
                    );

                    if (result > 0) {
                        updatePantryList();

                        Toast.makeText(
                                this,
                                "Ingredient deleted",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                this,
                                "Could not delete ingredient",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    private void showIngredientDialog(PantryItem existingItem) {
        boolean isEditing = existingItem != null;

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(40, 16, 40, 8);

        EditText nameField = new EditText(this);
        nameField.setHint("Ingredient name");
        nameField.setSingleLine(true);
        form.addView(nameField);

        EditText quantityField = new EditText(this);
        quantityField.setHint("Quantity");
        quantityField.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        quantityField.setSingleLine(true);
        form.addView(quantityField);

        TextView unitLabel = new TextView(this);
        unitLabel.setText("Unit of measurement");
        unitLabel.setPadding(0, 12, 0, 4);
        form.addView(unitLabel);

        Spinner unitSpinner = new Spinner(this);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                metricUnits
        );

        unitAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        unitSpinner.setAdapter(unitAdapter);
        form.addView(unitSpinner);

        EditText expiryField = new EditText(this);
        expiryField.setHint("Expiry date (optional)");
        expiryField.setFocusable(false);
        expiryField.setClickable(true);
        form.addView(expiryField);

        final Calendar selectedExpiryDate = Calendar.getInstance();

        if (isEditing && existingItem.getExpiryDate() != null) {
            String savedDate = existingItem.getExpiryDate().trim();

            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
            dateFormat.setLenient(false);

            try {
                Date parsedDate = dateFormat.parse(savedDate);

                if (parsedDate != null) {
                    selectedExpiryDate.setTime(parsedDate);
                }
            } catch (ParseException ignored) {
                // Use today's date when no valid saved date exists.
            }
        }

        expiryField.setOnClickListener(view -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    PantryActivity.this,
                    (datePickerView, year, month, day) -> {
                        selectedExpiryDate.set(
                                year,
                                month,
                                day
                        );

                        String date = String.format(
                                Locale.ROOT,
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                day
                        );

                        expiryField.setText(date);
                    },
                    selectedExpiryDate.get(Calendar.YEAR),
                    selectedExpiryDate.get(Calendar.MONTH),
                    selectedExpiryDate.get(Calendar.DAY_OF_MONTH)
            );

            datePicker.show();
        });

        if (isEditing) {
            nameField.setText(existingItem.getName());

            quantityField.setText(
                    String.valueOf(existingItem.getQuantity())
            );

            boolean unitFound = false;

            for (int i = 0; i < metricUnits.length; i++) {
                if (metricUnits[i].equalsIgnoreCase(
                        existingItem.getUnit())) {
                    unitSpinner.setSelection(i);
                    unitFound = true;
                    break;
                }
            }

            if (!unitFound && existingItem.getUnit() != null) {
                String[] updatedUnits =
                        new String[metricUnits.length + 1];

                System.arraycopy(
                        metricUnits, 0,
                        updatedUnits, 0,
                        metricUnits.length
                );

                updatedUnits[metricUnits.length] =
                        existingItem.getUnit();

                ArrayAdapter<String> updatedAdapter =
                        new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                updatedUnits
                        );

                updatedAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                );

                unitSpinner.setAdapter(updatedAdapter);
                unitSpinner.setSelection(metricUnits.length);
            }

            String expiry = existingItem.getExpiryDate();

            if (expiry != null
                    && !expiry.equals("Not specified")) {
                expiryField.setText(expiry);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEditing
                        ? "Edit Ingredient" : "Add Ingredient")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {

                            String name = nameField.getText()
                                    .toString().trim();

                            String quantityText = quantityField.getText()
                                    .toString().trim();

                            String unit = unitSpinner.getSelectedItem()
                                    .toString();

                            String expiryDate = expiryField.getText()
                                    .toString().trim();

                            if (name.isEmpty()) {
                                nameField.setError(
                                        "Enter the ingredient name"
                                );
                                nameField.requestFocus();
                                return;
                            }

                            if (quantityText.isEmpty()) {
                                quantityField.setError(
                                        "Enter the quantity"
                                );
                                quantityField.requestFocus();
                                return;
                            }

                            double quantity;

                            try {
                                quantity = Double.parseDouble(
                                        quantityText
                                );
                            } catch (NumberFormatException e) {
                                quantityField.setError(
                                        "Enter a valid quantity"
                                );
                                quantityField.requestFocus();
                                return;
                            }

                            if (quantity <= 0
                                    || Double.isInfinite(quantity)
                                    || Double.isNaN(quantity)) {
                                quantityField.setError(
                                        "Quantity must be greater than zero"
                                );
                                quantityField.requestFocus();
                                return;
                            }

                            if (expiryDate.isEmpty()) {
                                expiryDate = "Not specified";
                            }

                            PantryItem item = new PantryItem(
                                    isEditing
                                            ? existingItem.getId() : 0,
                                    name,
                                    quantity,
                                    unit,
                                    expiryDate
                            );

                            if (isEditing) {
                                int result =
                                        databaseHelper.updatePantryItem(item);

                                if (result > 0) {
                                    updatePantryList();

                                    Toast.makeText(
                                            this,
                                            "Ingredient updated",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(
                                            this,
                                            "Could not update ingredient",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                            } else {
                                long result =
                                        databaseHelper.addPantryItem(item);

                                if (result != -1) {
                                    updatePantryList();

                                    Toast.makeText(
                                            this,
                                            "Ingredient added successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(
                                            this,
                                            "Could not save ingredient",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                        })
        );

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && pantryAdapter != null) {
            loadExpirySettings();
            updatePantryList();
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
