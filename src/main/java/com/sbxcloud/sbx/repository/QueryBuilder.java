package com.sbxcloud.sbx.repository;

import com.sbxcloud.sbx.client.SBXService;
import com.sbxcloud.sbx.model.SBXFindResponse;
import com.sbxcloud.sbx.query.FindQuery;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Fluent query builder for SBX repositories.
 *
 * <pre>{@code
 * repo.query()
 *     .where(q -> q
 *         .andWhereIsGreaterThan("price", 10)
 *         .andWhereIsEqualTo("status", "ACTIVE"))
 *     .fetch("masterlist", "category")
 *     .page(1, 50)
 *     .list();
 * }</pre>
 *
 * @param <T> Entity type
 */
public class QueryBuilder<T> {

    private final SBXService service;
    private final Class<T> entityType;
    private final FindQuery query;

    QueryBuilder(SBXService service, Class<T> entityType, String modelName) {
        this.service = service;
        this.entityType = entityType;
        this.query = FindQuery.from(modelName);
    }

    // ==================== Conditions ====================

    /**
     * Adds WHERE conditions using the FindQuery fluent API.
     *
     * <pre>{@code
     * .where(q -> q
     *     .newGroupWithAnd()
     *     .andWhereIsEqualTo("status", "ACTIVE")
     *     .andWhereIsGreaterThan("price", 10)
     *     .newGroupWithOr()
     *     .orWhereContains("name", "test"))
     * }</pre>
     */
    public QueryBuilder<T> where(Consumer<FindQuery> conditions) {
        conditions.accept(query);
        return this;
    }

    /**
     * Shorthand for simple equality condition.
     */
    public QueryBuilder<T> whereEquals(String field, Object value) {
        query.andWhereIsEqualTo(field, value);
        return this;
    }

    /**
     * Queries by specific keys.
     */
    public QueryBuilder<T> whereKeys(String... keys) {
        query.whereWithKeys(keys);
        return this;
    }

    /**
     * Queries by specific keys.
     */
    public QueryBuilder<T> whereKeys(Collection<String> keys) {
        query.whereWithKeys(keys);
        return this;
    }

    // ==================== Fetch Related ====================

    /**
     * Fetches related models (forward references).
     */
    public QueryBuilder<T> fetch(String... models) {
        query.fetchModels(models);
        return this;
    }

    /**
     * Fetches related models (forward references).
     */
    public QueryBuilder<T> fetch(Collection<String> models) {
        query.fetchModels(models);
        return this;
    }

    /**
     * Fetches referencing models (reverse references).
     */
    public QueryBuilder<T> fetchReferencing(String... models) {
        query.fetchReferencingModels(models);
        return this;
    }

    /**
     * Sets fields for automatic reference resolution.
     */
    public QueryBuilder<T> autowire(String... fields) {
        query.setAutowire(fields);
        return this;
    }

    // ==================== Pagination ====================

    /**
     * Sets page number and size.
     *
     * @param page     page number (1-based)
     * @param pageSize items per page
     */
    public QueryBuilder<T> page(int page, int pageSize) {
        query.setPage(page);
        query.setPageSize(pageSize);
        return this;
    }

    /**
     * Sets page number (1-based).
     */
    public QueryBuilder<T> page(int page) {
        query.setPage(page);
        return this;
    }

    /**
     * Sets page size.
     */
    public QueryBuilder<T> pageSize(int size) {
        query.setPageSize(size);
        return this;
    }

    // ==================== Execution ====================

    /**
     * Executes the query and returns the full response.
     */
    public SBXFindResponse<T> execute() {
        return service.find(query, entityType);
    }

    /**
     * Executes the query and returns all matching results across all pages.
     */
    public List<T> list() {
        var response = service.findAll(query, entityType);
        return response.success() && response.results() != null
                ? response.results()
                : List.of();
    }

    /**
     * Executes the query and returns results for the current page only.
     */
    public List<T> listPage() {
        var response = execute();
        return response.success() && response.results() != null
                ? response.results()
                : List.of();
    }

    /**
     * Executes the query and returns the first result.
     */
    public Optional<T> first() {
        query.setPageSize(1);
        var response = execute();
        if (response.success() && response.results() != null && !response.results().isEmpty()) {
            return Optional.of(response.results().get(0));
        }
        return Optional.empty();
    }

    /**
     * Executes the query and returns the first result, or throws if none found.
     *
     * @throws NoSuchElementException if no result found
     */
    public T firstOrThrow() {
        return first().orElseThrow(() ->
                new NoSuchElementException("No entity found matching query"));
    }

    /**
     * Executes the query and returns the count of matching records.
     */
    public long count() {
        query.setPageSize(1);
        var response = execute();
        return response.success() && response.rowCount() != null
                ? response.rowCount()
                : 0;
    }

    /**
     * Checks if any records match the query.
     */
    public boolean exists() {
        return count() > 0;
    }

    /**
     * Returns the underlying FindQuery for advanced customization.
     */
    public FindQuery getQuery() {
        return query;
    }
}
