package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Represents the "where" clause in SBX queries.
 * Can be either condition-based (list of groups) or key-based.
 */
@JsonSerialize(using = WhereClause.Serializer.class)
public sealed interface WhereClause permits WhereClause.Conditions, WhereClause.Keys {

    /**
     * Condition-based where clause (list of logical groups).
     */
    record Conditions(List<LogicalGroup> groups) implements WhereClause {}

    /**
     * Key-based where clause for finding by primary keys.
     */
    record Keys(List<String> keys) implements WhereClause {}

    static WhereClause conditions(List<LogicalGroup> groups) {
        return new Conditions(groups);
    }

    static WhereClause keys(List<String> keys) {
        return new Keys(keys);
    }

    /**
     * Custom serializer to output the correct JSON format.
     */
    class Serializer extends JsonSerializer<WhereClause> {
        @Override
        public void serialize(WhereClause value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            switch (value) {
                case Conditions c -> gen.writeObject(c.groups());
                case Keys k -> gen.writeObject(Map.of("keys", k.keys()));
            }
        }
    }
}
