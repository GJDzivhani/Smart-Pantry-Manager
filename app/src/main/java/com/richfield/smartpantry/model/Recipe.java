package com.richfield.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

/** A recipe with its required ingredients (rows from recipes + recipe_ingredients). */
public class Recipe {
    private final long id;
    private final String name;
    private final String description;
    private final String steps;
    private final int prepMinutes;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, String description, String steps, int prepMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.prepMinutes = prepMinutes;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSteps() { return steps; }
    public int getPrepMinutes() { return prepMinutes; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}
