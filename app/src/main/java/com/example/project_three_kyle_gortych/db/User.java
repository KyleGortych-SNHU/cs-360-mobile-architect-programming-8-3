package com.example.project_three_kyle_gortych.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Row in the {@code users} table. The plain-text password is never stored; we
 * only persist the bcrypt hash produced by
 * {@link com.example.project_three_kyle_gortych.util.PasswordUtil}.
 */
@Entity(tableName = "users")
public class User {

    /** Username is unique and acts as the primary key. */
    @PrimaryKey
    @NonNull
    public String username;

    /** bcrypt-encoded hash, e.g. {@code $2a$12$...} (60 chars). */
    @NonNull
    public String passwordHash;

    public User(@NonNull String username, @NonNull String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
}