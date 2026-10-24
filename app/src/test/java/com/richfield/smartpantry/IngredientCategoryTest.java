package com.richfield.smartpantry;

import static org.junit.Assert.assertEquals;

import com.richfield.smartpantry.util.IngredientCategory;

import org.junit.Test;

/** Unit tests for the ingredient -> coloured icon category rules. */
public class IngredientCategoryTest {

    private static void assertAll(IngredientCategory expected, String... names) {
        for (String name : names) {
            assertEquals("\"" + name + "\"", expected, IngredientCategory.of(name));
        }
    }

    @Test
    public void everyIngredientInTheSeededRecipesHasTheRightIcon() {
        assertAll(IngredientCategory.EGG, "eggs", "Large Eggs", "Free Range Eggs");
        assertAll(IngredientCategory.DAIRY, "butter", "milk", "yogurt", "Yoghurt", "Full Cream Milk");
        assertAll(IngredientCategory.CHEESE, "cheese", "Cheddar", "Cream Cheese");
        assertAll(IngredientCategory.MEAT, "chicken breast", "Chicken", "beef mince", "Boerewors");
        assertAll(IngredientCategory.FISH, "tuna", "Tinned Tuna");
        assertAll(IngredientCategory.TOMATO, "tomato", "Tomatoes", "bell pepper", "Green Pepper");
        assertAll(IngredientCategory.ROOTS, "onion", "garlic", "potatoes", "carrots", "Sweet Potato");
        assertAll(IngredientCategory.FRUIT, "banana", "Bananas", "Cherries");
        assertAll(IngredientCategory.GRAIN, "bread", "rice", "pasta", "spaghetti", "oats", "flour",
                "lentils", "maize meal", "baked beans", "Egg Noodles");
        assertAll(IngredientCategory.OIL, "oil", "olive oil", "soy sauce", "honey", "mayonnaise");
        assertAll(IngredientCategory.SPICE, "salt", "sugar", "curry powder", "baking powder",
                "vegetable stock", "Black Pepper");
    }

    @Test
    public void lookAlikesAreNotMistakenForTheirLastWord() {
        // "peanut butter" is a nut spread, not dairy; "bell pepper" is a vegetable, not a spice
        assertAll(IngredientCategory.GRAIN, "peanut butter");
        assertAll(IngredientCategory.TOMATO, "bell pepper");
        // the noun a name ends with decides: cream CHEESE, chicken STOCK
        assertAll(IngredientCategory.CHEESE, "cream cheese");
        assertAll(IngredientCategory.SPICE, "chicken stock");
    }

    @Test
    public void unknownOrEmptyNamesGetTheJar() {
        assertAll(IngredientCategory.OTHER, "", "   ", "Xyzzy", "Tea", "123");
    }
}
