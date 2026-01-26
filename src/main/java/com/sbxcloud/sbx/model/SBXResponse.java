package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Base response from SBX API.
 *
 * @param <T> the type of items in the response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SBXResponse<T>(
        boolean success,
        String error,
        String message,
        List<String> keys,
        T item,
        List<T> items
) {
    /**
     * Creates a successful response with no data.
     */
    public static <T> SBXResponse<T> ok() {
        return new SBXResponse<>(true, null, null, null, null, null);
    }

    /**
     * Creates a successful response with keys.
     */
    public static <T> SBXResponse<T> ok(List<String> keys) {
        return new SBXResponse<>(true, null, null, keys, null, null);
    }

    /**
     * Creates a failed response with error message.
     */
    public static <T> SBXResponse<T> failure(String error) {
        return new SBXResponse<>(false, error, null, null, null, null);
    }

    /**
     * Creates a failed response with error and message.
     */
    public static <T> SBXResponse<T> failure(String error, String message) {
        return new SBXResponse<>(false, error, message, null, null, null);
    }

    /**
     * Returns the error message, preferring 'error' field over 'message'.
     */
    public String getErrorMessage() {
        return error != null ? error : message;
    }
}
