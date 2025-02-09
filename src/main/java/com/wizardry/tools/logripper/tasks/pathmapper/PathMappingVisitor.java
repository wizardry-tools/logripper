package com.wizardry.tools.logripper.tasks.pathmapper;

import com.wizardry.tools.logripper.tasks.PooledRipperVisitor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class PathMappingVisitor extends PooledRipperVisitor<Path, PathMappingTask> {

    private final long maxDepth;

    public PathMappingVisitor(List<PathMappingTask> subTasks, long maxDepth ) {
        super(subTasks);
        this.maxDepth = maxDepth;
    }
    @Override
    public FileVisitResult visitFile(Path input, BasicFileAttributes attrs) throws IOException {
        // Create a task for each file to calculate its size
        PathMappingTask task = new PathMappingTask(input, maxDepth);
        subTasks.add(task);
        return FileVisitResult.CONTINUE;
    }
}
