package com.wizardry.tools.logripper.util.filesystem;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface Mappable<T extends Mappable<T>> {

    /**
     * Returns the Mappable item's current level.
     * @return
     */
    default int getLevel() {
        return 0;
    }

    /**
     * Returns a list of Mappable child items.
     * @return
     */
    default ConcurrentLinkedQueue<T> getChildren() {
        return new ConcurrentLinkedQueue<>();
    }

    /**
     * Adds a child item that is Mappable.
     * @param child
     */
    void addChild(T child);

    /**
     * Add an array of child items that are Mappable.
     * @param children
     */
    default void addChildren(T... children) {
        Arrays.stream(children).forEach(this::addChild);
    }

    /**
     * Removes a child from the current Mappable.
     * @param child
     * @return
     */
    boolean removeChild(T child);
}
