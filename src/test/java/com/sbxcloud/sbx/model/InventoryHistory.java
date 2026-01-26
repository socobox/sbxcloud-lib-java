package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Test model for inventory_history records in SBX domain 96.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryHistory(
        @JsonProperty("_KEY") String key,
        @JsonProperty("_META") SBXMeta meta,
        String masterlist,
        Integer week,
        Double price,
        Integer quantity
) {
    public InventoryHistory(String masterlist, Integer week, Double price, Integer quantity) {
        this(null, null, masterlist, week, price, quantity);
    }
}
