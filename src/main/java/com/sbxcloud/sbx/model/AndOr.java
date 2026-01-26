package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Logical operators for combining query conditions.
 */
public enum AndOr {
    AND("AND"),
    OR("OR");

    private final String value;

    AndOr(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
