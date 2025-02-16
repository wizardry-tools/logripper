package com.wizardry.tools.logripper.util;


public class LiLoList<T> extends FifoList<T> {

    public LiLoList(int maxSize) {
        super(maxSize);
    }

    @Override
    public synchronized void add(T element) {
        if (maxSize == 0) {
            // dummy List, don't add anything.
            return;
        }
        if (list.size() >= maxSize) {
            list.removeLast();
        }
        list.addFirst(element);
    }
}
