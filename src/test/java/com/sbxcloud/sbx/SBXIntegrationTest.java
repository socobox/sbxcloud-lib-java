package com.sbxcloud.sbx;

import com.sbxcloud.sbx.client.SBXService;
import com.sbxcloud.sbx.client.SBXServiceFactory;
import com.sbxcloud.sbx.model.InventoryHistory;
import com.sbxcloud.sbx.query.FindQuery;
import com.sbxcloud.sbx.repository.SbxRepository;
import com.sbxcloud.sbx.util.Sbx;
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
        // Create entity using Sbx.create() - no manual constructor needed
        var entity = Sbx.create(InventoryHistory.class,
                "test-entity-" + System.currentTimeMillis(), 20250126, 3.99, 100);

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

        // Partial update - only key and changed field (price)
        var updated = new InventoryHistory(
                existing.key(),
                null,  // meta is ignored
                null,  // masterlist unchanged
                null,  // week unchanged
                9.99,  // new price
                null   // quantity unchanged
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
        // Create a new record to delete using Sbx.create()
        var entity = Sbx.create(InventoryHistory.class,
                "delete-test-" + System.currentTimeMillis(), 20250126, 0.99, 1);
        var createResponse = sbx.create(entity);
        assertTrue(createResponse.success());

        String keyToDelete = createResponse.keys().get(0);

        // Delete using class and key
        var response = sbx.delete(InventoryHistory.class, keyToDelete);

        assertTrue(response.success(), "Delete with class and key should succeed: " + response.error());
        System.out.println("Deleted with class and key: " + keyToDelete);
    }

    // ==================== Repository Pattern Tests ====================

    @Test
    @Order(20)
    void testRepositoryFindAll() {
        SbxRepository<InventoryHistory> repo = sbx.repository(InventoryHistory.class);

        var results = repo.findAll();

        assertNotNull(results);
        System.out.println("Repository findAll: " + results.size() + " records");
    }

    @Test
    @Order(21)
    void testRepositorySaveAndFindById() {
        SbxRepository<InventoryHistory> repo = sbx.repository(InventoryHistory.class);

        // Create using Sbx.create()
        var entity = Sbx.create(InventoryHistory.class,
                "repo-test-" + System.currentTimeMillis(), 20250126, 5.99, 25);
        String key = repo.save(entity);

        assertNotNull(key);
        System.out.println("Repository saved with key: " + key);

        // Find by ID
        var found = repo.findById(key);
        assertTrue(found.isPresent());
        assertEquals(key, found.get().key());
        System.out.println("Repository findById: " + found.get().masterlist());

        // Update
        var updated = new InventoryHistory(key, found.get().meta(),
                found.get().masterlist(), found.get().week(), 9.99, 50);
        repo.save(updated);

        // Verify update
        var afterUpdate = repo.findById(key);
        assertTrue(afterUpdate.isPresent());
        assertEquals(9.99, afterUpdate.get().price());
        System.out.println("Repository updated price to: " + afterUpdate.get().price());

        // Delete
        repo.deleteById(key);

        // Verify delete
        assertFalse(repo.existsById(key));
        System.out.println("Repository deleted: " + key);
    }

    @Test
    @Order(22)
    void testRepositoryQueryBuilder() {
        SbxRepository<InventoryHistory> repo = sbx.repository(InventoryHistory.class);

        // Fluent query
        var results = repo.query()
                .where(q -> q.andWhereIsGreaterThan("quantity", 0))
                .page(1, 10)
                .list();

        assertNotNull(results);
        System.out.println("Repository query found: " + results.size() + " records with quantity > 0");
    }

    @Test
    @Order(23)
    void testRepositoryQueryFirst() {
        SbxRepository<InventoryHistory> repo = sbx.repository(InventoryHistory.class);

        var first = repo.query()
                .where(q -> q.andWhereIsGreaterThan("price", 0))
                .first();

        if (first.isPresent()) {
            System.out.println("Repository query first: " + first.get().key() + " price=" + first.get().price());
        } else {
            System.out.println("Repository query first: no results");
        }
    }

    @Test
    @Order(24)
    void testRepositoryCount() {
        SbxRepository<InventoryHistory> repo = sbx.repository(InventoryHistory.class);

        long count = repo.count();

        System.out.println("Repository count: " + count + " total records");
    }

    @Test
    @Order(25)
    void testRepositoryFindWhere() {
        SbxRepository<InventoryHistory> repo = sbx.repository(InventoryHistory.class);

        // Simple findWhere
        var results = repo.findWhere(q -> q.andWhereIsGreaterThan("week", 0));

        assertNotNull(results);
        System.out.println("Repository findWhere: " + results.size() + " records with week > 0");
    }
}
