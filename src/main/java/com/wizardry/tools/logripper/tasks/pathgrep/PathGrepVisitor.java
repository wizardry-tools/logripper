package com.wizardry.tools.logripper.tasks.pathgrep;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.tasks.PooledRipperVisitor;
import com.wizardry.tools.logripper.util.wrapping.WrappedPath;
import org.jetbrains.annotations.NotNull;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PathGrepVisitor extends PooledRipperVisitor<Path, PathGrepTask> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    protected final LogRipperConfig config;
    protected final AtomicInteger totalMatches;

    public PathGrepVisitor(List<PathGrepTask> subTasks, LogRipperConfig config, AtomicInteger totalMatches) {
        super(subTasks);
        this.config = config;
        this.totalMatches = totalMatches;
        if (config.isDebug()) LOGGER.debug("New PathGrepVisitor");
    }

    @Override
    public @NotNull FileVisitResult visitFile(Path input, BasicFileAttributes attrs) {
        if(config.isDebug()) LOGGER.debug("visiting file @ "+input.toAbsolutePath());
        // create a task for each path to grep
        PathGrepTask task = new PathGrepTask(WrappedPath.of(input), config, totalMatches);
        subTasks.add(task);
        return FileVisitResult.CONTINUE;
    }
}
