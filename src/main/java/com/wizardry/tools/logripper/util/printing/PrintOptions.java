package com.wizardry.tools.logripper.util.printing;

public record PrintOptions(
        String linePrefix,
        String lineSuffix,
        int indentationLevel,
        int indentationSize,
        boolean includeSize,
        boolean includeChildren
) {
    public PrintOptions(int indentationLevel, int indentationSize) {
        this(null, null, indentationLevel, indentationSize, false, false);
    }
    public PrintOptions(Builder builder) {
        this(builder.linePrefix, builder.lineSuffix, builder.indentationLevel, builder.indentationSize, builder.includeSize, builder.includeChildren);
    }

    @lombok.Builder
    public static class Builder {
        String linePrefix = null;
        String lineSuffix = null;
        int indentationLevel = 0;
        int indentationSize = 2;
        boolean includeSize = false;
        boolean includeChildren = true;
    }
}
