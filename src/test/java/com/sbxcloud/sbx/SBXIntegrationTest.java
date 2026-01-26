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
}
