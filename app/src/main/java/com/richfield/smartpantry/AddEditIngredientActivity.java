package com.richfield.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.richfield.smartpantry.data.DatabaseHelper;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.util.IngredientMatcher;
import com.richfield.smartpantry.util.Prefs;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Add / Edit Ingredient screen. If the starting Intent carries EXTRA_ITEM_ID we are in
 * EDIT mode (UPDATE), otherwise ADD mode (CREATE). Includes full input validation.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private DatabaseHelper db;
    private long editId = -1;

    private TextInputLayout tilName, tilQuantity, tilUnit, tilExpiry;
    private TextInputEditText etName, etQuantity, etExpiry;
    private AutoCompleteTextView actvUnit;
    private Button btnSave;

    /** ISO yyyy-MM-dd, or null when no expiry date is chosen. */
    private String selectedExpiry = null;
    private List<String> units;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        db = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilName = findViewById(R.id.tilName);
        tilQuantity = findViewById(R.id.tilQuantity);
        tilUnit = findViewById(R.id.tilUnit);
        tilExpiry = findViewById(R.id.tilExpiry);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        actvUnit = findViewById(R.id.actvUnit);
        btnSave = findViewById(R.id.btnSave);

        units = Arrays.asList(getResources().getStringArray(R.array.units));
        actvUnit.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown, units));
        actvUnit.setText(Prefs.defaultUnit(this), false);        // false = don't filter the list

        etExpiry.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btnClearDate).setOnClickListener(v -> {
            selectedExpiry = null;
            updateExpiryField();
        });
        btnSave.setOnClickListener(v -> save());

        // Edit mode?
        editId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (editId != -1) {
            PantryItem item = db.getItem(editId);
            if (item == null) {                      // deleted in the meantime
                finish();
                return;
            }
            toolbar.setTitle(R.string.title_edit);
            btnSave.setText(R.string.btn_update);
            etName.setText(item.getName());
            etQuantity.setText(IngredientMatcher.formatQuantity(item.getQuantity()));
            actvUnit.setText(item.getUnit(), false);
            selectedExpiry = item.getExpiry();
            updateExpiryField();
        }
    }

    private void updateExpiryField() {
        etExpiry.setText(selectedExpiry == null ? "" : PantryItem.toDisplay(selectedExpiry));
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        Date existing = PantryItem.parseIso(selectedExpiry);
        if (existing != null) c.setTime(existing);

        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, day, 12, 0, 0);
            selectedExpiry = PantryItem.toIso(chosen.getTime());
            updateExpiryField();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Returns true when every field is valid; shows an inline error on each invalid one. */
    private boolean validate() {
        boolean ok = true;

        String name = textOf(etName);
        tilName.setError(null);
        if (name.isEmpty()) {
            tilName.setError("Ingredient name is required");
            ok = false;
        } else if (name.length() < 2) {
            tilName.setError("Name must be at least 2 characters");
            ok = false;
        } else if (name.length() > 40) {
            tilName.setError("Name must be 40 characters or fewer");
            ok = false;
        } else if (!name.matches("[\\p{L}][\\p{L}\\s'\\-]*")) {
            tilName.setError("Use letters only (spaces, hyphens and apostrophes are allowed)");
            ok = false;
        }

        tilQuantity.setError(null);
        String q = textOf(etQuantity).replace(',', '.');
        if (q.isEmpty()) {
            tilQuantity.setError("Quantity is required");
            ok = false;
        } else {
            try {
                double value = Double.parseDouble(q);
                if (value <= 0) {
                    tilQuantity.setError("Quantity must be greater than 0");
                    ok = false;
                } else if (value > 100000) {
                    tilQuantity.setError("Quantity is too large");
                    ok = false;
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError("Enter a valid number");
                ok = false;
            }
        }

        tilUnit.setError(null);
        if (!units.contains(actvUnit.getText().toString().trim())) {
            tilUnit.setError("Choose a unit from the list");
            ok = false;
        }
        return ok;
    }

    private void save() {
        if (!validate()) return;
        btnSave.setEnabled(false);          // guard against a double-tap saving the item twice

        String name = textOf(etName);
        double quantity = Double.parseDouble(textOf(etQuantity).replace(',', '.'));
        String unit = actvUnit.getText().toString().trim();

        PantryItem item = new PantryItem(editId, name, quantity, unit, selectedExpiry);

        if (editId == -1) {
            long rowId = db.insertItem(item);                       // CREATE
            Toast.makeText(this, rowId > 0 ? "Ingredient added" : "Could not save ingredient",
                    Toast.LENGTH_SHORT).show();
        } else {
            db.updateItem(item);                                    // UPDATE
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private static String textOf(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
