package com.richfield.smartpantry.data;

import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.util.IngredientMatcher;

import java.util.List;

/**
 * One place to ask for recipe information, so screens never write SQL or loops of their own.
 *
 * <pre>
 *   RecipeRepository recipes = new RecipeRepository(db);
 *   recipes.getAll();                      // every recipe with its ingredients
 *   recipes.getById(7);                    // one recipe (or null)
 *   recipes.getByName("Pancakes");         // one recipe (or null)
 *   recipes.search("egg");                 // by recipe name OR ingredient
 *   recipes.match(pantry, "egg", false);   // search + "can I cook it?" status for each result
 * </pre>
 */
public class RecipeRepository {

    private final DatabaseHelper db;

    public RecipeRepository(DatabaseHelper db) {
        this.db = db;
    }

    public List<Recipe> getAll() { return db.getAllRecipes(); }

    public Recipe getById(long id) { return db.getRecipe(id); }

    public Recipe getByName(String name) { return db.getRecipeByName(name); }

    /** Recipes whose name or any ingredient contains the text; blank text returns everything. */
    public List<Recipe> search(String query) { return db.searchRecipes(query); }

    public int count() { return db.getRecipeCount(); }

    /**
     * Searches (blank = all recipes) and checks every result against the pantry using the
     * strict-matching engine. Each MatchResult says whether the recipe is cookable right now
     * and exactly which ingredients are missing.
     */
    public List<MatchResult> match(List<PantryItem> pantry, String query, boolean ignoreExpired) {
        return IngredientMatcher.matchAll(search(query), pantry, ignoreExpired);
    }
}
