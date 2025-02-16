package com.wizardry.tools.logripper.util;

public final class StringUtil {

    public static final String EMPTY = "";

    public static boolean isEmpty(String string) {
        if (string == null) {
            return true;
        }
        return string.isEmpty();
    }
}
