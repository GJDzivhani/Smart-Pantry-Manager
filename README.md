# Smart Pantry Manager

A **Java Android application** (Mobile App Development 700 - Practical Assignment) that helps reduce
food waste. The user records the ingredients they already have at home, and the app suggests
**only** the recipes they can cook right now - no shopping trip required.

## The core idea: strict matching
A recipe is suggested **only if every ingredient it needs is in the pantry, in at least the required
quantity**. A recipe with 5 ingredients where the pantry has only 4 is *never* shown in the main list.
A separate, clearly labelled **"Almost there"** tab lists recipes missing exactly one ingredient (bonus).

The matching engine (`util/IngredientMatcher.java`) copes with everyday messiness:
- names: case, spacing, brackets, singular/plural (`Tomatoes` = `tomato`), filler words (`Fresh`, `Large`, `Full cream`, `Unsalted`), and synonyms (`yoghurt` = `yogurt`, `Chicken` = `chicken breast`, `Mince`/`Ground beef` = `beef mince`, `Mayo` = `mayonnaise`, `Mielie meal` = `maize meal`)
- units: kg/g/oz/lb, l/ml/tsp/tbsp/cup are converted; pieces (`pcs`) are counted separately
- duplicate pantry rows of the same ingredient are added together
- generic staples: a recipe that needs plain `oil` accepts `olive oil` / `sunflower oil`; `milk` accepts `full cream milk`; `pasta` accepts `spaghetti`/`macaroni`; `cheese` accepts `cheddar`. One-way only, and look-alikes never count (`peanut butter` is not `butter`, `coconut milk` is not `milk`, `cream cheese` is not `cheese`)
- optional setting: ignore expired ingredients

## Features
| Requirement | Where |
|---|---|
| Add / edit / delete pantry items (name, quantity, unit, optional expiry) | `AddEditIngredientActivity`, `MainActivity` |
| Pantry list (RecyclerView + custom adapter bound to the database) | `MainActivity`, `PantryAdapter` |
| 44 pre-loaded recipes (seeded on first run; existing installs receive the new ones on update) | `data/RecipeSeeder.java` |
| "You can cook N recipes now" banner on the Pantry screen (opens Recipes) | `MainActivity.updateRecipeBanner()` |
| Recipes screen: Ready (strict) / Almost there / All tabs + search by recipe or ingredient, with empty-state messages | `SuggestedRecipesActivity`, `RecipeAdapter` |
| One-call recipe lookups (all, by id, by name, search, match against pantry) | `data/RecipeRepository.java` |
| Recipe detail (ingredients with have/missing marks + method) | `RecipeDetailActivity` |
| Settings (expiry alerts, alert window, default unit, ignore-expired, demo data, clear pantry) | `SettingsActivity` |
| Navigation | Bottom navigation bar (`NavHelper`) + explicit Intents with extras |
| Input validation | `AddEditIngredientActivity.validate()` |
| No maps / GPS / location | No such permission or library is used |

## Database choice: SQLite (`SQLiteOpenHelper`)
SQLite was chosen because the app only needs the user's *own* data on *their own* device: it works
offline, needs no server, account or API keys, and is the approach covered in the module's persistent-data
chapter. Data survives closing and re-opening the app.

Tables: `pantry_items`, `recipes`, `recipe_ingredients` (FK `recipe_id` -> `recipes.id`, ON DELETE CASCADE,
indexed on `recipe_id`). Upgrades are non-destructive, so an app update never wipes the user's pantry.

### Fetching recipe information
```java
RecipeRepository recipes = new RecipeRepository(new DatabaseHelper(context));
recipes.getAll();                       // every recipe + its ingredients
recipes.getById(7);                     // one recipe, or null
recipes.getByName("Pancakes");          // one recipe, or null
recipes.search("egg");                  // by recipe name OR ingredient (case-insensitive)
recipes.match(pantry, "egg", false);    // search + can-I-cook-it status for each result
recipes.count();                        // number of recipes in the database
```

## Setup / run instructions
1. Install **Android Studio** (Koala or newer) - it bundles the required JDK 17.
2. `git clone <your-repo-url>` then **File > Open** and select the `SmartPantryManager` folder.
3. Wait for the Gradle sync to finish (internet needed the first time).
4. Create/start an emulator (API 24+) or plug in a phone with USB debugging.
5. Press **Run**.
6. Tip: **Settings > Add demo ingredients** loads sample items so recipes appear immediately.

Run the unit tests for the matching rule: *Project view > app > src > test > IngredientMatcherTest > Run*.

## Project structure
```
app/src/main/java/com/richfield/smartpantry/
  MainActivity.java                 pantry list
  AddEditIngredientActivity.java    create / update + validation
  SuggestedRecipesActivity.java     strict-matching results
  RecipeDetailActivity.java         recipe detail
  SettingsActivity.java             settings
  adapter/  PantryAdapter, RecipeAdapter
  data/     DatabaseHelper, RecipeRepository, RecipeSeeder
  model/    PantryItem, Recipe, RecipeIngredient, MatchResult
  util/     IngredientMatcher, IngredientCategory, IngredientIcons, NavHelper, Prefs
```

## Design
**Palette (two colours):** pantry green `#1F4D3F` for structure (headers, buttons, selected states) and
turmeric `#E8A11A` for the one thing to notice (the add button and the "You can cook N recipes now" call-out).
Text on white uses the darker turmeric `#8A5700` so it stays readable. Neutrals are green-tinted greys.

**Fonts:** Lora (headings, ingredient and recipe names) and Poppins (everything else), bundled in `res/font/`
so the app still needs no internet permission. Both are licensed under the SIL Open Font License 1.1.

**Ingredient icons:** every pantry item and every recipe ingredient gets a coloured icon for its food type
(egg, dairy, cheese, meat, fish, tomato and peppers, roots and bulbs, greens and herbs, fruit, grains and pulses,
oils and sauces, spices and baking, and a jar for anything unrecognised). The type is worked out from the
ingredient name by `util/IngredientCategory.java` (tested in `IngredientCategoryTest`); `util/IngredientIcons.java`
paints the icon. To add a keyword, add it to the matching `words(...)` line; to recolour a type, edit its
`cat_*` colour in `colors.xml`.

Cards are outlined with 16dp corners and rounded status pills. The navigation and action icons are the
Material Design icon set (Apache 2.0); the ingredient icons are drawn for this app. Every colour lives in
`res/values/colors.xml`, so re-theming is a one-file change.
