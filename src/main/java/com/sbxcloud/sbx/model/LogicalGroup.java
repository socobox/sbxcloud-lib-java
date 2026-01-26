package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of logical expressions combined with AND/OR.
 */
public record LogicalGroup(
        @JsonProperty("ANDOR") AndOr andOr,
        @JsonProperty("GROUP") List<LogicalExpression> group
) {
    public static LogicalGroup and() {
        return new LogicalGroup(AndOr.AND, new ArrayList<>());
    }

    public static LogicalGroup or() {
        return new LogicalGroup(AndOr.OR, new ArrayList<>());
    }

    public LogicalGroup addExpression(LogicalExpression expression) {
        group.add(expression);
        return this;
    }
}
