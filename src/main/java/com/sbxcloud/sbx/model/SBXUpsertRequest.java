package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Request payload for create/update operations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SBXUpsertRequest(
        @JsonProperty("row_model") String rowModel,
        String domain,
        List<Map<String, Object>> rows
) {
    public static SBXUpsertRequest of(String model, int domain, List<Map<String, Object>> rows) {
        return new SBXUpsertRequest(model, String.valueOf(domain), rows);
    }

    public static SBXUpsertRequest ofSingle(String model, int domain, Map<String, Object> row) {
        return of(model, domain, List.of(row));
    }
}
