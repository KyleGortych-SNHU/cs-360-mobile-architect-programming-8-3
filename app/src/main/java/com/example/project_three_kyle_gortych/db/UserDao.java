package com.example.project_three_kyle_gortych.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Data-access object for the {@code users} table. All operations are
 * synchronous and must be called on a background thread (see
 * {@link AppDatabase#ioExecutor}).
 */
@Dao
public interface UserDao {

    /** Returns the row for {@code username} or {@code null} if no match. */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    /**
     * Inserts a new user. {@link OnConflictStrategy#ABORT} causes a
     * {@code SQLiteConstraintException} if the username already exists –
     * callers should check {@link #findByUsername} first for a friendly
     * error message.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(User user);
}