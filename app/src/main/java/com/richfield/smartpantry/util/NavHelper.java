package com.richfield.smartpantry.util;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.richfield.smartpantry.MainActivity;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.SettingsActivity;
import com.richfield.smartpantry.SuggestedRecipesActivity;

/**
 * Wires the shared bottom navigation bar.
 *
 * Back-stack rules (so the Back button behaves like users expect):
 *  - Pantry is the home screen and always sits at the bottom of the stack.
 *  - Recipes and Settings replace each other (the old one is finished), so Back from either
 *    returns to Pantry and Back from Pantry leaves the app.
 *  - Tapping Pantry clears everything above it instead of stacking a second copy.
 *
 * The listener returns false after starting the new screen so this screen's bar keeps
 * highlighting ITS OWN tab (previously the highlight could be left on the wrong tab).
 */
public final class NavHelper {

    private NavHelper() { }

    public static void setup(final Activity activity, BottomNavigationView nav, final int selectedId) {
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;           // already here

            Intent intent;
            if (id == R.id.nav_pantry) {
                intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else {
                Class<?> target = (id == R.id.nav_recipes)
                        ? SuggestedRecipesActivity.class : SettingsActivity.class;
                intent = new Intent(activity, target);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);

            // Recipes <-> Settings replace each other rather than piling up.
            if (selectedId != R.id.nav_pantry && id != R.id.nav_pantry) activity.finish();
            return false;                                // keep our own tab highlighted
        });
    }
}
