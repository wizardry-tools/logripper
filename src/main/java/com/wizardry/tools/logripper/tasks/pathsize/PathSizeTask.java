package com.wizardry.tools.logripper.tasks.pathsize;

import com.wizardry.tools.logripper.tasks.PooledRipperTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PathSizeTask extends PooledRipperTask<Path,Long> {

    public PathSizeTask(Path path, boolean isDebug) {
        super(path, isDebug);
    }

    @Override
    protected Long compute() {
        long size = 0;
        try {
            if (Files.isDirectory(input, LinkOption.NOFOLLOW_LINKS)) {
                List<PathSizeTask> subTasks = new ArrayList<>();
                Files.walkFileTree(input, new PathSizeVisitor(subTasks, isDebug));

                if (!subTasks.isEmpty()) {
                    invokeAll(subTasks);  // Fork all the tasks

                    for (PathSizeTask task : subTasks) {
                        size += task.join();  // Join and sum up results
                    }
                }
            } else if (Files.isRegularFile(input, LinkOption.NOFOLLOW_LINKS)) {
                size = Files.size(input);
            }
        } catch (IOException e) {
            System.err.println("Error accessing file: " + input.toAbsolutePath() + ", error: " + e.getMessage());
        }

        return size;
    }
}
