package com.wizardry.tools.logripper.tasks.pathgrep;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.tasks.PooledRipper;
import com.wizardry.tools.logripper.util.Timestamp;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PathGrepRipper implements PooledRipper<Path,Map<String, Map<Integer,String>>> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final LogRipperConfig config;
    private final AtomicInteger matchCounter;

    public PathGrepRipper(LogRipperConfig config) {
        this.config = config;
        this.matchCounter = new AtomicInteger(0);
        LOGGER.info("New PathGrepRipper");
    }

    @Override
    public Map<String, Map<Integer, String>> rip(Path path, boolean isDebug) throws IOException {
        Map<String, Map<Integer, String>> pathMatches = new HashMap<>();

        Timestamp calculationTime = new Timestamp();
        try (ForkJoinPool pool = new ForkJoinPool()) {
            PathGrepTask task = new PathGrepTask(path, config, matchCounter, isDebug);
            pathMatches = pool.invoke(task);
            // call reporting
            if (!config.isCountOnly()) {
                reportMatches(pathMatches);
            } else {
                System.out.println(matchCounter.get());
            }
        } catch (Exception e) {
            throw new IOException("Error calculating file size", e);
        }
        if (isDebug) {
            LOGGER.info("Calculated file size in [" + calculationTime.toMillis() + "] milliseconds");
        }
        return pathMatches;
    }

    private void reportMatches(Map<String, Map<Integer, String>> matches) {
        if (config.isNumbered()) {
            announceNumberedMatches().accept(matches);
        } else {
            announceMatches().accept(matches);
        }
    }

    private static Consumer<Map<String, Map<Integer, String>>> announceMatches() {
        return (pathMatches) -> {
            pathMatches.forEach((path, matches) -> {
                System.out.println("["+path+"]");
                matches.forEach((key, value) -> {
                    matches.values().forEach(System.out::println);
                });
            });
        };
    }

    private static Consumer<Map<String, Map<Integer, String>>> announceNumberedMatches() {
        return (pathMatches) -> {
            pathMatches.forEach((path, matches) -> {
                System.out.println("["+path+"]");
                matches.forEach((key, value) -> {
                    System.out.println("#" + (key + 1) + ": " + value);
                });
            });
        };
    }
}
