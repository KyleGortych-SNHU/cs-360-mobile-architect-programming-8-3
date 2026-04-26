package com.example.project_three_kyle_gortych.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room database holding the two tables required by the project:
 * {@code users} (login credentials) and {@code inventory_items}
 * (warehouse stock). Persistent across app launches.
 */
@Database(
        entities = { User.class, InventoryItem.class },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    /** Shared background executor for synchronous DAO calls. */
    public static final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public abstract UserDao userDao();
    public abstract InventoryDao inventoryDao();

    // Double-checked locked singleton so we only open the SQLite file once.
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "inventory.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}