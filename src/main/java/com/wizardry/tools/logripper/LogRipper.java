package com.wizardry.tools.logripper;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.util.Timestamp;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wizardry.tools.logripper.util.SystemUtil.calculateOptimalPartSize;
import static com.wizardry.tools.logripper.util.StringUtil.EMPTY;
import static com.wizardry.tools.logripper.util.SystemUtil.readFile;

public class LogRipper {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final ExecutorService executor;
    private final AtomicInteger matchesFound;
    private final AtomicInteger matchLimit;
    private final LogRipperConfig config;
    private final ConcurrentLinkedQueue<String> debugMessages;

    public LogRipper(LogRipperConfig config) throws IllegalArgumentException {
        config.validate();
        this.config = config;
        // Use a fixed thread pool for concurrency
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.matchesFound = new AtomicInteger(0);
        this.matchLimit = new AtomicInteger(config.matchLimit());
        this.debugMessages = new ConcurrentLinkedQueue<>();
    }

    public void scanAndReport() {
        try {
            if (Files.isDirectory(config.path(), LinkOption.NOFOLLOW_LINKS)) {
                readAndProcessDir();
            } else {
                readAndProcessPath();
            }
        }  catch (IOException | InterruptedException | ExecutionException e) {
            LOGGER.error("Error occurred while processing path: ["+config.path().toAbsolutePath()+"]", e);
        }
        for (String message : debugMessages) {
            LOGGER.info(message);
        }
    }

    private void readAndProcessDir() throws IOException, InterruptedException, ExecutionException {
        // establish static values
        Pattern pattern = config.getTokenPattern();
        config.getPaths().forEach((path) -> {

        });
    }

    private Map<Integer, String> minReadAndProcessPath() throws IOException, InterruptedException, ExecutionException {
        // establish static values
        Pattern pattern = config.getTokenPattern();
        List<Future<Map<Integer, String>>> futures = new ArrayList<>();
        Map<Integer, String> matches = new TreeMap<>();

        // establish dynamic values
        List<String> fileContent = readFile(config.path().toFile());
        int partSize = calculateOptimalPartSize(fileContent.size());

        for (int i = 0; i < fileContent.size(); i += partSize) {
            int end = Math.min(i + partSize, fileContent.size());
            Callable<Map<Integer, String>> task = new FileScannerTask(fileContent.subList(i, end), pattern, config, matchesFound, matchLimit);
            futures.add(executor.submit(task));
        }

        // Collect results from all tasks
        for (Future<Map<Integer, String>> future : futures) {
            matches.putAll(future.get());
        }

        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        return matches;
    }

    private void readAndProcessPath() throws IOException, InterruptedException, ExecutionException {
        Timestamp readTime = new Timestamp();
        List<String> fileContent = readFile(config.path().toFile());
        Duration readDuration = readTime.getDuration();

        Pattern pattern = config.getTokenPattern();

        int partSize = calculateOptimalPartSize(fileContent.size());
        List<Future<Map<Integer, String>>> futures = new ArrayList<>();

        Timestamp tasksTime = new Timestamp();
        for (int i = 0; i < fileContent.size(); i += partSize) {
            LOGGER.info("Spawning FileScannerTask");
            int end = Math.min(i + partSize, fileContent.size());
            Callable<Map<Integer, String>> task = new FileScannerTask(fileContent.subList(i, end), pattern, config, matchesFound, matchLimit);
            futures.add(executor.submit(task));
        }

        // Collect results from all tasks
        Map<Integer, String> matches = new TreeMap<>();
        for (Future<Map<Integer, String>> future : futures) {
            matches.putAll(future.get());
        }



        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        // TODO: Match reporting exits early for some reason, not reporting all matches.
        LOGGER.info("matches: ", matches.size());
        if (!config.isCountOnly()) {
            reportMatches(matches);
        } else {
            System.out.println(matchesFound.get());
        }

        if (config.isDebug()) {
            debugMessages.add("["+config.path().toAbsolutePath()+"] lineChunks: " + partSize);
            debugMessages.add("Read [" + fileContent.size() + "] log lines in [" + readDuration.toMillis() + "] milliseconds");
            debugMessages.add("Spawned [" + (fileContent.size() / partSize) + "] Tasks for [" + Runtime.getRuntime().availableProcessors() + "] Threads for [" + tasksTime.toMillis() + "] milliseconds");
            debugMessages.add("Total Matches Found: " + matchesFound.get());
        }
    }


    private void reportMatches(Map<Integer, String> matches) {
        if (config.isNumbered()) {
            announceNumberedMatches().accept(matches);
        } else {
            announceMatches().accept(matches);
        }
    }

    private static Consumer<Map<Integer, String>> announceMatches() {
        return (matches) -> {
            matches.values().forEach(System.out::println);
        };
    }

    private static Consumer<Map<Integer, String>> announceNumberedMatches() {
        return (matches) -> {
            matches.forEach((key, value) -> {
                System.out.println("#" + (key + 1) + ": " + value);
            });
        };
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

