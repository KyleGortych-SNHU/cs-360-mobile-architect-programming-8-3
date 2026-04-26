package com.example.project_three_kyle_gortych.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * CRUD operations for the inventory grid. {@link #getAll()} returns a
 * {@link LiveData} so the UI updates automatically whenever a row is
 * inserted, updated or deleted; {@link #getAllSync()} exists for unit tests.
 */
@Dao
public interface InventoryDao {

    /** Observable list used by {@code DataActivity} to keep the grid in sync. */
    @Query("SELECT * FROM inventory_items ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<InventoryItem>> getAll();

    /** Synchronous variant used by instrumented tests. */
    @Query("SELECT * FROM inventory_items ORDER BY name COLLATE NOCASE ASC")
    List<InventoryItem> getAllSync();

    /** Inserts a new item; returns the generated row id. */
    @Insert
    long insert(InventoryItem item);

    /** Updates an existing row (used when the quantity spinner changes). */
    @Update
    void update(InventoryItem item);

    /** Removes an item completely from the warehouse. */
    @Delete
    void delete(InventoryItem item);
}