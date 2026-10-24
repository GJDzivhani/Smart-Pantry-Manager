# Smart Pantry Manager — Testing Guide

## 1. Automated unit tests

The project includes two JUnit test classes:

- `IngredientMatcherTest` — tests recipe matching, missing ingredients, insufficient quantities, unit conversions, duplicate pantry entries, ingredient-name normalisation, generic and specific ingredients, expired items, and misleading ingredient names.
- `IngredientCategoryTest` — tests ingredient category assignments, similar ingredient names, and fallback handling for unknown or empty names.

## 2. Run the tests

Open PowerShell in the project root and execute:

```powershell
.\gradlew.bat testDebugUnitTest
```

A successful run should finish without failed tests. Gradle writes the detailed HTML report to:

`app/build/reports/tests/testDebugUnitTest/index.html`

Open that file in a browser to inspect individual test results.

## 3. Build the Android app

Run:

```powershell
.\gradlew.bat assembleDebug
```

When the build succeeds, the debug APK is generated at:

`app/build/outputs/apk/debug/app-debug.apk`

The APK and generated build files should not be committed to Git.

## 4. Troubleshooting a locked build directory

If Windows reports that Gradle cannot delete or replace a file under `app\build`, close any process using the build output and stop Gradle:

```powershell
.\gradlew.bat --stop
```

If necessary, close Android Studio temporarily, remove only the generated app build directory, and rebuild:

```powershell
Remove-Item .\app\build -Recurse -Force
.\gradlew.bat assembleDebug
```

The deletion command removes generated build output, not application source code. Run it from the project root and ensure no important files have been stored inside `app\build`.

## 5. Scope of verification

Unit tests check the matching and ingredient-category logic. They do not replace manual testing of screens, navigation, SQLite persistence, or behaviour on an Android emulator or physical device.
