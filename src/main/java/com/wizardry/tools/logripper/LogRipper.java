package com.wizardry.tools.logripper;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogRipper {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final String searchToken;
    private final String path;
    private final ExecutorService executor;
    private final int linesBefore;
    private final int linesAfter;
    private final boolean isIgnoreCase;
    private final AtomicInteger matchesFound;

    public LogRipper(LogRipperConfig config) {
        this.searchToken = config.searchToken() != null ? config.searchToken() : "";
        this.isIgnoreCase = config.isIgnoreCase();
        this.path = config.path();
        this.linesBefore = Math.max(config.linesBeforeMatch(), 0);
        this.linesAfter = Math.max(config.linesAfterMatch(), 0);
        // Use a fixed thread pool for concurrency
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.matchesFound = new AtomicInteger(0);
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
            Callable<Map<Integer, String>> task = new FileScannerTask(fileContent.subList(i, end), pattern, linesBefore, linesAfter, matchesFound);
            futures.add(executor.submit(task));
        }

        // Collect results from all tasks
        Map<Integer, String> matches = new TreeMap<>();
        for (Future<Map<Integer, String>> future : futures) {
            matches.putAll(future.get());
        }

        reportMatches(matches);

        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        Instant endTasks = Instant.now();
        Duration taskDuration = Duration.between(startTasks, endTasks);

        LOGGER.info("LineChunks: " + partSize);
        LOGGER.info("Read [" + fileContent.size() + "] log lines in [" + readDuration.toMillis() + "] milliseconds");
        LOGGER.info("Spawned [" + (fileContent.size() / partSize) + "] Tasks for [" + Runtime.getRuntime().availableProcessors() + "] Threads for [" + taskDuration.toMillis() + "] milliseconds");
        LOGGER.info("Total Matches Found: " + this.matchesFound.get());
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

    private record FileScannerTask(List<String> lines, Pattern pattern, int linesBefore, int linesAfter,
                                   AtomicInteger counter) implements Callable<Map<Integer, String>> {

        @Override
        public Map<Integer, String> call() {
            Map<Integer, String> matches = new HashMap<>();
            for (int i = 0; i < lines.size(); i++) {
                Matcher matcher = pattern.matcher(lines.get(i));
                if (matcher.find()) {
                    StringBuilder matchDetails = new StringBuilder();
                    int start = Math.max(0, i - linesBefore);
                    int end = Math.min(lines.size() - 1, i + linesAfter);
                    for (int j = start; j <= end; j++) {
                        if (j == i) {
                            matchDetails.append("> ").append(lines.get(j)).append("\n");
                        } else {
                            matchDetails.append("  ").append(lines.get(j)).append("\n");
                        }
                    }
                    counter.incrementAndGet();
                    matches.put(i, matchDetails.toString());
                }
            }
            return matches;
        }
    }
}

