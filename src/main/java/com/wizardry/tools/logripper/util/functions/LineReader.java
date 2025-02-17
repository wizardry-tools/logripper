package com.wizardry.tools.logripper.util.functions;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.util.FifoList;
import com.wizardry.tools.logripper.util.LiLoList;
import com.wizardry.tools.logripper.util.matching.Match;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class LineReader implements TriFunction<String,Integer,ConcurrentLinkedQueue<Match>,Boolean> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final Pattern pattern;
    private final int limit;
    private final boolean numbered;
    private final AtomicInteger totalMatches;
    private final boolean initialized;
    private final int linesBefore;
    private final int linesAfter;
    private final FifoList<String> beforeLines;
    private final LiLoList<String> afterLines;
    private int afterLineCount = 0;
    private Match lastMatch;

    private LineReader(LogRipperConfig config, AtomicInteger totalMatches) {
        this.pattern = config.getTokenPattern();
        this.limit = config.matchLimit();
        this.numbered = config.isNumbered();
        this.totalMatches = totalMatches;
        this.linesBefore = config.linesBeforeMatch();
        this.linesAfter = config.linesAfterMatch();
        this.beforeLines = new FifoList<>(linesBefore);
        this.afterLines = new LiLoList<>(linesAfter);
        if (config.isDebug()) LOGGER.debug("New LineReader");
        this.initialized = true;
    }

    public static LineReader of(LogRipperConfig config, AtomicInteger totalMatches) {
        if (config == null || totalMatches == null) {
            throw new IllegalArgumentException("Cannot have null parameters.");
        }
        return new LineReader(config, totalMatches);
    }

    public synchronized boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public synchronized Boolean apply(String line, Integer lineNumber, ConcurrentLinkedQueue<Match> matches) {
        if (0 < afterLineCount) {
            // if above zero, we should be capturing lines after the last match
            afterLines.add(line);
            afterLineCount--;
        }
        if (limit < 1 || totalMatches.get() < limit + 1) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                // TODO: figure out how to accurately add lines after...
                // I'm not even sure if this is safe.`
                if (lastMatch != null && !afterLines.isEmpty()) {
                    lastMatch.addAllAfter(afterLines.asList());
                }
                lastMatch = Match.of(line, lineNumber, numbered);
                lastMatch.addAllBefore(beforeLines.asList());
                matches.add(lastMatch);
                totalMatches.incrementAndGet();
                afterLineCount = linesAfter;
                return true;
            }

        }
        beforeLines.add(line);
        return false;
    }
}
