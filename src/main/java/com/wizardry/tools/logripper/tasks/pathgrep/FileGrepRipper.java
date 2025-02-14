package com.wizardry.tools.logripper.tasks.pathgrep;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.tasks.PooledRipper;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wizardry.tools.logripper.util.StringUtil.EMPTY;

public class FileGrepRipper implements PooledRipper<Path, Map<Integer, String>> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final LogRipperConfig config;
    private final ExecutorService executor;
    private final int numThreads;
    private final AtomicInteger matchCounter;
    private final int limit;
    private final Pattern pattern;

    public FileGrepRipper(LogRipperConfig config, AtomicInteger matchCounter) {
        this.config = config;
        //this.numThreads = Runtime.getRuntime().availableProcessors();
        this.numThreads = 4;
        this.executor = Executors.newFixedThreadPool(numThreads);
        this.matchCounter = matchCounter;
        this.limit = config.matchLimit();
        this.pattern = config.getTokenPattern();
        LOGGER.info("New FileGrepRipper");
    }

    @Override
    public Map<Integer, String> rip(Path path) throws IOException {
        LOGGER.info("Ripping File: " + path.toAbsolutePath());
        try (RandomAccessFile aFile = new RandomAccessFile(path.toFile(), "r")) {
            FileChannel inChannel = aFile.getChannel();

            long fileSize = inChannel.size();


            List<Future<Map<Integer, String>>> futures = new ArrayList<>();

            // TODO: Fix this, it doesn't actually split the file into parts. Full files are being processed multiple times...
            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                futures.add(executor.submit(() -> {
                    Map<Integer, String> matches = new TreeMap<>();
                    try {
                        long start = fileSize * threadId / numThreads;
                        long end = fileSize * (threadId + 1) / numThreads;

                        // Adjust start position to the beginning of a line
                        goToLineStart(inChannel, start, end);

                        ByteBuffer buffer = ByteBuffer.allocate(4096);
                        StringBuilder lineBuilder = new StringBuilder();
                        AtomicInteger lineNumber = new AtomicInteger(0);

                        readNextLine(inChannel, buffer, lineBuilder, lineNumber, matches, end);

                        // Handle any remaining partial line at the end of the chunk
                        if (!lineBuilder.isEmpty()) {
                            processLine(lineNumber.get(), lineBuilder.toString(), matches);
                            lineNumber.incrementAndGet();
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error occurred while searching file: ", e);
                    }
                    LOGGER.info("File Portion Ripped and found ["+matches.size()+"] matches");
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
            LOGGER.info("File Ripped and found ["+allMatches.size()+"] matches");
            return allMatches;
        } catch (FileNotFoundException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void goToLineStart(FileChannel inChannel, long start, long end) throws IOException {
        // Adjust start position to the beginning of a line
        if (start != 0) {
            inChannel.position(start - 1);
            ByteBuffer buffer = ByteBuffer.allocate(1);
            inChannel.read(buffer);
            buffer.flip();
            if (buffer.hasRemaining()) {
                char c = (char) buffer.get();
                if (c != '\n') {
                    // Move start to the beginning of the next line
                    while (inChannel.position() < end && inChannel.read(buffer) != -1) {
                        buffer.flip();
                        if (buffer.hasRemaining()) {
                            c = (char) buffer.get();
                        }
                        buffer.clear();
                        if (c == '\n') break;
                    }
                }
            }
        }
    }

    private void readNextLine(FileChannel inChannel, ByteBuffer buffer, StringBuilder lineBuilder, AtomicInteger lineNumber, Map<Integer,String> matches, long end) throws IOException {
        while (inChannel.position() < end && inChannel.read(buffer) != -1) {
            buffer.flip();
            for (int j = 0; j < buffer.limit(); j++) {
                char c = (char) buffer.get(j);
                if (c == '\n') {
                    // Process the complete line
                    processLine(lineNumber.get(), lineBuilder.toString(), matches);
                    lineNumber.incrementAndGet();
                    lineBuilder.setLength(0); // Clear the StringBuilder
                } else {
                    lineBuilder.append(c);
                }
            }
            buffer.clear();
        }
    }

    private void processLine(int lineNumber, String line, Map<Integer, String> matches) {
        if (limit == 0 || matchCounter.get() < limit + 1) {
            Matcher matcher = pattern.matcher(line);
            LOGGER.info("checking : " +line);
            if (matcher.find()) {
                LOGGER.info("Match Found @ lineNumber: " +lineNumber);
                matchCounter.incrementAndGet();
                if (config.isSilent()) {
                    matches.put(lineNumber, EMPTY);
                    return;
                }
                matches.put(lineNumber, line);
            }
        }
    }
}
