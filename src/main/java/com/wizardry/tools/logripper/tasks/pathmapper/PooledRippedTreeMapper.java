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

public class PooledRippedTreeMapper implements FileTreeMapper<RippedTreeNode> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final ForkJoinPool forkJoinPool;

    public PooledRippedTreeMapper() {
        this.forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    public RippedTreeNode crawl(Path rootPath) throws InterruptedException, ExecutionException, IOException {
        return forkJoinPool.invoke(new CrawlTask(rootPath));
    }

    private static class CrawlTask extends RecursiveTask<RippedTreeNode> {

        private final Path path;

        public CrawlTask(Path path) {
            this.path = path;
        }

        @Override
        protected RippedTreeNode compute() throws SecurityException{
            if (!Files.isReadable(path)) {
                return RippedTreeNode.ofUnreadable(path);
            }
            RippedTreeNode node = null;
            try {
                node = new RippedTreeNode(path);
            } catch (IOException e) {
                LOGGER.info("Can't make new Node: "+path.toAbsolutePath());
                throw new RuntimeException(e);
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {

                List<CrawlTask> subTasks = new ArrayList<>();
                for (Path entry : stream) {
                    try {
                        if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                            CrawlTask task = new CrawlTask(entry);
                            task.fork();
                            subTasks.add(task);
                        } else if (Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS) || Files.isExecutable(entry)) {
                            node.addChild(new RippedTreeNode(entry));
                        } else {
                            LOGGER.info("Something else... " + entry);
                            //node.addUnreadable(entry);
                        }
                    } catch (IOException ioe) {
                        // it's actually faster to let the read fail that it is to check if it's readable...
                        // if we check each entry to see if it's readable, it drastically slows down the crawl.
                        node.addUnreadable(entry);
                    }
                }

                for (CrawlTask task : subTasks) {
                    node.addChild(task.join());
                }
                return node;
            } catch (IOException e) {
                LOGGER.info("Catching error and not throwing RuntimeException: " + e.getMessage());
                throw new RuntimeException("Error while crawling path: " + path, e);
            }
        }
    }

    public void shutdown() {
        // ForkJoinPool does not need to be explicitly shut down
        LOGGER.info("Shutdown is not needed for ForkJoinPool");
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
