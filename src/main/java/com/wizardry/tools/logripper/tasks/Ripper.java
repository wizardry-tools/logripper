package com.wizardry.tools.logripper.tasks;

import java.io.IOException;

/**
 * A Ripper is an interface for Objects that facilitates or performs multithreaded logic against
 * other Objects capable of being processed in parallel.
 * @param <T> The Object Class that can be processed in parallel.
 * @param <U> The Response that should be returned after the processing has been completed.
 */
public interface Ripper<T,U> {

    /**
     * The primary method of a Ripper. Accepts an Object that can be processed in parallel.
     * Implement the necessary logic to do the parallel processing and return a result
     * based on what your implementation needs.
     * @param toRip
     * @return
     * @throws IOException
     */
    U rip(T toRip) throws IOException;

}
