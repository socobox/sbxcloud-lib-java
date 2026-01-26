package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Query operations for filtering data.
 */
public enum Operation {
    LIKE("LIKE"),
    EQUAL("="),
    NOT_EQUAL("!="),
    GREATER_THAN(">"),
    GREATER_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_OR_EQUAL("<="),
    IN("IN"),
    NOT_IN("NOT IN"),
    IS("IS"),
    IS_NOT("IS NOT");

    private final String value;

    Operation(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
