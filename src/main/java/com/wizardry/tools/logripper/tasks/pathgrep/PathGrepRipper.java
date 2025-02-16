package com.wizardry.tools.logripper.tasks.pathgrep;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.tasks.PathRipper;
import com.wizardry.tools.logripper.util.Timestamp;
import com.wizardry.tools.logripper.util.matching.Match;
import com.wizardry.tools.logripper.util.wrapping.WrappedPath;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PathGrepRipper implements PathRipper<ConcurrentLinkedQueue<Match>> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final LogRipperConfig config;
    private final AtomicInteger totalMatches;
    private final boolean isCountOnly;
    private final boolean isDebug;
    private final boolean isSilent;
    private final boolean initialized;

    public PathGrepRipper(LogRipperConfig config, AtomicInteger totalMatches) {
        this.config = config;
        this.isCountOnly = config.isCountOnly();
        this.isDebug = config.isDebug();
        this.isSilent = config.isSilent();
        this.totalMatches = totalMatches;
        if (isDebug) LOGGER.debug("New PathGrepRipper");

        this.initialized = true;
    }

    @Override
    public Map<String, ConcurrentLinkedQueue<Match>> rip(WrappedPath path) throws IOException {
        if (!initialized) {
            throw new IllegalStateException("PathGrepRipper isn't initialized");
        }

        Map<String, ConcurrentLinkedQueue<Match>> pathMatches = new HashMap<>();

        Timestamp calculationTime = new Timestamp();
        try (ForkJoinPool pool = new ForkJoinPool()) {
            PathGrepTask task = new PathGrepTask(path, config, totalMatches);
            pathMatches = pool.invoke(task);
            // call reporting
            reportMatches(pathMatches);
        } catch (Exception e) {
            throw new IOException("Error grepping folder", e);
        }
        if(isDebug) LOGGER.debug("Grepped folder in [" + calculationTime.toMillis() + "] milliseconds");
        return pathMatches;
    }

    private void reportMatches(Map<String, ConcurrentLinkedQueue<Match>> matches) {
        if (!isCountOnly) {
            announceMatches(isSilent, isDebug).accept(matches);
        }
        LOGGER.info("Total matches found: "+totalMatches.get());
    }

    private static Consumer<Map<String,ConcurrentLinkedQueue<Match>>> announceMatches(boolean isSilent, boolean isDebug) {
        if (isDebug && !isSilent) {
            return (matches) -> matches.forEach((key, entry) -> {
                LOGGER.notice("Matches for: " + key);
                entry.forEach(match -> match.print(LOGGER));
            });
        }
        if (!isSilent) {
            return (matches) -> matches.forEach((key, entry) -> {
                LOGGER.notice("Matches for: " + key);
                entry.forEach(match -> match.print(System.out));
            });
        }
        return (matches) -> {};
    }
}
