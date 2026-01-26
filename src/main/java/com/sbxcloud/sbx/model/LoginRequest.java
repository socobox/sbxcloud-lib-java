package com.sbxcloud.sbx.model;

/**
 * Request payload for login.
 */
public record LoginRequest(
        String login,
        String password
) {
    public static LoginRequest of(String login, String password) {
        return new LoginRequest(login, password);
    }
}
