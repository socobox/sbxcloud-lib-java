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
        List<Map<String, Object>> rows
) {
    public static SBXUpsertRequest of(String model, List<Map<String, Object>> rows) {
        return new SBXUpsertRequest(model, rows);
    }

    public static SBXUpsertRequest ofSingle(String model, Map<String, Object> row) {
        return new SBXUpsertRequest(model, List.of(row));
    }
}
