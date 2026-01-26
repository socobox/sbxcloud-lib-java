package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Content item in a folder (file or subfolder).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FolderContent(
        Integer id,
        String key,
        String name,
        String mimetype,
        Long size,
        @JsonProperty("owner_login") String ownerLogin,
        String updated
) {
}
