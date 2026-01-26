package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model field/property definition.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SBXProperty(
        Integer id,
        String name,
        FieldType type,
        @JsonProperty("reference_model") Object referenceModel,
        @JsonProperty("reference_model_id") Integer referenceModelId,
        @JsonProperty("reference_type_name") String referenceTypeName,
        @JsonProperty("default_value") String defaultValue,
        Boolean required
) {
}
