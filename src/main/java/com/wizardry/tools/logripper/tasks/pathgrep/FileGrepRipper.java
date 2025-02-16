package com.wizardry.tools.logripper.tasks.pathgrep;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.tasks.Ripper;
import com.wizardry.tools.logripper.util.Timestamp;
import com.wizardry.tools.logripper.util.functions.LineReader;
import com.wizardry.tools.logripper.util.matching.Match;
import com.wizardry.tools.logripper.util.wrapping.WrappedPath;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class FileGrepRipper implements Ripper<WrappedPath, ConcurrentLinkedQueue<Match>> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final LogRipperConfig config;
    private final boolean isDebug;
    private final boolean isVerbose;
    private final boolean isSilent;
    private final ConcurrentLinkedQueue<String> messages;
    private final ConcurrentLinkedQueue<String> debugMessages;

    private final AtomicInteger totalMatches;
    private final AtomicInteger matchesInThisFile;
    private final boolean initialized;
    private final ReentrantLock lock = new ReentrantLock();

    public FileGrepRipper(LogRipperConfig config, AtomicInteger totalMatches) {
        this.config = config;
        this.isDebug = config.isDebug();
        this.isVerbose = config.isVerbose();
        this.isSilent = config.isSilent();
        this.messages = new ConcurrentLinkedQueue<>();
        this.debugMessages = new ConcurrentLinkedQueue<>();
        this.totalMatches = totalMatches;
        this.matchesInThisFile = new AtomicInteger(totalMatches.get());

        if (isDebug) LOGGER.debug("New FileGrepRipper");
        this.initialized = true;
    }

    @Override
    public ConcurrentLinkedQueue<Match> rip(WrappedPath path) {
        if (!initialized) {
            throw new IllegalStateException("FileGrepRipperTwo not initialized.");
        }
        lock.lock();
        ConcurrentLinkedQueue<Match> matches = new ConcurrentLinkedQueue<>();
        try {
            if (isDebug) LOGGER.debug("Ripping File: " + path.toAbsolutePath());

            runAndReport((it, itMatches) -> {
                LineReader lineReader = LineReader.of(config, matchesInThisFile);
                try {
                    itMatches.addAll(it.readLines(lineReader));
                } catch (Exception e) {
                    LOGGER.error("Error occurred while reading path: "+e);
                }
            }, path, matches);
        } finally {
            lock.unlock();
        }
        return matches;
    }

    private synchronized void runAndReport(BiConsumer<WrappedPath,ConcurrentLinkedQueue<Match>> consumer, WrappedPath path, ConcurrentLinkedQueue<Match> matches)  {
        Timestamp startTime = Timestamp.now();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        if(isDebug && isVerbose) {
            debugMessages.add("Initial memory: %.2f GB".formatted((double)memoryMXBean.getHeapMemoryUsage().getInit() /1073741824));
            debugMessages.add("Max memory: %.2f GB".formatted((double)memoryMXBean.getHeapMemoryUsage().getMax() /1073741824));
            debugMessages.add("Heap Memory before rip: %.2f GB".formatted((double)memoryMXBean.getHeapMemoryUsage().getUsed() /1073741824));
            debugMessages.add("Before Committed memory: %.2f GB".formatted((double)memoryMXBean.getHeapMemoryUsage().getCommitted() /1073741824));
        }
        try {
            consumer.accept(path, matches);
        } catch (Exception e) {
            messages.add("Exception running and reporting: " + e);
        }

        if(isDebug) {
            debugMessages.add("Finished running in [" + startTime.toMillis() + "] milliseconds.");
            if(isVerbose) {
                debugMessages.add("After Used memory: %.2f GB".formatted((double) memoryMXBean.getHeapMemoryUsage().getUsed() / 1073741824));
                debugMessages.add("After Committed memory: %.2f GB".formatted((double) memoryMXBean.getHeapMemoryUsage().getCommitted() / 1073741824));
            }
            debugMessages.add("------------------------------------------------------------------------");
        }

        reportMatches(matches);

        for (String message : messages) {
            LOGGER.info(message);
        }

        for (String message : debugMessages) {
            LOGGER.debug(message);
        }
        LOGGER.info("[" + matchesInThisFile.get() + "] matches in " + path);
        totalMatches.addAndGet(matchesInThisFile.get());
    }

    private void reportMatches(ConcurrentLinkedQueue<Match> matches) {
        if (config.isCountOnly() || (isSilent && !config.isNumbered())) {
            return; // don't announce matches
        }
        announceMatches(isDebug).accept(matches);
    }

    private static Consumer<ConcurrentLinkedQueue<Match>> announceMatches(boolean isDebug) {
        if (isDebug) {
            return (matches) -> matches.forEach(match -> match.print(LOGGER));
        }
        return (matches) -> matches.forEach(match -> match.print(System.out));
    }
}
