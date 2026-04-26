package com.example.project_three_kyle_gortych;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Lets the user enable SMS alerts for out-of-stock items.
 *
 * <p>The screen:
 * <ol>
 *   <li>Shows the current SMS permission status.</li>
 *   <li>Lets the user save a destination phone number in
 *       {@link SharedPreferences}.</li>
 *   <li>Asks the OS for {@code SEND_SMS} permission the first time
 *       the button is tapped.</li>
 * </ol>
 *
 * If the user denies the permission the rest of the application keeps
 * working – {@link DataActivity#notifyOutOfStock} simply falls back to an
 * in-app Toast.
 */
public class SmsActivity extends AppCompatActivity {

    private TextView tvStatus;
    private EditText etPhone;

    /** Modern replacement for the legacy {@code onRequestPermissionsResult}. */
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        setTitle(R.string.title_sms);

        tvStatus = findViewById(R.id.tvPermissionStatus);
        etPhone = findViewById(R.id.etPhone);
        Button btn = findViewById(R.id.btnRequestPermission);

        final SharedPreferences prefs = getSharedPreferences(
                DataActivity.SMS_PREFS, Context.MODE_PRIVATE);
        // Pre-fill the EditText with any previously saved number.
        etPhone.setText(prefs.getString(DataActivity.KEY_PHONE, ""));

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    updateStatus(granted);
                    int msg = granted
                            ? R.string.sms_permission_granted
                            : R.string.sms_permission_denied;
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });

        btn.setOnClickListener(v -> {
            // Persist the phone number regardless of the permission outcome;
            // the user may enable permission later from system settings.
            String phone = etPhone.getText().toString().trim();
            prefs.edit().putString(DataActivity.KEY_PHONE, phone).apply();

            // Don't re-prompt if permission was already granted earlier.
            if (isPermissionGranted()) {
                Toast.makeText(this,
                        R.string.sms_permission_granted,
                        Toast.LENGTH_SHORT).show();
                updateStatus(true);
            } else {
                permissionLauncher.launch(Manifest.permission.SEND_SMS);
            }
        });

        updateStatus(isPermissionGranted());
    }

    /** True when the user has already granted {@code SEND_SMS}. */
    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Updates the on-screen status text to match the permission state. */
    private void updateStatus(boolean granted) {
        tvStatus.setText(granted
                ? R.string.sms_status_granted
                : R.string.sms_status_not_granted);
    }
}