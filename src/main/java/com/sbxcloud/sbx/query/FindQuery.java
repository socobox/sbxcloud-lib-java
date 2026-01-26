package com.sbxcloud.sbx.query;

import com.sbxcloud.sbx.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Fluent query builder for SBX find operations.
 * <p>
 * Example usage:
 * <pre>{@code
 * var query = FindQuery.from("contact")
 *     .newGroupWithAnd()
 *     .andWhereIsEqualTo("status", "ACTIVE")
 *     .andWhereIsGreaterThan("age", 18)
 *     .newGroupWithOr()
 *     .orWhereContains("name", "John")
 *     .fetchModels("account", "owner")
 *     .setPage(1)
 *     .setPageSize(50)
 *     .compile();
 * }</pre>
 */
public class FindQuery {

    private final String model;
    private final List<LogicalGroup> groups = new ArrayList<>();
    private List<String> fetchModels;
    private List<String> fetchReferencingModels;
    private List<String> autowire;
    private List<String> keys;
    private Integer page;
    private Integer pageSize;
    private LogicalGroup currentGroup;

    private FindQuery(String model) {
        this.model = model;
    }

    /**
     * Creates a new query for the given model.
     */
    public static FindQuery from(String model) {
        return new FindQuery(model);
    }

    // ==================== Group Operations ====================

    /**
     * Starts a new AND group. Conditions added after this will be combined with AND.
     */
    public FindQuery newGroupWithAnd() {
        currentGroup = LogicalGroup.and();
        groups.add(currentGroup);
        return this;
    }

    /**
     * Starts a new OR group. Conditions added after this will be combined with OR.
     */
    public FindQuery newGroupWithOr() {
        currentGroup = LogicalGroup.or();
        groups.add(currentGroup);
        return this;
    }

    // ==================== AND Conditions ====================

    /**
     * Adds an AND condition: field = value
     */
    public FindQuery andWhereIsEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.EQUAL, value));
        return this;
    }

    /**
     * Adds an AND condition: field != value
     */
    public FindQuery andWhereIsNotEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.NOT_EQUAL, value));
        return this;
    }

    /**
     * Adds an AND condition: field IS NULL
     */
    public FindQuery andWhereIsNull(String field) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.IS, null));
        return this;
    }

    /**
     * Adds an AND condition: field IS NOT NULL
     */
    public FindQuery andWhereIsNotNull(String field) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.IS_NOT, null));
        return this;
    }

    /**
     * Adds an AND condition: field > value
     */
    public FindQuery andWhereIsGreaterThan(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.GREATER_THAN, value));
        return this;
    }

    /**
     * Adds an AND condition: field >= value
     */
    public FindQuery andWhereIsGreaterOrEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.GREATER_OR_EQUAL, value));
        return this;
    }

    /**
     * Adds an AND condition: field < value
     */
    public FindQuery andWhereIsLessThan(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.LESS_THAN, value));
        return this;
    }

    /**
     * Adds an AND condition: field <= value
     */
    public FindQuery andWhereIsLessOrEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.LESS_OR_EQUAL, value));
        return this;
    }

    /**
     * Adds an AND condition: field LIKE 'value%' (starts with)
     */
    public FindQuery andWhereStartsWith(String field, String value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.LIKE, value + "%"));
        return this;
    }

    /**
     * Adds an AND condition: field LIKE '%value' (ends with)
     */
    public FindQuery andWhereEndsWith(String field, String value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.LIKE, "%" + value));
        return this;
    }

    /**
     * Adds an AND condition: field LIKE '%value%' (contains)
     */
    public FindQuery andWhereContains(String field, String value) {
        ensureGroup();
        String escaped = value.replace("%", "");
        currentGroup.addExpression(LogicalExpression.and(field, Operation.LIKE, "%" + escaped + "%"));
        return this;
    }

    /**
     * Adds an AND condition: field IN (values)
     */
    public FindQuery andWhereIsIn(String field, Collection<?> values) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.IN, new ArrayList<>(values)));
        return this;
    }

    /**
     * Adds an AND condition: field IN (values)
     */
    public FindQuery andWhereIsIn(String field, Object... values) {
        return andWhereIsIn(field, List.of(values));
    }

    /**
     * Adds an AND condition: field NOT IN (values)
     */
    public FindQuery andWhereIsNotIn(String field, Collection<?> values) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.NOT_IN, new ArrayList<>(values)));
        return this;
    }

    /**
     * Adds an AND condition: field NOT IN (values)
     */
    public FindQuery andWhereIsNotIn(String field, Object... values) {
        return andWhereIsNotIn(field, List.of(values));
    }

    // ==================== OR Conditions ====================

    /**
     * Adds an OR condition: field = value
     */
    public FindQuery orWhereIsEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.EQUAL, value));
        return this;
    }

    /**
     * Adds an OR condition: field != value
     */
    public FindQuery orWhereIsNotEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.NOT_EQUAL, value));
        return this;
    }

    /**
     * Adds an OR condition: field IS NULL
     */
    public FindQuery orWhereIsNull(String field) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.IS, null));
        return this;
    }

    /**
     * Adds an OR condition: field IS NOT NULL
     */
    public FindQuery orWhereIsNotNull(String field) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.IS_NOT, null));
        return this;
    }

    /**
     * Adds an OR condition: field > value
     */
    public FindQuery orWhereIsGreaterThan(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.GREATER_THAN, value));
        return this;
    }

    /**
     * Adds an OR condition: field >= value
     */
    public FindQuery orWhereIsGreaterOrEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.GREATER_OR_EQUAL, value));
        return this;
    }

    /**
     * Adds an OR condition: field < value
     */
    public FindQuery orWhereIsLessThan(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.LESS_THAN, value));
        return this;
    }

    /**
     * Adds an OR condition: field <= value
     */
    public FindQuery orWhereIsLessOrEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.LESS_OR_EQUAL, value));
        return this;
    }

    /**
     * Adds an OR condition: field LIKE 'value%' (starts with)
     */
    public FindQuery orWhereStartsWith(String field, String value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.LIKE, value + "%"));
        return this;
    }

    /**
     * Adds an OR condition: field LIKE '%value' (ends with)
     */
    public FindQuery orWhereEndsWith(String field, String value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.LIKE, "%" + value));
        return this;
    }

    /**
     * Adds an OR condition: field LIKE '%value%' (contains)
     */
    public FindQuery orWhereContains(String field, String value) {
        ensureGroup();
        String escaped = value.replace("%", "");
        currentGroup.addExpression(LogicalExpression.or(field, Operation.LIKE, "%" + escaped + "%"));
        return this;
    }

    /**
     * Adds an OR condition: field IN (values)
     */
    public FindQuery orWhereIsIn(String field, Collection<?> values) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.IN, new ArrayList<>(values)));
        return this;
    }

    /**
     * Adds an OR condition: field IN (values)
     */
    public FindQuery orWhereIsIn(String field, Object... values) {
        return orWhereIsIn(field, List.of(values));
    }

    /**
     * Adds an OR condition: field NOT IN (values)
     */
    public FindQuery orWhereIsNotIn(String field, Collection<?> values) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.NOT_IN, new ArrayList<>(values)));
        return this;
    }

    /**
     * Adds an OR condition: field NOT IN (values)
     */
    public FindQuery orWhereIsNotIn(String field, Object... values) {
        return orWhereIsNotIn(field, List.of(values));
    }

    // ==================== Key-based Query ====================

    /**
     * Queries by specific keys (primary keys).
     */
    public FindQuery whereWithKeys(Collection<String> keys) {
        this.keys = new ArrayList<>(keys);
        return this;
    }

    /**
     * Queries by specific keys (primary keys).
     */
    public FindQuery whereWithKeys(String... keys) {
        this.keys = List.of(keys);
        return this;
    }

    // ==================== Fetch Related ====================

    /**
     * Fetches related models (forward references).
     */
    public FindQuery fetchModels(String... models) {
        this.fetchModels = List.of(models);
        return this;
    }

    /**
     * Fetches related models (forward references).
     */
    public FindQuery fetchModels(Collection<String> models) {
        this.fetchModels = new ArrayList<>(models);
        return this;
    }

    /**
     * Fetches referencing models (reverse references).
     */
    public FindQuery fetchReferencingModels(String... models) {
        this.fetchReferencingModels = List.of(models);
        return this;
    }

    /**
     * Fetches referencing models (reverse references).
     */
    public FindQuery fetchReferencingModels(Collection<String> models) {
        this.fetchReferencingModels = new ArrayList<>(models);
        return this;
    }

    /**
     * Sets fields for automatic reference resolution.
     */
    public FindQuery setAutowire(String... fields) {
        this.autowire = List.of(fields);
        return this;
    }

    /**
     * Sets fields for automatic reference resolution.
     */
    public FindQuery setAutowire(Collection<String> fields) {
        this.autowire = new ArrayList<>(fields);
        return this;
    }

    // ==================== Pagination ====================

    /**
     * Sets the page number (1-based).
     */
    public FindQuery setPage(int page) {
        this.page = page;
        return this;
    }

    /**
     * Sets the page size.
     */
    public FindQuery setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    // ==================== Build ====================

    /**
     * Compiles the query into a request payload.
     */
    public SBXFindRequest compile() {
        return new SBXFindRequest(
                model,
                groups.isEmpty() ? null : groups,
                page,
                pageSize,
                fetchModels,
                fetchReferencingModels,
                autowire,
                keys
        );
    }

    /**
     * Returns the model name.
     */
    public String getModel() {
        return model;
    }

    /**
     * Returns the page number.
     */
    public Integer getPage() {
        return page;
    }

    /**
     * Returns the page size.
     */
    public Integer getPageSize() {
        return pageSize;
    }

    private void ensureGroup() {
        if (currentGroup == null) {
            newGroupWithAnd();
        }
    }
}
