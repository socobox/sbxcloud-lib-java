package com.sbxcloud.sbx.exception;

/**
 * Exception thrown when SBX API operations fail.
 */
public class SBXException extends RuntimeException {

    private final String errorCode;

    public SBXException(String message) {
        super(message);
        this.errorCode = null;
    }

    public SBXException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public SBXException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SBXException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
