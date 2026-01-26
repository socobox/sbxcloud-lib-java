package com.sbxcloud.sbx.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for SBX Cloud client.
 * <p>
 * Configurable via application.properties or application.yml:
 * <pre>
 * sbx.app-key=your-app-key
 * sbx.token=your-bearer-token
 * sbx.domain=0
 * sbx.base-url=https://sbxcloud.com
 * sbx.debug=false
 * </pre>
 */
@ConfigurationProperties(prefix = "sbx")
public class SBXProperties {

    /**
     * Application key (UUID).
     */
    private String appKey;

    /**
     * Bearer token for authentication.
     */
    private String token;

    /**
     * Domain ID.
     */
    private int domain = 0;

    /**
     * Base URL (without /api suffix).
     */
    private String baseUrl;

    /**
     * Enable debug logging.
     */
    private boolean debug = false;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getDomain() {
        return domain;
    }

    public void setDomain(int domain) {
        this.domain = domain;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
