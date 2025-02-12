package com.wizardry.tools.logripper.util.printing;

public record PrintOptions(int indentationLevel, String linePrefix, String lineSuffix, boolean includeSize, boolean includeChildren) {
}
