package com.sbxcloud.sbx.model;

import com.sbxcloud.sbx.annotation.SbxEntity;
import com.sbxcloud.sbx.annotation.SbxModel;

/**
 * Test model for inventory_history records in SBX domain 96.
 * <p>
 * No convenience constructor needed - use Sbx.create() instead:
 * <pre>{@code
 * var entity = Sbx.create(InventoryHistory.class, "masterlist", 20250126, 1.99, 100);
 * }</pre>
 */
@SbxModel("inventory_history")
public record InventoryHistory(
        String key,
        SBXMeta meta,
        String masterlist,
        Integer week,
        Double price,
        Integer quantity
) implements SbxEntity {}
