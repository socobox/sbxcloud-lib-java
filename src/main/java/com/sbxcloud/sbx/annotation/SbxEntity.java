package com.sbxcloud.sbx.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sbxcloud.sbx.model.SBXMeta;

/**
 * Interface for SBX entities with standard key and meta fields.
 * <p>
 * Records implementing this interface automatically get the standard SBX fields.
 * Use with @SbxModel for full annotation support.
 *
 * <pre>{@code
 * @SbxModel("inventory_history")
 * @JsonIgnoreProperties(ignoreUnknown = true)
 * public record InventoryHistory(
 *         @JsonProperty("_KEY") String key,
 *         @JsonProperty("_META") SBXMeta meta,
 *         String masterlist,
 *         Integer week
 * ) implements SbxEntity {}
 * }</pre>
 */
public interface SbxEntity {

    /**
     * Returns the SBX primary key (_KEY).
     */
    @JsonProperty("_KEY")
    String key();

    /**
     * Returns the SBX metadata (_META).
     */
    @JsonProperty("_META")
    SBXMeta meta();
}
