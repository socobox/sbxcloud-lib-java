package com.sbxcloud.sbx;

import com.sbxcloud.sbx.client.SBXService;
import com.sbxcloud.sbx.client.SBXServiceFactory;
import com.sbxcloud.sbx.model.InventoryHistory;
import com.sbxcloud.sbx.query.FindQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SBXService.
 * <p>
 * To run: set environment variables and remove @Disabled
 * <pre>
 * export SBX_APP_KEY=your-app-key
 * export SBX_TOKEN=your-token
 * export SBX_DOMAIN=96
 * export SBX_BASE_URL=https://sbxcloud.com
 * mvn test -Dtest=SBXIntegrationTest
 * </pre>
 */
@Disabled("Set env vars and remove this to run integration tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SBXIntegrationTest {

    private static final String MODEL = "inventory_history";
    private static SBXService sbx;
    private static String createdKey;

    @BeforeAll
    static void setup() {
        sbx = SBXServiceFactory.withEnv();
    }

    @Test
    @Order(1)
    void testFind() {
        var query = FindQuery.from(MODEL)
                .setPageSize(5);

        var response = sbx.find(query, InventoryHistory.class);

        assertTrue(response.success(), "Find should succeed: " + response.error());
        assertNotNull(response.results());

        System.out.println("Found " + response.results().size() + " inventory_history records");
        response.results().forEach(r ->
                System.out.println("  - " + r.key() + ": week=" + r.week() + ", price=" + r.price())
        );
    }

    @Test
    @Order(2)
    void testFindWithCondition() {
        var query = FindQuery.from(MODEL)
                .newGroupWithAnd()
                .andWhereIsGreaterThan("price", 0)
                .setPageSize(10);

        var response = sbx.find(query, InventoryHistory.class);

        assertTrue(response.success(), "Find should succeed: " + response.error());
        System.out.println("Found " + (response.results() != null ? response.results().size() : 0) + " records with price > 0");
    }

    @Test
    @Order(3)
    void testFindOne() {
        var query = FindQuery.from(MODEL);

        var response = sbx.findOne(query, InventoryHistory.class);

        assertTrue(response.success(), "FindOne should succeed: " + response.error());
        assertNotNull(response.results());
        assertEquals(1, response.results().size());

        var record = response.results().get(0);
        System.out.println("Found one: " + record.key() + " - week=" + record.week());

        if (record.meta() != null) {
            System.out.println("  Created: " + record.meta().createdTime());
            System.out.println("  Updated: " + record.meta().updateTime());
        }
    }

    @Test
    @Order(4)
    void testCreate() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("masterlist", "test-masterlist-" + System.currentTimeMillis());
        row.put("week", 20250126);
        row.put("price", 1.25);
        row.put("quantity", 50);

        var response = sbx.create(MODEL, row);

        assertTrue(response.success(), "Create should succeed: " + response.error());
        assertNotNull(response.keys());
        assertFalse(response.keys().isEmpty());

        createdKey = response.keys().get(0);
        System.out.println("Created inventory_history with key: " + createdKey);
    }

    @Test
    @Order(5)
    void testUpdate() {
        if (createdKey == null) {
            System.out.println("Skipping update - no created key");
            return;
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("_KEY", createdKey);
        row.put("price", 2.50);
        row.put("quantity", 75);

        var response = sbx.update(MODEL, row);

        assertTrue(response.success(), "Update should succeed: " + response.error());
        System.out.println("Updated inventory_history: " + createdKey);
    }

    @Test
    @Order(6)
    void testFindByKey() {
        if (createdKey == null) {
            System.out.println("Skipping findByKey - no created key");
            return;
        }

        var query = FindQuery.from(MODEL)
                .whereWithKeys(createdKey);

        var response = sbx.find(query, InventoryHistory.class);

        assertTrue(response.success(), "Find by key should succeed: " + response.error());
        assertNotNull(response.results());
        assertEquals(1, response.results().size());

        var record = response.results().get(0);
        assertEquals(createdKey, record.key());
        System.out.println("Found by key: price=" + record.price() + ", quantity=" + record.quantity());
    }

    @Test
    @Order(7)
    void testDelete() {
        if (createdKey == null) {
            System.out.println("Skipping delete - no created key");
            return;
        }

        var response = sbx.delete(MODEL, createdKey);

        assertTrue(response.success(), "Delete should succeed: " + response.error());
        System.out.println("Deleted inventory_history: " + createdKey);
    }

    @Test
    @Order(8)
    void testFindAll() {
        var query = FindQuery.from(MODEL)
                .newGroupWithAnd()
                .andWhereIsGreaterThan("quantity", 0)
                .setPageSize(50);

        var response = sbx.findAll(query, InventoryHistory.class);

        assertTrue(response.success(), "FindAll should succeed: " + response.error());
        System.out.println("FindAll returned " + (response.results() != null ? response.results().size() : 0) + " total records");
    }

    // ==================== Type-safe Annotation Tests ====================

    @Test
    @Order(10)
    void testFindWithAnnotatedClass() {
        // Uses @SbxModel annotation to infer model name
        var query = FindQuery.from(InventoryHistory.class)
                .setPageSize(5);

        var response = sbx.find(query, InventoryHistory.class);

        assertTrue(response.success(), "Find with annotated class should succeed: " + response.error());
        System.out.println("Found " + (response.results() != null ? response.results().size() : 0) + " records using @SbxModel");
    }

    @Test
    @Order(11)
    void testSimpleFindWithClass() {
        // Simplest form - just pass the class
        var response = sbx.find(InventoryHistory.class);

        assertTrue(response.success(), "Simple find should succeed: " + response.error());
        System.out.println("Simple find returned " + (response.results() != null ? response.results().size() : 0) + " records");
    }

    @Test
    @Order(12)
    void testCreateWithEntity() {
        // Create entity using constructor
        var entity = new InventoryHistory("test-entity-" + System.currentTimeMillis(), 20250126, 3.99, 100);

        var response = sbx.create(entity);

        assertTrue(response.success(), "Create with entity should succeed: " + response.error());
        assertNotNull(response.keys());
        assertFalse(response.keys().isEmpty());

        createdKey = response.keys().get(0);
        System.out.println("Created using entity: " + createdKey);
    }

    @Test
    @Order(13)
    void testUpdateWithEntity() {
        if (createdKey == null) {
            System.out.println("Skipping - no created key");
            return;
        }

        // Fetch the entity first
        var query = FindQuery.from(InventoryHistory.class).whereWithKeys(createdKey);
        var findResponse = sbx.find(query, InventoryHistory.class);
        assertTrue(findResponse.success());
        assertNotNull(findResponse.results());
        assertFalse(findResponse.results().isEmpty());

        var existing = findResponse.results().get(0);

        // Create updated entity with new price
        var updated = new InventoryHistory(
                existing.key(),
                existing.meta(),
                existing.masterlist(),
                existing.week(),
                9.99, // new price
                existing.quantity()
        );

        var response = sbx.update(updated);

        assertTrue(response.success(), "Update with entity should succeed: " + response.error());
        System.out.println("Updated entity price to 9.99");
    }

    @Test
    @Order(14)
    void testDeleteWithEntity() {
        if (createdKey == null) {
            System.out.println("Skipping - no created key");
            return;
        }

        // Fetch entity to delete
        var query = FindQuery.from(InventoryHistory.class).whereWithKeys(createdKey);
        var findResponse = sbx.find(query, InventoryHistory.class);
        assertTrue(findResponse.success());
        assertNotNull(findResponse.results());

        if (!findResponse.results().isEmpty()) {
            var entity = findResponse.results().get(0);

            // Delete using entity
            var response = sbx.delete(entity);

            assertTrue(response.success(), "Delete with entity should succeed: " + response.error());
            System.out.println("Deleted entity: " + entity.key());
        }
    }

    @Test
    @Order(15)
    void testDeleteWithClassAndKey() {
        // Create a new record to delete
        var entity = new InventoryHistory("delete-test-" + System.currentTimeMillis(), 20250126, 0.99, 1);
        var createResponse = sbx.create(entity);
        assertTrue(createResponse.success());

        String keyToDelete = createResponse.keys().get(0);

        // Delete using class and key
        var response = sbx.delete(InventoryHistory.class, keyToDelete);

        assertTrue(response.success(), "Delete with class and key should succeed: " + response.error());
        System.out.println("Deleted with class and key: " + keyToDelete);
    }
}
