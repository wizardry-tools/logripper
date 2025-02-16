package com.wizardry.tools.logripper.util.filesystem;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface Crawlable<T extends Crawlable<T>> {

    /**
     * Returns the Crawlable item's current level.
     * @return
     */
    default int getLevel() {
        return 0;
    }

    /**
     * Returns a list of Crawlable child items.
     * @return
     */
    default ConcurrentLinkedQueue<T> getChildren() {
        return new ConcurrentLinkedQueue<>();
    }

    /**
     * Adds a child item that is Crawlable.
     * @param child
     */
    void addChild(T child);

    /**
     * Add an array of child items that are Crwalable.
     * @param children
     */
    default void addChildren(T... children) {
        Arrays.stream(children).forEach(this::addChild);
    }

    /**
     * Removes a child from the current Crawlable.
     * @param child
     * @return
     */
    boolean removeChild(T child);
}
