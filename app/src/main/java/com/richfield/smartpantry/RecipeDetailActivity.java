package com.richfield.smartpantry;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.richfield.smartpantry.data.DatabaseHelper;
import com.richfield.smartpantry.data.RecipeRepository;
import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.util.IngredientIcons;
import com.richfield.smartpantry.util.IngredientMatcher;
import com.richfield.smartpantry.util.Prefs;

import java.util.Map;

/** Recipe Detail screen: full ingredient list (with have / missing marks) and method. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        DatabaseHelper db = new DatabaseHelper(this);
        Recipe recipe = new RecipeRepository(db).getById(getIntent().getLongExtra(EXTRA_RECIPE_ID, -1));
        if (recipe == null) {
            finish();
            return;
        }

        Map<String, double[]> stock =
                IngredientMatcher.buildStock(db.getAllItems(), Prefs.ignoreExpired(this));
        MatchResult match = IngredientMatcher.analyse(recipe, stock);

        toolbar.setTitle(recipe.getName());
        ((TextView) findViewById(R.id.tvDetailName)).setText(recipe.getName());
        ((TextView) findViewById(R.id.tvDetailMeta)).setText(
                recipe.getPrepMinutes() + " min  \u2022  " + recipe.getIngredients().size() + " ingredients");
        ((TextView) findViewById(R.id.tvDetailDescription)).setText(recipe.getDescription());

        TextView tvBanner = findViewById(R.id.tvBanner);
        if (match.isFullMatch()) {
            tvBanner.setText("You have everything you need to cook this right now.");
            tvBanner.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess));
            tvBanner.setBackgroundResource(R.drawable.bg_banner_success);
        } else {
            tvBanner.setText("Missing: " + match.missingSummary());
            tvBanner.setTextColor(ContextCompat.getColor(this, R.color.colorWarning));
            tvBanner.setBackgroundResource(R.drawable.bg_banner_warning);
        }

        LinearLayout container = findViewById(R.id.ingredientsContainer);
        LayoutInflater inflater = LayoutInflater.from(this);
        int okColor = ContextCompat.getColor(this, R.color.colorSuccess);
        int badColor = ContextCompat.getColor(this, R.color.colorDanger);
        for (MatchResult.IngredientStatus st : match.getStatuses()) {
            View row = inflater.inflate(R.layout.row_ingredient, container, false);
            ImageView icon = row.findViewById(R.id.ivStatus);
            IngredientIcons.bind(row.findViewById(R.id.ivIcon), st.ingredient.getName());
            TextView line = row.findViewById(R.id.tvLine);
            TextView note = row.findViewById(R.id.tvNote);

            icon.setImageResource(st.available ? R.drawable.ic_check_circle : R.drawable.ic_cancel);
            icon.setColorFilter(st.available ? okColor : badColor);
            icon.setContentDescription(getString(st.available ? R.string.status_have : R.string.status_missing));

            line.setText(IngredientMatcher.capitalise(st.ingredient.getName()) + " \u2013 "
                    + IngredientMatcher.formatQuantity(st.ingredient.getQuantity()) + " "
                    + st.ingredient.getUnit());
            if (!st.available) {
                line.setTypeface(line.getTypeface(), Typeface.BOLD);
                note.setText(IngredientMatcher.capitalise(st.note));
                note.setVisibility(View.VISIBLE);
            }
            container.addView(row);
        }

        ((TextView) findViewById(R.id.tvSteps)).setText(recipe.getSteps());
    }
}
