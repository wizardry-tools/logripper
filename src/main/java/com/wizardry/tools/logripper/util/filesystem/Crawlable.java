package com.wizardry.tools.logripper.util.filesystem;

import java.util.List;

public interface Crawlable<T> {
    int getLevel();
    List<? extends T> getChildren();
    <K extends T>void addChild(K child);
    void addChildren(Crawlable<? extends T>... children);
    boolean removeChild(Crawlable<? extends T> child);
}
