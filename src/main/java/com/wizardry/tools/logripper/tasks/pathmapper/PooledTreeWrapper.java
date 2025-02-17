package com.wizardry.tools.logripper.tasks.pathmapper;

import com.wizardry.tools.logripper.util.wrapping.WrappedPath;
import java.util.concurrent.ForkJoinPool;

public class PooledTreeWrapper implements FileTreeMapper<WrappedPath, WrappedTreeNode> {

    private final ForkJoinPool forkJoinPool;

    public PooledTreeWrapper() {
        this.forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public WrappedTreeNode crawl(WrappedPath rootPath) {
        return forkJoinPool.invoke(new PooledTreeWrapperTask(rootPath));
    }
}
