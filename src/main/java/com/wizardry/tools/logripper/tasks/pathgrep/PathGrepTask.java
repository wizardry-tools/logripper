package com.wizardry.tools.logripper.tasks.pathgrep;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.tasks.PooledRipperTask;
import com.wizardry.tools.logripper.util.matching.Match;
import com.wizardry.tools.logripper.util.wrapping.WrappedPath;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PathGrepTask extends PooledRipperTask<WrappedPath, Map<String, ConcurrentLinkedQueue<Match>>> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    protected final LogRipperConfig config;
    protected final AtomicInteger totalMatches;

    public PathGrepTask(WrappedPath path, LogRipperConfig config, AtomicInteger totalMatches) {
        super(path);
        this.config = config;
        this.totalMatches = totalMatches;
        if(config.isDebug()) LOGGER.debug("New PathGrepTask");
    }

    @Override
    protected Map<String, ConcurrentLinkedQueue<Match>> compute() {
        Map<String, ConcurrentLinkedQueue<Match>> matches = new HashMap<>();
        try {
            if (input.isDir()) {
                List<PathGrepTask> subTasks = new ArrayList<>();
                Files.walkFileTree(input.unwrap(), new PathGrepVisitor(subTasks, config, totalMatches));

                if (!subTasks.isEmpty()) {
                    invokeAll(subTasks);  // Fork all the tasks

                    for (PathGrepTask task : subTasks) {
                        matches.putAll(task.join());  // Join and sum up results
                    }
                }
            } else if (input.isFile()) {
                // Call Grep functionality
                matches.putAll(findMatches(input));
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred while computing a Grep Task: " + e.getMessage());
        }

        return matches;
    }

    private Map<String, ConcurrentLinkedQueue<Match>> findMatches(WrappedPath input) {
        try {
            FileGrepRipper fileGrepRipper = new FileGrepRipper(config, totalMatches);
            return Map.of(input.toAbsolutePath().toString(), fileGrepRipper.rip(input));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
