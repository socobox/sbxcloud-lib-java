package com.sbxcloud.sbx.jackson;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;

/**
 * Jackson naming strategy for SBX entities.
 * <p>
 * Automatically maps:
 * <ul>
 *   <li>{@code key} ↔ {@code _KEY}</li>
 *   <li>{@code meta} ↔ {@code _META}</li>
 * </ul>
 * Other fields are left unchanged (camelCase).
 *
 * <pre>{@code
 * @SbxModel("contact")
 * @JsonNaming(SbxNamingStrategy.class)
 * public record Contact(
 *     String key,      // maps to _KEY
 *     SBXMeta meta,    // maps to _META
 *     String name,     // stays as "name"
 *     String email     // stays as "email"
 * ) implements SbxEntity {}
 * }</pre>
 */
public class SbxNamingStrategy extends PropertyNamingStrategy {

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        return translate(defaultName);
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return translate(defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return translate(defaultName);
    }

    @Override
    public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam, String defaultName) {
        return translate(defaultName);
    }

    private String translate(String name) {
        return switch (name) {
            case "key" -> "_KEY";
            case "meta" -> "_META";
            default -> name;
        };
    }
}
