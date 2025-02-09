package com.wizardry.tools.logripper.tasks.pathsize;

import com.wizardry.tools.logripper.tasks.PooledRipperTask;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class PathSizeTask extends PooledRipperTask<Path,Long> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    public PathSizeTask(Path path) {
        super(path);
    }

    @Override
    protected Long compute() {
        long size = 0;
        try {
            boolean isDir = false;
            boolean isFile = false;
            try {
                isDir = Files.isDirectory(input, LinkOption.NOFOLLOW_LINKS);
                if (!isDir) {
                    // only called if not directory and readable
                    isFile = Files.isRegularFile(input, LinkOption.NOFOLLOW_LINKS) || Files.isExecutable(input);
                }
            } catch (Exception e) {
                // do nothing, can't read, can't get size
                LOGGER.info("Can't Read: " + input);
            }

            if (isDir) {
                List<PathSizeTask> subTasks = new ArrayList<>();
                Files.walkFileTree(input, EnumSet.noneOf(FileVisitOption.class), 0, new PathSizeVisitor(subTasks));

                if (!subTasks.isEmpty()) {
                    invokeAll(subTasks);  // Fork all the tasks

                    for (PathSizeTask task : subTasks) {
                        size += task.join();  // Join and sum up results
                    }
                }
            } else if (isFile) {
                size = Files.size(input);
            }
        } catch (IOException e) {
            System.err.println("Error accessing file: " + input.toAbsolutePath() + ", error: " + e.getMessage());
        }

        return size;
    }
}
