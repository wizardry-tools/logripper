package com.wizardry.tools.logripper.util;

import static com.wizardry.tools.logripper.util.StringUtil.EMPTY;

public class DataUtil {

    public static final String DASH = "-";

    private static final String SIZE_FORMAT = "%s%.2f%s";

    /**
     * This is a helper method for converting byte length
     * into a Human Readable Format.
     * @param bytes long value of bytes
     * @return String
     */
    public static String humanReadableByteCountSI(long bytes) {
        String sign = bytes < 0 ? DASH : EMPTY;
        long absBytes = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absBytes < 1000L) {
            return bytes + "B";
        }

        String[] units = {"KB", "MB", "GB", "TB", "PB", "EB"};
        int unitIndex = 0;
        while (absBytes >= 999_950L && unitIndex < units.length - 1) {
            absBytes /= 1000;
            unitIndex++;
        }

        return String.format(SIZE_FORMAT, sign, absBytes / 1e3, units[unitIndex]);
    }
}
