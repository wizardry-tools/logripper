package com.wizardry.tools.logripper.tasks;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public abstract class PooledRipperVisitor<T,U> extends SimpleFileVisitor<T> {

    protected final List<U> subTasks;
    protected final boolean isDebug;

    public PooledRipperVisitor(List<U> subTasks, boolean isDebug) {
        this.subTasks = subTasks;
        this.isDebug = isDebug;
    }
    public abstract FileVisitResult visitFile(T input, BasicFileAttributes attrs) throws IOException;
}
