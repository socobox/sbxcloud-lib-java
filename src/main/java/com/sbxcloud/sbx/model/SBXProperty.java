package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model field/property definition.
 */
public record SBXProperty(
        Integer id,
        String name,
        FieldType type,
        @JsonProperty("reference_model") String referenceModel,
        @JsonProperty("reference_model_id") Integer referenceModelId,
        @JsonProperty("default_value") String defaultValue,
        Boolean required
) {
}
