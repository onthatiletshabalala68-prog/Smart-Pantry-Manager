
package com.example.smartpantrymanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class ShoppingActivity extends Activity {

    private DatabaseHelper databaseHelper;
    private LinearLayout shoppingListLayout;
    private EditText itemNameField;
    private EditText quantityField;
    private Spinner unitSpinner;

    private final String[] units = {
            "item", "piece", "g", "kg", "mg",
            "mL", "L", "clove", "slice",
            "packets", "cans", "bottles"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(24, 16, 24, 16);

        TextView title = new TextView(this);
        title.setText("Shopping List");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 16);
        mainLayout.addView(title);

        itemNameField = new EditText(this);
        itemNameField.setHint("Enter ingredient name");
        itemNameField.setSingleLine(true);
        mainLayout.addView(itemNameField);

        quantityField = new EditText(this);
        quantityField.setHint("Enter quantity (e.g. 2)");
        quantityField.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        quantityField.setSingleLine(true);
        mainLayout.addView(quantityField);

        TextView unitLabel = new TextView(this);
        unitLabel.setText("Unit");
        unitLabel.setTextSize(16);
        unitLabel.setPadding(0, 8, 0, 4);
        mainLayout.addView(unitLabel);

        unitSpinner = new Spinner(this);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );

        unitAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        unitSpinner.setAdapter(unitAdapter);
        mainLayout.addView(unitSpinner);

        Button addButton = new Button(this);
        addButton.setText("Add Item");
        addButton.setOnClickListener(view -> addShoppingItem());
        mainLayout.addView(addButton);

        TextView instructions = new TextView(this);
        instructions.setText(
                "Tick an item when purchased to add its quantity to My Pantry."
        );
        instructions.setPadding(0, 12, 0, 12);
        mainLayout.addView(instructions);

        ScrollView scrollView = new ScrollView(this);

        shoppingListLayout = new LinearLayout(this);
        shoppingListLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(shoppingListLayout);

        mainLayout.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        Button backButton = new Button(this);
        backButton.setText("Back");
        backButton.setOnClickListener(view -> finish());
        mainLayout.addView(backButton);

        rootLayout.addView(
                mainLayout,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        LinearLayout bottomNavigation =
                BottomNavigationHelper.create(
                        this,
                        ShoppingActivity.class
                );

        rootLayout.addView(
                bottomNavigation,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(rootLayout);

        displayShoppingItems();
    }

    private void addShoppingItem() {
        String name = itemNameField.getText().toString().trim();
        String quantityText = quantityField.getText().toString().trim();
        String unit = unitSpinner.getSelectedItem().toString();

        if (name.isEmpty()) {
            itemNameField.setError("Enter an ingredient name");
            return;
        }

        if (quantityText.isEmpty()) {
            quantityField.setError("Enter a quantity");
            return;
        }

        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            quantityField.setError("Enter a valid quantity");
            return;
        }

        if (Double.isNaN(quantity)
                || Double.isInfinite(quantity)
                || quantity <= 0) {
            quantityField.setError("Quantity must be greater than zero");
            return;
        }

        long result = databaseHelper.addShoppingItem(
                name, quantity, unit
        );

        if (result != -1) {
            itemNameField.setText("");
            quantityField.setText("");
            unitSpinner.setSelection(0);

            displayShoppingItems();

            Toast.makeText(
                    this,
                    "Added " + quantity + " " + unit + " of " + name,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    "Could not add item",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void displayShoppingItems() {
        shoppingListLayout.removeAllViews();

        List<ShoppingItem> items =
                databaseHelper.getAllShoppingItems();

        if (items.isEmpty()) {
            TextView emptyMessage = new TextView(this);
            emptyMessage.setText("Your shopping list is empty.");
            emptyMessage.setPadding(8, 16, 8, 16);
            shoppingListLayout.addView(emptyMessage);
            return;
        }

        for (ShoppingItem item : items) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 8, 0, 8);

            CheckBox purchasedCheckBox = new CheckBox(this);
            purchasedCheckBox.setText(
                    item.getName()
                            + " — "
                            + formatQuantity(item.getQuantity())
                            + " "
                            + item.getUnit()
            );
            purchasedCheckBox.setTextSize(16);
            purchasedCheckBox.setChecked(item.isPurchased());
            purchasedCheckBox.setEnabled(!item.isPurchased());

            row.addView(purchasedCheckBox);

            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");
            row.addView(deleteButton);

            purchasedCheckBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (!isChecked) {
                            return;
                        }

                        int updated =
                                databaseHelper.updateShoppingItemPurchased(
                                        item.getId(), true
                                );

                        if (updated > 0) {
                            boolean added = addPurchasedItemToPantry(item);

                            displayShoppingItems();

                            Toast.makeText(
                                    ShoppingActivity.this,
                                    added
                                            ? "Purchased quantity added to My Pantry."
                                            : "Could not add the quantity to My Pantry.",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            Toast.makeText(
                                    ShoppingActivity.this,
                                    "Could not update purchased status.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            displayShoppingItems();
                        }
                    }
            );

            deleteButton.setOnClickListener(view ->
                    new AlertDialog.Builder(ShoppingActivity.this)
                            .setTitle("Delete item")
                            .setMessage(
                                    "Remove " + item.getName()
                                            + " from your Shopping List?"
                            )
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (dialog, which) -> {
                                databaseHelper.deleteShoppingItem(
                                        item.getId()
                                );
                                displayShoppingItems();
                            })
                            .show()
            );

            shoppingListLayout.addView(row);

            View divider = new View(this);
            divider.setBackgroundColor(0xFFDDDDDD);

            shoppingListLayout.addView(
                    divider,
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1
                    )
            );
        }
    }

    private boolean addPurchasedItemToPantry(ShoppingItem item) {
        List<PantryItem> pantryItems =
                databaseHelper.getAllPantryItems();

        String targetName = normalizeName(item.getName());
        String purchasedUnit = normalizeUnit(item.getUnit());

        for (PantryItem pantryItem : pantryItems) {
            if (!normalizeName(pantryItem.getName()).equals(targetName)) {
                continue;
            }

            String pantryUnit = normalizeUnit(pantryItem.getUnit());

            if (!areCompatibleUnits(purchasedUnit, pantryUnit)) {
                continue;
            }

            double quantityToAdd = convertQuantity(
                    item.getQuantity(),
                    purchasedUnit,
                    pantryUnit
            );

            double newQuantity =
                    pantryItem.getQuantity() + quantityToAdd;

            PantryItem updatedItem = new PantryItem(
                    pantryItem.getId(),
                    pantryItem.getName(),
                    newQuantity,
                    pantryItem.getUnit(),
                    pantryItem.getExpiryDate()
            );

            return databaseHelper.updatePantryItem(updatedItem) > 0;
        }

        PantryItem newItem = new PantryItem(
                0,
                item.getName(),
                item.getQuantity(),
                item.getUnit(),
                ""
        );

        return databaseHelper.addPantryItem(newItem) != -1;
    }

    private String normalizeName(String name) {
        return name == null
                ? ""
                : name.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeUnit(String unit) {
        if (unit == null) {
            return "";
        }

        String value = unit.trim().toLowerCase(Locale.ROOT);

        if (value.equals("piece")) {
            return "item";
        }

        return value;
    }

    private boolean areCompatibleUnits(String first, String second) {
        if (first.equals(second)) {
            return true;
        }

        return (isMassUnit(first) && isMassUnit(second))
                || (isVolumeUnit(first) && isVolumeUnit(second));
    }

    private boolean isMassUnit(String unit) {
        return unit.equals("mg")
                || unit.equals("g")
                || unit.equals("kg");
    }

    private boolean isVolumeUnit(String unit) {
        return unit.equals("ml") || unit.equals("l");
    }

    private double convertQuantity(
            double quantity,
            String fromUnit,
            String toUnit
    ) {
        if (fromUnit.equals(toUnit)) {
            return quantity;
        }

        double baseQuantity;

        if (isMassUnit(fromUnit)) {
            if (fromUnit.equals("kg")) {
                baseQuantity = quantity * 1_000_000;
            } else if (fromUnit.equals("g")) {
                baseQuantity = quantity * 1_000;
            } else {
                baseQuantity = quantity;
            }

            if (toUnit.equals("kg")) {
                return baseQuantity / 1_000_000;
            } else if (toUnit.equals("g")) {
                return baseQuantity / 1_000;
            }

            return baseQuantity;
        }

        if (isVolumeUnit(fromUnit)) {
            baseQuantity = fromUnit.equals("l")
                    ? quantity * 1_000
                    : quantity;

            return toUnit.equals("l")
                    ? baseQuantity / 1_000
                    : baseQuantity;
        }

        return quantity;
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.format(Locale.ROOT, "%.0f", quantity);
        }

        return String.format(Locale.ROOT, "%s", quantity);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && shoppingListLayout != null) {
            displayShoppingItems();
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
