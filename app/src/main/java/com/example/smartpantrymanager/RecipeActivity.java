
package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeActivity extends Activity {

    private DatabaseHelper databaseHelper;

    private ListView recipeListView;
    private TextView messageView;
    private TextView sectionHeading;
    private Button suggestedButton;
    private Button almostThereButton;

    private RecipeAdapter recipeAdapter;

    private final List<RecipeResult> recipeResults =
            new ArrayList<>();

    private final List<RecipeResult> suggestedRecipes =
            new ArrayList<>();

    private final List<RecipeResult> almostThereRecipes =
            new ArrayList<>();

    private boolean showingSuggested = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        AppBackgroundHelper.apply(rootLayout);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 16, 24, 16);

        TextView title = new TextView(this);
        title.setText("Smart Pantry Recipes");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 12);
        layout.addView(title);

        TextView description = new TextView(this);
        description.setText(
                "Discover recipes using ingredients already "
                        + "available in your pantry."
        );
        description.setPadding(0, 0, 0, 12);
        layout.addView(description);

        suggestedButton = new Button(this);
        suggestedButton.setText("Suggested Recipes");
        suggestedButton.setOnClickListener(view -> {
            showingSuggested = true;
            displaySelectedRecipes();
        });
        layout.addView(suggestedButton);

        almostThereButton = new Button(this);
        almostThereButton.setText(
                "Almost There / Missing Ingredients"
        );
        almostThereButton.setOnClickListener(view -> {
            showingSuggested = false;
            displaySelectedRecipes();
        });
        layout.addView(almostThereButton);

        sectionHeading = new TextView(this);
        sectionHeading.setTextSize(20);
        sectionHeading.setGravity(Gravity.CENTER);
        sectionHeading.setPadding(0, 12, 0, 8);
        layout.addView(sectionHeading);

        messageView = new TextView(this);
        messageView.setGravity(Gravity.CENTER);
        messageView.setPadding(8, 8, 8, 12);
        layout.addView(messageView);

        recipeListView = new ListView(this);

        layout.addView(
                recipeListView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        Button shoppingButton = new Button(this);
        shoppingButton.setText("Open Shopping List");
        shoppingButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    RecipeActivity.this,
                    ShoppingActivity.class
            );
            startActivity(intent);
        });
        layout.addView(shoppingButton);

        Button refreshButton = new Button(this);
        refreshButton.setText("Refresh Recipes");
        refreshButton.setOnClickListener(
                view -> loadSuggestedRecipes()
        );
        layout.addView(refreshButton);

        Button backButton = new Button(this);
        backButton.setText("Back to Home");
        backButton.setOnClickListener(view -> finish());
        layout.addView(backButton);

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
                        RecipeActivity.class
                );

        rootLayout.addView(
                bottomNavigation,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(rootLayout);

        recipeAdapter = new RecipeAdapter();
        recipeListView.setAdapter(recipeAdapter);

        recipeListView.setOnItemClickListener(
                (parent, view, position, id) -> {

                    RecipeResult result =
                            recipeAdapter.getItem(position);

                    if (result != null) {
                        openRecipeDetails(result);
                    }
                }
        );

        loadSuggestedRecipes();
    }

    private void loadSuggestedRecipes() {

        List<PantryItem> pantryItems =
                databaseHelper.getAllPantryItems();

        List<Recipe> recipes =
                databaseHelper.getAllRecipes();

        suggestedRecipes.clear();
        almostThereRecipes.clear();

        for (Recipe recipe : recipes) {

            List<Recipe.IngredientRequirement> requirements =
                    recipe.getRequirements();

            if (requirements == null ||
                    requirements.isEmpty()) {
                continue;
            }

            List<String> available = new ArrayList<>();

            List<MissingIngredient> missing =
                    new ArrayList<>();

            boolean validRecipe = true;

            for (Recipe.IngredientRequirement requirement
                    : requirements) {

                if (requirement == null ||
                        requirement.getName() == null ||
                        requirement.getName().trim().isEmpty() ||
                        requirement.getUnit() == null ||
                        requirement.getUnit().trim().isEmpty() ||
                        requirement.getQuantity() <= 0 ||
                        Double.isNaN(
                                requirement.getQuantity()
                        ) ||
                        Double.isInfinite(
                                requirement.getQuantity()
                        )) {

                    validRecipe = false;
                    break;
                }

                double requiredQuantity =
                        requirement.getQuantity();

                String requiredUnit =
                        normalizeUnit(
                                requirement.getUnit()
                        );

                double pantryQuantity =
                        getAvailableQuantity(
                                requirement.getName(),
                                requiredUnit,
                                pantryItems
                        );

                if (pantryQuantity + 0.000001
                        >= requiredQuantity) {

                    available.add(
                            requirement.getName()
                                    + " - "
                                    + formatQuantity(
                                    requiredQuantity
                            )
                                    + " "
                                    + requirement.getUnit()
                    );

                } else {

                    double shortage = Math.max(
                            0,
                            requiredQuantity - pantryQuantity
                    );

                    missing.add(
                            new MissingIngredient(
                                    requirement.getName(),
                                    shortage,
                                    requirement.getUnit()
                            )
                    );
                }
            }

            if (!validRecipe) {
                continue;
            }

            RecipeResult result = new RecipeResult(
                    recipe,
                    available,
                    missing
            );

            if (missing.isEmpty()) {
                suggestedRecipes.add(result);
            } else {
                almostThereRecipes.add(result);
            }
        }

        displaySelectedRecipes();
    }

    private void displaySelectedRecipes() {

        recipeResults.clear();

        if (showingSuggested) {

            recipeResults.addAll(suggestedRecipes);

            sectionHeading.setText(
                    "Suggested Recipes"
            );

            if (suggestedRecipes.isEmpty()) {

                messageView.setText(
                        "No recipes match your pantry yet.\n"
                                + "Add more ingredients or "
                                + "check the Almost There section."
                );

            } else {

                messageView.setText(
                        suggestedRecipes.size()
                                + " recipe(s) ready to make.\n"
                                + "Every required ingredient "
                                + "is available in your pantry."
                );
            }

        } else {

            recipeResults.addAll(almostThereRecipes);

            sectionHeading.setText(
                    "Almost There - Missing Ingredients"
            );

            if (almostThereRecipes.isEmpty()) {

                messageView.setText(
                        "No incomplete recipes found."
                );

            } else {

                messageView.setText(
                        almostThereRecipes.size()
                                + " recipe(s) need additional "
                                + "ingredients or quantities.\n"
                                + "Select a recipe to add "
                                + "missing quantities to "
                                + "your Shopping List."
                );
            }
        }

        suggestedButton.setEnabled(!showingSuggested);
        almostThereButton.setEnabled(showingSuggested);

        if (recipeAdapter != null) {
            recipeAdapter.notifyDataSetChanged();
        }
    }

    private void openRecipeDetails(RecipeResult result) {

        Intent intent = new Intent(
                RecipeActivity.this,
                RecipeDetailActivity.class
        );

        intent.putExtra(
                RecipeDetailActivity.EXTRA_RECIPE_NAME,
                result.recipe.getName()
        );

        ArrayList<String> missingNames =
                new ArrayList<>();

        ArrayList<String> missingUnits =
                new ArrayList<>();

        double[] missingQuantities =
                new double[result.missing.size()];

        for (int i = 0;
             i < result.missing.size();
             i++) {

            MissingIngredient ingredient =
                    result.missing.get(i);

            missingNames.add(ingredient.name);
            missingUnits.add(ingredient.unit);

            missingQuantities[i] =
                    ingredient.quantity;
        }

        intent.putStringArrayListExtra(
                RecipeDetailActivity.EXTRA_MISSING_NAMES,
                missingNames
        );

        intent.putStringArrayListExtra(
                RecipeDetailActivity.EXTRA_MISSING_UNITS,
                missingUnits
        );

        intent.putExtra(
                RecipeDetailActivity.EXTRA_MISSING_QUANTITIES,
                missingQuantities
        );

        startActivity(intent);
    }

    private double getAvailableQuantity(
            String ingredientName,
            String requiredUnit,
            List<PantryItem> pantryItems) {

        double total = 0;

        String targetName =
                normalizeIngredient(ingredientName);

        for (PantryItem item : pantryItems) {

            if (item == null ||
                    item.getName() == null ||
                    item.getUnit() == null ||
                    item.getQuantity() <= 0 ||
                    Double.isNaN(item.getQuantity()) ||
                    Double.isInfinite(item.getQuantity())) {
                continue;
            }

            String pantryName =
                    normalizeIngredient(item.getName());

            if (!ingredientNamesMatch(
                    targetName,
                    pantryName)) {
                continue;
            }

            String pantryUnit =
                    normalizeUnit(item.getUnit());

            if (!areUnitsCompatible(
                    pantryUnit,
                    requiredUnit)) {
                continue;
            }

            total += convertQuantity(
                    item.getQuantity(),
                    pantryUnit,
                    requiredUnit
            );
        }

        return total;
    }

    private boolean ingredientNamesMatch(
            String required,
            String pantry) {

        if (required.isEmpty() ||
                pantry.isEmpty()) {
            return false;
        }

        return required.equals(pantry);
    }

    private String normalizeIngredient(String value) {

        if (value == null) {
            return "";
        }

        String normalized = value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ");

        String[] words = normalized.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {

            if (word.isEmpty()) {
                continue;
            }

            String singular = word;

            if (word.equals("tomatoes")) {
                singular = "tomato";

            } else if (word.equals("potatoes")) {
                singular = "potato";

            } else if (word.length() > 4 &&
                    word.endsWith("ies")) {

                singular =
                        word.substring(
                                0,
                                word.length() - 3
                        ) + "y";

            } else if (word.length() > 3 &&
                    word.endsWith("s") &&
                    !word.endsWith("ss") &&
                    !word.endsWith("us")) {

                singular =
                        word.substring(
                                0,
                                word.length() - 1
                        );
            }

            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(singular);
        }

        return result.toString().trim();
    }

    private String normalizeUnit(String unit) {

        if (unit == null) {
            return "";
        }

        String value =
                unit.trim().toLowerCase(Locale.ROOT);

        switch (value) {

            case "gram":
            case "grams":
            case "gramme":
            case "grammes":
                return "g";

            case "kilogram":
            case "kilograms":
            case "kilogramme":
            case "kilogrammes":
                return "kg";

            case "milligram":
            case "milligrams":
                return "mg";

            case "millilitre":
            case "millilitres":
            case "milliliter":
            case "milliliters":
                return "ml";

            case "litre":
            case "litres":
            case "liter":
            case "liters":
                return "l";

            case "item":
            case "items":
            case "piece":
            case "pieces":
            case "slice":
            case "slices":
            case "clove":
            case "cloves":
                return "count";

            case "packet":
            case "packets":
                return "packet";

            case "can":
            case "cans":
                return "can";

            case "bottle":
            case "bottles":
                return "bottle";

            default:
                return value;
        }
    }

    private boolean areUnitsCompatible(
            String first,
            String second) {

        if (first.isEmpty() ||
                second.isEmpty()) {
            return false;
        }

        if (first.equals(second)) {
            return true;
        }

        return (isMassUnit(first) &&
                isMassUnit(second))
                ||
                (isVolumeUnit(first) &&
                        isVolumeUnit(second));
    }

    private boolean isMassUnit(String unit) {

        return unit.equals("mg") ||
                unit.equals("g") ||
                unit.equals("kg");
    }

    private boolean isVolumeUnit(String unit) {

        return unit.equals("ml") ||
                unit.equals("l");
    }

    private double convertQuantity(
            double quantity,
            String fromUnit,
            String toUnit) {

        if (fromUnit.equals(toUnit)) {
            return quantity;
        }

        if (isMassUnit(fromUnit) &&
                isMassUnit(toUnit)) {

            double grams;

            if (fromUnit.equals("kg")) {
                grams = quantity * 1000.0;

            } else if (fromUnit.equals("mg")) {
                grams = quantity / 1000.0;

            } else {
                grams = quantity;
            }

            if (toUnit.equals("kg")) {
                return grams / 1000.0;
            }

            if (toUnit.equals("mg")) {
                return grams * 1000.0;
            }

            return grams;
        }

        if (isVolumeUnit(fromUnit) &&
                isVolumeUnit(toUnit)) {

            double millilitres =
                    fromUnit.equals("l")
                            ? quantity * 1000.0
                            : quantity;

            return toUnit.equals("l")
                    ? millilitres / 1000.0
                    : millilitres;
        }

        return quantity;
    }

    private String formatQuantity(double quantity) {

        if (Math.abs(
                quantity - Math.round(quantity)
        ) < 0.000001) {

            return String.format(
                    Locale.ROOT,
                    "%.0f",
                    quantity
            );
        }

        return String.format(
                        Locale.ROOT,
                        "%.2f",
                        quantity
                )
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    private static class MissingIngredient {

        final String name;
        final double quantity;
        final String unit;

        MissingIngredient(
                String name,
                double quantity,
                String unit) {

            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    private static class RecipeResult {

        final Recipe recipe;
        final List<String> available;
        final List<MissingIngredient> missing;

        RecipeResult(
                Recipe recipe,
                List<String> available,
                List<MissingIngredient> missing) {

            this.recipe = recipe;
            this.available = available;
            this.missing = missing;
        }
    }

    private class RecipeAdapter
            extends ArrayAdapter<RecipeResult> {

        RecipeAdapter() {

            super(
                    RecipeActivity.this,
                    android.R.layout.simple_list_item_2,
                    android.R.id.text1,
                    recipeResults
            );
        }

        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent) {

            View view = super.getView(
                    position,
                    convertView,
                    parent
            );

            RecipeResult result =
                    getItem(position);

            TextView nameView =
                    view.findViewById(
                            android.R.id.text1
                    );

            TextView detailsView =
                    view.findViewById(
                            android.R.id.text2
                    );

            if (result != null) {

                nameView.setText(
                        result.recipe.getName()
                );

                if (result.missing.isEmpty()) {

                    detailsView.setText(
                            "Ready to make - "
                                    + "all ingredients available"
                    );

                } else {

                    detailsView.setText(
                            result.missing.size()
                                    + " missing ingredient(s) "
                                    + "or quantity shortfall(s)"
                    );
                }
            }

            return view;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null &&
                recipeAdapter != null) {

            loadSuggestedRecipes();
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
