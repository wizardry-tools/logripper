package com.wizardry.tools.logripper.tasks.pathsize;

import com.wizardry.tools.logripper.tasks.PooledRipperVisitor;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class PathSizeVisitor extends PooledRipperVisitor<Path, PathSizeTask> {

    public PathSizeVisitor(List<PathSizeTask> subTasks) {
        super(subTasks);
    }
    @Override
    public FileVisitResult visitFile(Path input, BasicFileAttributes attrs) {
        // Create a task for each file to calculate its size
        PathSizeTask task = new PathSizeTask(input);
        subTasks.add(task);
        return FileVisitResult.CONTINUE;
    }
}
