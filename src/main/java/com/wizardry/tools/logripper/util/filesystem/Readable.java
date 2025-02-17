package com.wizardry.tools.logripper.util.filesystem;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public interface Readable<T,R> {

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

    Stream<String> readLines() throws IOException;

    ConcurrentLinkedQueue<R> readLines(T lineReader) throws IOException;
}
