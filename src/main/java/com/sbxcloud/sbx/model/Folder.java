package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Folder information.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Folder(
        String key,
        String name,
        @JsonProperty("parent_key") String parentKey,
        String path,
        @JsonProperty("key_path") String keyPath,
        @JsonProperty("row_key_id") String rowKeyId,
        @JsonProperty("row_model") String rowModel
) {
}
