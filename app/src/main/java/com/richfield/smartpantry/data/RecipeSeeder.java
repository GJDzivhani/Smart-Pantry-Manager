package com.richfield.smartpantry.data;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

/**
 * Pre-loads the recipe collection. Safe to run more than once: a recipe whose name is already in
 * the database is skipped, so an app update can add new recipes without creating duplicates.
 * Ingredient format: "name|quantity|unit"  (units: pcs, g, kg, ml, l, tsp, tbsp, cup, oz, lb)
 */
final class RecipeSeeder {

    private RecipeSeeder() { }

    static void seed(SQLiteDatabase db) {

        add(db, "Scrambled Eggs on Toast", "A fast, protein-packed breakfast.", 10,
                steps("Whisk the eggs with the salt.",
                        "Melt the butter in a pan over medium heat.",
                        "Pour in the eggs and stir gently until just set.",
                        "Toast the bread and serve the eggs on top."),
                "eggs|3|pcs", "bread|2|pcs", "butter|10|g", "salt|1|tsp");

        add(db, "Cheese Omelette", "Fluffy omelette with melted cheese.", 10,
                steps("Beat the eggs with the salt.",
                        "Melt the butter in a non-stick pan.",
                        "Pour in the eggs and cook until the base sets.",
                        "Sprinkle over the cheese, fold in half and serve."),
                "eggs|3|pcs", "cheese|40|g", "butter|10|g", "salt|1|tsp");

        add(db, "Veggie Omelette", "A colourful omelette that uses up odd vegetables.", 15,
                steps("Dice the onion, tomato and bell pepper.",
                        "Fry the vegetables in the oil for 4 minutes.",
                        "Beat the eggs with the salt and pour over the vegetables.",
                        "Add the cheese, cook until set and fold."),
                "eggs|3|pcs", "onion|1|pcs", "tomato|1|pcs", "bell pepper|1|pcs",
                "cheese|30|g", "oil|1|tbsp", "salt|1|tsp");

        add(db, "Tomato Pasta", "Simple pasta in a fresh garlic-tomato sauce.", 25,
                steps("Boil the pasta in salted water until al dente.",
                        "Fry the chopped onion and garlic in olive oil.",
                        "Add the chopped tomatoes and simmer for 12 minutes.",
                        "Toss the drained pasta through the sauce and serve."),
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs",
                "olive oil|2|tbsp", "salt|1|tsp");

        add(db, "Beef Mince Spaghetti", "A hearty Bolognese-style supper.", 35,
                steps("Brown the beef mince with the onion and garlic.",
                        "Add the chopped tomatoes and salt; simmer for 20 minutes.",
                        "Cook the spaghetti until al dente.",
                        "Serve the sauce over the spaghetti."),
                "spaghetti|250|g", "beef mince|300|g", "onion|1|pcs", "tomato|4|pcs",
                "garlic|2|pcs", "oil|1|tbsp", "salt|1|tsp");

        add(db, "Tuna Mayo Pasta", "Cold or warm pasta tossed in creamy tuna.", 15,
                steps("Cook the pasta and let it cool slightly.",
                        "Drain the tuna and flake it into a bowl.",
                        "Stir in the mayonnaise, finely chopped onion and salt.",
                        "Fold the pasta through the mixture."),
                "pasta|200|g", "tuna|1|pcs", "mayonnaise|3|tbsp", "onion|1|pcs", "salt|1|tsp");

        add(db, "Garlic Butter Rice", "Fragrant side dish in 20 minutes.", 20,
                steps("Rinse the rice and cook it in lightly salted water.",
                        "Melt the butter and fry the crushed garlic for 1 minute.",
                        "Stir the garlic butter through the hot rice."),
                "rice|200|g", "butter|30|g", "garlic|3|pcs", "salt|1|tsp");

        add(db, "Egg Fried Rice", "The classic way to use leftover rice.", 15,
                steps("Heat the oil in a wok and fry the chopped onion.",
                        "Push the onion aside and scramble the eggs.",
                        "Add the cooked rice and soy sauce; stir-fry for 4 minutes."),
                "rice|300|g", "eggs|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");

        add(db, "Chicken and Rice Bowl", "Pan-seared chicken on fluffy rice.", 30,
                steps("Cook the rice.",
                        "Slice the chicken and fry it with the onion in oil.",
                        "Season with salt and cook until golden.",
                        "Serve the chicken over the rice."),
                "chicken breast|300|g", "rice|200|g", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");

        add(db, "Vegetable Stir-Fry", "Quick, crunchy and full of vegetables.", 15,
                steps("Slice the carrots, onion and bell pepper.",
                        "Heat the oil and fry the garlic for 30 seconds.",
                        "Add the vegetables and stir-fry for 6 minutes.",
                        "Finish with the soy sauce."),
                "carrots|2|pcs", "onion|1|pcs", "bell pepper|1|pcs", "soy sauce|2|tbsp",
                "oil|1|tbsp", "garlic|2|pcs");

        add(db, "Tomato Soup", "Warm and comforting soup from simple staples.", 30,
                steps("Fry the onion and garlic in olive oil until soft.",
                        "Add the chopped tomatoes and the stock; simmer for 20 minutes.",
                        "Blend until smooth and season with salt."),
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "vegetable stock|500|ml",
                "olive oil|1|tbsp", "salt|1|tsp");

        add(db, "Lentil Curry", "Budget-friendly, filling and full of flavour.", 40,
                steps("Fry the onion and garlic in the oil.",
                        "Stir in the curry powder for 1 minute.",
                        "Add the rinsed lentils, chopped tomatoes and 500 ml water.",
                        "Simmer for 25 minutes until the lentils are soft."),
                "lentils|200|g", "onion|1|pcs", "tomato|2|pcs", "garlic|3|pcs",
                "curry powder|2|tsp", "oil|1|tbsp");

        add(db, "Cheese Toastie", "Crispy outside, gooey inside.", 10,
                steps("Butter one side of every slice of bread.",
                        "Place the cheese between the unbuttered sides.",
                        "Toast in a pan for 3 minutes per side."),
                "bread|4|pcs", "cheese|80|g", "butter|20|g");

        add(db, "Garlic Bread", "Golden, buttery and great with soup.", 15,
                steps("Mash the crushed garlic into the soft butter.",
                        "Spread over the bread.",
                        "Bake at 200 C for 8 minutes until golden."),
                "bread|4|pcs", "butter|40|g", "garlic|3|pcs");

        add(db, "Mini Pizza Toast", "Pizza flavours on a slice of bread.", 15,
                steps("Toast the bread lightly.",
                        "Top with sliced tomato, olive oil and cheese.",
                        "Grill until the cheese bubbles."),
                "bread|4|pcs", "tomato|2|pcs", "cheese|100|g", "olive oil|1|tbsp");

        add(db, "French Toast", "Sweet eggy bread for a slow morning.", 15,
                steps("Whisk the eggs, milk and sugar.",
                        "Soak each slice of bread in the mixture.",
                        "Fry in the butter until golden on both sides."),
                "bread|4|pcs", "eggs|2|pcs", "milk|100|ml", "sugar|1|tbsp", "butter|15|g");

        add(db, "Pancakes", "Light, fluffy pancakes from pantry basics.", 20,
                steps("Mix the flour, baking powder and sugar.",
                        "Whisk in the milk and eggs to a smooth batter.",
                        "Fry ladlefuls in melted butter until bubbles form, then flip."),
                "flour|200|g", "milk|300|ml", "eggs|2|pcs", "sugar|2|tbsp",
                "butter|20|g", "baking powder|2|tsp");

        add(db, "Banana Oat Porridge", "Creamy oats sweetened with banana and honey.", 10,
                steps("Simmer the oats in the milk for 5 minutes, stirring.",
                        "Slice in the banana.",
                        "Drizzle with honey and serve."),
                "oats|80|g", "milk|250|ml", "banana|1|pcs", "honey|1|tbsp");

        add(db, "Peanut Butter Banana Toast", "Energy-boosting snack or breakfast.", 5,
                steps("Toast the bread.",
                        "Spread with peanut butter.",
                        "Top with sliced banana and a drizzle of honey."),
                "bread|2|pcs", "peanut butter|2|tbsp", "banana|1|pcs", "honey|1|tbsp");

        add(db, "Fruit Yogurt Bowl", "A cool, no-cook breakfast.", 5,
                steps("Spoon the yogurt into a bowl.",
                        "Slice the banana on top.",
                        "Sprinkle with oats and drizzle with honey."),
                "yogurt|200|g", "banana|1|pcs", "honey|1|tbsp", "oats|30|g");

        add(db, "Mashed Potatoes", "Smooth and buttery comfort food.", 30,
                steps("Peel and boil the potatoes until tender.",
                        "Drain and mash with the butter and warm milk.",
                        "Season with salt."),
                "potatoes|500|g", "butter|40|g", "milk|60|ml", "salt|1|tsp");

        add(db, "Cheesy Potato Bake", "Creamy sliced potatoes under a cheese crust.", 60,
                steps("Slice the potatoes thinly and layer in a dish.",
                        "Pour over the milk, dot with butter and season with salt.",
                        "Cover with cheese and bake at 190 C for 45 minutes."),
                "potatoes|600|g", "cheese|100|g", "milk|200|ml", "butter|20|g", "salt|1|tsp");

        // ---------------------------------------------------------------- added in database v3

        add(db, "Egg Mayo Sandwich", "Creamy egg filling between fresh bread.", 15,
                steps("Boil the eggs for 9 minutes, cool and peel them.",
                        "Mash the eggs with the mayonnaise and salt.",
                        "Spread over half of the bread and top with the rest."),
                "eggs|3|pcs", "bread|4|pcs", "mayonnaise|2|tbsp", "salt|0.5|tsp");

        add(db, "Beans on Toast", "Ready in five minutes and endlessly satisfying.", 5,
                steps("Toast the bread and spread it with butter.",
                        "Warm the baked beans in a small pot.",
                        "Spoon the beans over the toast."),
                "baked beans|1|pcs", "bread|2|pcs", "butter|10|g");

        add(db, "Tuna Melt", "Crispy toasted sandwich with tuna and melted cheese.", 12,
                steps("Drain the tuna and mix it with the mayonnaise.",
                        "Spread over half the bread, top with cheese and close the sandwiches.",
                        "Toast in a pan or grill until golden and the cheese has melted."),
                "tuna|1|pcs", "bread|4|pcs", "cheese|60|g", "mayonnaise|2|tbsp");

        add(db, "Spaghetti Aglio e Olio", "Garlic and olive oil: the simplest spaghetti there is.", 20,
                steps("Cook the spaghetti in salted water until al dente.",
                        "Gently warm the sliced garlic in the olive oil for 2 minutes.",
                        "Toss the drained spaghetti through the garlic oil and season with salt."),
                "spaghetti|200|g", "garlic|4|pcs", "olive oil|4|tbsp", "salt|1|tsp");

        add(db, "Macaroni Cheese", "Baked-style comfort food with a silky cheese sauce.", 30,
                steps("Cook the pasta until just tender and drain.",
                        "Melt the butter, stir in the flour and cook for 1 minute.",
                        "Whisk in the milk and simmer until thick, then stir in the cheese and salt.",
                        "Mix the pasta through the sauce and serve."),
                "pasta|250|g", "butter|30|g", "flour|30|g", "milk|400|ml", "cheese|150|g", "salt|1|tsp");

        add(db, "Chicken Fried Rice", "A takeaway favourite made at home.", 25,
                steps("Cook the rice and let it cool.",
                        "Fry the diced chicken and onion in the oil until cooked through.",
                        "Push to one side, scramble the eggs, then stir everything together.",
                        "Add the rice and soy sauce and stir-fry for 4 minutes."),
                "chicken breast|250|g", "rice|300|g", "eggs|2|pcs", "onion|1|pcs",
                "soy sauce|2|tbsp", "oil|1|tbsp");

        add(db, "Chicken Curry", "A fragrant, medium-spiced curry for rice or roti.", 50,
                steps("Fry the chopped onion in the oil until golden.",
                        "Add the garlic and curry powder and stir for 1 minute.",
                        "Add the chicken and brown on all sides.",
                        "Add the chopped tomatoes and salt, cover and simmer for 30 minutes."),
                "chicken breast|400|g", "onion|2|pcs", "tomato|3|pcs", "garlic|3|pcs",
                "curry powder|2|tbsp", "oil|2|tbsp", "salt|1|tsp");

        add(db, "Chicken and Potato Bake", "One tray, minimal washing up.", 55,
                steps("Heat the oven to 200 C.",
                        "Toss the chicken, chunks of potato and sliced onion with the oil and salt.",
                        "Roast on a tray for 40 minutes, turning once, until golden."),
                "chicken breast|400|g", "potatoes|500|g", "onion|1|pcs", "oil|2|tbsp", "salt|1|tsp");

        add(db, "Chicken Stir-Fry", "Quick weeknight dinner with crunchy vegetables.", 20,
                steps("Slice the chicken, carrots, bell pepper and onion.",
                        "Fry the chicken in the oil until golden, then remove.",
                        "Stir-fry the vegetables for 5 minutes.",
                        "Return the chicken, add the soy sauce and toss for 2 minutes."),
                "chicken breast|300|g", "carrots|2|pcs", "bell pepper|1|pcs", "onion|1|pcs",
                "soy sauce|2|tbsp", "oil|1|tbsp");

        add(db, "Mince Curry", "Hearty, quick-cooking curry with potato.", 40,
                steps("Fry the onion and garlic in the oil until soft.",
                        "Stir in the curry powder, then brown the mince.",
                        "Add the chopped tomatoes, diced potato and salt with a cup of water.",
                        "Simmer for 25 minutes until the potato is tender."),
                "beef mince|400|g", "onion|1|pcs", "tomato|2|pcs", "potatoes|300|g",
                "garlic|2|pcs", "curry powder|2|tbsp", "oil|1|tbsp", "salt|1|tsp");

        add(db, "Cottage Pie", "Savoury mince under a golden mash.", 60,
                steps("Boil the potatoes until soft, then mash with the butter, milk and salt.",
                        "Brown the mince with the diced onion and carrots.",
                        "Spoon the mince into a dish and cover with the mash.",
                        "Bake at 200 C for 25 minutes until golden."),
                "beef mince|400|g", "potatoes|600|g", "onion|1|pcs", "carrots|2|pcs",
                "butter|30|g", "milk|80|ml", "salt|1|tsp");

        add(db, "Mince and Rice", "A one-pot family dinner.", 35,
                steps("Fry the onion in the oil, then brown the mince.",
                        "Add the chopped tomatoes and salt.",
                        "Stir in the rice with 400 ml water, cover and cook for 18 minutes."),
                "beef mince|300|g", "rice|200|g", "onion|1|pcs", "tomato|2|pcs",
                "oil|1|tbsp", "salt|1|tsp");

        add(db, "Pap with Tomato Gravy", "South African comfort food: soft pap with a tomato and onion gravy.", 35,
                steps("Cook the maize meal in salted boiling water, stirring, until thick and stiff.",
                        "Fry the sliced onion in the oil until soft.",
                        "Add the chopped tomatoes and simmer into a thick gravy.",
                        "Serve the gravy over the pap."),
                "maize meal|250|g", "tomato|3|pcs", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");

        add(db, "Roast Potatoes", "Crisp outside, fluffy inside.", 50,
                steps("Heat the oven to 220 C.",
                        "Cut the potatoes into chunks, toss with the oil and salt.",
                        "Roast for 40 minutes, turning halfway."),
                "potatoes|600|g", "oil|3|tbsp", "salt|1|tsp");

        add(db, "Potato and Egg Omelette", "A Spanish-style tortilla from four basics.", 30,
                steps("Fry thinly sliced potato and onion in the oil until soft.",
                        "Beat the eggs with the salt and stir in the potato mixture.",
                        "Cook in the pan until set underneath, then flip and finish the other side."),
                "potatoes|400|g", "eggs|4|pcs", "onion|1|pcs", "oil|3|tbsp", "salt|1|tsp");

        add(db, "Vegetable Soup", "Thick, warming soup from everyday vegetables.", 40,
                steps("Fry the chopped onion in the oil.",
                        "Add the diced carrots and potatoes and the stock.",
                        "Simmer for 25 minutes until soft, season with salt and blend if you like."),
                "carrots|3|pcs", "potatoes|300|g", "onion|1|pcs", "vegetable stock|750|ml",
                "oil|1|tbsp", "salt|1|tsp");

        add(db, "Lentils and Rice", "Cheap, filling and full of flavour (also known as mujadara).", 40,
                steps("Fry the sliced onions in the oil until deep golden; set a few aside.",
                        "Add the lentils, rice, salt and 600 ml water.",
                        "Cover and simmer for 25 minutes, then top with the fried onions."),
                "lentils|150|g", "rice|150|g", "onion|2|pcs", "oil|2|tbsp", "salt|1|tsp");

        add(db, "Tuna and Rice Salad", "A cold lunch that keeps well.", 20,
                steps("Cook the rice and let it cool.",
                        "Drain the tuna and flake it over the rice.",
                        "Add the chopped tomato and onion, drizzle with olive oil and mix."),
                "tuna|1|pcs", "rice|200|g", "tomato|2|pcs", "onion|1|pcs", "olive oil|2|tbsp");

        add(db, "Banana Oat Pancakes", "Naturally sweet pancakes with no flour.", 15,
                steps("Mash the bananas and mix in the eggs and oats.",
                        "Melt a little butter in a pan.",
                        "Cook spoonfuls for 2 minutes per side until golden."),
                "banana|2|pcs", "eggs|2|pcs", "oats|60|g", "butter|10|g");

        add(db, "Banana Smoothie", "Creamy, cold and ready in two minutes.", 5,
                steps("Peel the bananas and add them to a blender.",
                        "Add the milk, yogurt and honey.",
                        "Blend until smooth and pour into glasses."),
                "banana|2|pcs", "milk|300|ml", "yogurt|100|g", "honey|1|tbsp");

        add(db, "Cheese Scones", "Light savoury scones, perfect warm with butter.", 30,
                steps("Heat the oven to 220 C.",
                        "Rub the butter into the flour, baking powder and salt.",
                        "Stir in the cheese and enough milk to make a soft dough.",
                        "Cut into rounds and bake for 15 minutes until golden."),
                "flour|300|g", "baking powder|4|tsp", "butter|60|g", "cheese|100|g",
                "milk|150|ml", "salt|0.5|tsp");

        add(db, "Creamy Rice Pudding", "Slow-cooked dessert with a golden, sweet finish.", 45,
                steps("Put the rice, milk and sugar in a pot over low heat.",
                        "Simmer for 35 minutes, stirring often, until thick and creamy.",
                        "Stir in the butter and serve warm."),
                "rice|100|g", "milk|600|ml", "sugar|3|tbsp", "butter|10|g");
    }

    /** Numbers the steps: "1. ...\n2. ...". */
    private static String steps(String... s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            if (i > 0) sb.append('\n');
            sb.append(i + 1).append(". ").append(s[i]);
        }
        return sb.toString();
    }

    private static void add(SQLiteDatabase db, String name, String description, int minutes,
                            String steps, String... ingredients) {
        // already in the database (e.g. after an app update)? leave it alone
        if (DatabaseUtils.queryNumEntries(db, DatabaseHelper.T_RECIPES, "name = ?",
                new String[]{name}) > 0) return;

        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("description", description);
        cv.put("steps", steps);
        cv.put("prep_minutes", minutes);
        long recipeId = db.insert(DatabaseHelper.T_RECIPES, null, cv);

        for (String line : ingredients) {
            String[] p = line.split("\\|");
            ContentValues ic = new ContentValues();
            ic.put("recipe_id", recipeId);
            ic.put("name", p[0]);
            ic.put("quantity", Double.parseDouble(p[1]));
            ic.put("unit", p[2]);
            db.insert(DatabaseHelper.T_INGREDIENTS, null, ic);
        }
    }
}
