package com.sbxcloud.sbx.model;

import java.util.List;

/**
 * Model definition with its properties.
 */
public record SBXModel(
        Integer id,
        String name,
        String label,
        List<SBXProperty> properties
) {
}
