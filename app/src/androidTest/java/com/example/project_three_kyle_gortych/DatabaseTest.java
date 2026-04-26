package com.example.project_three_kyle_gortych;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.project_three_kyle_gortych.db.AppDatabase;
import com.example.project_three_kyle_gortych.db.InventoryDao;
import com.example.project_three_kyle_gortych.db.InventoryItem;
import com.example.project_three_kyle_gortych.db.User;
import com.example.project_three_kyle_gortych.db.UserDao;
import com.example.project_three_kyle_gortych.util.PasswordUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Instrumented tests for the Room database. These run on a device or
 * emulator (Build &gt; Run &gt; select device) rather than on the JVM,
 * because  Room generates code that depends on the Android SQLite bindings.
 *
 * <p>Each test uses an in-memory database that is destroyed in
 * {@link #closeDb()}, so tests cannot interfere with each other or with
 * real user data.
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    private AppDatabase db;
    private UserDao userDao;
    private InventoryDao inventoryDao;

    @Before
    public void createDb() {
        Context ctx = ApplicationProvider.getApplicationContext();
        // inMemoryDatabaseBuilder + allowMainThreadQueries keeps these tests
        // simple – real application code still goes through ioExecutor.
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        userDao = db.userDao();
        inventoryDao = db.inventoryDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    // --- User table --------------------------------------------------------

    @Test
    public void insertUser_thenFind_returnsSameRow() {
        String hash = PasswordUtil.hash("secret");
        userDao.insert(new User("alice", hash));

        User found = userDao.findByUsername("alice");
        assertNotNull(found);
        assertEquals("alice", found.username);
        assertTrue(PasswordUtil.verify("secret", found.passwordHash));
    }

    @Test
    public void findByUsername_returnsNullForMissingUser() {
        assertNull(userDao.findByUsername("ghost"));
    }

    @Test
    public void insertDuplicateUsername_throws() {
        userDao.insert(new User("bob", "h1"));
        try {
            userDao.insert(new User("bob", "h2"));
            fail("Duplicate insert should have thrown");
        } catch (Exception expected) {
            // Room wraps the SQLite constraint violation; any exception is
            // acceptable here.
        }
    }

    // --- Inventory table ---------------------------------------------------

    @Test
    public void insertInventoryItem_appearsInGetAllSync() {
        inventoryDao.insert(new InventoryItem("Widget", 5));
        List<InventoryItem> items = inventoryDao.getAllSync();
        assertEquals(1, items.size());
        assertEquals("Widget", items.get(0).name);
        assertEquals(5, items.get(0).quantity);
    }

    @Test
    public void updateInventoryItem_persistsNewQuantity() {
        inventoryDao.insert(new InventoryItem("Widget", 5));
        InventoryItem item = inventoryDao.getAllSync().get(0);

        item.quantity = 0;
        inventoryDao.update(item);

        List<InventoryItem> after = inventoryDao.getAllSync();
        assertEquals(1, after.size());
        assertEquals(0, after.get(0).quantity);
    }

    @Test
    public void deleteInventoryItem_removesRow() {
        inventoryDao.insert(new InventoryItem("Widget", 5));
        InventoryItem item = inventoryDao.getAllSync().get(0);

        inventoryDao.delete(item);

        assertTrue(inventoryDao.getAllSync().isEmpty());
    }

    @Test
    public void getAllSync_ordersByNameCaseInsensitive() {
        inventoryDao.insert(new InventoryItem("banana", 1));
        inventoryDao.insert(new InventoryItem("Apple", 2));
        inventoryDao.insert(new InventoryItem("cherry", 3));

        List<InventoryItem> items = inventoryDao.getAllSync();
        assertEquals(3, items.size());
        assertEquals("Apple", items.get(0).name);
        assertEquals("banana", items.get(1).name);
        assertEquals("cherry", items.get(2).name);
    }
}
