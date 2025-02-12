package com.wizardry.tools.logripper.util.filesystem;

public interface Readable {

    /**
     * Tests whether the Readable is a directory.
     */
    boolean isDir();

    /**
     * Tests whether a Readable is executable.
     */
    boolean isExe();

    /**
     * Tests whether a Readable is a regular file with opaque content.
     */
    boolean isFile();

    /**
     * Tests whether a Readable is actually readable.
     */
    boolean isReadable();
}
