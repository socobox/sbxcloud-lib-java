package com.sbxcloud.sbx.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sbxcloud.sbx.annotation.SbxEntity;
import com.sbxcloud.sbx.annotation.SbxModels;
import com.sbxcloud.sbx.exception.SBXException;
import com.sbxcloud.sbx.jackson.SbxModule;
import com.sbxcloud.sbx.model.*;
import com.sbxcloud.sbx.query.FindQuery;
import com.sbxcloud.sbx.repository.SbxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main service class for interacting with SBX Cloud APIs.
 * <p>
 * Use {@link SBXServiceFactory} to create instances.
 */
public class SBXService {

    private static final Logger log = LoggerFactory.getLogger(SBXService.class);

    private static final int DEFAULT_CHUNK_SIZE = 100;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(150);

    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private RestClient restClient;
    private String appKey;
    private String token;
    private int domain;
    private boolean debug;
    private SBXConfig config;

    SBXService(String appKey, String token, int domain, String baseUrl, boolean debug) {
        this.appKey = appKey;
        this.token = token;
        this.domain = domain;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.debug = debug;

        this.objectMapper = createObjectMapper();
        this.restClient = createRestClient();
    }

    // ==================== Repository Factory ====================

    /**
     * Creates a type-safe repository for the given entity class.
     * <p>
     * The entity class must be annotated with @SbxModel and implement SbxEntity.
     *
     * <pre>{@code
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
     * @param entityType class annotated with @SbxModel, implementing SbxEntity
     * @return typed repository for the entity
     */
    public <T extends SbxEntity> SbxRepository<T> repository(Class<T> entityType) {
        return new SbxRepository<>(this, entityType);
    }

    // ==================== Data Operations ====================

    /**
     * Finds records matching the typed query.
     * Type is inferred from FindQuery - no need to pass class twice.
     *
     * <pre>{@code
     * var query = FindQuery.from(Contact.class)
     *     .andWhereIsEqualTo("status", "ACTIVE");
     *
     * var response = sbx.find(query);  // Type inferred!
     * }</pre>
     *
     * @throws IllegalStateException if query was created with from(String) instead of from(Class)
     */
    @SuppressWarnings("unchecked")
    public <T> SBXFindResponse<T> find(FindQuery<T> query) {
        Class<T> type = query.getType();
        if (type == null) {
            throw new IllegalStateException(
                    "Query created with from(String) - use find(query, Class) instead or use from(Class)");
        }
        return find(query.compile(), type);
    }

    /**
     * Finds records matching the query.
     */
    public <T> SBXFindResponse<T> find(FindQuery<?> query, Class<T> type) {
        return find(query.compile(), type);
    }

    /**
     * Finds records matching the query.
     */
    public <T> SBXFindResponse<T> find(SBXFindRequest request, Class<T> type) {
        var requestWithDomain = request.withDomain(domain);
        if (debug) {
            log.info("SBX find: {}", requestWithDomain);
        }

        try {
            var response = restClient.post()
                    .uri("/api/data/v1/row/find")
                    .body(requestWithDomain)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            return mapFindResponse(response, type);
        } catch (Exception e) {
            log.error("Find operation failed", e);
            return SBXFindResponse.failure(e.getMessage());
        }
    }

    /**
     * Finds a single record matching the typed query (sets page size to 1).
     */
    public <T> SBXFindResponse<T> findOne(FindQuery<T> query) {
        query.setPageSize(1);
        return find(query);
    }

    /**
     * Finds a single record matching the query (sets page size to 1).
     */
    public <T> SBXFindResponse<T> findOne(FindQuery<?> query, Class<T> type) {
        query.setPageSize(1);
        return find(query, type);
    }

    /**
     * Finds all records matching the typed query, automatically handling pagination.
     */
    public <T> SBXFindResponse<T> findAll(FindQuery<T> query) {
        Class<T> type = query.getType();
        if (type == null) {
            throw new IllegalStateException(
                    "Query created with from(String) - use findAll(query, Class) instead");
        }
        return findAll(query, type);
    }

    /**
     * Finds all records matching the query, automatically handling pagination.
     */
    public <T> SBXFindResponse<T> findAll(FindQuery<?> query, Class<T> type) {
        List<T> allResults = new ArrayList<>();
        Map<String, Map<String, Object>> allFetchedResults = new HashMap<>();

        int currentPage = 1;
        SBXFindResponse<T> response;

        do {
            query.setPage(currentPage);
            response = find(query, type);

            if (!response.success()) {
                return response;
            }

            if (response.results() != null) {
                allResults.addAll(response.results());
            }

            if (response.fetchedResults() != null) {
                response.fetchedResults().forEach((model, items) ->
                        allFetchedResults.merge(model, items, (existing, newItems) -> {
                            existing.putAll(newItems);
                            return existing;
                        })
                );
            }

            currentPage++;
        } while (response.hasMorePages(currentPage - 1));

        return new SBXFindResponse<>(
                true,
                null,
                null,
                response.totalPages(),
                allResults.size(),
                allResults,
                allFetchedResults.isEmpty() ? null : allFetchedResults,
                response.model()
        );
    }

    /**
     * Creates new records.
     */
    public <T> SBXResponse<T> create(String model, List<Map<String, Object>> rows) {
        return upsert("/api/data/v1/row", model, rows, true);
    }

    /**
     * Creates a single new record.
     */
    public <T> SBXResponse<T> create(String model, Map<String, Object> row) {
        return create(model, List.of(row));
    }

    /**
     * Updates existing records.
     */
    public <T> SBXResponse<T> update(String model, List<Map<String, Object>> rows) {
        return upsert("/api/data/v1/row/update", model, rows, false);
    }

    /**
     * Updates a single record.
     */
    public <T> SBXResponse<T> update(String model, Map<String, Object> row) {
        return update(model, List.of(row));
    }

    /**
     * Deletes records by keys.
     */
    public SBXResponse<Void> delete(String model, List<String> keys) {
        if (debug) {
            log.info("SBX delete: model={}, keys={}", model, keys.size());
        }

        try {
            for (List<String> chunk : partition(keys, DEFAULT_CHUNK_SIZE)) {
                var request = SBXDeleteRequest.of(model, domain, chunk);
                var response = restClient.post()
                        .uri("/api/data/v1/row/delete")
                        .body(request)
                        .retrieve()
                        .body(new ParameterizedTypeReference<SBXResponse<Void>>() {});

                if (response == null || !response.success()) {
                    return response != null ? response : SBXResponse.failure("Delete operation failed");
                }
            }
            return SBXResponse.ok();
        } catch (Exception e) {
            log.error("Delete operation failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    /**
     * Deletes a single record by key.
     */
    public SBXResponse<Void> delete(String model, String key) {
        return delete(model, List.of(key));
    }

    // ==================== Type-safe Operations (with @SbxModel) ====================

    /**
     * Finds all records for an @SbxModel annotated class.
     *
     * @param type class annotated with @SbxModel
     * @return find response with results
     */
    public <T> SBXFindResponse<T> find(Class<T> type) {
        return find(FindQuery.from(type), type);
    }

    /**
     * Creates a new record from an @SbxModel annotated entity.
     * Model name is inferred from the @SbxModel annotation.
     *
     * @param entity the entity to create
     * @return response with created key
     */
    public <T> SBXResponse<T> create(T entity) {
        String model = SbxModels.getModelName(entity.getClass());
        Map<String, Object> row = objectMapper.convertValue(entity, new TypeReference<>() {});
        return create(model, row);
    }

    /**
     * Creates multiple records from @SbxModel annotated entities.
     * Model name is inferred from the @SbxModel annotation of the first entity.
     *
     * @param entities the entities to create
     * @return response with created keys
     */
    @SafeVarargs
    public final <T> SBXResponse<T> create(T... entities) {
        if (entities == null || entities.length == 0) {
            return SBXResponse.failure("No entities provided");
        }
        String model = SbxModels.getModelName(entities[0].getClass());
        List<Map<String, Object>> rows = Arrays.stream(entities)
                .map(e -> objectMapper.convertValue(e, new TypeReference<Map<String, Object>>() {}))
                .toList();
        return create(model, rows);
    }

    /**
     * Updates an @SbxModel annotated entity.
     * The entity must have a non-null key.
     *
     * @param entity the entity to update
     * @return response
     */
    public <T> SBXResponse<T> update(T entity) {
        String model = SbxModels.getModelName(entity.getClass());
        Map<String, Object> row = objectMapper.convertValue(entity, new TypeReference<>() {});
        return update(model, row);
    }

    /**
     * Updates multiple @SbxModel annotated entities.
     *
     * @param entities the entities to update
     * @return response
     */
    @SafeVarargs
    public final <T> SBXResponse<T> update(T... entities) {
        if (entities == null || entities.length == 0) {
            return SBXResponse.failure("No entities provided");
        }
        String model = SbxModels.getModelName(entities[0].getClass());
        List<Map<String, Object>> rows = Arrays.stream(entities)
                .map(e -> objectMapper.convertValue(e, new TypeReference<Map<String, Object>>() {}))
                .toList();
        return update(model, rows);
    }

    /**
     * Deletes an @SbxEntity by extracting its key.
     *
     * @param entity the entity to delete (must implement SbxEntity)
     * @return response
     */
    public <T extends SbxEntity> SBXResponse<Void> delete(T entity) {
        if (entity.key() == null) {
            return SBXResponse.failure("Entity has no key");
        }
        String model = SbxModels.getModelName(entity.getClass());
        return delete(model, entity.key());
    }

    /**
     * Deletes records by keys using @SbxModel annotation for model name.
     *
     * @param type class annotated with @SbxModel
     * @param keys the keys to delete
     * @return response
     */
    public SBXResponse<Void> delete(Class<?> type, String... keys) {
        String model = SbxModels.getModelName(type);
        return delete(model, List.of(keys));
    }

    /**
     * Deletes records by keys using @SbxModel annotation for model name.
     *
     * @param type class annotated with @SbxModel
     * @param keys the keys to delete
     * @return response
     */
    public SBXResponse<Void> delete(Class<?> type, List<String> keys) {
        String model = SbxModels.getModelName(type);
        return delete(model, keys);
    }

    // ==================== Authentication ====================

    /**
     * Authenticates a user.
     */
    public SBXUserResponse login(String login, String password) {
        if (debug) {
            log.info("SBX login: {}", login);
        }

        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/user/v1/login")
                            .queryParam("domain", domain)
                            .build())
                    .body(LoginRequest.of(login, password))
                    .retrieve()
                    .body(SBXUserResponse.class);
        } catch (Exception e) {
            log.error("Login failed", e);
            return SBXUserResponse.failure(e.getMessage());
        }
    }

    /**
     * Validates the current session token.
     */
    public SBXUserResponse validateSession() {
        if (debug) {
            log.info("SBX validate session");
        }

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/user/v1/validate")
                            .queryParam("domain", domain)
                            .build())
                    .retrieve()
                    .body(SBXUserResponse.class);
        } catch (Exception e) {
            log.error("Session validation failed", e);
            return SBXUserResponse.failure(e.getMessage());
        }
    }

    /**
     * Changes the user's password.
     */
    public SBXResponse<Void> changePassword(String currentPassword, String newPassword, int userId) {
        if (debug) {
            log.info("SBX change password for user: {}", userId);
        }

        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/user/v1/password/change")
                            .queryParam("domain", domain)
                            .queryParam("current", currentPassword)
                            .queryParam("password", newPassword)
                            .queryParam("user_id", userId)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Change password failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    /**
     * Sends a password reset request email.
     */
    public SBXResponse<Void> sendPasswordResetRequest(String email, String subject, String templateKey) {
        if (debug) {
            log.info("SBX send password reset request: {}", email);
        }

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/user/v1/password/request")
                            .queryParam("domain", domain)
                            .queryParam("user_email", email)
                            .queryParam("subject", subject)
                            .queryParam("email_template", templateKey)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Send password reset request failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    /**
     * Completes a password reset using a reset code.
     */
    public SBXResponse<Void> resetPassword(int userId, String code, String newPassword) {
        if (debug) {
            log.info("SBX reset password for user: {}", userId);
        }

        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/user/v1/password")
                            .queryParam("domain", domain)
                            .queryParam("user_id", userId)
                            .queryParam("code", code)
                            .queryParam("password", newPassword)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Reset password failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    /**
     * Checks if an email is available (not registered).
     */
    public SBXResponse<Void> checkEmailAvailable(String email) {
        if (debug) {
            log.info("SBX check email available: {}", email);
        }

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/user/v1/user/exist")
                            .queryParam("domain", domain)
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Check email available failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    // ==================== Files ====================

    /**
     * Uploads a file.
     *
     * @param fileName    the name of the file
     * @param fileContent Base64 encoded file content (optionally with data URL prefix)
     * @param folderKey   optional folder key to upload into
     */
    public SBXResponse<Void> uploadFile(String fileName, String fileContent, String folderKey) {
        if (debug) {
            log.info("SBX upload file: {}", fileName);
        }

        try {
            byte[] data = decodeBase64Content(fileContent);
            String mimeType = detectMimeType(fileName, fileContent);

            var params = new LinkedHashMap<String, Object>();
            params.put("file_name", fileName);
            params.put("file", Base64.getEncoder().encodeToString(data));
            params.put("mimetype", mimeType);
            if (folderKey != null) {
                params.put("folder", folderKey);
            }

            return restClient.post()
                    .uri("/api/content/v1/upload")
                    .body(params)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Upload file failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    /**
     * Downloads a file by key.
     */
    public byte[] downloadFile(String key) {
        if (debug) {
            log.info("SBX download file: {}", key);
        }

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/content/v1/download")
                        .queryParam("key", key)
                        .build())
                .retrieve()
                .body(byte[].class);
    }

    /**
     * Deletes a file by key.
     */
    public SBXResponse<Void> deleteFile(String key) {
        if (debug) {
            log.info("SBX delete file: {}", key);
        }

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/content/v1/delete")
                            .queryParam("key", key)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Delete file failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    // ==================== Folders ====================

    /**
     * Creates a folder.
     */
    public SBXResponse<Void> createFolder(String name, String parentKey) {
        if (debug) {
            log.info("SBX create folder: {} in {}", name, parentKey);
        }

        try {
            var builder = restClient.get()
                    .uri(uriBuilder -> {
                        var ub = uriBuilder.path("/api/content/v1/folder/create")
                                .queryParam("name", name);
                        if (parentKey != null) {
                            ub.queryParam("parent_key", parentKey);
                        }
                        return ub.build();
                    });

            return builder.retrieve().body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Create folder failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    /**
     * Deletes a folder.
     */
    public SBXResponse<Void> deleteFolder(String key) {
        if (debug) {
            log.info("SBX delete folder: {}", key);
        }

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/content/v1/folder/delete")
                            .queryParam("key", key)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Delete folder failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    /**
     * Lists folder contents.
     */
    public SBXResponse<FolderContent> listFolder(String key) {
        if (debug) {
            log.info("SBX list folder: {}", key);
        }

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/content/v1/folder/list")
                            .queryParam("key", key)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("List folder failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    /**
     * Renames a folder.
     */
    public SBXResponse<Void> renameFolder(String key, String newName) {
        if (debug) {
            log.info("SBX rename folder: {} to {}", key, newName);
        }

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/content/v1/folder/rename")
                            .queryParam("key", key)
                            .queryParam("name", newName)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Rename folder failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    // ==================== Email ====================

    /**
     * Sends an email.
     */
    public SBXResponse<Void> sendEmail(EmailParams params) {
        if (debug) {
            log.info("SBX send email to: {}", params.to());
        }

        try {
            return restClient.post()
                    .uri("/api/email/v1/send")
                    .body(params)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Send email failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    /**
     * Sends an email using V2 API.
     */
    public SBXResponse<Void> sendEmailV2(EmailParams params) {
        if (debug) {
            log.info("SBX send email V2 to: {}", params.to());
        }

        try {
            return restClient.post()
                    .uri("/api/email/v2/send")
                    .body(params)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Send email V2 failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    // ==================== Cloud Scripts ====================

    /**
     * Executes a cloud script.
     *
     * @param key    the script key
     * @param params optional parameters
     * @param test   whether to run in test mode
     * @param type   the expected return type
     */
    public <T> T runCloudScript(String key, Map<String, Object> params, boolean test, Class<T> type) {
        if (debug) {
            log.info("SBX run cloud script: {} (test={})", key, test);
        }

        var body = new LinkedHashMap<String, Object>();
        body.put("key", key);
        if (params != null && !params.isEmpty()) {
            body.put("params", params);
        }

        String uri = test ? "/api/cloudscript/v1/run/test" : "/api/cloudscript/v1/run";

        var response = restClient.post()
                .uri(uri)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response != null && response.containsKey("response")) {
            return objectMapper.convertValue(response.get("response"), type);
        }

        return objectMapper.convertValue(response, type);
    }

    /**
     * Executes a cloud script without test mode.
     */
    public <T> T runCloudScript(String key, Map<String, Object> params, Class<T> type) {
        return runCloudScript(key, params, false, type);
    }

    /**
     * Executes a cloud script without parameters.
     */
    public <T> T runCloudScript(String key, Class<T> type) {
        return runCloudScript(key, null, false, type);
    }

    // ==================== Configuration ====================

    /**
     * Loads the application configuration from the server.
     */
    public void loadConfig() {
        if (debug) {
            log.info("SBX load config");
        }

        this.config = restClient.get()
                .uri("/api/domain/v1/app/config")
                .retrieve()
                .body(SBXConfig.class);
    }

    /**
     * Returns the cached configuration.
     *
     * @throws SBXException if configuration hasn't been loaded
     */
    public SBXConfig getConfig() {
        if (config == null) {
            throw new SBXException("Configuration not loaded. Call loadConfig() first.");
        }
        return config;
    }

    // ==================== Multi-domain Support ====================

    /**
     * Updates credentials for multi-domain scenarios.
     */
    public void setMultidomainCredentials(int domain, String appKey, String token) {
        this.domain = domain;
        this.appKey = appKey;
        this.token = token;
        this.restClient = createRestClient();
    }

    /**
     * Updates only the token.
     */
    public void setToken(String token) {
        this.token = token;
        this.restClient = createRestClient();
    }

    // ==================== Accessors ====================

    public String getAppKey() {
        return appKey;
    }

    public int getDomain() {
        return domain;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    // ==================== Private Helpers ====================

    private <T> SBXResponse<T> upsert(String endpoint, String model, List<Map<String, Object>> rows, boolean isCreate) {
        if (debug) {
            log.info("SBX {}: model={}, rows={}", isCreate ? "create" : "update", model, rows.size());
        }

        try {
            // Clean rows for upsert (remove meta, nulls)
            List<Map<String, Object>> cleanedRows = rows.stream()
                    .map(this::cleanForUpsert)
                    .toList();

            List<String> allKeys = new ArrayList<>();

            for (List<Map<String, Object>> chunk : partition(cleanedRows, DEFAULT_CHUNK_SIZE)) {
                var request = SBXUpsertRequest.of(model, domain, chunk);
                var response = restClient.post()
                        .uri(endpoint)
                        .body(request)
                        .retrieve()
                        .body(new ParameterizedTypeReference<SBXResponse<T>>() {});

                if (response == null || !response.success()) {
                    return response != null ? response : SBXResponse.failure("Upsert operation failed");
                }

                if (response.keys() != null) {
                    allKeys.addAll(response.keys());
                }
            }

            return SBXResponse.ok(allKeys);
        } catch (Exception e) {
            log.error("Upsert operation failed", e);
            return SBXResponse.failure(e.getMessage());
        }
    }

    /**
     * Cleans a row for create/update:
     * - Removes _META/meta (read-only)
     * <p>
     * Null values are preserved so fields can be explicitly cleared.
     * For entity-based updates, nulls are already stripped by @SbxModel's
     * {@code @JsonInclude(NON_NULL)} during entity-to-Map conversion.
     */
    private Map<String, Object> cleanForUpsert(Map<String, Object> row) {
        var cleaned = new LinkedHashMap<String, Object>();
        for (var entry : row.entrySet()) {
            String key = entry.getKey();

            // Skip meta fields (read-only)
            if ("_META".equals(key) || "meta".equals(key)) {
                continue;
            }

            cleaned.put(key, entry.getValue());
        }
        return cleaned;
    }

    @SuppressWarnings("unchecked")
    private <T> SBXFindResponse<T> mapFindResponse(Map<String, Object> response, Class<T> type) {
        if (response == null) {
            return SBXFindResponse.failure("Empty response");
        }

        boolean success = Boolean.TRUE.equals(response.get("success"));
        String error = (String) response.get("error");
        String message = (String) response.get("message");

        if (!success) {
            return SBXFindResponse.failure(error, message);
        }

        Integer totalPages = response.get("total_pages") != null
                ? ((Number) response.get("total_pages")).intValue()
                : null;
        Integer rowCount = response.get("row_count") != null
                ? ((Number) response.get("row_count")).intValue()
                : null;

        List<T> results = null;
        Object resultsObj = response.get("results");
        if (resultsObj instanceof List<?> resultsList) {
            results = resultsList.stream()
                    .map(item -> objectMapper.convertValue(item, type))
                    .toList();
        }

        Map<String, Map<String, Object>> fetchedResults = null;
        Object fetchedObj = response.get("fetched_results");
        if (fetchedObj instanceof Map<?, ?> fetchedMap) {
            fetchedResults = objectMapper.convertValue(fetchedMap,
                    new TypeReference<Map<String, Map<String, Object>>>() {});
        }

        List<SBXProperty> model = null;
        Object modelObj = response.get("model");
        if (modelObj instanceof List<?> modelList) {
            model = modelList.stream()
                    .map(item -> objectMapper.convertValue(item, SBXProperty.class))
                    .toList();
        }

        return new SBXFindResponse<>(success, error, message, totalPages, rowCount, results, fetchedResults, model);
    }

    private RestClient createRestClient() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(DEFAULT_TIMEOUT);
        requestFactory.setReadTimeout(DEFAULT_TIMEOUT);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("App-Key", appKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    private ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new SbxModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private byte[] decodeBase64Content(String content) {
        String base64;
        if (content.contains(",")) {
            // Data URL format: data:mime/type;base64,xxxxx
            base64 = content.substring(content.indexOf(",") + 1);
        } else {
            base64 = content;
        }
        return Base64.getDecoder().decode(base64);
    }

    private String detectMimeType(String fileName, String content) {
        // Check for data URL mime type
        if (content.startsWith("data:") && content.contains(";")) {
            return content.substring(5, content.indexOf(";"));
        }

        // Fallback to extension-based detection
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() : "";
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "zip" -> "application/zip";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default -> "application/octet-stream";
        };
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }
}
