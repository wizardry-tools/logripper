package com.wizardry.tools.logripper.tasks.pathgrep;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.tasks.PooledRipper;
import com.wizardry.tools.logripper.util.SystemUtil;
import com.wizardry.tools.logripper.util.matching.Match;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wizardry.tools.logripper.util.StringUtil.EMPTY;

public class FileGrepRipperTwo implements PooledRipper<Path, Map<Integer, String>> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final LogRipperConfig config;
    private final ExecutorService executor;
    private final int numThreads;
    private final AtomicInteger matchCounter;
    private final int limit;
    private final Pattern pattern;
    private final boolean isDebug;
    private final boolean isVerbose;
    private final boolean isSilent;
    private final ConcurrentLinkedQueue<String> messages;
    private final boolean isInitialized;

    public FileGrepRipperTwo(LogRipperConfig config, AtomicInteger matchCounter) {
        LOGGER.info("New FileGrepRipperTwo");
        this.config = config;
        //this.numThreads = Runtime.getRuntime().availableProcessors();
        this.numThreads = 4;
        this.executor = Executors.newFixedThreadPool(numThreads);
        this.matchCounter = matchCounter;
        this.limit = config.matchLimit();
        this.pattern = config.getTokenPattern();
        this.isDebug = config.isDebug();
        this.isVerbose = config.isVerbose();
        this.isSilent = config.isSilent();
        this.messages = new ConcurrentLinkedQueue<>();

        this.isInitialized = true;
    }

    @Override
    public Map<Integer, String> rip(Path path) throws IOException {
        if (!isInitialized) {
            throw new IllegalStateException("FileGrepRipperTwo not initialized.");
        }
        if (isDebug) LOGGER.debug("Ripping File: " + path.toAbsolutePath());
        //Map<Integer, String> matches = strategyOne(path);
        //Map<Integer, String> matches = strategyTwo(path);
        ConcurrentLinkedQueue<Match> matches = strategyThree(path);

        for (String message : messages) {
            LOGGER.debug(message);
        }

        matches.forEach(match -> match.print(System.out));

        //reportMatches(matches);

        LOGGER.debug("File Ripped and found ["+matches.size()+"] matches");

        return Map.of();
        //return matches;
    }

    private Map<Integer, String> strategyOne(Path path) throws IOException {
        List<Future<Map<Integer, String>>> futures = new ArrayList<>();
        AtomicInteger lineNumber = new AtomicInteger(1);
        try (RandomAccessFile aFile = new RandomAccessFile(path.toFile(), "r")) {
            FileChannel inChannel = aFile.getChannel();
            long fileSize = inChannel.size();
            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                futures.add(executor.submit(() -> {
                    Map<Integer, String> matches = new TreeMap<>();
                    try {
                        long start = fileSize * threadId / numThreads;
                        long end = fileSize * (threadId + 1) / numThreads;

                        // Adjust start position to the beginning of a line
                        if (start != 0) {
                            inChannel.position(start - 1);
                            ByteBuffer buffer = ByteBuffer.allocate(1);
                            inChannel.read(buffer);
                            buffer.flip();
                            char c = (char) buffer.get();
                            if (c != '\n') {
                                // Move start to the beginning of the next line
                                while (inChannel.position() < end && inChannel.read(buffer) != -1) {
                                    buffer.flip();
                                    c = (char) buffer.get();
                                    buffer.clear();
                                    if (c == '\n') break;
                                }
                            }
                        }

                        ByteBuffer buffer = ByteBuffer.allocate(4096);
                        StringBuilder lineBuilder = new StringBuilder();
                        while (inChannel.position() < end && inChannel.read(buffer) != -1) {
                            buffer.flip();
                            for (int j = 0; j < buffer.limit(); j++) {
                                char c = (char) buffer.get(j);
                                if (c == '\n') {
                                    // Process the complete line
                                    processLine(lineNumber.getAndIncrement(), lineBuilder.toString(), matches);
                                    lineBuilder.setLength(0); // Clear the StringBuilder
                                } else {
                                    lineBuilder.append(c);
                                }
                            }
                            buffer.clear();
                        }

                        // Handle any remaining partial line at the end of the chunk
                        if (!lineBuilder.isEmpty() && inChannel.position() >= end) {
                            processLine(lineNumber.getAndIncrement(), lineBuilder.toString(), matches);
                        }

                        if (config.isDebug()) LOGGER.debug("File Portion Ripped and found ["+matches.size()+"] matches");
                        return matches;
                    } catch (Exception e) {
                        LOGGER.error("Error occurred while searching file: ", e);
                    }
                    return matches;
                }));
            }

            // Collect results from all tasks
            Map<Integer, String> allMatches = new TreeMap<>();
            for (Future<Map<Integer, String>> future : futures) {
                allMatches.putAll(future.get());
            }
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }

            // TODO: Fix this, it doesn't actually split the file into parts. Full files are being processed multiple times...


            return allMatches;
        } catch (FileNotFoundException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<Integer, String> strategyTwo(Path path) {
        List<Future<Map<Integer, String>>> futures = new ArrayList<>();
        AtomicInteger lineNumber = new AtomicInteger(1);
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toAbsolutePath().toString()))) {
            // Assuming the file can be divided into chunks
            String line;
            while (true) {
                try {
                    if ((line = reader.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String finalLine = line;
                futures.add(executor.submit(() -> {
                    Map<Integer, String> matches = new TreeMap<>();
                    processLine(lineNumber.getAndIncrement(), finalLine, matches);
                    return matches;
                }));
            }
            Map<Integer, String> allMatches = new TreeMap<>();
            for (Future<Map<Integer, String>> future : futures) {
                allMatches.putAll(future.get());
            }

            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            return allMatches;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int hash(String s) {
        return s.hashCode();
    }

    private synchronized void processLine(int lineNumber, String line, Map<Integer, String> matches) {
        if (limit == 0 || matchCounter.get() < limit + 1) {
            Matcher matcher = pattern.matcher(line);
            if (isVerbose && isDebug) {
                messages.add("checking @ ["+ lineNumber+"] : " +line);
            }
            if (matcher.find()) {
                if (isDebug) {
                    messages.add("Match Found @ lineNumber: " + lineNumber);
                }
                matchCounter.incrementAndGet();
                if (isSilent) {
                    matches.put(lineNumber, EMPTY);
                    return;
                }
                matches.put(lineNumber, line);
            }
        }
    }

    private void reportMatches(Map<Integer, String> matches) {
        if (config.isCountOnly() || (isSilent && !config.isNumbered())) {
            return; // don't announce matches
        }
        if (config.isNumbered()) {
            announceNumberedMatches(isDebug).accept(matches);
        } else {
            announceMatches(isDebug).accept(matches);
        }
    }

    private static Consumer<Map<Integer, String>> announceMatches(boolean isDebug) {
        if (isDebug) {
            return (matches) -> matches.values().forEach(LOGGER::debug);
        }
        return (matches) -> matches.values().forEach(System.out::println);
    }

    private static Consumer<Map<Integer, String>> announceNumberedMatches(boolean isDebug) {
        if (isDebug) {
            return (matches) -> matches.forEach((key, value) -> LOGGER.debug("#" + (key + 1) + ": " + value));
        }
        return (matches) -> matches.forEach((key, value) -> System.out.println("#" + (key + 1) + ": " + value));
    }

    private ConcurrentLinkedQueue<Match> strategyThree(Path path) {
        LOGGER.info("Randomly Reading Strategy Three");
        return SystemUtil.randomRead(path, config);
    }
}
