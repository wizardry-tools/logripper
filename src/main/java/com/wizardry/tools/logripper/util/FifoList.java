package com.wizardry.tools.logripper.util;

import java.util.LinkedList;
import java.util.List;

public class FifoList<T> {
    protected final LinkedList<T> list;
    protected final int maxSize;

    public FifoList(int maxSize) {
        this.maxSize = maxSize;
        this.list = new LinkedList<>();
    }

    public synchronized void add(T element) {
        if (maxSize == 0) {
            // dummy List, don't add anything.
            return;
        }
        if (list.size() >= maxSize) {
            list.removeFirst();
        }
        list.addLast(element);
    }

    public synchronized int size() {
        return list.size();
    }

    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    public List<T> asList() {
        return List.copyOf(list);
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
