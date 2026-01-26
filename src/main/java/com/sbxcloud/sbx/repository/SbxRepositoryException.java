package com.sbxcloud.sbx.repository;

/**
 * Exception thrown by SbxRepository operations.
 */
public class SbxRepositoryException extends RuntimeException {

    public SbxRepositoryException(String message) {
        super(message);
    }

    public SbxRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
