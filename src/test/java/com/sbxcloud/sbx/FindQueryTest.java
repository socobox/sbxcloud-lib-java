package com.sbxcloud.sbx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbxcloud.sbx.model.AndOr;
import com.sbxcloud.sbx.model.Operation;
import com.sbxcloud.sbx.model.WhereClause;
import com.sbxcloud.sbx.model.InventoryHistory;
import com.sbxcloud.sbx.query.FindQuery;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FindQueryTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldBuildSimpleQuery() {
        var query = FindQuery.from("contact")
                .andWhereIsEqualTo("status", "ACTIVE")
                .compile();

        assertEquals("contact", query.rowModel());
        assertNotNull(query.where());
        assertInstanceOf(WhereClause.Conditions.class, query.where());

        var conditions = (WhereClause.Conditions) query.where();
        assertEquals(1, conditions.groups().size());
        assertEquals(AndOr.AND, conditions.groups().get(0).andOr());
        assertEquals(1, conditions.groups().get(0).group().size());

        var expr = conditions.groups().get(0).group().get(0);
        assertEquals("status", expr.field());
        assertEquals(Operation.EQUAL, expr.operation());
        assertEquals("ACTIVE", expr.value());
    }

    @Test
    void shouldBuildQueryWithMultipleConditions() {
        var query = FindQuery.from("contact")
                .newGroupWithAnd()
                .andWhereIsEqualTo("status", "ACTIVE")
                .andWhereIsGreaterThan("age", 18)
                .newGroupWithOr()
                .orWhereContains("name", "John")
                .orWhereContains("name", "Jane")
                .compile();

        assertEquals("contact", query.rowModel());
        var conditions = (WhereClause.Conditions) query.where();
        assertEquals(2, conditions.groups().size());

        // First group: AND
        assertEquals(AndOr.AND, conditions.groups().get(0).andOr());
        assertEquals(2, conditions.groups().get(0).group().size());

        // Second group: OR
        assertEquals(AndOr.OR, conditions.groups().get(1).andOr());
        assertEquals(2, conditions.groups().get(1).group().size());
    }

    @Test
    void shouldBuildQueryWithPagination() {
        var query = FindQuery.from("contact")
                .andWhereIsEqualTo("status", "ACTIVE")
                .setPage(2)
                .setPageSize(50)
                .compile();

        assertEquals(2, query.page());
        assertEquals(50, query.size());
    }

    @Test
    void shouldBuildQueryWithFetchModels() {
        var query = FindQuery.from("contact")
                .fetchModels("account", "owner")
                .fetchReferencingModels("tasks")
                .setAutowire("account")
                .compile();

        assertEquals(List.of("account", "owner"), query.fetchModels());
        assertEquals(List.of("tasks"), query.fetchReferencingModels());
        assertEquals(List.of("account"), query.autowire());
    }

    @Test
    void shouldBuildQueryWithKeys() {
        var query = FindQuery.from("contact")
                .whereWithKeys("key1", "key2", "key3")
                .compile();

        assertNotNull(query.where());
        assertInstanceOf(WhereClause.Keys.class, query.where());

        var keysClause = (WhereClause.Keys) query.where();
        assertEquals(List.of("key1", "key2", "key3"), keysClause.keys());
    }

    @Test
    void shouldBuildQueryWithInOperator() {
        var query = FindQuery.from("contact")
                .andWhereIsIn("status", "ACTIVE", "PENDING")
                .compile();

        var conditions = (WhereClause.Conditions) query.where();
        var expr = conditions.groups().get(0).group().get(0);
        assertEquals(Operation.IN, expr.operation());
        assertEquals(List.of("ACTIVE", "PENDING"), expr.value());
    }

    @Test
    void shouldBuildQueryWithNullChecks() {
        var query = FindQuery.from("contact")
                .andWhereIsNull("deletedAt")
                .andWhereIsNotNull("email")
                .compile();

        var conditions = (WhereClause.Conditions) query.where();
        assertEquals(2, conditions.groups().get(0).group().size());

        var nullExpr = conditions.groups().get(0).group().get(0);
        assertEquals(Operation.IS, nullExpr.operation());
        assertNull(nullExpr.value());

        var notNullExpr = conditions.groups().get(0).group().get(1);
        assertEquals(Operation.IS_NOT, notNullExpr.operation());
        assertNull(notNullExpr.value());
    }

    @Test
    void shouldBuildQueryWithLikeOperations() {
        var query = FindQuery.from("contact")
                .andWhereStartsWith("name", "John")
                .andWhereEndsWith("email", "@example.com")
                .andWhereContains("address", "Street")
                .compile();

        var conditions = (WhereClause.Conditions) query.where();
        var group = conditions.groups().get(0).group();

        assertEquals("John%", group.get(0).value());
        assertEquals("%@example.com", group.get(1).value());
        assertEquals("%Street%", group.get(2).value());
    }

    @Test
    void shouldEscapePercentInContains() {
        var query = FindQuery.from("contact")
                .andWhereContains("name", "100%")
                .compile();

        var conditions = (WhereClause.Conditions) query.where();
        var expr = conditions.groups().get(0).group().get(0);
        assertEquals("%100%", expr.value());
    }

    @Test
    void shouldBuildQueryFromAnnotatedClass() {
        // InventoryHistory is annotated with @SbxModel("inventory_history")
        var query = FindQuery.from(InventoryHistory.class)
                .andWhereIsGreaterThan("price", 10)
                .setPageSize(25)
                .compile();

        assertEquals("inventory_history", query.rowModel());
        assertNotNull(query.where());
        assertEquals(25, query.size());
    }

    @Test
    void shouldThrowForNonAnnotatedClass() {
        // String is not annotated with @SbxModel
        assertThrows(IllegalArgumentException.class, () -> {
            FindQuery.from(String.class);
        });
    }
}
