package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sbxcloud.sbx.annotation.SbxEntity;
import com.sbxcloud.sbx.annotation.SbxModel;

/**
 * Test model for inventory_history records in SBX domain 96.
 */
@SbxModel("inventory_history")
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryHistory(
        @JsonProperty("_KEY") String key,
        @JsonProperty("_META") SBXMeta meta,
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
