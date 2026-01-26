package com.sbxcloud.sbx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an SBX model and specifies its row_model name.
 *
 * <pre>{@code
 * @SbxModel("inventory_history")
 * public record InventoryHistory(
 *     @JsonProperty("_KEY") String key,
 *     @JsonProperty("_META") SBXMeta meta,
 *     String masterlist,
 *     Integer week,
 *     Double price,
 *     Integer quantity
 * ) {}
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SbxModel {
    /**
     * The SBX row_model name.
     */
    String value();
}
