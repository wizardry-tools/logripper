package com.wizardry.tools.logripper.tasks.pathgrep;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.tasks.PooledRipperVisitor;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PathGrepVisitor extends PooledRipperVisitor<Path, PathGrepTask> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    protected final LogRipperConfig config;
    protected final AtomicInteger matchCounter;

    public PathGrepVisitor(List<PathGrepTask> subTasks, LogRipperConfig config, AtomicInteger matchCounter) {
        super(subTasks);
        this.config = config;
        this.matchCounter = matchCounter;
        LOGGER.info("New PathGrepVisitor");
    }

    @Override
    public FileVisitResult visitFile(Path input, BasicFileAttributes attrs) throws IOException {
        // create a task for each path to grep
        PathGrepTask task = new PathGrepTask(input, config, matchCounter);
        subTasks.add(task);
        return FileVisitResult.CONTINUE;
    }
}
