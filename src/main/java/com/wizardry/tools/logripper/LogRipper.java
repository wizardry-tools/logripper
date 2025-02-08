package com.wizardry.tools.logripper;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import org.apache.commons.logging.Log;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogRipper {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();
    private static final String EMPTY = "";

    private final String searchToken;
    private final String path;
    private final ExecutorService executor;
    private final boolean isIgnoreCase;
    private final AtomicInteger matchesFound;
    private final AtomicInteger matchLimit;
    private final boolean isVerbose;
    private final boolean isDebug;
    private final LogRipperConfig config;

    public LogRipper(LogRipperConfig config) {
        this.config = config;
        this.searchToken = config.searchToken() != null ? config.searchToken() : EMPTY;
        this.isIgnoreCase = config.isIgnoreCase();
        this.path = config.path();
        // Use a fixed thread pool for concurrency
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.matchesFound = new AtomicInteger(0);
        this.matchLimit = new AtomicInteger(config.matchLimit());
        this.isVerbose = config.isVerbose();
        this.isDebug = config.isDebug();
    }

    public void scanAndReport() throws IOException, InterruptedException, ExecutionException {
        Instant startRead = Instant.now();
        List<String> fileContent = readFile(path);
        Instant endRead = Instant.now();
        Duration readDuration = Duration.between(startRead, endRead);

        if (searchToken.isEmpty()) return;

        Pattern pattern = isIgnoreCase ? Pattern.compile(searchToken, Pattern.CASE_INSENSITIVE) : Pattern.compile(searchToken);

        int partSize = calculateOptimalPartSize(fileContent.size());
        List<Future<Map<Integer, String>>> futures = new ArrayList<>();

        Instant startTasks = Instant.now();
        for (int i = 0; i < fileContent.size(); i += partSize) {
            int end = Math.min(i + partSize, fileContent.size());
            Callable<Map<Integer, String>> task = new FileScannerTask(fileContent.subList(i, end), pattern, config, matchesFound, matchLimit);
            futures.add(executor.submit(task));
        }

        // Collect results from all tasks
        Map<Integer, String> matches = new TreeMap<>();
        for (Future<Map<Integer, String>> future : futures) {
            matches.putAll(future.get());
        }

        if (config.isCountOnly()) {
            reportMatches(matches);
        } else {
            LOGGER.info("Read [" + fileContent.size() + "] log lines in [" + readDuration.toMillis() + "] milliseconds");
        }

        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        Instant endTasks = Instant.now();
        Duration taskDuration = Duration.between(startTasks, endTasks);

        if (isVerbose) {
            LOGGER.info("LineChunks: " + partSize);
            LOGGER.info("Read [" + fileContent.size() + "] log lines in [" + readDuration.toMillis() + "] milliseconds");
            LOGGER.info("Spawned [" + (fileContent.size() / partSize) + "] Tasks for [" + Runtime.getRuntime().availableProcessors() + "] Threads for [" + taskDuration.toMillis() + "] milliseconds");
            LOGGER.info("Total Matches Found: " + this.matchesFound.get());
        }
    }

    public static List<String> collectPaths(String directoryPath) {
        List<String> paths = new ArrayList<>();
        try {
            Files.walk(Paths.get(directoryPath))
                    .forEach(path -> paths.add(path.toString()));
        } catch (IOException e) {
            System.err.println("Error traversing the directory: " + e.getMessage());
        }
        return paths;
    }

    private List<String> readFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return new ArrayList<>(reader.lines().toList());
        }
    }

    private void reportMatches(Map<Integer, String> matches) {
        for (Map.Entry<Integer, String> entry : matches.entrySet()) {
            int index = entry.getKey();
            System.out.println("line#["+(index+1)+"]: " + entry.getValue());
        }
    }

    private static int calculateOptimalPartSize(int fileSize) {
        // Simple heuristic
        return (fileSize / (Runtime.getRuntime().availableProcessors() * 2)) + 1;
    }

    private record FileScannerTask(List<String> lines, Pattern pattern, LogRipperConfig config, AtomicInteger counter, AtomicInteger limit)
            implements Callable<Map<Integer, String>> {

        @Override
        public Map<Integer, String> call() {
            Map<Integer, String> matches = new HashMap<>();
            for (int i = 0; i < lines.size(); i++) {
                if (limit.get() == 0 || counter.get() < limit.get() + 1) {
                    Matcher matcher = pattern.matcher(lines.get(i));
                    if (matcher.find()) {
                        recordMatch(i, matches);
                    }
                } else {
                    break;
                }
            }
            return matches;
        }

        private void recordMatch(int index, Map<Integer, String> matches) {
            counter.incrementAndGet();
            if (config.isSilent()) {
                matches.put(index, EMPTY);
                return;
            }
            StringBuilder matchDetails = new StringBuilder();
            int start = Math.max(0, index - config.linesBeforeMatch());
            int end = Math.min(lines.size() - 1, index + config.linesAfterMatch());
            for (int j = start; j <= end; j++) {
                if (j == index) {
                    matchDetails.append("> ").append(lines.get(j)).append("\n");
                } else {
                    matchDetails.append("  ").append(lines.get(j)).append("\n");
                }
            }
            matches.put(index, matchDetails.toString());
        }
    }
}

