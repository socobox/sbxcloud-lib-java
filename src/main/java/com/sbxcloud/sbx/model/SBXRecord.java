package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base interface for SBX record types.
 * <p>
 * All SBX records have a {@code _KEY} (primary key) and optional {@code _META} (metadata).
 *
 * <h2>Usage with Java Records</h2>
 * <pre>{@code
 * @JsonIgnoreProperties(ignoreUnknown = true)
 * public record Contact(
 *     @JsonProperty("_KEY") String key,
 *     @JsonProperty("_META") SBXMeta meta,
 *     String name,
 *     String email,
 *     String status
 * ) implements SBXRecord {}
 * }</pre>
 *
 * <h2>Creating New Records</h2>
 * <pre>{@code
 * // For inserts, key and meta are null
 * var newContact = new Contact(null, null, "John", "john@example.com", "ACTIVE");
 *
 * // Or add a convenience constructor
 * public record Contact(...) implements SBXRecord {
 *     public Contact(String name, String email, String status) {
 *         this(null, null, name, email, status);
 *     }
 * }
 * }</pre>
 *
 * <h2>Accessing Metadata</h2>
 * <pre>{@code
 * Contact contact = response.results().get(0);
 * System.out.println("Key: " + contact.key());
 * if (contact.meta() != null) {
 *     System.out.println("Created: " + contact.meta().createdTime());
 *     System.out.println("Updated: " + contact.meta().updateTime());
 * }
 * }</pre>
 *
 * @see SBXMeta
 */
public interface SBXRecord {

    /**
     * The unique key for this record ({@code _KEY} in JSON).
     */
    @JsonProperty("_KEY")
    String key();

    /**
     * The metadata for this record ({@code _META} in JSON).
     * Contains createdTime, updateTime, modelId, modelName, domain.
     */
    @JsonProperty("_META")
    SBXMeta meta();
}
