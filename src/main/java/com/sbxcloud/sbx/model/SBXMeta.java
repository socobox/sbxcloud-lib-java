package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Metadata attached to SBX objects.
 */
public record SBXMeta(
        @JsonProperty("created_time") Instant createdTime,
        @JsonProperty("update_time") Instant updateTime,
        @JsonProperty("model_id") Integer modelId,
        @JsonProperty("model_name") String modelName,
        Integer domain
) {
}
