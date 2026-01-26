package com.sbxcloud.sbx.repository;

import com.sbxcloud.sbx.annotation.SbxEntity;
import com.sbxcloud.sbx.annotation.SbxModels;
import com.sbxcloud.sbx.client.SBXService;
import com.sbxcloud.sbx.model.SBXFindResponse;
import com.sbxcloud.sbx.model.SBXResponse;
import com.sbxcloud.sbx.query.FindQuery;

import java.util.*;
import java.util.function.Consumer;

/**
 * Type-safe repository for SBX entities.
 * <p>
 * Provides Spring Data-like CRUD operations without repeating model names or types.
 *
 * <pre>{@code
 * // Get a typed repository
 * SbxRepository<InventoryHistory> repo = sbx.repository(InventoryHistory.class);
 *
 * // Simple CRUD
 * repo.findAll();
 * repo.findById("key123");
 * repo.save(entity);
 * repo.delete(entity);
 *
 * // Fluent queries
 * repo.query()
 *     .where(q -> q.andWhereIsGreaterThan("price", 10))
 *     .fetch("masterlist")
 *     .page(1, 50)
 *     .list();
 * }</pre>
 *
 * @param <T> Entity type, must be annotated with @SbxModel and implement SbxEntity
 */
public class SbxRepository<T extends SbxEntity> {

    private final SBXService service;
    private final Class<T> entityType;
    private final String modelName;

    public SbxRepository(SBXService service, Class<T> entityType) {
        this.service = service;
        this.entityType = entityType;
        this.modelName = SbxModels.getModelName(entityType);
    }

    // ==================== Find Operations ====================

    /**
     * Finds an entity by its key.
     *
     * @param key the entity key
     * @return Optional containing the entity, or empty if not found
     */
    public Optional<T> findById(String key) {
        var response = service.find(
                FindQuery.from(modelName).whereWithKeys(key),
                entityType
        );
        if (response.success() && response.results() != null && !response.results().isEmpty()) {
            return Optional.of(response.results().get(0));
        }
        return Optional.empty();
    }

    /**
     * Finds entities by their keys.
     */
    public List<T> findByIds(String... keys) {
        return findByIds(Arrays.asList(keys));
    }

    /**
     * Finds entities by their keys.
     */
    public List<T> findByIds(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        var response = service.find(
                FindQuery.from(modelName).whereWithKeys(keys),
                entityType
        );
        return response.success() && response.results() != null
                ? response.results()
                : List.of();
    }

    /**
     * Finds all entities (with default pagination).
     */
    public List<T> findAll() {
        var response = service.findAll(FindQuery.from(modelName), entityType);
        return response.success() && response.results() != null
                ? response.results()
                : List.of();
    }

    /**
     * Finds all entities with pagination.
     *
     * @param page     page number (1-based)
     * @param pageSize number of items per page
     */
    public SBXFindResponse<T> findAll(int page, int pageSize) {
        return service.find(
                FindQuery.from(modelName).setPage(page).setPageSize(pageSize),
                entityType
        );
    }

    /**
     * Finds entities matching conditions.
     *
     * <pre>{@code
     * repo.findWhere(q -> q
     *     .andWhereIsGreaterThan("price", 10)
     *     .andWhereIsEqualTo("status", "ACTIVE")
     * );
     * }</pre>
     */
    public List<T> findWhere(Consumer<FindQuery> conditions) {
        var query = FindQuery.from(modelName);
        conditions.accept(query);
        var response = service.findAll(query, entityType);
        return response.success() && response.results() != null
                ? response.results()
                : List.of();
    }

    /**
     * Checks if an entity with the given key exists.
     */
    public boolean existsById(String key) {
        return findById(key).isPresent();
    }

    /**
     * Counts all entities.
     */
    public long count() {
        var response = service.find(
                FindQuery.from(modelName).setPage(1).setPageSize(1),
                entityType
        );
        return response.success() && response.rowCount() != null
                ? response.rowCount()
                : 0;
    }

    // ==================== Save Operations ====================

    /**
     * Saves an entity (insert if new, update if exists).
     * An entity is considered new if its key is null.
     *
     * @param entity the entity to save
     * @return the saved entity's key
     */
    public String save(T entity) {
        SBXResponse<?> response;
        if (entity.key() == null) {
            response = service.create(entity);
        } else {
            response = service.update(entity);
            if (response.success()) {
                return entity.key();
            }
        }

        if (response.success() && response.keys() != null && !response.keys().isEmpty()) {
            return response.keys().get(0);
        }

        throw new SbxRepositoryException("Save failed: " + response.error());
    }

    /**
     * Saves multiple entities.
     *
     * @return list of saved keys
     */
    @SafeVarargs
    public final List<String> saveAll(T... entities) {
        return saveAll(Arrays.asList(entities));
    }

    /**
     * Saves multiple entities, separating inserts from updates.
     *
     * @return list of saved keys
     */
    public List<String> saveAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        List<T> toInsert = new ArrayList<>();
        List<T> toUpdate = new ArrayList<>();

        for (T entity : entities) {
            if (entity.key() == null) {
                toInsert.add(entity);
            } else {
                toUpdate.add(entity);
            }
        }

        List<String> savedKeys = new ArrayList<>();

        if (!toInsert.isEmpty()) {
            var response = service.create(toInsert.toArray());
            if (response.success() && response.keys() != null) {
                savedKeys.addAll(response.keys());
            }
        }

        if (!toUpdate.isEmpty()) {
            var response = service.update(toUpdate.toArray());
            if (response.success()) {
                toUpdate.forEach(e -> savedKeys.add(e.key()));
            }
        }

        return savedKeys;
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes an entity.
     */
    public void delete(T entity) {
        if (entity.key() == null) {
            throw new SbxRepositoryException("Cannot delete entity without key");
        }
        deleteById(entity.key());
    }

    /**
     * Deletes an entity by key.
     */
    public void deleteById(String key) {
        var response = service.delete(modelName, key);
        if (!response.success()) {
            throw new SbxRepositoryException("Delete failed: " + response.error());
        }
    }

    /**
     * Deletes entities by keys.
     */
    public void deleteByIds(String... keys) {
        deleteByIds(Arrays.asList(keys));
    }

    /**
     * Deletes entities by keys.
     */
    public void deleteByIds(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        var response = service.delete(modelName, new ArrayList<>(keys));
        if (!response.success()) {
            throw new SbxRepositoryException("Delete failed: " + response.error());
        }
    }

    /**
     * Deletes all given entities.
     */
    @SafeVarargs
    public final void deleteAll(T... entities) {
        deleteAll(Arrays.asList(entities));
    }

    /**
     * Deletes all given entities.
     */
    public void deleteAll(Collection<T> entities) {
        List<String> keys = entities.stream()
                .map(SbxEntity::key)
                .filter(Objects::nonNull)
                .toList();
        deleteByIds(keys);
    }

    // ==================== Query Builder ====================

    /**
     * Starts a fluent query builder.
     *
     * <pre>{@code
     * repo.query()
     *     .where(q -> q.andWhereIsGreaterThan("price", 10))
     *     .fetch("masterlist")
     *     .page(1, 50)
     *     .list();
     * }</pre>
     */
    public QueryBuilder<T> query() {
        return new QueryBuilder<>(service, entityType, modelName);
    }

    // ==================== Accessors ====================

    /**
     * Returns the model name for this repository.
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Returns the entity type for this repository.
     */
    public Class<T> getEntityType() {
        return entityType;
    }
}
