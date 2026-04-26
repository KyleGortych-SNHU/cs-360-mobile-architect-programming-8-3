package com.example.project_three_kyle_gortych.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Row in the {@code inventory_items} table – represents a single product
 * and the quantity currently on-hand in the warehouse.
 */
@Entity(tableName = "inventory_items")
public class InventoryItem {

    /** Auto-generated row id. */
    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Display name shown in the grid. */
    @NonNull
    public String name;

    /** Number of units on hand. When this drops to 0 we fire a low-stock alert. */
    public int quantity;

    public InventoryItem(@NonNull String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}