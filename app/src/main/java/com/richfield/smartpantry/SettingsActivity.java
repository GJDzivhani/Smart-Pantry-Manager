package com.richfield.smartpantry;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.richfield.smartpantry.data.DatabaseHelper;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.util.IngredientMatcher;
import com.richfield.smartpantry.util.NavHelper;
import com.richfield.smartpantry.util.Prefs;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/** Settings screen: expiring-soon alerts, default unit, matching option and data tools. */
public class SettingsActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = new DatabaseHelper(this);

        // --- expiring-soon alerts ---
        SwitchMaterial switchAlerts = findViewById(R.id.switchAlerts);
        switchAlerts.setChecked(Prefs.alertsEnabled(this));
        switchAlerts.setOnCheckedChangeListener((b, checked) -> Prefs.setAlertsEnabled(this, checked));

        AutoCompleteTextView actvDays = findViewById(R.id.actvDays);
        actvDays.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown,
                getResources().getStringArray(R.array.expiry_days)));
        actvDays.setText(String.valueOf(Prefs.alertDays(this)), false);
        actvDays.setOnItemClickListener((parent, view, pos, id) ->
                Prefs.setAlertDays(this, Integer.parseInt((String) parent.getItemAtPosition(pos))));

        // --- default unit ---
        AutoCompleteTextView actvUnit = findViewById(R.id.actvDefaultUnit);
        actvUnit.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown,
                getResources().getStringArray(R.array.units)));
        actvUnit.setText(Prefs.defaultUnit(this), false);
        actvUnit.setOnItemClickListener((parent, view, pos, id) ->
                Prefs.setDefaultUnit(this, (String) parent.getItemAtPosition(pos)));

        // --- ignore expired ---
        SwitchMaterial switchIgnore = findViewById(R.id.switchIgnoreExpired);
        switchIgnore.setChecked(Prefs.ignoreExpired(this));
        switchIgnore.setOnCheckedChangeListener((b, checked) -> Prefs.setIgnoreExpired(this, checked));

        // --- data tools ---
        findViewById(R.id.btnDemo).setOnClickListener(v -> addDemoData());
        findViewById(R.id.btnClear).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Clear entire pantry?")
                        .setMessage("All ingredients will be permanently removed.")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Clear", (d, w) -> {
                            db.deleteAllItems();
                            Toast.makeText(this, "Pantry cleared", Toast.LENGTH_SHORT).show();
                        })
                        .show());

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavHelper.setup(this, nav, R.id.nav_settings);
    }

    /**
     * Handy for testing / the video demo: adds a few staples (some expiring soon).
     * Ingredients that are already in the pantry are skipped, so tapping the button twice
     * does not create duplicate rows.
     */
    private void addDemoData() {
        Set<String> existing = new HashSet<>();
        for (PantryItem p : db.getAllItems()) existing.add(IngredientMatcher.normalizeName(p.getName()));

        int added = 0;
        added += demo(existing, "Eggs", 6, "pcs", inDays(4));
        added += demo(existing, "Bread", 4, "pcs", inDays(2));
        added += demo(existing, "Butter", 250, "g", inDays(30));
        added += demo(existing, "Cheese", 200, "g", inDays(10));
        added += demo(existing, "Salt", 100, "g", null);
        added += demo(existing, "Milk", 1, "l", inDays(1));
        added += demo(existing, "Flour", 500, "g", null);
        added += demo(existing, "Sugar", 500, "g", null);

        Toast.makeText(this, added == 0
                ? "Demo ingredients are already in your pantry"
                : "Added " + added + " demo ingredient" + (added == 1 ? "" : "s"),
                Toast.LENGTH_SHORT).show();
    }

    /** Inserts one demo row unless that ingredient already exists; returns 1 if inserted. */
    private int demo(Set<String> existing, String name, double qty, String unit, String expiry) {
        if (!existing.add(IngredientMatcher.normalizeName(name))) return 0;
        db.insertItem(new PantryItem(-1, name, qty, unit, expiry));
        return 1;
    }

    private static String inDays(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, days);
        return PantryItem.toIso(c.getTime());
    }
}
