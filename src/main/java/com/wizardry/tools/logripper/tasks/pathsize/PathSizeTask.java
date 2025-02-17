package com.wizardry.tools.logripper.tasks.pathsize;

import com.wizardry.tools.logripper.tasks.PooledRipperTask;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PathSizeTask extends PooledRipperTask<Path,Long> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    public PathSizeTask(Path path) {
        super(path);
    }

    @Override
    protected Long compute() {
        LOGGER.info("Compute Task: " + input);
        long size = 0;
        try {
            boolean isDir = false;
            boolean isFile = false;
            try {
                isDir = isDir(input);
                if (!isDir) {
                    // only called if not directory and readable
                    isFile = isFile(input);
                }
            } catch (SecurityException e) {
                // do nothing, can't read, can't get size
                LOGGER.info("Can't Read: " + input);
            }

            if (isDir) {
                LOGGER.info("IsDir: " + input);
                List<PathSizeTask> subTasks = new ArrayList<>();
                Files.walkFileTree(input, new PathSizeVisitor(subTasks));

                if (!subTasks.isEmpty()) {
                    invokeAll(subTasks);  // Fork all the tasks

                    for (PathSizeTask task : subTasks) {
                        size += task.join();  // Join and sum up results
                    }
                }
            } else if (isFile) {
                LOGGER.info("IsFile: " + input);
                size = Files.size(input);
            }
        } catch (IOException e) {
            System.err.println("Error accessing file: " + input.toAbsolutePath() + ", error: " + e.getMessage());
        }

        return size;
    }

    private static boolean isDir(Path input) throws SecurityException {
        return Files.isDirectory(input, LinkOption.NOFOLLOW_LINKS);
    }

    private static boolean isFile(Path input) throws SecurityException {
        return Files.isRegularFile(input, LinkOption.NOFOLLOW_LINKS) || Files.isExecutable(input);
    }
}
