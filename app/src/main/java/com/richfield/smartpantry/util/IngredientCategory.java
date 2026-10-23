package com.richfield.smartpantry.util;

import com.richfield.smartpantry.R;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The kind of food an ingredient is, used to pick its coloured icon.
 *
 * Ingredient names are free text ("Full Cream Milk", "Tomatoes", "Boerewors"), so the category is
 * worked out from the normalised name (the same normaliser the recipe matcher uses):
 *   1. a few whole-phrase rules win first ("bell pepper" is a vegetable, "peanut butter" is not dairy);
 *   2. otherwise the LAST word that is a known keyword decides, because the noun a name ends with is
 *      usually what it is ("olive OIL", "chicken STOCK", "cream CHEESE");
 *   3. otherwise a keyword hidden inside a longer word still counts ("oatMEAL", "CORNflakes").
 * Anything unrecognised gets the plain jar icon, so every ingredient always has an icon.
 */
public enum IngredientCategory {
    EGG(R.drawable.ic_ing_egg, R.color.cat_egg),
    DAIRY(R.drawable.ic_ing_dairy, R.color.cat_dairy),
    CHEESE(R.drawable.ic_ing_cheese, R.color.cat_cheese),
    MEAT(R.drawable.ic_ing_meat, R.color.cat_meat),
    FISH(R.drawable.ic_ing_fish, R.color.cat_fish),
    TOMATO(R.drawable.ic_ing_tomato, R.color.cat_tomato),        // tomatoes, peppers, chillies
    ROOTS(R.drawable.ic_ing_bulb, R.color.cat_bulb),             // onion, garlic, potato, carrot ...
    GREENS(R.drawable.ic_ing_leaf, R.color.cat_greens),          // leafy veg, herbs, mushrooms ...
    FRUIT(R.drawable.ic_ing_fruit, R.color.cat_fruit),
    GRAIN(R.drawable.ic_ing_grain, R.color.cat_grain),           // bread, rice, pasta, flour, pulses, nuts
    OIL(R.drawable.ic_ing_oil, R.color.cat_oil),                 // oils, sauces, honey, jam
    SPICE(R.drawable.ic_ing_spice, R.color.cat_spice),           // salt, spices, stock, sugar, baking
    OTHER(R.drawable.ic_ing_jar, R.color.cat_other);

    /** Drawable resource of the glyph. */
    public final int icon;
    /** Colour resource used for the glyph (the chip behind it is the same colour, faded). */
    public final int color;

    IngredientCategory(int icon, int color) {
        this.icon = icon;
        this.color = color;
    }

    private static final Map<String, IngredientCategory> PHRASES = new LinkedHashMap<>();
    private static final Map<String, IngredientCategory> KEYWORDS = new LinkedHashMap<>();

    static {
        // ---- whole-phrase rules (checked first, on the normalised name) ----
        PHRASES.put("peanut butter", GRAIN);
        PHRASES.put("nut butter", GRAIN);
        PHRASES.put("almond butter", GRAIN);
        PHRASES.put("cashew butter", GRAIN);
        PHRASES.put("bell pepper", TOMATO);
        PHRASES.put("chili pepper", TOMATO);
        PHRASES.put("green bean", GREENS);
        PHRASES.put("sweet corn", GREENS);
        PHRASES.put("sweetcorn", GREENS);

        // ---- single words (already singular + lower-case, as normalizeName returns them) ----
        words(EGG, "egg");
        words(DAIRY, "milk", "cream", "yogurt", "butter", "buttermilk", "margarine", "ghee",
                "custard", "kefir");
        words(CHEESE, "cheese", "cheddar", "mozzarella", "parmesan", "feta", "gouda", "edam",
                "brie", "halloumi", "ricotta", "paneer");
        words(MEAT, "chicken", "beef", "mince", "pork", "lamb", "mutton", "bacon", "sausage",
                "boerewor", "ham", "steak", "turkey", "meat", "rib", "breast", "thigh",
                "drumstick", "patty", "salami", "biltong", "venison");
        words(FISH, "fish", "tuna", "salmon", "sardine", "pilchard", "hake", "cod", "trout",
                "mackerel", "anchovy", "snoek", "prawn", "shrimp", "calamari", "squid", "seafood");
        words(TOMATO, "tomato", "chili", "capsicum", "radish");
        words(ROOTS, "onion", "garlic", "potato", "carrot", "ginger", "leek", "shallot", "beetroot",
                "beet", "turnip", "parsnip", "pumpkin", "butternut", "squash");
        words(GREENS, "spinach", "lettuce", "cabbage", "broccoli", "cauliflower", "celery",
                "cucumber", "zucchini", "eggplant", "kale", "asparagus", "mushroom", "pea",
                "avocado", "parsley", "basil", "cilantro", "mint", "thyme", "rosemary", "dill",
                "oregano", "herb", "salad", "veg", "vegetable");
        words(FRUIT, "banana", "apple", "orange", "lemon", "lime", "grape", "strawberry", "berry",
                "blueberry", "mango", "pineapple", "peach", "pear", "cherry", "melon", "plum",
                "apricot", "naartjie", "guava", "coconut", "fruit", "raisin");
        words(GRAIN, "bread", "toast", "bun", "roll", "wrap", "tortilla", "cracker", "biscuit",
                "rice", "pasta", "spaghetti", "macaroni", "penne", "fusilli", "linguine",
                "tagliatelle", "noodle", "couscous", "flour", "oat", "cereal", "muesli", "granola",
                "maize", "corn", "meal", "pap", "samp", "polenta", "wheat", "barley", "quinoa",
                "lentil", "bean", "chickpea", "nut", "peanut", "almond", "cashew", "seed");
        words(OIL, "oil", "vinegar", "sauce", "ketchup", "mayonnaise", "mustard", "honey", "syrup",
                "dressing", "marinade", "chutney", "jam", "relish");
        words(SPICE, "salt", "pepper", "spice", "curry", "cumin", "turmeric", "paprika", "cinnamon",
                "cardamom", "masala", "seasoning", "stock", "bouillon", "cube", "powder", "sugar",
                "cocoa", "chocolate", "vanilla", "yeast", "soda", "nutmeg");
    }

    private static void words(IngredientCategory category, String... keywords) {
        for (String k : keywords) KEYWORDS.put(k, category);
    }

    /** The category for a pantry or recipe ingredient name; never null. */
    public static IngredientCategory of(String ingredientName) {
        String name = IngredientMatcher.normalizeName(ingredientName);
        if (name.isEmpty()) return OTHER;

        for (Map.Entry<String, IngredientCategory> e : PHRASES.entrySet()) {
            if (name.contains(e.getKey())) return e.getValue();
        }

        String[] parts = name.split(" ");
        for (int i = parts.length - 1; i >= 0; i--) {
            IngredientCategory c = KEYWORDS.get(parts[i]);
            if (c != null) return c;
        }

        for (Map.Entry<String, IngredientCategory> e : KEYWORDS.entrySet()) {
            if (e.getKey().length() >= 4 && name.contains(e.getKey())) return e.getValue();
        }
        return OTHER;
    }
}
