# Smart Pantry Manager

## Project Overview

Smart Pantry Manager is an Android mobile application developed using Java in Android Studio. The application helps users organise their kitchen ingredients, monitor expiry dates, discover recipes based on available ingredients, and manage shopping lists.

The main purpose of the application is to simplify everyday pantry management, reduce unnecessary food waste, and assist users in making better use of the ingredients they already have.

The application was developed as part of a Mobile App Development academic project.

## Objectives

The main objectives of Smart Pantry Manager are to:

- Provide a convenient way to manage pantry ingredients.
- Allow users to record ingredient quantities, measurement units, and expiry dates.
- Recommend recipes based on ingredients available in the pantry.
- Identify missing ingredients and quantity shortages for recipes.
- Assist users in organising their grocery shopping.
- Store application data locally using SQLite.
- Provide a simple and accessible Android user interface.

## Main Features

### 1. Pantry Management

Users can add, view, edit, and delete pantry ingredients.

Each ingredient contains information such as its name, quantity, measurement unit, and expiry date.

The application also supports searching and filtering ingredients, making it easier to locate items and identify ingredients that are expired or approaching their expiry dates.

### 2. Recipe Suggestions

Smart Pantry Manager provides recipe suggestions based on the ingredients currently available in the user's pantry.

The application compares recipe ingredient requirements against pantry ingredients, including their quantities and measurement units.

Recipes are organised into two categories:

**Suggested Recipes:** Recipes for which all required ingredients and quantities are available.

**Almost There:** Recipes that require additional ingredients or have insufficient quantities of certain ingredients.

This functionality helps users identify meals they can prepare immediately and meals that require additional shopping.

### 3. Recipe Details

Users can select recipes to view additional information and identify missing ingredients.

The application supports transferring missing ingredient quantities to the shopping list, helping users prepare for future meals.

### 4. Shopping List Management

The shopping list allows users to manage ingredients they need to purchase.

Users can add and manage shopping items, mark ingredients as purchased, and transfer purchased ingredients into the pantry.

This helps maintain an organised connection between grocery shopping and pantry inventory.

### 5. Expiry Date Management

The application records ingredient expiry dates and identifies ingredients that are expired or approaching expiry.

Users can configure expiry warning preferences through the Settings screen.

The project also includes expiry notification scheduling functionality.

### 6. Settings and Profile

The application includes Settings and Profile screens to support user preferences and navigation.

Settings allow users to manage expiry-related preferences.

### 7. Local Data Storage

Smart Pantry Manager uses SQLite to store application information locally.

The database supports pantry ingredient management, recipe retrieval, and shopping list operations.

Locally stored information remains available after the application is closed and reopened.

## Technologies Used

| Technology | Purpose |
|---|---|
| Java | Main application programming language |
| Android Studio | Development environment |
| Android SDK | Android application development |
| SQLite | Local database management |
| XML | Android configuration and resources |
| Gradle | Project building and dependency management |
| Git | Version control |
| GitHub | Source code hosting and project submission |

## Application Screens

**Home Screen:** Provides access to the application's main features.

**My Pantry:** Displays stored ingredients and supports ingredient management, searching, and expiry filtering.

**Recipes:** Displays suggested recipes and recipes with missing ingredients.

**Recipe Details:** Provides recipe-specific information and missing ingredient details.

**Shopping List:** Supports grocery list management and transferring purchased ingredients to the pantry.

**Settings:** Allows users to configure application preferences.

**Profile:** Provides a dedicated profile screen.

## Installation and Setup

### Requirements

- Android Studio
- Android SDK
- Java Development Kit supported by the project's Gradle configuration
- Android emulator or compatible Android device

### Installation Steps

1. Clone the GitHub repository:

   ```bash
   git clone https://github.com/onthatiletshabalala68-prog/Smart-Pantry-Manager.git
   ```

2. Open Android Studio.
3. Select **Open** and navigate to the cloned project folder.
4. Allow Gradle to download dependencies and complete synchronization.
5. Select an Android emulator or connected Android device.
6. Click **Run** to build and launch the application.

## How to Use the Application

1. Launch Smart Pantry Manager.
2. Open **My Pantry** and add ingredients with their quantities, units, and expiry dates.
3. Use the search and filtering options to organise pantry items.
4. Navigate to **Recipes** to view meals that can be prepared using available ingredients.
5. Open **Almost There** to identify missing ingredients.
6. Select a recipe and add missing ingredients to the shopping list.
7. Open **Shopping List** to manage grocery items.
8. Mark purchased items and transfer them into the pantry.
9. Open **Settings** to configure expiry warning preferences.

## Testing

The application was manually tested using an Android emulator.

The following functional tests were completed successfully:

1. Application launches without crashing.
2. Adding pantry ingredients.
3. Editing existing pantry ingredients.
4. Deleting pantry ingredients.
5. Searching and filtering ingredients.
6. Displaying recipes with fully matched ingredients.
7. Identifying recipes with missing ingredients.
8. Opening recipe details.
9. Adding missing ingredients to the shopping list.
10. Marking purchased items and transferring them into the pantry.
11. Saving application settings.
12. Navigating between application screens.
13. Preserving stored data after closing and reopening the application.

**Manual testing result: 13 out of 13 checks passed.**

These results describe functional testing performed during development and do not represent a complete automated test suite.

## Challenges Encountered

During development, several technical challenges were encountered.

**Gradle Configuration:** Initial dependency resolution and synchronization issues required troubleshooting before the application could build successfully.

**AndroidManifest Configuration:** Activity declarations and XML formatting errors were corrected to allow the application to compile and launch.

**Java and Kotlin Configuration:** An initial duplicate Activity class issue was resolved by retaining the Java implementation.

**Recipe Matching:** Ingredient comparisons required careful handling of ingredient names, quantities, and compatible measurement units.

**User Interface Design:** The application interface was refined to improve navigation and the presentation of pantry and recipe information.

## Future Improvements

Potential future enhancements include:

- Barcode scanning for faster ingredient entry.
- Cloud synchronization across multiple devices.
- More advanced recipe recommendations.
- Additional recipe categories and dietary filters.
- Improved accessibility and responsive layouts.
- Expanded automated testing.

These are proposed enhancements and are not claimed as existing features.

## Developer

**Onthatile Tshabalala**

Bachelor of Information Technology

Richfield Graduate Institute of Technology

## Repository

[Smart Pantry Manager on GitHub](https://github.com/onthatiletshabalala68-prog/Smart-Pantry-Manager)

## Project Status

The core application has been implemented and has passed the documented manual functional tests. Final academic documentation and submission preparation are in progress.