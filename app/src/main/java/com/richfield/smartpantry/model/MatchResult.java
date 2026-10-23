package com.richfield.smartpantry.model;

import com.richfield.smartpantry.util.IngredientMatcher;

import java.util.List;

/**
 * The outcome of checking ONE recipe against the pantry.
 * A recipe is a "full match" (and therefore suggested) only when EVERY ingredient is available.
 */
public class MatchResult {

    /** Availability of a single recipe ingredient. */
    public static class IngredientStatus {
        public final RecipeIngredient ingredient;
        public final boolean available;
        public final String note; // e.g. "have 200 g" / "need 300 g, have 150 g" / "not in pantry"

        public IngredientStatus(RecipeIngredient ingredient, boolean available, String note) {
            this.ingredient = ingredient;
            this.available = available;
            this.note = note;
        }
    }

    private final Recipe recipe;
    private final List<IngredientStatus> statuses;

    public MatchResult(Recipe recipe, List<IngredientStatus> statuses) {
        this.recipe = recipe;
        this.statuses = statuses;
    }

    public Recipe getRecipe() { return recipe; }
    public List<IngredientStatus> getStatuses() { return statuses; }

    public int missingCount() {
        int n = 0;
        for (IngredientStatus s : statuses) if (!s.available) n++;
        return n;
    }

    /** THE strict rule: no missing ingredient at all. */
    public boolean isFullMatch() {
        return !statuses.isEmpty() && missingCount() == 0;
    }

    /** Names of the ingredients that are missing / insufficient, e.g. "Garlic, Butter". */
    public String missingSummary() {
        StringBuilder sb = new StringBuilder();
        for (IngredientStatus s : statuses) {
            if (s.available) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(IngredientMatcher.capitalise(s.ingredient.getName()));
        }
        return sb.toString();
    }
}
