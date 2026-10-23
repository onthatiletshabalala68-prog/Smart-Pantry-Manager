# Smart Pantry Manager – SQLite Database Design

## 1. Introduction

Smart Pantry Manager uses SQLite to provide persistent local storage for pantry ingredients, recipes, and shopping list items.

The database is implemented through the `DatabaseHelper.java` class, which extends Android's `SQLiteOpenHelper`.

This document describes the actual database schema, operations, version upgrades, and data management techniques implemented in the application.

## 2. Database Configuration

| Property | Implementation |
|---|---|
| Database name | `smart_pantry.db` |
| Database version | `5` |
| Database helper class | `DatabaseHelper.java` |
| Base class | `SQLiteOpenHelper` |
| Database engine | SQLite |
| Programming language | Java |
| Number of tables | 3 |

The database is created through the `onCreate()` method and upgraded through the `onUpgrade()` method.

## 3. Database Tables

### 3.1 Pantry Items Table

**Table name:** `pantry_items`

This table stores ingredients currently available in the user's pantry.

| Column | SQLite Data Type | Constraint | Description |
|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique pantry item identifier |
| `name` | TEXT | NOT NULL | Ingredient name |
| `quantity` | REAL | NOT NULL | Available ingredient quantity |
| `unit` | TEXT | Nullable | Measurement unit |
| `expiry_date` | TEXT | Nullable | Ingredient expiry date |

**SQL table definition:**

```sql
CREATE TABLE IF NOT EXISTS pantry_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    quantity REAL NOT NULL,
    unit TEXT,
    expiry_date TEXT
);
```

The `id` column uniquely identifies each pantry ingredient. The `quantity` column uses the REAL data type to support fractional quantities.

The `expiry_date` column is stored as TEXT, allowing the application to retain a date value when one is provided.

### 3.2 Shopping Items Table

**Table name:** `shopping_items`

This table stores items that the user intends to purchase or has already purchased.

| Column | SQLite Data Type | Constraint | Description |
|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique shopping item identifier |
| `name` | TEXT | NOT NULL | Shopping ingredient name |
| `is_purchased` | INTEGER | NOT NULL DEFAULT 0 | Purchase status |
| `quantity` | REAL | NOT NULL DEFAULT 1 | Required quantity |
| `unit` | TEXT | NOT NULL DEFAULT 'item' | Measurement unit |

**SQL table definition:**

```sql
CREATE TABLE IF NOT EXISTS shopping_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    is_purchased INTEGER NOT NULL DEFAULT 0,
    quantity REAL NOT NULL DEFAULT 1,
    unit TEXT NOT NULL DEFAULT 'item'
);
```

SQLite represents the purchase status using an integer.

- `0` indicates that the item has not been purchased.
- `1` indicates that the item has been purchased.

The default quantity is 1 and the default measurement unit is `item`.

### 3.3 Recipes Table

**Table name:** `recipes`

This table stores recipe information and the ingredients required to prepare each recipe.

| Column | SQLite Data Type | Constraint | Description |
|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique recipe identifier |
| `name` | TEXT | NOT NULL UNIQUE | Recipe name |
| `ingredients` | TEXT | NOT NULL | Ingredient requirements stored as JSON |
| `instructions` | TEXT | NOT NULL | Recipe preparation instructions |

**SQL table definition:**

```sql
CREATE TABLE IF NOT EXISTS recipes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    ingredients TEXT NOT NULL,
    instructions TEXT NOT NULL
);
```

The UNIQUE constraint prevents multiple recipe records from having the same name.

## 4. Recipe Ingredient Storage

Smart Pantry Manager stores recipe ingredient requirements in JSON format inside the `ingredients` column.

Each ingredient requirement contains:

- `name`: The ingredient name.
- `quantity`: The required amount.
- `unit`: The measurement unit.

An illustrative JSON value is:

```json
[
  {
    "name": "Flour",
    "quantity": 250,
    "unit": "g"
  },
  {
    "name": "Milk",
    "quantity": 200,
    "unit": "mL"
  }
]
```

This is an example of the supported JSON structure, not a claim about a particular recipe stored in the application.

The `serializeRequirements()` method converts ingredient requirements into a JSON array using `JSONArray` and `JSONObject`.

The `getAllRecipes()` method reads the JSON data and reconstructs `Recipe.IngredientRequirement` objects.

If a recipe contains malformed JSON, the method logs the error and skips that recipe rather than adding an incomplete recipe object.

## 5. Database Relationships

The application uses three separate SQLite tables.

No explicit foreign key relationships are defined between `pantry_items`, `shopping_items`, and `recipes`.

The connection between these data categories is managed through Java application logic.

For example, the recipe recommendation functionality compares recipe ingredient requirements with pantry inventory data.

The shopping list functionality allows ingredients required for recipes to be recorded for purchasing.

The absence of foreign keys means these interactions are not enforced as relational database constraints.

## 6. Database Creation

When the database is first created, `onCreate()` performs the following operations:

1. Creates the `pantry_items` table.
2. Creates the `shopping_items` table.
3. Creates the `recipes` table.
4. Inserts the application's predefined sample recipes.

The `seedRecipes()` method obtains recipes from `Recipe.getSampleRecipes()`.

Recipe insertion uses `SQLiteDatabase.CONFLICT_IGNORE` to avoid replacing records when a recipe name conflicts with an existing UNIQUE value.

The seeding process is executed inside a database transaction.

## 7. Database Version Management

The application currently uses database version 5.

The `onUpgrade()` method contains conditional migration logic for older database versions.

| Upgrade condition | Operation |
|---|---|
| `oldVersion < 2` | Creates the shopping table |
| `oldVersion < 3` | Creates the recipes table and seeds recipes |
| `oldVersion < 4` | Recreates the recipes table if necessary, deletes existing recipe records, and reseeds recipes |
| `oldVersion < 5` | Adds missing `quantity` and `unit` columns to the shopping table |

The `columnExists()` method uses SQLite's `PRAGMA table_info()` command to determine whether a column already exists.

This helps avoid attempting to add a column that is already present.

The version 4 migration replaces recipe records, whereas the version 5 migration updates the shopping table structure.

## 8. Pantry Database Operations

The following methods manage pantry inventory:

| Method | Purpose |
|---|---|
| `addPantryItem()` | Inserts a pantry ingredient |
| `getAllPantryItems()` | Retrieves pantry ingredients |
| `updatePantryItem()` | Updates an existing ingredient |
| `deletePantryItem()` | Deletes an ingredient by ID |

The retrieval method orders pantry ingredients by descending ID, displaying more recently inserted records first.

Update and delete operations use parameterized ID conditions to identify the relevant record.

## 9. Shopping List Database Operations

The following methods manage shopping list information:

| Method | Purpose |
|---|---|
| `addShoppingItem(String name)` | Adds an item with default quantity and unit |
| `addShoppingItem(String name, double quantity, String unit)` | Adds or merges a shopping item |
| `getAllShoppingItems()` | Retrieves shopping list items |
| `updateShoppingItemPurchased()` | Updates an item's purchase status |
| `deleteShoppingItem()` | Deletes a shopping item |

### 9.1 Shopping Item Validation

Before inserting a shopping item, the application rejects:

- Null or empty ingredient names.
- Quantities less than or equal to zero.
- `NaN` quantities.
- Infinite quantities.

Ingredient names are trimmed and repeated spaces are normalized.

### 9.2 Duplicate Ingredient Handling

When a new shopping item is added, the application checks existing unpurchased items.

If an existing item has the same normalized ingredient name and a compatible measurement unit, the quantities are combined.

If no compatible item is found, a new shopping item is inserted.

This operation uses a transaction to help maintain database consistency.

### 9.3 Measurement Unit Conversion

The database helper supports compatible measurement conversions.

**Mass units:**

- Milligrams (`mg`)
- Grams (`g`)
- Kilograms (`kg`)

**Volume units:**

- Millilitres (`mL`)
- Litres (`L`)

For example, adding 500 g to an existing shopping item containing 1 kg of the same ingredient can produce a combined quantity of 1.5 kg.

Mass and volume measurements are not treated as interchangeable.

## 10. Recipe Retrieval

The `getAllRecipes()` method retrieves recipe names, JSON ingredient requirements, and instructions from the `recipes` table.

Recipes are ordered by ascending ID.

The method converts the stored JSON into Java objects and returns a list of recipes for use by the application.

## 11. Data Integrity and Reliability

The database implementation uses several techniques to support reliable operations.

**Primary keys:** Each table has an automatically incremented unique identifier.

**NOT NULL constraints:** Required database fields cannot contain SQL NULL values.

**UNIQUE constraint:** Recipe names must be unique.

**Transactions:** Recipe seeding and shopping item merging use transactions.

**Parameterized conditions:** Pantry and shopping updates and deletions identify records using parameterized ID values.

**Cursor management:** Database query cursors are managed using Java try-with-resources statements.

**Input validation:** Shopping item insertion validates names and quantities.

**Upgrade checks:** Shopping table migrations check for existing columns before adding new ones.

## 12. Database Testing

The following application workflows were previously tested manually:

| Test | Result |
|---|---|
| Insert pantry ingredients | PASS |
| Retrieve pantry ingredients | PASS |
| Update pantry ingredients | PASS |
| Delete pantry ingredients | PASS |
| Retrieve recipes | PASS |
| Add ingredients to the shopping list | PASS |
| Transfer purchased ingredients to pantry | PASS |
| Retain stored data after reopening the application | PASS |

These results represent the previously documented functional tests, not additional automated database tests.

## 13. Future Improvements

Possible future database improvements include:

- Introducing normalized recipe and ingredient tables.
- Defining foreign key relationships where appropriate.
- Adding database-level CHECK constraints for positive quantities.
- Adding automated database migration tests.
- Implementing backup and restore functionality.
- Supporting optional cloud synchronization.

## 14. Conclusion

Smart Pantry Manager uses a three-table SQLite database managed by `DatabaseHelper.java`.

The implementation supports pantry CRUD operations, recipe storage using JSON ingredient requirements, shopping list management, measurement conversions, and database version upgrades.

This database structure provides the persistent storage needed for the application's core features while leaving opportunities for future improvements.