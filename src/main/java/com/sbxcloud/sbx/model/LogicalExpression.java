package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single query condition expression.
 */
public record LogicalExpression(
        @JsonProperty("ANDOR") AndOr andOr,
        @JsonProperty("FIELD") String field,
        @JsonProperty("OP") Operation operation,
        @JsonProperty("VAL") Object value
) {
    public static LogicalExpression and(String field, Operation op, Object value) {
        return new LogicalExpression(AndOr.AND, field, op, value);
    }

    public static LogicalExpression or(String field, Operation op, Object value) {
        return new LogicalExpression(AndOr.OR, field, op, value);
    }
}
