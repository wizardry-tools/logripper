package com.wizardry.tools.logripper.tasks;

import java.util.concurrent.RecursiveTask;

public abstract class PooledRipperTask<T, U> extends RecursiveTask<U> {
    protected final T input;

    public PooledRipperTask(T input) {
        this.input = input;
    }
}
