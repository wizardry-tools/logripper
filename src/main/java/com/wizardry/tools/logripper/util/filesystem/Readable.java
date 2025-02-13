package com.wizardry.tools.logripper.util.filesystem;

public interface Readable {

    /**
     * Tests whether the Readable is a directory.
     */
    default boolean isDir() {
        return false;
    }

    /**
     * Tests whether a Readable is executable.
     */
    default boolean isExe() {
        return false;
    }

    /**
     * Tests whether a Readable is a regular file with opaque content.
     */
    default boolean isFile() {
        return false;
    }

    /**
     * Tests whether a Readable is actually readable.
     */
    default boolean isReadable() {
        return false;
    }
}
