
# Smart Pantry Manager
## User Guide and Final Functional Testing Report

### 1. Project Overview

Smart Pantry Manager is an Android application developed in Java
using Android Studio.

The application helps users manage pantry ingredients, monitor
expiry dates, discover recipes based on available ingredients,
and maintain a shopping list.

SQLite is used to store application data locally.

### 2. Application Features

#### 2.1 Pantry Management

The My Pantry screen allows users to:

- Add ingredients.
- View saved ingredients.
- Edit ingredient information.
- Delete ingredients.
- Search for ingredients by name.
- Filter ingredients according to expiry status.

Each ingredient includes a name, quantity, measurement unit,
and optional expiry date.

#### 2.2 Expiry Date Management

The application supports the following expiry categories:

- Expired
- Expiring today
- Expiring soon
- Fresh

Expiry status is calculated using calendar dates.

Users can configure expiry warning preferences in Settings.

#### 2.3 Recipe Suggestions

The application provides recipe suggestions based on
ingredients available in the pantry.

Recipes that can be prepared using the available ingredients
are distinguished from recipes requiring additional items.

The Almost There feature identifies missing ingredients.

Users can view recipe details, including ingredient
requirements and preparation instructions.

#### 2.4 Shopping List

The Shopping List screen allows users to:

- View shopping items.
- Add required ingredients.
- Mark items as purchased.
- Remove shopping items.

The application supports transferring missing recipe
ingredients to the shopping list.

Compatible quantities can be combined using supported
measurement-unit conversions.

#### 2.5 Settings

The Settings screen provides preferences for expiry
notifications and warning periods.

### 3. User Instructions

#### 3.1 Adding a Pantry Ingredient

1. Open Smart Pantry Manager.
2. Navigate to My Pantry.
3. Select Add Ingredient.
4. Enter the ingredient name.
5. Enter a valid quantity.
6. Select the measurement unit.
7. Choose an expiry date if applicable.
8. Save the ingredient.

The ingredient will appear in the pantry list.

#### 3.2 Editing an Ingredient

1. Open My Pantry.
2. Long-press the ingredient.
3. Select Edit.
4. Update the required fields.
5. Save the changes.

#### 3.3 Deleting an Ingredient

1. Open My Pantry.
2. Long-press the ingredient.
3. Select Delete.
4. Confirm the deletion.

#### 3.4 Searching and Filtering

1. Open My Pantry.
2. Enter an ingredient name in the search field.
3. Use the expiry filter to display the required category.

#### 3.5 Finding Recipes

1. Navigate to Suggested Recipes.
2. Review the available recipe suggestions.
3. Open a recipe to view its details.
4. Check whether all required ingredients are available.
5. Use the missing-ingredient functionality when needed.

#### 3.6 Managing the Shopping List

1. Navigate to the Shopping List.
2. Review or add shopping items.
3. Mark purchased items as completed.
4. Delete items when no longer required.

### 4. Database Implementation

The application uses SQLite with the database name:

smart_pantry.db

Database version: 5

The database contains three main tables:

1. pantry_items
2. shopping_items
3. recipes

The DatabaseHelper class manages database creation,
upgrades, queries, and data modifications.

Pantry database operations validate ingredient names,
quantities, and expiry dates before saving.

### 5. Final Functional Testing

The following functional areas were tested during
application development:

| Test Area | Expected Result | Status |
|---|---|---|
| Add ingredient | Valid ingredient is saved | Passed |
| Edit ingredient | Changes are saved | Passed |
| Delete ingredient | Ingredient is removed | Passed |
| Search pantry | Matching ingredients appear | Passed |
| Expiry filtering | Correct expiry categories appear | Passed |
| Date picker | Selected date is retained | Passed |
| Recipe suggestions | Recipes use pantry ingredients | Passed |
| Missing ingredients | Missing items are identified | Passed |
| Recipe details | Recipe information is displayed | Passed |
| Shopping list | Shopping items can be managed | Passed |
| Settings | Expiry preferences can be configured | Passed |
| SQLite persistence | Saved data remains available | Passed |
| Input validation | Invalid pantry input is rejected | Passed |

### 6. Data Validation

The application rejects:

- Empty ingredient names.
- Zero quantities.
- Negative quantities.
- Non-finite numeric quantities.
- Invalid expiry-date formats at the database layer.

Valid ingredient names are normalized before storage.

The expiry date may also be left unspecified.

### 7. Final Project Verification

The application was built and manually tested during
development.

The completed implementation includes:

- Java-based Android activities.
- SQLite database integration.
- Pantry ingredient management.
- Recipe suggestion functionality.
- Shopping-list functionality.
- Expiry date tracking.
- Input validation.
- Persistent local storage.
- Git version control.

### 8. Conclusion

Smart Pantry Manager provides an integrated solution
for managing pantry ingredients, tracking expiry dates,
identifying possible recipes, and organizing shopping needs.

The final implementation demonstrates Android development
using Java, SQLite database management, user interface
development, input validation, and functional testing.
