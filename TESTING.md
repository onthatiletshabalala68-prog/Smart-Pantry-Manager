# Smart Pantry Manager – Functional Testing Report

## 1. Introduction

This document records the manual functional testing performed on the Smart Pantry Manager Android application.

The purpose of testing was to verify that the application's main features operate correctly, including pantry management, recipe recommendations, shopping list integration, settings, navigation, and local data persistence.

## 2. Testing Environment

**Application:** Smart Pantry Manager

**Development Environment:** Android Studio

**Programming Language:** Java

**Database:** SQLite

**Testing Platform:** Android Emulator (Medium Phone API 37)

**Testing Method:** Manual functional testing

## 3. Functional Test Results

| Test ID | Test Case | Expected Result | Actual Result | Status |
|---|---|---|---|---|
| TC01 | Launch application | Application opens without crashing | Application launched successfully | PASS |
| TC02 | Add pantry ingredient | New ingredient is saved and displayed | Ingredient added successfully | PASS |
| TC03 | Edit pantry ingredient | Ingredient information is updated | Ingredient updated successfully | PASS |
| TC04 | Delete pantry ingredient | Selected ingredient is removed | Ingredient deleted successfully | PASS |
| TC05 | Search and filter pantry | Matching ingredients are displayed | Search and filtering worked | PASS |
| TC06 | View suggested recipes | Fully matched recipes are displayed | Matching recipes displayed | PASS |
| TC07 | View Almost There recipes | Missing ingredients are identified | Missing ingredients displayed | PASS |
| TC08 | Open recipe details | Selected recipe details are displayed | Recipe details opened | PASS |
| TC09 | Add missing ingredients to shopping list | Missing ingredients are added to the list | Ingredients added successfully | PASS |
| TC10 | Transfer purchased items to pantry | Purchased ingredients are added to pantry | Transfer completed successfully | PASS |
| TC11 | Save settings | Preferences are saved correctly | Settings saved successfully | PASS |
| TC12 | Test bottom navigation | Navigation opens the correct screens | Navigation worked correctly | PASS |
| TC13 | Restart application | Previously saved information remains available | Data persisted after reopening | PASS |

## 4. Testing Summary

**Total Test Cases:** 13

**Passed:** 13

**Failed:** 0

**Pass Rate:** 100%

All documented manual functional tests passed.

## 5. Database Verification

The application uses SQLite for local data storage.

Manual testing confirmed that pantry ingredient operations work and that saved information remains available after the application is closed and reopened.

Shopping list operations and the transfer of purchased items into the pantry were also successfully tested.

## 6. Recipe Matching Verification

Recipe functionality was tested to confirm that the application distinguishes between recipes with sufficient available ingredients and recipes requiring additional ingredients or quantities.

Both the Suggested Recipes and Almost There sections worked during testing.

## 7. Limitations

The results represent manual functional testing rather than comprehensive automated testing.

Performance testing, security testing, accessibility testing, and testing across multiple physical Android devices have not been documented.

## 8. Conclusion

Smart Pantry Manager passed all 13 documented functional tests.

The results indicate that the tested core application features are functioning as intended in the development environment.

Additional testing may be conducted as part of future development.