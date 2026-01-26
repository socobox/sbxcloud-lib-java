package com.sbxcloud.sbx.model;

import com.sbxcloud.sbx.annotation.SbxEntity;
import com.sbxcloud.sbx.annotation.SbxModel;

/**
 * Test model for inventory_history records in SBX domain 96.
 * <p>
 * Single annotation does everything - no @JsonProperty, @JsonNaming, or @JsonIgnoreProperties needed!
 */
@SbxModel("inventory_history")
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
