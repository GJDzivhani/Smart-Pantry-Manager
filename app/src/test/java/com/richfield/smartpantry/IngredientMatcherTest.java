package com.richfield.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;
import com.richfield.smartpantry.util.IngredientMatcher;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Unit tests for the strict-matching rule (run with: Gradle > app > Tasks > verification > test). */
public class IngredientMatcherTest {

    private Recipe pasta() {
        Recipe r = new Recipe(1, "Tomato Pasta", "", "", 20);
        r.getIngredients().add(new RecipeIngredient("pasta", 200, "g"));
        r.getIngredients().add(new RecipeIngredient("tomato", 4, "pcs"));
        r.getIngredients().add(new RecipeIngredient("garlic", 2, "pcs"));
        r.getIngredients().add(new RecipeIngredient("olive oil", 2, "tbsp"));
        r.getIngredients().add(new RecipeIngredient("salt", 1, "tsp"));
        return r;
    }

    private List<PantryItem> fullPantry() {
        return new ArrayList<>(Arrays.asList(
                new PantryItem(1, "Pasta", 500, "g", null),
                new PantryItem(2, "Tomatoes", 6, "pcs", null),      // plural vs singular
                new PantryItem(3, "Garlic", 5, "pcs", null),
                new PantryItem(4, "Olive Oil", 0.5, "l", null),     // l vs tbsp
                new PantryItem(5, "Salt", 100, "g", null)));        // g vs tsp
    }

    @Test
    public void everyIngredientPresent_isSuggested() {
        MatchResult m = IngredientMatcher.analyse(pasta(),
                IngredientMatcher.buildStock(fullPantry(), false));
        assertTrue(m.isFullMatch());
    }

    @Test
    public void fiveIngredientsFourInPantry_isNotSuggested() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(2);                                           // remove garlic
        MatchResult m = IngredientMatcher.analyse(pasta(),
                IngredientMatcher.buildStock(pantry, false));
        assertFalse(m.isFullMatch());
        assertEquals(1, m.missingCount());
        assertEquals("Garlic", m.missingSummary());
    }

    @Test
    public void insufficientQuantity_isNotSuggested() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(0, new PantryItem(1, "Pasta", 150, "g", null));  // needs 200 g
        MatchResult m = IngredientMatcher.analyse(pasta(),
                IngredientMatcher.buildStock(pantry, false));
        assertFalse(m.isFullMatch());
    }

    @Test
    public void unitConversion_kgSatisfiesGrams() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(0, new PantryItem(1, "pasta", 0.25, "kg", null)); // 250 g >= 200 g
        assertTrue(IngredientMatcher.analyse(pasta(),
                IngredientMatcher.buildStock(pantry, false)).isFullMatch());
    }

    @Test
    public void duplicateRowsAreSummed() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(0, new PantryItem(1, "Pasta", 120, "g", null));
        pantry.add(new PantryItem(9, "pasta", 100, "g", null));      // 120 + 100 = 220 g
        assertTrue(IngredientMatcher.analyse(pasta(),
                IngredientMatcher.buildStock(pantry, false)).isFullMatch());
    }

    @Test
    public void pieceCountDoesNotMatchWeight() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(1, new PantryItem(2, "Tomatoes", 800, "g", null)); // pieces needed
        assertFalse(IngredientMatcher.analyse(pasta(),
                IngredientMatcher.buildStock(pantry, false)).isFullMatch());
    }

    @Test
    public void normalisationHandlesPluralsAndCase() {
        assertEquals("tomato", IngredientMatcher.normalizeName("Tomatoes"));
        assertEquals("tomato", IngredientMatcher.normalizeName(" TOMATO "));
        assertEquals("cherry", IngredientMatcher.normalizeName("Cherries"));
        assertEquals("egg", IngredientMatcher.normalizeName("Eggs"));
        assertEquals("yogurt", IngredientMatcher.normalizeName("Yoghurt"));
    }

    // ---------- generic staples ("oil" is satisfied by "olive oil") ----------

    private Recipe friedRice() {
        Recipe r = new Recipe(2, "Egg Fried Rice", "", "", 15);
        r.getIngredients().add(new RecipeIngredient("rice", 300, "g"));
        r.getIngredients().add(new RecipeIngredient("eggs", 2, "pcs"));
        r.getIngredients().add(new RecipeIngredient("oil", 1, "tbsp"));
        return r;
    }

    private List<PantryItem> friedRicePantry(String oilName) {
        return new ArrayList<>(Arrays.asList(
                new PantryItem(1, "Rice", 1, "kg", null),
                new PantryItem(2, "Eggs", 6, "pcs", null),
                new PantryItem(3, oilName, 500, "ml", null)));
    }

    @Test
    public void genericOil_isSatisfiedByOliveOil() {
        assertTrue(IngredientMatcher.analyse(friedRice(),
                IngredientMatcher.buildStock(friedRicePantry("Olive Oil"), false)).isFullMatch());
        assertTrue(IngredientMatcher.analyse(friedRice(),
                IngredientMatcher.buildStock(friedRicePantry("Sunflower oil"), false)).isFullMatch());
    }

    @Test
    public void specificOil_isNotSatisfiedByADifferentOil() {
        // pasta() needs "olive oil" specifically
        List<PantryItem> pantry = fullPantry();
        pantry.set(3, new PantryItem(4, "Sunflower Oil", 0.5, "l", null));
        assertFalse(IngredientMatcher.analyse(pasta(),
                IngredientMatcher.buildStock(pantry, false)).isFullMatch());
    }

    @Test
    public void peanutButter_neverCountsAsButter() {
        Recipe toast = new Recipe(3, "Toast", "", "", 5);
        toast.getIngredients().add(new RecipeIngredient("butter", 10, "g"));
        List<PantryItem> pantry = new ArrayList<>(Arrays.asList(
                new PantryItem(1, "Peanut Butter", 400, "g", null)));
        assertFalse(IngredientMatcher.analyse(toast,
                IngredientMatcher.buildStock(pantry, false)).isFullMatch());
    }

    @Test
    public void expiredItemsAreIgnoredWhenSettingIsOn() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(2, new PantryItem(3, "Garlic", 5, "pcs", "2000-01-01"));   // long expired
        assertTrue(IngredientMatcher.analyse(pasta(),
                IngredientMatcher.buildStock(pantry, false)).isFullMatch());
        assertFalse(IngredientMatcher.analyse(pasta(),
                IngredientMatcher.buildStock(pantry, true)).isFullMatch());
    }

    // ---------- name matching: fillers, synonyms, staple types ----------

    /** True when a pantry holding ONE item satisfies a recipe that needs ONE ingredient. */
    private boolean satisfies(String pantryName, double pantryQty, String pantryUnit,
                              String recipeName, double recipeQty, String recipeUnit) {
        Recipe r = new Recipe(99, "T", "", "", 5);
        r.getIngredients().add(new RecipeIngredient(recipeName, recipeQty, recipeUnit));
        List<PantryItem> pantry = new ArrayList<>(Arrays.asList(
                new PantryItem(1, pantryName, pantryQty, pantryUnit, null)));
        return IngredientMatcher.analyse(r, IngredientMatcher.buildStock(pantry, false)).isFullMatch();
    }

    @Test
    public void fillerWordsAreIgnored() {
        assertEquals("tomato", IngredientMatcher.normalizeName("Fresh Tomatoes (ripe)"));
        assertEquals("egg", IngredientMatcher.normalizeName("Large Eggs"));
        assertEquals("milk", IngredientMatcher.normalizeName("Full Cream Milk"));
        assertEquals("butter", IngredientMatcher.normalizeName("Unsalted Butter"));
        assertEquals("olive oil", IngredientMatcher.normalizeName("Extra Virgin Olive Oil"));
        assertEquals("fresh", IngredientMatcher.normalizeName("Fresh"));   // only a filler: keep as typed
    }

    @Test
    public void chickenAndMinceNamesMatchTheRecipeWording() {
        assertTrue(satisfies("Chicken", 500, "g", "chicken breast", 300, "g"));
        assertTrue(satisfies("Chicken fillets", 500, "g", "chicken breast", 300, "g"));
        assertTrue(satisfies("Mince", 500, "g", "beef mince", 300, "g"));
        assertTrue(satisfies("Ground Beef", 500, "g", "beef mince", 300, "g"));
        assertTrue(satisfies("Mayo", 200, "g", "mayonnaise", 3, "tbsp"));
        assertTrue(satisfies("Mielie meal", 1, "kg", "maize meal", 250, "g"));
    }

    @Test
    public void specificStaplesSatisfyGenericRecipeNames() {
        assertTrue(satisfies("Full Cream Milk", 1, "l", "milk", 300, "ml"));
        assertTrue(satisfies("Spaghetti", 500, "g", "pasta", 200, "g"));
        assertTrue(satisfies("Macaroni", 500, "g", "pasta", 200, "g"));
        assertTrue(satisfies("Cheddar", 200, "g", "cheese", 80, "g"));
        assertTrue(satisfies("Cheddar cheese", 200, "g", "cheese", 80, "g"));
        assertTrue(satisfies("Salted Butter", 250, "g", "butter", 20, "g"));
        assertTrue(satisfies("Greek Yoghurt", 500, "g", "yogurt", 200, "g"));
        assertTrue(satisfies("Brown Sugar", 500, "g", "sugar", 2, "tbsp"));
    }

    @Test
    public void lookAlikesNeverCount() {
        assertFalse(satisfies("Coconut Milk", 400, "ml", "milk", 100, "ml"));
        assertFalse(satisfies("Almond Milk", 1, "l", "milk", 100, "ml"));
        assertFalse(satisfies("Cream Cheese", 200, "g", "cheese", 80, "g"));
        assertFalse(satisfies("Crunchy Peanut Butter", 400, "g", "butter", 20, "g"));
        assertFalse(satisfies("Garlic Salt", 100, "g", "salt", 1, "tsp"));
        assertFalse(satisfies("Icing Sugar", 500, "g", "sugar", 2, "tbsp"));
        assertFalse(satisfies("Frozen Yoghurt", 500, "g", "yogurt", 200, "g"));
    }

    @Test
    public void matchingIsOneWayForSpecificRecipeNames() {
        assertFalse(satisfies("Pasta", 500, "g", "spaghetti", 200, "g"));      // recipe wants spaghetti
        assertFalse(satisfies("Macaroni", 500, "g", "spaghetti", 200, "g"));
        assertFalse(satisfies("Sunflower Oil", 500, "ml", "olive oil", 2, "tbsp"));
    }
}
