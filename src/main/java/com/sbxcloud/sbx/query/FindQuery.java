package com.sbxcloud.sbx.query;

import com.sbxcloud.sbx.annotation.SbxModels;
import com.sbxcloud.sbx.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Fluent query builder for SBX find operations.
 * <p>
 * Example usage:
 * <pre>{@code
 * var query = FindQuery.from(Contact.class)
 *     .newGroupWithAnd()
 *     .andWhereIsEqualTo("status", "ACTIVE")
 *     .andWhereIsGreaterThan("age", 18)
 *     .setPageSize(50);
 *
 * // Type is inferred - no need to pass class twice!
 * var response = sbx.find(query);
 * }</pre>
 *
 * @param <T> the entity type (inferred when using from(Class))
 */
public class FindQuery<T> {

    private final String model;
    private final Class<T> type;
    private final List<LogicalGroup> groups = new ArrayList<>();
    private List<String> fetchModels;
    private List<String> fetchReferencingModels;
    private List<String> autowire;
    private List<String> keys;
    private Integer page;
    private Integer pageSize;
    private LogicalGroup currentGroup;

    private FindQuery(String model, Class<T> type) {
        this.model = model;
        this.type = type;
    }

    /**
     * Creates a new query for the given model name.
     * Note: When using this method, you'll need to pass the class to find().
     */
    public static FindQuery<Object> from(String model) {
        return new FindQuery<>(model, null);
    }

    /**
     * Creates a new typed query for a class annotated with @SbxModel.
     * The type is preserved so you don't need to pass it again to find().
     *
     * @param type class annotated with @SbxModel
     * @throws IllegalArgumentException if class is not annotated
     */
    public static <T> FindQuery<T> from(Class<T> type) {
        return new FindQuery<>(SbxModels.getModelName(type), type);
    }

    // ==================== Group Operations ====================

    /**
     * Starts a new AND group. Conditions added after this will be combined with AND.
     */
    public FindQuery<T> newGroupWithAnd() {
        currentGroup = LogicalGroup.and();
        groups.add(currentGroup);
        return this;
    }

    /**
     * Starts a new OR group. Conditions added after this will be combined with OR.
     */
    public FindQuery<T> newGroupWithOr() {
        currentGroup = LogicalGroup.or();
        groups.add(currentGroup);
        return this;
    }

    // ==================== AND Conditions ====================

    /**
     * Adds an AND condition: field = value
     */
    public FindQuery<T> andWhereIsEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.EQUAL, value));
        return this;
    }

    /**
     * Adds an AND condition: field != value
     */
    public FindQuery<T> andWhereIsNotEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.NOT_EQUAL, value));
        return this;
    }

    /**
     * Adds an AND condition: field IS NULL
     */
    public FindQuery<T> andWhereIsNull(String field) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.IS, null));
        return this;
    }

    /**
     * Adds an AND condition: field IS NOT NULL
     */
    public FindQuery<T> andWhereIsNotNull(String field) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.IS_NOT, null));
        return this;
    }

    /**
     * Adds an AND condition: field > value
     */
    public FindQuery<T> andWhereIsGreaterThan(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.GREATER_THAN, value));
        return this;
    }

    /**
     * Adds an AND condition: field >= value
     */
    public FindQuery<T> andWhereIsGreaterOrEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.GREATER_OR_EQUAL, value));
        return this;
    }

    /**
     * Adds an AND condition: field is less than value
     */
    public FindQuery<T> andWhereIsLessThan(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.LESS_THAN, value));
        return this;
    }

    /**
     * Adds an AND condition: field is less than or equal to value
     */
    public FindQuery<T> andWhereIsLessOrEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.LESS_OR_EQUAL, value));
        return this;
    }

    /**
     * Adds an AND condition: field LIKE 'value%' (starts with)
     */
    public FindQuery<T> andWhereStartsWith(String field, String value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.LIKE, value + "%"));
        return this;
    }

    /**
     * Adds an AND condition: field LIKE '%value' (ends with)
     */
    public FindQuery<T> andWhereEndsWith(String field, String value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.LIKE, "%" + value));
        return this;
    }

    /**
     * Adds an AND condition: field LIKE '%value%' (contains)
     */
    public FindQuery<T> andWhereContains(String field, String value) {
        ensureGroup();
        String escaped = value.replace("%", "");
        currentGroup.addExpression(LogicalExpression.and(field, Operation.LIKE, "%" + escaped + "%"));
        return this;
    }

    /**
     * Adds an AND condition: field IN (values)
     */
    public FindQuery<T> andWhereIsIn(String field, Collection<?> values) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.IN, new ArrayList<>(values)));
        return this;
    }

    /**
     * Adds an AND condition: field IN (values)
     */
    public FindQuery<T> andWhereIsIn(String field, Object... values) {
        return andWhereIsIn(field, List.of(values));
    }

    /**
     * Adds an AND condition: field NOT IN (values)
     */
    public FindQuery<T> andWhereIsNotIn(String field, Collection<?> values) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.and(field, Operation.NOT_IN, new ArrayList<>(values)));
        return this;
    }

    /**
     * Adds an AND condition: field NOT IN (values)
     */
    public FindQuery<T> andWhereIsNotIn(String field, Object... values) {
        return andWhereIsNotIn(field, List.of(values));
    }

    // ==================== OR Conditions ====================

    /**
     * Adds an OR condition: field = value
     */
    public FindQuery<T> orWhereIsEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.EQUAL, value));
        return this;
    }

    /**
     * Adds an OR condition: field != value
     */
    public FindQuery<T> orWhereIsNotEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.NOT_EQUAL, value));
        return this;
    }

    /**
     * Adds an OR condition: field IS NULL
     */
    public FindQuery<T> orWhereIsNull(String field) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.IS, null));
        return this;
    }

    /**
     * Adds an OR condition: field IS NOT NULL
     */
    public FindQuery<T> orWhereIsNotNull(String field) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.IS_NOT, null));
        return this;
    }

    /**
     * Adds an OR condition: field > value
     */
    public FindQuery<T> orWhereIsGreaterThan(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.GREATER_THAN, value));
        return this;
    }

    /**
     * Adds an OR condition: field >= value
     */
    public FindQuery<T> orWhereIsGreaterOrEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.GREATER_OR_EQUAL, value));
        return this;
    }

    /**
     * Adds an OR condition: field is less than value
     */
    public FindQuery<T> orWhereIsLessThan(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.LESS_THAN, value));
        return this;
    }

    /**
     * Adds an OR condition: field is less than or equal to value
     */
    public FindQuery<T> orWhereIsLessOrEqualTo(String field, Object value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.LESS_OR_EQUAL, value));
        return this;
    }

    /**
     * Adds an OR condition: field LIKE 'value%' (starts with)
     */
    public FindQuery<T> orWhereStartsWith(String field, String value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.LIKE, value + "%"));
        return this;
    }

    /**
     * Adds an OR condition: field LIKE '%value' (ends with)
     */
    public FindQuery<T> orWhereEndsWith(String field, String value) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.LIKE, "%" + value));
        return this;
    }

    /**
     * Adds an OR condition: field LIKE '%value%' (contains)
     */
    public FindQuery<T> orWhereContains(String field, String value) {
        ensureGroup();
        String escaped = value.replace("%", "");
        currentGroup.addExpression(LogicalExpression.or(field, Operation.LIKE, "%" + escaped + "%"));
        return this;
    }

    /**
     * Adds an OR condition: field IN (values)
     */
    public FindQuery<T> orWhereIsIn(String field, Collection<?> values) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.IN, new ArrayList<>(values)));
        return this;
    }

    /**
     * Adds an OR condition: field IN (values)
     */
    public FindQuery<T> orWhereIsIn(String field, Object... values) {
        return orWhereIsIn(field, List.of(values));
    }

    /**
     * Adds an OR condition: field NOT IN (values)
     */
    public FindQuery<T> orWhereIsNotIn(String field, Collection<?> values) {
        ensureGroup();
        currentGroup.addExpression(LogicalExpression.or(field, Operation.NOT_IN, new ArrayList<>(values)));
        return this;
    }

    /**
     * Adds an OR condition: field NOT IN (values)
     */
    public FindQuery<T> orWhereIsNotIn(String field, Object... values) {
        return orWhereIsNotIn(field, List.of(values));
    }

    // ==================== Key-based Query ====================

    /**
     * Queries by specific keys (primary keys).
     */
    public FindQuery<T> whereWithKeys(Collection<String> keys) {
        this.keys = new ArrayList<>(keys);
        return this;
    }

    /**
     * Queries by specific keys (primary keys).
     */
    public FindQuery<T> whereWithKeys(String... keys) {
        this.keys = List.of(keys);
        return this;
    }

    // ==================== Fetch Related ====================

    /**
     * Fetches related models (forward references).
     */
    public FindQuery<T> fetchModels(String... models) {
        this.fetchModels = List.of(models);
        return this;
    }

    /**
     * Fetches related models (forward references).
     */
    public FindQuery<T> fetchModels(Collection<String> models) {
        this.fetchModels = new ArrayList<>(models);
        return this;
    }

    /**
     * Fetches referencing models (reverse references).
     */
    public FindQuery<T> fetchReferencingModels(String... models) {
        this.fetchReferencingModels = List.of(models);
        return this;
    }

    /**
     * Fetches referencing models (reverse references).
     */
    public FindQuery<T> fetchReferencingModels(Collection<String> models) {
        this.fetchReferencingModels = new ArrayList<>(models);
        return this;
    }

    /**
     * Sets fields for automatic reference resolution.
     */
    public FindQuery<T> setAutowire(String... fields) {
        this.autowire = List.of(fields);
        return this;
    }

    /**
     * Sets fields for automatic reference resolution.
     */
    public FindQuery<T> setAutowire(Collection<String> fields) {
        this.autowire = new ArrayList<>(fields);
        return this;
    }

    // ==================== Pagination ====================

    /**
     * Sets the page number (1-based).
     */
    public FindQuery<T> setPage(int page) {
        this.page = page;
        return this;
    }

    /**
     * Sets the page size.
     */
    public FindQuery<T> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    // ==================== Build ====================

    /**
     * Compiles the query into a request payload.
     */
    public SBXFindRequest compile() {
        WhereClause whereClause;
        if (keys != null && !keys.isEmpty()) {
            whereClause = WhereClause.keys(keys);
        } else if (!groups.isEmpty()) {
            whereClause = WhereClause.conditions(groups);
        } else {
            whereClause = null;
        }

        return new SBXFindRequest(
                model,
                null, // domain is added by SBXService
                whereClause,
                page,
                pageSize,
                fetchModels,
                fetchReferencingModels,
                autowire
        );
    }

    /**
     * Returns the model name.
     */
    public String getModel() {
        return model;
    }

    /**
     * Returns the entity type class, or null if created with from(String).
     */
    public Class<T> getType() {
        return type;
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
