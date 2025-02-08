package com.wizardry.tools.logripper.tasks;

import com.wizardry.tools.logripper.util.DataUtil;
import com.wizardry.tools.logripper.util.Timestamp;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class PathSizeCalculator {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    public long calculatePathSize(Path path, boolean isDebug) throws IOException {
        long size = 0;
        Timestamp calculationTime = new Timestamp();
        try (ForkJoinPool pool = new ForkJoinPool()) {
            FileSizeTask task = new FileSizeTask(path);
            size = pool.invoke(task);
            String humanReadableSize = DataUtil.humanReadableByteCountSI(size);
            LOGGER.info("Total size for [" + path.toAbsolutePath() + "] is [" + humanReadableSize + "]");
        } catch (Exception e) {
            throw new IOException("Error calculating file size", e);
        }
        if (isDebug) {
            LOGGER.info("Calculated file size in [" + calculationTime.toMillis() + "] milliseconds");
        }
        return size;
    }

    private static class FileSizeTask extends RecursiveTask<Long> {

        private final Path path;

        public FileSizeTask(Path path) {
            this.path = path;
        }

        @Override
        protected Long compute() {
            long size = 0;
            try {
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    List<FileSizeTask> subTasks = new ArrayList<>();
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            // Create a task for each file to calculate its size
                            FileSizeTask task = new FileSizeTask(file);
                            subTasks.add(task);
                            return FileVisitResult.CONTINUE;
                        }
                    });

                    if (!subTasks.isEmpty()) {
                        invokeAll(subTasks);  // Fork all the tasks

                        for (FileSizeTask task : subTasks) {
                            size += task.join();  // Join and sum up results
                        }
                    }
                } else if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                    size = Files.size(path);
                }
            } catch (IOException e) {
                System.err.println("Error accessing file: " + path.toAbsolutePath() + ", error: " + e.getMessage());
            }

            return size;
        }
    }
}

