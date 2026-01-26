package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sbxcloud.sbx.annotation.SbxEntity;
import com.sbxcloud.sbx.annotation.SbxModel;
import com.sbxcloud.sbx.jackson.SbxNamingStrategy;

/**
 * Test model for inventory_history records in SBX domain 96.
 * <p>
 * Clean syntax using @JsonNaming - no @JsonProperty needed!
 */
@SbxModel("inventory_history")
@JsonNaming(SbxNamingStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryHistory(
        String key,           // Auto-mapped to _KEY
        SBXMeta meta,         // Auto-mapped to _META
        String masterlist,
        Integer week,
        Double price,
        Integer quantity
) implements SbxEntity {

    /**
     * Convenience constructor for creating new entities (without key/meta).
     */
    public InventoryHistory(String masterlist, Integer week, Double price, Integer quantity) {
        this(null, null, masterlist, week, price, quantity);
    }
}
