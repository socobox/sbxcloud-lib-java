package com.sbxcloud.sbx.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sbxcloud.sbx.jackson.SbxNamingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record as an SBX model entity.
 * <p>
 * This single annotation replaces the need for multiple Jackson annotations.
 * It automatically:
 * <ul>
 *   <li>Sets the SBX model name for queries</li>
 *   <li>Maps {@code key} to {@code _KEY} in JSON</li>
 *   <li>Maps {@code meta} to {@code _META} in JSON</li>
 *   <li>Ignores unknown JSON properties</li>
 * </ul>
 *
 * <pre>{@code
 * @SbxModel("contact")
 * public record Contact(
 *     String key,        // auto-mapped to _KEY
 *     SBXMeta meta,      // auto-mapped to _META
 *     String name,
 *     String email
 * ) implements SbxEntity {}
 * }</pre>
 *
 * <p>Compare to the verbose alternative:
 * <pre>{@code
 * @JsonNaming(SbxNamingStrategy.class)
 * @JsonIgnoreProperties(ignoreUnknown = true)
 * public record Contact(
 *     @JsonProperty("_KEY") String key,
 *     @JsonProperty("_META") SBXMeta meta,
 *     ...
 * ) {}
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonNaming(SbxNamingStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public @interface SbxModel {
    /**
     * The SBX row_model name.
     */
    String value();
}
