package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * SBX application configuration.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SBXConfig(
        List<SBXModel> models,
        Map<String, Object> properties
) {
}
