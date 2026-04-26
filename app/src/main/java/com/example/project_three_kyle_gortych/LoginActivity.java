package com.example.project_three_kyle_gortych;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_three_kyle_gortych.db.AppDatabase;
import com.example.project_three_kyle_gortych.db.User;
import com.example.project_three_kyle_gortych.util.PasswordUtil;

/**
 * Single screen that handles both login and first-time account creation.
 *
 * <ul>
 *   <li>Tapping <b>Login</b> looks up the username, verifies the password
 *       against the stored bcrypt hash, and on success starts
 *       {@link DataActivity}.</li>
 *   <li>Tapping <b>Create Account</b> inserts a new {@link User} row with a
 *       freshly hashed password, then drops straight into the inventory
 *       screen.</li>
 * </ul>
 *
 * All database and bcrypt work happens on {@link AppDatabase#ioExecutor}
 * so the UI thread never blocks on SQLite or hashing.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnCreate;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreate = findViewById(R.id.btnCreateAccount);

        db = AppDatabase.getInstance(this);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnCreate.setOnClickListener(v -> attemptCreate());
    }

    /** Validates input, looks up the user, and verifies the bcrypt hash. */
    private void attemptLogin() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString();
        if (!validateInput(username, password)) {
            return;
        }
        setButtonsEnabled(false);
        AppDatabase.ioExecutor.execute(() -> {
            User user = db.userDao().findByUsername(username);
            final boolean success =
                    user != null && PasswordUtil.verify(password, user.passwordHash);
            runOnUiThread(() -> {
                setButtonsEnabled(true);
                if (success) {
                    goToInventory();
                } else {
                    Toast.makeText(this,
                            R.string.err_invalid_credentials,
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /** Validates input, enforces uniqueness, hashes the password and inserts. */
    private void attemptCreate() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString();
        if (!validateInput(username, password)) {
            return;
        }
        if (password.length() < 4) {
            Toast.makeText(this,
                    R.string.err_short_password,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        setButtonsEnabled(false);
        AppDatabase.ioExecutor.execute(() -> {
            User existing = db.userDao().findByUsername(username);
            if (existing != null) {
                runOnUiThread(() -> {
                    setButtonsEnabled(true);
                    Toast.makeText(this,
                            R.string.err_user_exists,
                            Toast.LENGTH_SHORT).show();
                });
                return;
            }
            // Hashing is CPU-intensive – we are already off the UI thread here.
            String hash = PasswordUtil.hash(password);
            db.userDao().insert(new User(username, hash));
            runOnUiThread(() -> {
                setButtonsEnabled(true);
                Toast.makeText(this,
                        R.string.create_account_success,
                        Toast.LENGTH_SHORT).show();
                goToInventory();
            });
        });
    }

    /** Returns {@code true} iff both fields are non-empty. */
    private boolean validateInput(String u, String p) {
        if (u.isEmpty() || p.isEmpty()) {
            Toast.makeText(this,
                    R.string.err_empty_fields,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /** Disables buttons during DB work to stop double-taps. */
    private void setButtonsEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnCreate.setEnabled(enabled);
    }

    /** Navigates to the inventory grid and finishes the login screen. */
    private void goToInventory() {
        startActivity(new Intent(this, DataActivity.class));
        finish();
    }
}