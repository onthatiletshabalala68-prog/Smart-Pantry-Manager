# Smart Pantry Manager – Database Design

## 1. Introduction

Smart Pantry Manager uses SQLite as its local database management system. SQLite provides persistent storage for pantry ingredients, recipe information, and shopping list items without requiring an external database server.

The database supports the application's core operations, including ingredient management, recipe recommendations, and shopping list processing.

## 2. Database Implementation

The application's database operations are managed through the `DatabaseHelper.java` class.

This class provides methods for accessing and modifying stored application information.

SQLite is suitable for this project because it is lightweight, integrated into Android, and supports structured data storage.

## 3. Pantry Ingredient Data

The application stores information about ingredients available in the user's pantry.

The `PantryItem.java` model represents an individual pantry ingredient.

Its main properties include:

| Property | Description |
|---|---|
| ID | Unique identifier for the pantry item |
| Name | Name of the ingredient |
| Quantity | Amount currently available |
| Unit | Measurement unit |
| Expiry Date | Recorded expiry date |

### Pantry Operations

The application supports:

- Adding new pantry ingredients.
- Retrieving stored pantry ingredients.
- Updating ingredient information.
- Deleting pantry ingredients.
- Searching and filtering displayed ingredients.

These operations allow users to maintain an accurate record of their pantry inventory.

## 4. Recipe Data

The `Recipe.java` model represents a recipe and its ingredient requirements.

Recipe requirements include ingredient names, required quantities, and measurement units.

The application retrieves recipe information through `DatabaseHelper.java` and compares the requirements against available pantry ingredients.

Recipes are categorized as Suggested Recipes or Almost There based on ingredient availability.

## 5. Shopping List Data

The `ShoppingItem.java` model represents items managed through the shopping list.

Shopping list functionality supports recording required ingredients, managing purchased items, and transferring purchased ingredients into the pantry.

This creates a connection between shopping activities and pantry inventory management.

## 6. Data Persistence

SQLite stores application data locally on the Android device.

The application was manually tested to confirm that saved information remains available after closing and reopening the application.

This verifies persistence for the tested workflows.

## 7. Data Validation

The application includes validation mechanisms for ingredient information.

Examples include:

- Checking that ingredient names are provided.
- Validating numerical quantities.
- Rejecting quantities that are zero or negative.
- Using supported measurement units.
- Handling optional expiry dates.

These checks help prevent invalid pantry information from being saved.

## 8. Database Access and Application Flow

A typical ingredient insertion follows this process:

1. The user enters ingredient information.
2. `PantryActivity.java` validates the information.
3. The application passes the ingredient to `DatabaseHelper.java`.
4. DatabaseHelper performs the SQLite operation.
5. The pantry display refreshes to show the updated information.

Recipe recommendations retrieve stored information and use application logic to compare ingredient requirements against pantry quantities.

## 9. Database Testing

The following database-related operations were successfully tested:

| Operation | Result |
|---|---|
| Add pantry ingredient | PASS |
| Retrieve and display pantry ingredients | PASS |
| Update pantry ingredient | PASS |
| Delete pantry ingredient | PASS |
| Retrieve recipes for ingredient matching | PASS |
| Add missing ingredients to shopping list | PASS |
| Transfer purchased items into pantry | PASS |
| Preserve data after restarting application | PASS |

These results are based on manual application testing.

## 10. Future Database Improvements

Potential improvements include:

- Cloud database synchronization.
- Backup and restore functionality.
- Expanded ingredient categorization.
- Additional data integrity checks.
- More extensive automated database testing.

## 11. Conclusion

SQLite provides the persistent storage required for Smart Pantry Manager's core functionality.

Through the `DatabaseHelper.java` class, the application supports pantry inventory operations, recipe retrieval, and shopping list management.

The database implementation contributes to the application's reliability and usability.