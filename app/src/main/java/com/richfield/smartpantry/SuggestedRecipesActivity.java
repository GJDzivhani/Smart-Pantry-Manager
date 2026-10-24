package com.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.richfield.smartpantry.adapter.RecipeAdapter;
import com.richfield.smartpantry.data.DatabaseHelper;
import com.richfield.smartpantry.data.RecipeRepository;
import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.util.NavHelper;
import com.richfield.smartpantry.util.Prefs;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipes screen. Three views of the recipe database, all checked against the pantry:
 *   Ready  - STRICT rule: only recipes where every ingredient is available (the main suggestions)
 *   Almost - recipes missing exactly one ingredient (bonus, kept separate)
 *   All    - every recipe, with its have/missing status, so any recipe is easy to look up
 * The search box filters whichever view is open by recipe name OR ingredient (a database query).
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    /** Boolean extra: open on the "Almost" tab (used by the Pantry banner when nothing is ready yet). */
    public static final String EXTRA_SHOW_ALMOST = "extra_show_almost";

    private DatabaseHelper db;
    private RecipeRepository recipes;
    private RecipeAdapter adapter;
    private RecyclerView rvRecipes;
    private View emptyView;
    private TextView tvEmpty, tvInfo;
    private MaterialButton btnReady, btnAlmost, btnAll;
    private MaterialButtonToggleGroup toggle;
    private TextInputEditText etSearch;

    private final List<MatchResult> ready = new ArrayList<>();
    private final List<MatchResult> almost = new ArrayList<>();
    private final List<MatchResult> all = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        db = new DatabaseHelper(this);
        recipes = new RecipeRepository(db);

        rvRecipes = findViewById(R.id.rvRecipes);
        emptyView = findViewById(R.id.emptyView);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvInfo = findViewById(R.id.tvInfo);
        btnReady = findViewById(R.id.btnReady);
        btnAlmost = findViewById(R.id.btnAlmost);
        btnAll = findViewById(R.id.btnAll);
        etSearch = findViewById(R.id.etSearch);

        adapter = new RecipeAdapter(this);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvRecipes.setAdapter(adapter);

        toggle = findViewById(R.id.toggleGroup);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) render();
        });

        if (savedInstanceState == null && getIntent().getBooleanExtra(EXTRA_SHOW_ALMOST, false)) {
            toggle.check(R.id.btnAlmost);
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { refresh(); }
        });

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavHelper.setup(this, nav, R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();      // pantry may have changed since we were last visible
    }

    private String query() {
        return etSearch.getText() == null ? "" : etSearch.getText().toString().trim();
    }

    /** Fetches the (optionally searched) recipes from the database and re-checks them against the pantry. */
    private void refresh() {
        List<PantryItem> pantry = db.getAllItems();
        List<MatchResult> results = recipes.match(pantry, query(), Prefs.ignoreExpired(this));

        ready.clear();
        almost.clear();
        all.clear();
        for (MatchResult m : results) {
            all.add(m);
            if (m.isFullMatch()) ready.add(m);              // strict: nothing missing
            else if (m.missingCount() == 1) almost.add(m);  // bonus list, kept separate
        }
        btnReady.setText(getString(R.string.tab_ready) + " (" + ready.size() + ")");
        btnAlmost.setText(getString(R.string.tab_almost) + " (" + almost.size() + ")");
        btnAll.setText(getString(R.string.tab_all) + " (" + all.size() + ")");
        render();
    }

    private void render() {
        // Read the tab from the toggle itself so the list can never disagree with the highlighted tab
        // (e.g. after a rotation, when Android restores the toggle state).
        int checked = toggle.getCheckedButtonId();
        List<MatchResult> list;
        int info, emptyMsg;
        if (checked == R.id.btnAlmost) {
            list = almost;  info = R.string.info_almost;  emptyMsg = R.string.msg_no_almost;
        } else if (checked == R.id.btnAll) {
            list = all;     info = R.string.info_all;     emptyMsg = R.string.msg_no_recipes;
        } else {
            list = ready;   info = R.string.info_ready;   emptyMsg = R.string.msg_no_recipes;
        }

        adapter.setResults(list);
        tvInfo.setText(info);

        boolean empty = list.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvRecipes.setVisibility(empty ? View.GONE : View.VISIBLE);
        String q = query();
        tvEmpty.setText(q.isEmpty() ? getString(emptyMsg) : getString(R.string.msg_no_search_results, q));
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
