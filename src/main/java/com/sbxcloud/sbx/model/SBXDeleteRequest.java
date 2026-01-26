package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Request payload for delete operations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SBXDeleteRequest(
        @JsonProperty("row_model") String rowModel,
        String domain,
        Map<String, List<String>> where
) {
    public static SBXDeleteRequest of(String model, int domain, List<String> keys) {
        return new SBXDeleteRequest(model, String.valueOf(domain), Map.of("keys", keys));
    }

    public static SBXDeleteRequest ofSingle(String model, int domain, String key) {
        return of(model, domain, List.of(key));
    }
}
