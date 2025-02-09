package com.wizardry.tools.logripper.tasks.pathmapper;

import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class RippedTreeMapper implements FileTreeMapper<RippedTreeNode> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final ExecutorService executor;

    public RippedTreeMapper() {
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public RippedTreeNode crawl(Path rootPath) throws InterruptedException, ExecutionException, IOException {
        Future<RippedTreeNode> futureRoot = crawlDirectory(rootPath);
        return futureRoot.get();
    }

    private Future<RippedTreeNode> crawlDirectory(Path path) throws IOException {
        return executor.submit(() -> {
            RippedTreeNode node = new RippedTreeNode(path);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                List<Future<RippedTreeNode>> futures = new ArrayList<>();
                for (Path entry : stream) {

                    if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                        futures.add(crawlDirectory(entry));
                    } else {
                        node.addChild(new RippedTreeNode(entry));
                    }
                }

                // Wait for all directory crawling tasks to complete and add their results as children
                for (Future<RippedTreeNode> future : futures) {
                    node.addChild(future.get());
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                LOGGER.info("Catching error and throwing RuntimeException");
                throw new RuntimeException("Error while crawling path: " + path, e);
            }

            return node;
        });
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.warn("RippedTreeMapper Executor interrupted.", e);
            executor.shutdownNow();

        }
    }

    public static void printTree(RippedTreeNode node, int level, boolean includeSize) {
        StringBuilder sb = new StringBuilder();
        if (includeSize) {
            sb.append("[").append(node.getReadableSize()).append("]");
            // add additional spacing so that tree indentation accounts for short size strings.
            while(sb.length() < 10) {
                sb.append(" ");
            }
        }
        sb.append(" ".repeat(level * 2));
        if (node.isDir()) {
            sb.append("/");
        }
        sb.append(node.getPath().getFileName());
        System.out.println(sb);
        for (RippedTreeNode child : node.getChildren()) {
            printTree(child, level + 1, includeSize);
        }
    }
}
