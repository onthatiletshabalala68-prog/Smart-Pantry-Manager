
package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeDetailActivity extends Activity {

    public static final String EXTRA_RECIPE_NAME =
            "recipe_name";

    public static final String EXTRA_MISSING_NAMES =
            "missing_names";

    public static final String EXTRA_MISSING_QUANTITIES =
            "missing_quantities";

    public static final String EXTRA_MISSING_UNITS =
            "missing_units";

    private DatabaseHelper databaseHelper;

    private final List<String> missingNames =
            new ArrayList<>();

    private final List<Double> missingQuantities =
            new ArrayList<>();

    private final List<String> missingUnits =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);

        Intent intent = getIntent();

        String recipeName = intent.getStringExtra(
                EXTRA_RECIPE_NAME
        );

        if (recipeName == null ||
                recipeName.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Recipe information is unavailable",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        Recipe selectedRecipe = null;

        for (Recipe recipe : databaseHelper.getAllRecipes()) {
            if (recipeName.equals(recipe.getName())) {
                selectedRecipe = recipe;
                break;
            }
        }

        if (selectedRecipe == null) {
            Toast.makeText(
                    this,
                    "Recipe could not be found",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        ArrayList<String> names =
                intent.getStringArrayListExtra(
                        EXTRA_MISSING_NAMES
                );

        ArrayList<String> units =
                intent.getStringArrayListExtra(
                        EXTRA_MISSING_UNITS
                );

        double[] quantities =
                intent.getDoubleArrayExtra(
                        EXTRA_MISSING_QUANTITIES
                );

        if (names != null &&
                units != null &&
                quantities != null &&
                names.size() == units.size() &&
                names.size() == quantities.length) {

            for (int i = 0; i < names.size(); i++) {
                missingNames.add(names.get(i));
                missingUnits.add(units.get(i));
                missingQuantities.add(quantities[i]);
            }
        }

        buildScreen(selectedRecipe);
    }

    private void buildScreen(Recipe recipe) {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(36, 28, 36, 36);

        TextView title = new TextView(this);
        title.setText(recipe.getName());
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);

        TextView status = new TextView(this);
        status.setTextSize(17);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, 20);

        if (missingNames.isEmpty()) {
            status.setText(
                    "Ready to Make - All Ingredients Available"
            );
        } else {
            status.setText(
                    "Almost There - "
                            + missingNames.size()
                            + " Ingredient Shortfall(s)"
            );
        }

        layout.addView(status);

        addHeading(layout, "Required Ingredients");

        for (Recipe.IngredientRequirement requirement
                : recipe.getRequirements()) {

            addParagraph(
                    layout,
                    "\u2022 "
                            + requirement.getName()
                            + " - "
                            + formatQuantity(
                            requirement.getQuantity()
                    )
                            + " "
                            + requirement.getUnit()
            );
        }

        if (!missingNames.isEmpty()) {

            addHeading(
                    layout,
                    "Missing Ingredients or Quantities"
            );

            for (int i = 0; i < missingNames.size(); i++) {

                addParagraph(
                        layout,
                        "\u2022 "
                                + missingNames.get(i)
                                + " - "
                                + formatQuantity(
                                missingQuantities.get(i)
                        )
                                + " "
                                + missingUnits.get(i)
                );
            }
        }

        addHeading(layout, "Preparation Method");

        addParagraph(
                layout,
                recipe.getInstructions() == null
                        ? "No preparation instructions available."
                        : recipe.getInstructions()
        );

        if (!missingNames.isEmpty()) {

            Button addShoppingButton =
                    new Button(this);

            addShoppingButton.setText(
                    "Add Missing Ingredients to Shopping List"
            );

            addShoppingButton.setOnClickListener(
                    view -> addMissingIngredients()
            );

            layout.addView(addShoppingButton);
        }

        Button shoppingButton = new Button(this);
        shoppingButton.setText("Open Shopping List");

        shoppingButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    this,
                    ShoppingActivity.class
            );
            startActivity(intent);
        });

        layout.addView(shoppingButton);

        Button backButton = new Button(this);
        backButton.setText("Back to Recipes");
        backButton.setOnClickListener(view -> finish());

        layout.addView(backButton);

        scrollView.addView(layout);
        setContentView(scrollView);
    }

    private void addHeading(
            LinearLayout layout,
            String heading) {

        TextView headingView = new TextView(this);
        headingView.setText(heading);
        headingView.setTextSize(20);
        headingView.setPadding(0, 18, 0, 12);

        layout.addView(headingView);
    }

    private void addParagraph(
            LinearLayout layout,
            String content) {

        TextView paragraph = new TextView(this);
        paragraph.setText(content);
        paragraph.setTextSize(16);
        paragraph.setPadding(0, 0, 0, 12);

        layout.addView(paragraph);
    }

    private void addMissingIngredients() {

        int added = 0;
        int failed = 0;

        for (int i = 0; i < missingNames.size(); i++) {

            double quantity = missingQuantities.get(i);

            if (quantity <= 0 ||
                    Double.isNaN(quantity) ||
                    Double.isInfinite(quantity)) {
                continue;
            }

            long result = databaseHelper.addShoppingItem(
                    missingNames.get(i),
                    quantity,
                    missingUnits.get(i)
            );

            if (result != -1) {
                added++;
            } else {
                failed++;
            }
        }

        String message = added
                + " ingredient(s) added to Shopping List.";

        if (failed > 0) {
            message += " "
                    + failed
                    + " could not be added.";
        }

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
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

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
