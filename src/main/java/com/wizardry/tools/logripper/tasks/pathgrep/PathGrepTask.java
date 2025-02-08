package com.wizardry.tools.logripper.tasks.pathgrep;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.tasks.PooledRipperTask;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PathGrepTask extends PooledRipperTask<Path, Map<String, Map<Integer,String>>> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    protected final LogRipperConfig config;
    protected final AtomicInteger matchCounter;

    public PathGrepTask(Path path, LogRipperConfig config, AtomicInteger matchCounter, boolean isDebug) {
        super(path, isDebug);
        this.config = config;
        this.matchCounter = matchCounter;
        LOGGER.info("New PathGrepTask");
    }

    @Override
    protected Map<String, Map<Integer,String>> compute() {
        Map<String, Map<Integer,String>> matches = new HashMap<>();
        try {
            if (Files.isDirectory(input, LinkOption.NOFOLLOW_LINKS)) {
                List<PathGrepTask> subTasks = new ArrayList<>();
                Files.walkFileTree(input, new PathGrepVisitor(subTasks, config, matchCounter, isDebug));

                if (!subTasks.isEmpty()) {
                    invokeAll(subTasks);  // Fork all the tasks

                    for (PathGrepTask task : subTasks) {
                        matches.putAll(task.join());  // Join and sum up results
                    }
                }
            } else if (Files.isRegularFile(input, LinkOption.NOFOLLOW_LINKS)) {
                // Call Grep functionality
                matches.putAll(findMatches(input));
            }
        } catch (IOException e) {
            System.err.println("Error accessing file: " + input.toAbsolutePath() + ", error: " + e.getMessage());
        }

        return matches;
    }

    private Map<String, Map<Integer,String>> findMatches(Path input) {
        try {
            FileGrepRipper fileGrepRipper = new FileGrepRipper(config, matchCounter);
            return Map.of(input.toAbsolutePath().toString(), fileGrepRipper.rip(input, isDebug));
        } catch (IOException e) {
            System.err.println("Error reading file: " + input.toAbsolutePath() + ", error: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new HashMap<>();
    }

}
