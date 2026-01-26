package com.sbxcloud.sbx.annotation;

/**
 * Utility for working with @SbxModel annotations.
 */
public final class SbxModels {

    private SbxModels() {}

    /**
     * Extracts the model name from a class annotated with @SbxModel.
     *
     * @param type the annotated class
     * @return the model name
     * @throws IllegalArgumentException if class is not annotated with @SbxModel
     */
    public static String getModelName(Class<?> type) {
        var annotation = type.getAnnotation(SbxModel.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                "Class " + type.getName() + " is not annotated with @SbxModel"
            );
        }
        return annotation.value();
    }

    /**
     * Checks if a class is annotated with @SbxModel.
     */
    public static boolean isAnnotated(Class<?> type) {
        return type.isAnnotationPresent(SbxModel.class);
    }
}
