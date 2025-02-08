package com.wizardry.tools.logripper.tasks;

import java.util.concurrent.RecursiveTask;

public abstract class PooledRipperTask<T, U> extends RecursiveTask<U> {
    protected final T input;
    protected final boolean isDebug;

    public PooledRipperTask(T input, boolean isDebug) {
        this.input = input;
        this.isDebug = isDebug;
    }
}
