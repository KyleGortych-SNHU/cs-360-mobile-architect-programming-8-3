package com.example.project_three_kyle_gortych;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_three_kyle_gortych.adapter.InventoryAdapter;
import com.example.project_three_kyle_gortych.db.AppDatabase;
import com.example.project_three_kyle_gortych.db.InventoryItem;

/**
 * Main screen after login. Shows the inventory as a vertical grid (one row
 * per item) and exposes the four CRUD operations required by the project:
 * <ul>
 *   <li><b>Create</b> – the "Add Item" button opens a dialog for a name.</li>
 *   <li><b>Read</b>   – the RecyclerView observes {@code LiveData} from
 *                      Room so the grid refreshes automatically.</li>
 *   <li><b>Update</b> – the spinner in each row picks a new quantity.</li>
 *   <li><b>Delete</b> – the "Delete" button removes the row.</li>
 * </ul>
 *
 * When a row's quantity transitions to zero the activity fires a low-stock
 * alert – via SMS if the user has enabled and permitted notifications,
 * otherwise via an in-app Toast so the rest of the app still works.
 */
public class DataActivity extends AppCompatActivity {

    /** SharedPreferences file shared with {@link SmsActivity}. */
    static final String SMS_PREFS = "sms_prefs";
    static final String KEY_PHONE = "phone";

    private AppDatabase db;
    private InventoryAdapter adapter;
    private int[] quantityOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        db = AppDatabase.getInstance(this);
        quantityOptions = loadQuantityOptions();

        RecyclerView rv = findViewById(R.id.rvDataGrid);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new InventoryAdapter(new InventoryAdapter.Listener() {
            @Override
            public void onQuantityChanged(InventoryItem item, int newQty) {
                updateQuantity(item, newQty);
            }
            @Override
            public void onDelete(InventoryItem item) {
                deleteItem(item);
            }
        }, quantityOptions);
        rv.setAdapter(adapter);

        // LiveData + ListAdapter keep the grid in sync automatically.
        db.inventoryDao().getAll().observe(this, items -> adapter.submitList(items));

        findViewById(R.id.btnAddData).setOnClickListener(v -> showAddDialog());
        findViewById(R.id.btnSmsSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SmsActivity.class)));
    }

    /** Parses the string-array resource into a primitive int[] once. */
    private int[] loadQuantityOptions() {
        String[] raw = getResources().getStringArray(R.array.quantity_options);
        int[] opts = new int[raw.length];
        for (int i = 0; i < raw.length; i++) {
            opts[i] = Integer.parseInt(raw[i]);
        }
        return opts;
    }

    // --- CRUD helpers -------------------------------------------------------

    /** Opens a small dialog that asks for an item name and inserts a row. */
    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_item, null, false);
        EditText etName = dialogView.findViewById(R.id.etItemName);

        new AlertDialog.Builder(this)
                .setTitle(R.string.title_add_item)
                .setView(dialogView)
                .setPositiveButton(R.string.action_add, (d, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this,
                                R.string.err_empty_name,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AppDatabase.ioExecutor.execute(() ->
                            db.inventoryDao().insert(new InventoryItem(name, 1)));
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    /** Writes the new quantity and, if it dropped to zero, fires an alert. */
    private void updateQuantity(InventoryItem item, int newQty) {
        final int oldQty = item.quantity;
        AppDatabase.ioExecutor.execute(() -> {
            item.quantity = newQty;
            db.inventoryDao().update(item);
            // Only notify on the transition TO zero to avoid repeat alerts.
            if (newQty == 0 && oldQty != 0) {
                runOnUiThread(() -> notifyOutOfStock(item));
            }
        });
    }

    /** Removes an item from the database. */
    private void deleteItem(InventoryItem item) {
        AppDatabase.ioExecutor.execute(() -> db.inventoryDao().delete(item));
    }

    // --- SMS alert ----------------------------------------------------------

    /**
     * Attempts to send an SMS alert to the user's saved phone number.
     * Falls back to a Toast whenever either the permission or the number
     * is missing – the project requirement is that the app "continue to
     * function without the SMS messaging notification feature" if the user
     * denies permission.
     */
    private void notifyOutOfStock(InventoryItem item) {
        String message = getString(R.string.sms_zero_stock, item.name);

        SharedPreferences prefs = getSharedPreferences(SMS_PREFS, Context.MODE_PRIVATE);
        String phone = prefs.getString(KEY_PHONE, "");
        boolean granted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;

        if (granted && !phone.isEmpty()) {
            try {
                // getSystemService is the modern replacement for the
                // deprecated SmsManager.getDefault() on API 23+.
                SmsManager sm = getSystemService(SmsManager.class);
                sm.sendTextMessage(phone, null, message, null, null);
                Toast.makeText(this,
                        getString(R.string.sms_sent_for, item.name),
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // If the carrier rejects the send we still want to surface
                // the alert to the user.
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } else {
            // No permission or no number – show the alert in-app.
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
}