package com.wizardry.tools.logripper.util.filesystem;

public interface Sizable {

    default long getSize() {
        return 0L;
    }
    default String getReadableSize() {
        return "0B";
    }
}
