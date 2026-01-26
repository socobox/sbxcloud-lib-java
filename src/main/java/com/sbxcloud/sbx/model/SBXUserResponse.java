package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response from authentication operations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SBXUserResponse(
        boolean success,
        String token,
        SBXUser user,
        String error,
        String message
) {
    /**
     * Creates a failed response.
     */
    public static SBXUserResponse failure(String error) {
        return new SBXUserResponse(false, null, null, error, null);
    }

    /**
     * Creates a failed response with error and message.
     */
    public static SBXUserResponse failure(String error, String message) {
        return new SBXUserResponse(false, null, null, error, message);
    }

    /**
     * Returns the error message, preferring 'error' field over 'message'.
     */
    public String getErrorMessage() {
        return error != null ? error : message;
    }
}
