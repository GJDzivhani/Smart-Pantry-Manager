package com.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.richfield.smartpantry.adapter.PantryAdapter;
import com.google.android.material.card.MaterialCardView;
import com.richfield.smartpantry.data.DatabaseHelper;
import com.richfield.smartpantry.data.RecipeRepository;
import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.util.IngredientMatcher;
import com.richfield.smartpantry.util.NavHelper;
import com.richfield.smartpantry.util.Prefs;

import java.util.List;

/**
 * Pantry List screen (launcher). READ + DELETE happen here; CREATE / UPDATE are handled by
 * AddEditIngredientActivity, which is started with an explicit Intent.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView rvPantry;
    private View emptyView;
    private TextView tvSummary, tvBannerTitle, tvBannerSub;
    private MaterialCardView bannerRecipes;
    private RecipeRepository recipes;
    private boolean openAlmostTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        recipes = new RecipeRepository(db);

        rvPantry = findViewById(R.id.rvPantry);
        emptyView = findViewById(R.id.emptyView);
        tvSummary = findViewById(R.id.tvSummary);
        bannerRecipes = findViewById(R.id.bannerRecipes);
        tvBannerTitle = findViewById(R.id.tvBannerTitle);
        tvBannerSub = findViewById(R.id.tvBannerSub);
        bannerRecipes.setOnClickListener(v -> {
            Intent intent = new Intent(this, SuggestedRecipesActivity.class);
            // nothing ready yet but some recipes are one ingredient away? open that list instead
            intent.putExtra(SuggestedRecipesActivity.EXTRA_SHOW_ALMOST, openAlmostTab);
            startActivity(intent);
        });

        adapter = new PantryAdapter(this);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        rvPantry.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditIngredientActivity.class)));

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavHelper.setup(this, nav, R.id.nav_pantry);
    }

    /** Reload from the database every time the screen becomes visible again. */
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = db.getAllItems();
        boolean alerts = Prefs.alertsEnabled(this);
        int days = Prefs.alertDays(this);

        adapter.setAlertSettings(alerts, days);
        adapter.setItems(items);

        boolean empty = items.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvPantry.setVisibility(empty ? View.GONE : View.VISIBLE);

        int expired = 0, soon = 0;
        for (PantryItem p : items) {
            Long d = p.getDaysUntilExpiry();
            if (d == null) continue;
            if (d < 0) expired++;
            else if (d <= days) soon++;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(items.size()).append(items.size() == 1 ? " ingredient" : " ingredients");
        if (alerts && soon > 0) sb.append("  \u2022  ").append(soon).append(" expiring soon");
        if (expired > 0) sb.append("  \u2022  ").append(expired).append(" expired");
        tvSummary.setText(sb.toString());

        updateRecipeBanner(items);
    }

    /**
     * Tells the user, right on the Pantry screen, how many recipes they can cook with what they
     * have (strict rule) and how many are only one ingredient away. Tapping it opens Recipes.
     */
    private void updateRecipeBanner(List<PantryItem> items) {
        if (items.isEmpty()) {
            bannerRecipes.setVisibility(View.GONE);
            return;
        }
        int ready = 0, almost = 0;
        for (MatchResult m : recipes.match(items, "", Prefs.ignoreExpired(this))) {
            if (m.isFullMatch()) ready++;
            else if (m.missingCount() == 1) almost++;
        }
        openAlmostTab = (ready == 0 && almost > 0);

        tvBannerTitle.setText(ready == 0 ? "No recipes ready yet"
                : ready == 1 ? "You can cook 1 recipe now"
                : "You can cook " + ready + " recipes now");
        if (almost > 0) {
            tvBannerSub.setText(almost + (almost == 1 ? " more is" : " more are")
                    + " just one ingredient away");
        } else {
            tvBannerSub.setText(ready == 0 ? "Add more ingredients to unlock recipes"
                    : "Tap to see what you can cook");
        }
        bannerRecipes.setVisibility(View.VISIBLE);
    }

    // ---------------------------------------------------------------- adapter callbacks

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());   // pass data
        startActivity(intent);
    }

    @Override
    public void onDelete(final PantryItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete ingredient?")
                .setMessage("Remove " + IngredientMatcher.capitalise(item.getName()) + " from your pantry?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    db.deleteItem(item.getId());
                    loadItems();
                    Snackbar.make(findViewById(R.id.bottomNav), "Ingredient deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> {
                                db.insertItem(item);       // re-create the removed row
                                loadItems();
                            })
                            .show();
                })
                .show();
    }
}
