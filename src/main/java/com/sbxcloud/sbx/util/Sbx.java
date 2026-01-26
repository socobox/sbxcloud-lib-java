package com.sbxcloud.sbx.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sbxcloud.sbx.annotation.SbxEntity;
import com.sbxcloud.sbx.jackson.SbxModule;
import com.sbxcloud.sbx.model.SBXMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Map;

/**
 * Utility class with convenient static methods for SBX operations.
 *
 * <pre>{@code
 * // Convert entity to JSON
 * String json = Sbx.toJson(entity);
 *
 * // Convert entity to Map
 * Map<String, Object> map = Sbx.toMap(entity);
 *
 * // Parse JSON to entity
 * Contact contact = Sbx.fromJson(json, Contact.class);
 *
 * // Pretty print
 * String pretty = Sbx.toPrettyJson(entity);
 * }</pre>
 */
public final class Sbx {

    private static final ObjectMapper MAPPER = createMapper();

    private Sbx() {}

    private static ObjectMapper createMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new SbxModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Returns the shared ObjectMapper instance.
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * Converts an object to JSON string.
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new SbxUtilException("Failed to convert to JSON", e);
        }
    }

    /**
     * Converts an object to pretty-printed JSON string.
     */
    public static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new SbxUtilException("Failed to convert to JSON", e);
        }
    }

    /**
     * Converts an object to a Map.
     */
    public static Map<String, Object> toMap(Object obj) {
        return MAPPER.convertValue(obj, new TypeReference<>() {});
    }

    /**
     * Parses JSON string to an object.
     */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new SbxUtilException("Failed to parse JSON", e);
        }
    }

    /**
     * Parses JSON string to an object using TypeReference (for generics).
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new SbxUtilException("Failed to parse JSON", e);
        }
    }

    /**
     * Converts a Map to an object.
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> type) {
        return MAPPER.convertValue(map, type);
    }

    /**
     * Creates a new entity instance, automatically setting key and meta to null.
     * <p>
     * Pass only the data fields (excluding key and meta) in the order they appear
     * in the record definition.
     *
     * <pre>{@code
     * // Instead of:
     * new Contact(null, null, "John", "john@example.com", "ACTIVE")
     *
     * // Use:
     * Sbx.create(Contact.class, "John", "john@example.com", "ACTIVE")
     * }</pre>
     *
     * @param type   the entity class (must be a record implementing SbxEntity)
     * @param values the field values (excluding key and meta)
     * @return new entity instance with key=null, meta=null
     */
    @SuppressWarnings("unchecked")
    public static <T extends SbxEntity> T create(Class<T> type, Object... values) {
        if (!type.isRecord()) {
            throw new SbxUtilException("Class must be a record: " + type.getName(), null);
        }

        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] paramTypes = new Class<?>[components.length];
        Object[] args = new Object[components.length];

        int valueIndex = 0;
        for (int i = 0; i < components.length; i++) {
            RecordComponent comp = components[i];
            paramTypes[i] = comp.getType();

            // Skip key and meta - set to null
            if ("key".equals(comp.getName()) || "meta".equals(comp.getName())) {
                args[i] = null;
            } else {
                if (valueIndex >= values.length) {
                    throw new SbxUtilException(
                            "Not enough values provided. Expected " + (components.length - 2) +
                                    " but got " + values.length, null);
                }
                args[i] = values[valueIndex++];
            }
        }

        if (valueIndex < values.length) {
            throw new SbxUtilException(
                    "Too many values provided. Expected " + (components.length - 2) +
                            " but got " + values.length, null);
        }

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(paramTypes);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new SbxUtilException("Failed to create entity: " + type.getName(), e);
        }
    }

    /**
     * Runtime exception for utility operations.
     */
    public static class SbxUtilException extends RuntimeException {
        public SbxUtilException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
