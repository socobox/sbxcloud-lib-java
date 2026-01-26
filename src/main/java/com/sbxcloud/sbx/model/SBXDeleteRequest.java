package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request payload for delete operations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SBXDeleteRequest(
        @JsonProperty("row_model") String rowModel,
        List<String> keys
) {
    public static SBXDeleteRequest of(String model, List<String> keys) {
        return new SBXDeleteRequest(model, keys);
    }

    public static SBXDeleteRequest ofSingle(String model, String key) {
        return new SBXDeleteRequest(model, List.of(key));
    }
}
