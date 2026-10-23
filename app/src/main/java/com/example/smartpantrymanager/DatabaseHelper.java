
package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_PANTRY = "pantry_items";
    private static final String TABLE_SHOPPING = "shopping_items";
    private static final String TABLE_RECIPES = "recipes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_PANTRY + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "quantity REAL NOT NULL, " +
                        "unit TEXT, " +
                        "expiry_date TEXT" +
                        ")"
        );

        createShoppingTable(db);
        createRecipesTable(db);
        seedRecipes(db);
    }

    private void createShoppingTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_SHOPPING + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "is_purchased INTEGER NOT NULL DEFAULT 0, " +
                        "quantity REAL NOT NULL DEFAULT 1, " +
                        "unit TEXT NOT NULL DEFAULT 'item'" +
                        ")"
        );
    }

    private void createRecipesTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "ingredients TEXT NOT NULL, " +
                        "instructions TEXT NOT NULL" +
                        ")"
        );
    }

    private void seedRecipes(SQLiteDatabase db) {
        List<Recipe> recipes = Recipe.getSampleRecipes();

        db.beginTransaction();

        try {
            for (Recipe recipe : recipes) {
                ContentValues values = new ContentValues();

                values.put("name", recipe.getName());
                values.put(
                        "ingredients",
                        serializeRequirements(recipe.getRequirements())
                );
                values.put("instructions", recipe.getInstructions());

                db.insertWithOnConflict(
                        TABLE_RECIPES,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE
                );
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private String serializeRequirements(
            List<Recipe.IngredientRequirement> requirements) {

        JSONArray array = new JSONArray();

        for (Recipe.IngredientRequirement requirement : requirements) {
            JSONObject ingredient = new JSONObject();

            try {
                ingredient.put("name", requirement.getName());
                ingredient.put("quantity", requirement.getQuantity());
                ingredient.put("unit", requirement.getUnit());

                array.put(ingredient);
            } catch (JSONException e) {
                throw new IllegalStateException(
                        "Could not save recipe ingredients.", e
                );
            }
        }

        return array.toString();
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        if (oldVersion < 2) {
            createShoppingTable(db);
        }

        if (oldVersion < 3) {
            createRecipesTable(db);
            seedRecipes(db);
        }

        if (oldVersion < 4) {
            createRecipesTable(db);
            db.delete(TABLE_RECIPES, null, null);
            seedRecipes(db);
        }

        if (oldVersion < 5) {
            if (!columnExists(db, TABLE_SHOPPING, "quantity")) {
                db.execSQL(
                        "ALTER TABLE " + TABLE_SHOPPING +
                                " ADD COLUMN quantity REAL NOT NULL DEFAULT 1"
                );
            }

            if (!columnExists(db, TABLE_SHOPPING, "unit")) {
                db.execSQL(
                        "ALTER TABLE " + TABLE_SHOPPING +
                                " ADD COLUMN unit TEXT NOT NULL DEFAULT 'item'"
                );
            }
        }
    }

    private boolean columnExists(
            SQLiteDatabase db,
            String tableName,
            String columnName) {

        try (Cursor cursor = db.rawQuery(
                "PRAGMA table_info(" + tableName + ")",
                null)) {

            while (cursor.moveToNext()) {
                if (columnName.equals(cursor.getString(1))) {
                    return true;
                }
            }
        }

        return false;
    }

    // PANTRY METHODS

    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry_date", item.getExpiryDate());

        return db.insert(TABLE_PANTRY, null, values);
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT id, name, quantity, unit, expiry_date " +
                        "FROM " + TABLE_PANTRY + " ORDER BY id DESC",
                null)) {

            while (cursor.moveToNext()) {
                items.add(new PantryItem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getString(4)
                ));
            }
        }

        return items;
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry_date", item.getExpiryDate());

        return db.update(
                TABLE_PANTRY,
                values,
                "id = ?",
                new String[]{String.valueOf(item.getId())}
        );
    }

    public int deletePantryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_PANTRY,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // SHOPPING-LIST METHODS

    public long addShoppingItem(String name) {
        return addShoppingItem(name, 1.0, "item");
    }

    public long addShoppingItem(
            String name,
            double quantity,
            String unit) {

        if (name == null || name.trim().isEmpty()
                || quantity <= 0
                || Double.isNaN(quantity)
                || Double.isInfinite(quantity)) {
            return -1;
        }

        String cleanedName = name.trim().replaceAll("\\s+", " ");
        String cleanedUnit = normalizeShoppingUnit(unit);

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {
            int existingId = -1;
            double existingQuantity = 0;
            double convertedQuantity = 0;

            try (Cursor cursor = db.rawQuery(
                    "SELECT id, name, quantity, unit FROM " +
                            TABLE_SHOPPING +
                            " WHERE is_purchased = 0" +
                            " ORDER BY id ASC",
                    null)) {

                while (cursor.moveToNext()) {
                    String existingName = cursor.getString(1);

                    if (!normalizeShoppingName(existingName)
                            .equals(normalizeShoppingName(cleanedName))) {
                        continue;
                    }

                    String existingUnit =
                            normalizeShoppingUnit(cursor.getString(3));

                    double converted = convertShoppingQuantity(
                            quantity,
                            cleanedUnit,
                            existingUnit
                    );

                    if (!Double.isNaN(converted)
                            && !Double.isInfinite(converted)) {
                        existingId = cursor.getInt(0);
                        existingQuantity = cursor.getDouble(2);
                        convertedQuantity = converted;
                        break;
                    }
                }
            }

            if (existingId != -1) {
                double totalQuantity =
                        existingQuantity + convertedQuantity;

                if (Double.isInfinite(totalQuantity)
                        || Double.isNaN(totalQuantity)) {
                    return -1;
                }

                ContentValues values = new ContentValues();
                values.put("quantity", totalQuantity);

                int updated = db.update(
                        TABLE_SHOPPING,
                        values,
                        "id = ?",
                        new String[]{String.valueOf(existingId)}
                );

                if (updated > 0) {
                    db.setTransactionSuccessful();
                    return existingId;
                }

                return -1;
            }

            ContentValues values = new ContentValues();
            values.put("name", cleanedName);
            values.put("quantity", quantity);
            values.put("unit", cleanedUnit);
            values.put("is_purchased", 0);

            long newId = db.insert(
                    TABLE_SHOPPING,
                    null,
                    values
            );

            if (newId != -1) {
                db.setTransactionSuccessful();
            }

            return newId;

        } finally {
            db.endTransaction();
        }
    }

    private String normalizeShoppingName(String name) {
        if (name == null) {
            return "";
        }

        return name.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeShoppingUnit(String unit) {

        if (unit == null || unit.trim().isEmpty()) {
            return "item";
        }

        String normalized = unit.trim()
                .toLowerCase(Locale.ROOT);

        switch (normalized) {
            case "milligram":
            case "milligrams":
                return "mg";

            case "gram":
            case "grams":
                return "g";

            case "kilogram":
            case "kilograms":
                return "kg";

            case "milliliter":
            case "milliliters":
            case "millilitre":
            case "millilitres":
            case "ml":
                return "mL";

            case "liter":
            case "liters":
            case "litre":
            case "litres":
                return "L";

            case "pieces":
                return "piece";

            case "items":
                return "item";

            default:
                return normalized;
        }
    }

    private double convertShoppingQuantity(
            double quantity,
            String fromUnit,
            String toUnit) {

        if (fromUnit.equalsIgnoreCase(toUnit)) {
            return quantity;
        }

        double fromMass = getMassFactor(fromUnit);
        double toMass = getMassFactor(toUnit);

        if (fromMass > 0 && toMass > 0) {
            return quantity * fromMass / toMass;
        }

        double fromVolume = getVolumeFactor(fromUnit);
        double toVolume = getVolumeFactor(toUnit);

        if (fromVolume > 0 && toVolume > 0) {
            return quantity * fromVolume / toVolume;
        }

        return Double.NaN;
    }

    private double getMassFactor(String unit) {

        switch (unit.toLowerCase(Locale.ROOT)) {
            case "mg":
                return 0.001;

            case "g":
                return 1.0;

            case "kg":
                return 1000.0;

            default:
                return -1.0;
        }
    }

    private double getVolumeFactor(String unit) {

        switch (unit.toLowerCase(Locale.ROOT)) {
            case "ml":
                return 1.0;

            case "l":
                return 1000.0;

            default:
                return -1.0;
        }
    }

    public List<ShoppingItem> getAllShoppingItems() {
        List<ShoppingItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT id, name, quantity, unit, is_purchased " +
                        "FROM " + TABLE_SHOPPING + " ORDER BY id DESC",
                null)) {

            while (cursor.moveToNext()) {
                items.add(new ShoppingItem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getInt(4) == 1
                ));
            }
        }

        return items;
    }

    public int updateShoppingItemPurchased(
            int id,
            boolean purchased) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("is_purchased", purchased ? 1 : 0);

        return db.update(
                TABLE_SHOPPING,
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public int deleteShoppingItem(int id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_SHOPPING,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // RECIPE METHODS

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT name, ingredients, instructions FROM " +
                        TABLE_RECIPES + " ORDER BY id ASC",
                null)) {

            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String ingredientsJson = cursor.getString(1);
                String instructions = cursor.getString(2);

                try {
                    JSONArray array = new JSONArray(ingredientsJson);

                    List<Recipe.IngredientRequirement> requirements =
                            new ArrayList<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject ingredient = array.getJSONObject(i);

                        String ingredientName =
                                ingredient.getString("name");

                        double quantity =
                                ingredient.getDouble("quantity");

                        String unit = ingredient.getString("unit");

                        requirements.add(
                                new Recipe.IngredientRequirement(
                                        ingredientName,
                                        quantity,
                                        unit
                                )
                        );
                    }

                    recipes.add(new Recipe(
                            name,
                            requirements,
                            instructions,
                            true
                    ));

                } catch (JSONException e) {
                    android.util.Log.e(
                            "DatabaseHelper",
                            "Could not read recipe: " + name,
                            e
                    );
                }
            }
        }

        return recipes;
    }
}
