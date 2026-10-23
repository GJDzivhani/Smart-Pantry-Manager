package com.richfield.smartpantry.util;

import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * THE STRICT-MATCHING ENGINE (assignment section 2.3).
 *
 * A recipe is only "suggested" when EVERY ingredient it needs is in the pantry in at least
 * the required quantity. One missing / insufficient ingredient => the recipe is excluded.
 *
 * To survive real-world messiness the engine:
 *   1. Normalises names     : case, punctuation, brackets, plural/singular
 *                             ("Tomatoes" == "tomato", "Cherries" == "cherry")
 *   2. Knows a few synonyms : yoghurt/yogurt, courgette/zucchini, capsicum/bell pepper ...
 *   3. Converts units       : kg -> g, l -> ml, cup/tbsp/tsp -> ml, oz/lb -> g
 *                             (mass and volume are compared on a rough 1 g = 1 ml basis,
 *                             which is the usual kitchen approximation)
 *   4. Adds up duplicates   : two pantry rows of "milk" are summed before comparing.
 *   5. Counts separately    : "pcs" (eggs, onions ...) never matches grams/millilitres.
 *   6. Ignores filler words : "Fresh", "Large", "Full cream", "Unsalted", "Extra virgin" ...
 *                             ("Full Cream Milk" == "milk", "Large Eggs" == "egg")
 *   7. Accepts specific     : a recipe that asks for plain "oil" is satisfied by "olive oil",
 *      types of staples       "sunflower oil" ...; "milk" by "full cream milk"; "pasta" by
 *                             "spaghetti" or "macaroni"; "cheese" by "cheddar". It works one
 *                             way only: a recipe that needs "olive oil" is NOT satisfied by
 *                             "sunflower oil", and look-alikes never count ("peanut butter"
 *                             is not "butter", "coconut milk" is not "milk", "cream cheese"
 *                             is not "cheese").
 */
public final class IngredientMatcher {

    private static final double EPS = 1e-6;

    /**
     * Staples where a more specific pantry item ("olive oil", "sea salt", "full cream milk") can
     * stand in for the generic name a recipe uses ("oil", "salt", "milk"). The value is the set of
     * look-alike words that must NOT count ("peanut butter" is not butter).
     */
    private static final Map<String, Set<String>> GENERIC_TYPES = new HashMap<>();

    /** Single-word pantry names that also count as a generic staple ("spaghetti" is pasta). */
    private static final Map<String, String> ALSO_COUNTS_AS = new HashMap<>();

    /** Multi-word fillers removed before comparing names. */
    private static final Pattern FILLER_PHRASES = Pattern.compile(
            "\\b(full cream|low fat|fat free|free range|extra virgin|semi skimmed|long life)\\b");

    /** Single-word fillers removed before comparing names. */
    private static final Set<String> FILLER_WORDS = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "ripe", "whole", "raw", "organic",
            "skimmed", "skim", "boneless", "skinless", "salted", "unsalted", "plain", "natural"));

    /** Spelling variants fixed word by word, so "Greek Yoghurt" == "greek yogurt". */
    private static final Map<String, String> WORD_VARIANTS = new HashMap<>();

    private static final Map<String, String> SYNONYMS = new HashMap<>();
    private static final Map<String, String> UNIT_ALIASES = new HashMap<>();
    private static final Map<String, Double> UNIT_FACTORS = new HashMap<>();

    static {
        // ---- generic staples (word -> look-alikes that must not count) ----
        generic("oil");
        generic("flour");
        generic("rice");
        generic("pasta");
        generic("yogurt", "frozen");
        generic("salt", "garlic", "celery", "onion");
        generic("sugar", "icing");
        generic("bread", "garlic", "banana");
        generic("cheese", "cream", "cottage");
        generic("butter", "peanut", "almond", "cashew", "cocoa", "shea");
        generic("milk", "coconut", "almond", "soy", "soya", "oat", "rice", "cashew",
                "condensed", "evaporated", "powdered", "chocolate");
        for (String p : new String[]{"spaghetti", "macaroni", "penne", "fusilli", "tagliatelle", "linguine"})
            ALSO_COUNTS_AS.put(p, "pasta");
        for (String c : new String[]{"cheddar", "mozzarella", "gouda", "parmesan", "edam"})
            ALSO_COUNTS_AS.put(c, "cheese");

        WORD_VARIANTS.put("yoghurt", "yogurt");
        WORD_VARIANTS.put("mozarella", "mozzarella");
        WORD_VARIANTS.put("parmesean", "parmesan");
        WORD_VARIANTS.put("chilli", "chili");

        // ---- name synonyms (keys/values are already singular + lower-case) ----
        SYNONYMS.put("mince", "beef mince");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("chicken", "chicken breast");
        SYNONYMS.put("chicken fillet", "chicken breast");
        SYNONYMS.put("mayo", "mayonnaise");
        SYNONYMS.put("mielie meal", "maize meal");
        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("green pepper", "bell pepper");
        SYNONYMS.put("red pepper", "bell pepper");
        SYNONYMS.put("yellow pepper", "bell pepper");
        SYNONYMS.put("tinned tuna", "tuna");
        SYNONYMS.put("canned tuna", "tuna");
        SYNONYMS.put("tin tuna", "tuna");
        SYNONYMS.put("veg stock", "vegetable stock");
        SYNONYMS.put("yoghurt", "yogurt");
        SYNONYMS.put("aubergine", "eggplant");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("capsicum", "bell pepper");
        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("coriander", "cilantro");

        // ---- units: canonical name, factor to base unit (g / ml / piece), aliases ----
        unit("g", 1, "gram", "gm", "gms");
        unit("kg", 1000, "kilogram", "kilo", "kgs");
        unit("oz", 28.3495, "ounce");
        unit("lb", 453.592, "lbs", "pound");
        unit("ml", 1, "millilitre", "milliliter", "mls");
        unit("l", 1000, "litre", "liter", "ltr");
        unit("tsp", 5, "teaspoon");
        unit("tbsp", 15, "tablespoon", "tbs");
        unit("cup", 240);
        unit("pcs", 1, "pc", "piece", "whole", "each", "unit", "item");
    }

    private IngredientMatcher() { }

    private static void generic(String word, String... lookAlikes) {
        GENERIC_TYPES.put(word, new HashSet<>(Arrays.asList(lookAlikes)));
    }

    private static void unit(String canonical, double factor, String... aliases) {
        UNIT_FACTORS.put(canonical, factor);
        UNIT_ALIASES.put(canonical, canonical);
        for (String a : aliases) UNIT_ALIASES.put(a, canonical);
    }

    // ------------------------------------------------------------------ names

    /** "Fresh Tomatoes (ripe)" -> "tomato";  "Full Cream Milk" -> "milk";  "Ground Beef" -> "beef mince". */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT)
                .replaceAll("\\(.*?\\)", " ")        // drop "(ripe)"
                .replaceAll("[^\\p{L}\\s]", " ")     // drop digits / punctuation
                .replaceAll("\\s+", " ")
                .trim();
        if (s.isEmpty()) return "";

        String simplified = FILLER_PHRASES.matcher(s).replaceAll(" ").replaceAll("\\s+", " ").trim();
        StringBuilder sb = new StringBuilder();
        for (String word : simplified.split(" ")) {
            if (word.isEmpty() || FILLER_WORDS.contains(word)) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(variant(singular(word)));
        }
        if (sb.length() == 0) {                         // the name was ONLY filler words: keep it as typed
            for (String word : s.split(" ")) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(variant(singular(word)));
            }
        }
        String key = sb.toString();
        String syn = SYNONYMS.get(key);
        return syn != null ? syn : key;
    }

    private static String variant(String w) {
        String v = WORD_VARIANTS.get(w);
        return v != null ? v : w;
    }

    /** Very small rule-based singulariser - enough for kitchen vocabulary. */
    static String singular(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";   // cherries -> cherry
        if (w.endsWith("oes")) return w.substring(0, w.length() - 2);         // tomatoes -> tomato
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("xes") || w.endsWith("sses")) {
            return w.substring(0, w.length() - 2);                            // peaches -> peach
        }
        if (w.endsWith("ss") || w.endsWith("us") || w.endsWith("is")) return w; // hummus, asparagus
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);           // eggs -> egg
        return w;
    }

    // ------------------------------------------------------------------ units

    /** Maps "Grams", "kg.", "tablespoons" ... to the canonical unit; unknown -> "pcs". */
    public static String canonicalUnit(String raw) {
        if (raw == null) return "pcs";
        String u = raw.trim().toLowerCase(Locale.ROOT).replace(".", "");
        String c = UNIT_ALIASES.get(u);
        if (c == null && u.endsWith("s")) c = UNIT_ALIASES.get(u.substring(0, u.length() - 1)); // "grams"
        return c != null ? c : "pcs";
    }

    private static boolean isCount(String canonicalUnit) {
        return "pcs".equals(canonicalUnit);
    }

    // ------------------------------------------------------------------ stock

    /**
     * Totals the pantry per normalised ingredient name.
     * Value array: [0] = total "measure" (g or ml), [1] = total pieces.
     */
    public static Map<String, double[]> buildStock(List<PantryItem> pantry, boolean ignoreExpired) {
        Map<String, double[]> stock = new HashMap<>();
        for (PantryItem p : pantry) {
            if (ignoreExpired) {
                Long days = p.getDaysUntilExpiry();
                if (days != null && days < 0) continue;      // expired -> not usable
            }
            String key = normalizeName(p.getName());
            if (key.isEmpty()) continue;
            String unit = canonicalUnit(p.getUnit());
            double base = p.getQuantity() * UNIT_FACTORS.get(unit);
            addToStock(stock, key, unit, base);

            // "olive oil" also counts as plain "oil", "spaghetti" as "pasta", and so on.
            String generic = genericFor(key);
            if (generic != null) addToStock(stock, generic, unit, base);
        }
        return stock;
    }

    /** The generic staple a specific pantry name also counts as, or null. */
    private static String genericFor(String key) {
        String also = ALSO_COUNTS_AS.get(key);
        if (also != null) return also;
        int sp = key.lastIndexOf(' ');
        if (sp <= 0) return null;
        String last = key.substring(sp + 1);
        Set<String> lookAlikes = GENERIC_TYPES.get(last);
        if (lookAlikes == null) return null;
        for (String w : key.substring(0, sp).split(" ")) {
            if (lookAlikes.contains(w)) return null;     // "peanut butter" is not butter
        }
        return last;
    }

    private static void addToStock(Map<String, double[]> stock, String key, String unit, double base) {
        double[] totals = stock.get(key);
        if (totals == null) {
            totals = new double[2];
            stock.put(key, totals);
        }
        totals[isCount(unit) ? 1 : 0] += base;
    }

    // ------------------------------------------------------------------ matching

    /** Checks every ingredient of ONE recipe against the pantry stock. */
    public static MatchResult analyse(Recipe recipe, Map<String, double[]> stock) {
        List<MatchResult.IngredientStatus> list = new ArrayList<>();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            String key = normalizeName(ing.getName());
            String unit = canonicalUnit(ing.getUnit());
            double factor = UNIT_FACTORS.get(unit);
            double required = ing.getQuantity() * factor;
            double[] have = stock.get(key);
            double available = (have == null) ? 0 : (isCount(unit) ? have[1] : have[0]);

            boolean ok;
            String note;
            if (have == null) {
                ok = false;
                note = "not in pantry";
            } else if (available <= EPS) {
                ok = false;
                note = "in pantry, but in a different kind of unit";
            } else if (available + EPS < required) {
                ok = false;
                note = "need " + formatQuantity(ing.getQuantity()) + " " + unit
                        + ", have " + formatQuantity(available / factor) + " " + unit;
            } else {
                ok = true;
                note = "have " + formatQuantity(available / factor) + " " + unit;
            }
            list.add(new MatchResult.IngredientStatus(ing, ok, note));
        }
        return new MatchResult(recipe, list);
    }

    /** Runs {@link #analyse} for every recipe. */
    public static List<MatchResult> matchAll(List<Recipe> recipes, List<PantryItem> pantry,
                                             boolean ignoreExpired) {
        Map<String, double[]> stock = buildStock(pantry, ignoreExpired);
        List<MatchResult> results = new ArrayList<>();
        for (Recipe r : recipes) results.add(analyse(r, stock));
        return results;
    }

    // ------------------------------------------------------------------ formatting

    /** 3.0 -> "3", 2.5 -> "2.5", 0.333 -> "0.33". */
    public static String formatQuantity(double q) {
        if (Math.abs(q - Math.rint(q)) < 0.005) return String.valueOf((long) Math.rint(q));
        String s = String.format(Locale.US, "%.2f", q);
        if (s.contains(".")) s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }

    /** "olive oil" -> "Olive oil". */
    public static String capitalise(String s) {
        if (s == null || s.isEmpty()) return "";
        String t = s.trim();
        return t.substring(0, 1).toUpperCase(Locale.getDefault()) + t.substring(1);
    }
}
