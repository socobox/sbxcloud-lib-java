package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Response from find operations with pagination and related data.
 *
 * @param <T> the type of result items
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SBXFindResponse<T>(
        boolean success,
        String error,
        String message,
        @JsonProperty("total_pages") Integer totalPages,
        @JsonProperty("row_count") Integer rowCount,
        List<T> results,
        @JsonProperty("fetched_results") Map<String, Map<String, Object>> fetchedResults,
        List<SBXProperty> model
) {
    /**
     * Creates a failed response.
     */
    public static <T> SBXFindResponse<T> failure(String error) {
        return new SBXFindResponse<>(false, error, null, null, null, null, null, null);
    }

    /**
     * Creates a failed response with error and message.
     */
    public static <T> SBXFindResponse<T> failure(String error, String message) {
        return new SBXFindResponse<>(false, error, message, null, null, null, null, null);
    }

    /**
     * Returns the error message, preferring 'error' field over 'message'.
     */
    public String getErrorMessage() {
        return error != null ? error : message;
    }

    /**
     * Returns true if there are more pages.
     */
    public boolean hasMorePages(int currentPage) {
        return totalPages != null && currentPage < totalPages;
    }
}
