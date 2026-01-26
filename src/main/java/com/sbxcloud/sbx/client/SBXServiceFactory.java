package com.sbxcloud.sbx.client;

import com.sbxcloud.sbx.exception.SBXException;

/**
 * Factory for creating {@link SBXService} instances.
 * <p>
 * Provides multiple configuration methods:
 * <ul>
 *   <li>{@link #withEnv()} - Configuration from environment variables</li>
 *   <li>{@link #withToken(String)} - Custom token with env-based app key and domain</li>
 *   <li>{@link #withAppKeyAndToken(String, String)} - Custom credentials with env-based domain</li>
 *   <li>{@link #withCustom(String, String, int, String)} - Full custom configuration</li>
 *   <li>{@link #multidomain(String)} - Multi-tenant support with per-request credentials</li>
 * </ul>
 *
 * <h3>Environment Variables</h3>
 * <ul>
 *   <li>{@code SBX_APP_KEY} - Application key (UUID)</li>
 *   <li>{@code SBX_TOKEN} - Bearer token</li>
 *   <li>{@code SBX_DOMAIN} - Domain ID (numeric)</li>
 *   <li>{@code SBX_BASE_URL} - Base URL (without /api suffix)</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // From environment
 * var sbx = SBXServiceFactory.withEnv();
 *
 * // Custom configuration
 * var sbx = SBXServiceFactory.withCustom(
 *     "app-key-uuid",
 *     "bearer-token",
 *     0,
 *     "https://sbxcloud.com"
 * );
 *
 * // With builder
 * var sbx = SBXServiceFactory.builder()
 *     .appKey("app-key-uuid")
 *     .token("bearer-token")
 *     .domain(0)
 *     .baseUrl("https://sbxcloud.com")
 *     .debug(true)
 *     .build();
 * }</pre>
 */
public final class SBXServiceFactory {

    private static final String ENV_APP_KEY = "SBX_APP_KEY";
    private static final String ENV_TOKEN = "SBX_TOKEN";
    private static final String ENV_DOMAIN = "SBX_DOMAIN";
    private static final String ENV_BASE_URL = "SBX_BASE_URL";

    private SBXServiceFactory() {
    }

    /**
     * Creates a service configured from environment variables.
     * <p>
     * Reads: SBX_APP_KEY, SBX_TOKEN, SBX_DOMAIN, SBX_BASE_URL
     *
     * @throws SBXException if required environment variables are missing
     */
    public static SBXService withEnv() {
        return builder()
                .appKey(requireEnv(ENV_APP_KEY))
                .token(requireEnv(ENV_TOKEN))
                .domain(Integer.parseInt(requireEnv(ENV_DOMAIN)))
                .baseUrl(requireEnv(ENV_BASE_URL))
                .build();
    }

    /**
     * Creates a service with a custom token but env-based app key and domain.
     * <p>
     * Useful for multi-session scenarios where you have different tokens.
     *
     * @param token the bearer token
     * @throws SBXException if required environment variables are missing
     */
    public static SBXService withToken(String token) {
        return builder()
                .appKey(requireEnv(ENV_APP_KEY))
                .token(token)
                .domain(Integer.parseInt(requireEnv(ENV_DOMAIN)))
                .baseUrl(requireEnv(ENV_BASE_URL))
                .build();
    }

    /**
     * Creates a service with custom app key and token but env-based domain.
     *
     * @param appKey the application key
     * @param token  the bearer token
     * @throws SBXException if required environment variables are missing
     */
    public static SBXService withAppKeyAndToken(String appKey, String token) {
        return builder()
                .appKey(appKey)
                .token(token)
                .domain(Integer.parseInt(requireEnv(ENV_DOMAIN)))
                .baseUrl(requireEnv(ENV_BASE_URL))
                .build();
    }

    /**
     * Creates a service with fully custom configuration.
     *
     * @param appKey  the application key
     * @param token   the bearer token
     * @param domain  the domain ID
     * @param baseUrl the base URL (without /api suffix)
     */
    public static SBXService withCustom(String appKey, String token, int domain, String baseUrl) {
        return builder()
                .appKey(appKey)
                .token(token)
                .domain(domain)
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Creates a service for multi-domain scenarios.
     * <p>
     * The service starts with empty credentials. Use
     * {@link SBXService#setMultidomainCredentials(int, String, String)} to set
     * credentials per tenant.
     *
     * @param baseUrl the base URL
     */
    public static SBXService multidomain(String baseUrl) {
        return builder()
                .appKey("")
                .token("")
                .domain(0)
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Creates a builder for custom configuration.
     */
    public static Builder builder() {
        return new Builder();
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new SBXException("Required environment variable not set: " + name);
        }
        return value;
    }

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /**
     * Builder for creating {@link SBXService} instances with custom configuration.
     */
    public static class Builder {
        private String appKey;
        private String token;
        private int domain;
        private String baseUrl;
        private boolean debug;

        private Builder() {
        }

        /**
         * Sets the application key.
         */
        public Builder appKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        /**
         * Sets the bearer token.
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * Sets the domain ID.
         */
        public Builder domain(int domain) {
            this.domain = domain;
            return this;
        }

        /**
         * Sets the base URL (without /api suffix).
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Enables debug logging.
         */
        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        /**
         * Configures from environment variables where values are not set.
         * <p>
         * Does not override values already set via builder methods.
         */
        public Builder withEnvDefaults() {
            if (appKey == null) appKey = getEnv(ENV_APP_KEY, null);
            if (token == null) token = getEnv(ENV_TOKEN, null);
            if (domain == 0) {
                String domainEnv = getEnv(ENV_DOMAIN, "0");
                domain = Integer.parseInt(domainEnv);
            }
            if (baseUrl == null) baseUrl = getEnv(ENV_BASE_URL, null);
            return this;
        }

        /**
         * Builds the service instance.
         *
         * @throws SBXException if required configuration is missing
         */
        public SBXService build() {
            if (appKey == null) {
                throw new SBXException("appKey is required");
            }
            if (token == null) {
                throw new SBXException("token is required");
            }
            if (baseUrl == null) {
                throw new SBXException("baseUrl is required");
            }
            return new SBXService(appKey, token, domain, baseUrl, debug);
        }
    }
}
