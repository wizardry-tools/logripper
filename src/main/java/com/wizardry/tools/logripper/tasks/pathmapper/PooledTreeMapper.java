package com.wizardry.tools.logripper.tasks.pathmapper;

import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;

public class PooledTreeMapper implements FileTreeMapper<Path,MappedTreeNode> {

    private final ForkJoinPool forkJoinPool;

    public PooledTreeMapper() {
        this.forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    public MappedTreeNode crawl(Path rootPath) {
        return forkJoinPool.invoke(new PooledTreeMapperTask(rootPath));
    }
}
