package com.richfield.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local SQLite database (SQLiteOpenHelper) - persists on the device, so data survives
 * closing and re-opening the app.
 *
 * Tables
 *   pantry_items       (id PK, name, quantity, unit, expiry)             <- user data, full CRUD
 *   recipes            (id PK, name, description, steps, prep_minutes)   <- seeded on first run
 *   recipe_ingredients (id PK, recipe_id FK -> recipes.id, name, quantity, unit)
 *                      (indexed on recipe_id since DB version 2)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "smart_pantry.db";
    public static final int DB_VERSION = 3;

    public static final String T_PANTRY = "pantry_items";
    public static final String T_RECIPES = "recipes";
    public static final String T_INGREDIENTS = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);   // cascade-delete recipe ingredients
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "description TEXT, "
                + "steps TEXT NOT NULL, "
                + "prep_minutes INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + T_INGREDIENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + T_RECIPES + "(id) ON DELETE CASCADE)");

        createIndexes(db);
        RecipeSeeder.seed(db);                      // pre-load the recipe collection
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Non-destructive migrations: the user's pantry must never be wiped by an app update.
        if (oldVersion < 2) createIndexes(db);      // v2: index for fast recipe -> ingredient lookups
        if (oldVersion < 3) RecipeSeeder.seed(db);  // v3: 22 more recipes (existing ones are skipped)
    }

    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ingredients_recipe ON "
                + T_INGREDIENTS + "(recipe_id)");
    }

    // =====================================================================
    //  PANTRY - CREATE / READ / UPDATE / DELETE
    // =====================================================================

    private ContentValues toValues(PantryItem item) {
        ContentValues cv = new ContentValues();
        cv.put("name", item.getName());
        cv.put("quantity", item.getQuantity());
        cv.put("unit", item.getUnit());
        if (item.getExpiry() == null) cv.putNull("expiry"); else cv.put("expiry", item.getExpiry());
        return cv;
    }

    private PantryItem readItem(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.isNull(c.getColumnIndexOrThrow("expiry")) ? null
                        : c.getString(c.getColumnIndexOrThrow("expiry")));
    }

    /** CREATE - returns the new row id (or -1 on failure). */
    public long insertItem(PantryItem item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    /** READ (list) - every pantry item, A-Z. */
    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null, null, null,
                "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) list.add(readItem(c));
        }
        return list;
    }

    /** READ (single) - null when the id does not exist. */
    public PantryItem getItem(long id) {
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readItem(c) : null;
        }
    }

    /** UPDATE - returns the number of rows changed (1 on success). */
    public int updateItem(PantryItem item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item), "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE one item. */
    public int deleteItem(long id) {
        return getWritableDatabase().delete(T_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    /** DELETE everything in the pantry (used by Settings). */
    public void deleteAllItems() {
        getWritableDatabase().delete(T_PANTRY, null, null);
    }

    // =====================================================================
    //  RECIPES - read only
    // =====================================================================

    private Recipe readRecipe(Cursor c) {
        return new Recipe(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("description")),
                c.getString(c.getColumnIndexOrThrow("steps")),
                c.getInt(c.getColumnIndexOrThrow("prep_minutes")));
    }

    private RecipeIngredient readIngredient(Cursor c) {
        return new RecipeIngredient(
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")));
    }

    /** All recipes, each already filled with its ingredient list (2 queries in total). */
    public List<Recipe> getAllRecipes() {
        return loadRecipes(null, null);
    }

    /**
     * Recipes whose NAME or any INGREDIENT contains the text (case-insensitive), A-Z.
     * "egg" finds "Egg Fried Rice" and every recipe that uses eggs. Blank text returns all recipes.
     */
    public List<Recipe> searchRecipes(String query) {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) return getAllRecipes();
        String like = "%" + escapeLike(q) + "%";
        return loadRecipes(
                "name LIKE ? ESCAPE '\\' OR id IN (SELECT recipe_id FROM " + T_INGREDIENTS
                        + " WHERE name LIKE ? ESCAPE '\\')",
                new String[]{like, like});
    }

    /** First recipe with exactly this name (ignoring case), or null. */
    public Recipe getRecipeByName(String name) {
        List<Recipe> found = loadRecipes("name = ? COLLATE NOCASE", new String[]{name == null ? "" : name.trim()});
        return found.isEmpty() ? null : found.get(0);
    }

    /** Number of recipes stored in the database. */
    public int getRecipeCount() {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + T_RECIPES, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    /** Makes %, _ and \ in user text match literally inside a LIKE pattern. */
    static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /**
     * Shared loader: recipes matching the optional WHERE clause, plus their ingredients,
     * using exactly two queries however many recipes match.
     */
    private List<Recipe> loadRecipes(String where, String[] args) {
        SQLiteDatabase db = getReadableDatabase();
        Map<Long, Recipe> byId = new LinkedHashMap<>();

        try (Cursor c = db.query(T_RECIPES, null, where, args, null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                Recipe r = readRecipe(c);
                byId.put(r.getId(), r);
            }
        }
        if (byId.isEmpty()) return new ArrayList<>();

        String ingredientWhere = (where == null) ? null
                : "recipe_id IN (SELECT id FROM " + T_RECIPES + " WHERE " + where + ")";
        try (Cursor c = db.query(T_INGREDIENTS, null, ingredientWhere, args, null, null, "id ASC")) {
            while (c.moveToNext()) {
                Recipe r = byId.get(c.getLong(c.getColumnIndexOrThrow("recipe_id")));
                if (r != null) r.getIngredients().add(readIngredient(c));
            }
        }
        return new ArrayList<>(byId.values());
    }

    /** One recipe with its ingredients, or null. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Recipe recipe;
        try (Cursor c = db.query(T_RECIPES, null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (!c.moveToFirst()) return null;
            recipe = readRecipe(c);
        }
        try (Cursor c = db.query(T_INGREDIENTS, null, "recipe_id = ?", new String[]{String.valueOf(id)},
                null, null, "id ASC")) {
            while (c.moveToNext()) recipe.getIngredients().add(readIngredient(c));
        }
        return recipe;
    }
}
