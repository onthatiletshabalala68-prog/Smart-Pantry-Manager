# Smart Pantry Manager – Application Architecture

## 1. Introduction

Smart Pantry Manager is a native Android application developed primarily in Java. It provides pantry inventory management, recipe recommendations, shopping list functionality, expiry management, and application settings.

The application uses Android Activities for its user interface and SQLite for local data persistence.

## 2. Architectural Overview

The application follows an Activity-based structure, with supporting classes responsible for database operations, data representation, navigation, and expiry notifications.

The main architectural components are:

- **User Interface Layer:** Android Activities and interface components.
- **Application Logic Layer:** Recipe matching, ingredient quantity comparisons, shopping list operations, and expiry handling.
- **Data Access Layer:** DatabaseHelper and SQLite database operations.
- **Data Model Layer:** Classes representing pantry items, recipes, and shopping items.

These are logical groupings of responsibilities rather than independently deployed architectural layers.

## 3. User Interface Components

### MainActivity.java

Provides the application's home screen and access to its main features.

### PantryActivity.java

Manages pantry ingredient operations, including adding, editing, deleting, searching, and filtering ingredients.

### RecipeActivity.java

Retrieves recipes and pantry ingredients, evaluates ingredient availability, and displays Suggested Recipes and Almost There categories.

### RecipeDetailActivity.java

Displays information about a selected recipe and supports interaction with missing ingredient requirements.

### ShoppingActivity.java

Manages shopping list operations and the transfer of purchased ingredients into the pantry.

### SettingsActivity.java

Provides controls for application preferences, including expiry warning settings.

### ProfileActivity.java

Provides the application's profile screen.

## 4. Data Models

### PantryItem.java

Represents an ingredient stored in the pantry.

Pantry items include information such as ingredient name, quantity, measurement unit, and expiry date.

### Recipe.java

Represents a recipe and its ingredient requirements.

Recipe requirements are used to determine whether a recipe can be prepared using available pantry ingredients.

### ShoppingItem.java

Represents an item in the shopping list.

Shopping items support grocery management and purchased-item processing.

## 5. Database Architecture

The application uses SQLite as its local database system.

### DatabaseHelper.java

DatabaseHelper manages the application's database access and supports operations involving pantry ingredients, recipes, and shopping list information.

The application uses local storage so that saved information remains available after the application is closed and reopened.

## 6. Recipe Recommendation Logic

The recipe recommendation process involves:

1. Retrieving pantry ingredients from the database.
2. Retrieving stored recipes and their ingredient requirements.
3. Comparing ingredient names after normalization.
4. Checking measurement-unit compatibility.
5. Converting compatible measurement quantities where necessary.
6. Comparing available quantities against required quantities.
7. Categorizing recipes based on the results.

A recipe appears under **Suggested Recipes** when all required ingredients and quantities are available.

A recipe appears under **Almost There** when one or more ingredients are missing or insufficient.

## 7. Navigation

The application uses Android Intents to navigate between Activities.

The `BottomNavigationHelper.java` class provides shared navigation functionality across the application's main screens.

This helps users move between Home, Pantry, Recipes, Shopping, and Settings.

## 8. Expiry Management

The application includes expiry-related functionality through:

- Pantry ingredient expiry dates.
- Expiry filtering.
- Configurable expiry warning preferences.
- ExpiryNotificationScheduler.java.
- ExpiryNotificationWorker.java.

These components support expiry monitoring and notification scheduling.

## 9. User Interface Design

The user interface is constructed using Android components, including LinearLayout, TextView, Button, EditText, Spinner, and ListView.

The application includes a food-themed home background and gradient backgrounds on selected screens.

`AppBackgroundHelper.java` provides reusable gradient styling.

## 10. Application Data Flow

A typical pantry operation follows this process:

**User interaction → PantryActivity → DatabaseHelper → SQLite database**

After the database operation completes, the application refreshes the displayed pantry information.

A typical recipe recommendation operation follows this process:

**SQLite data → DatabaseHelper → RecipeActivity matching logic → Recipe categories → User interface**

The shopping workflow connects missing recipe ingredients to shopping list management and supports transferring purchased ingredients into pantry inventory.

## 11. Design Considerations

The application's design emphasizes:

- Separation of responsibilities between Activities, models, and database operations.
- Reuse of navigation and background styling helpers.
- Local data persistence.
- Ingredient quantity validation.
- Consistent access to core application features.
- Maintainable Java classes.

## 12. Conclusion

Smart Pantry Manager combines Android Activities, Java application logic, SQLite storage, and supporting helper classes to provide an integrated pantry management application.

Its structure supports the core functional requirements while allowing additional features and improvements to be introduced in future development.