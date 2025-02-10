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
import java.util.concurrent.RecursiveTask;

public class PooledTreeMapperTask extends RecursiveTask<MappedTreeNode> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final Path path;

    public PooledTreeMapperTask(Path path) {
        this.path = path;
    }

    @Override
    protected MappedTreeNode compute() throws SecurityException{
        MappedTreeNode node = getMappedTreeNode(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            List<PooledTreeMapperTask> subTasks = new ArrayList<>();

            stream.forEach(child -> {
                childHandler(node, child, subTasks);
            });

            subTasks.forEach(subTask -> {
                node.addChild(subTask.join());
            });

            return node;
        } catch (IOException e) {
            LOGGER.info("Catching error and not throwing RuntimeException: " + e.getMessage());
            throw new RuntimeException("Error while crawling path: " + path, e);
        }
    }

    private MappedTreeNode getMappedTreeNode(Path path) {
        try {
            return new MappedTreeNode(path);
        } catch (IOException e) {
            LOGGER.info("Can't map node: "+path.toAbsolutePath());
            throw new RuntimeException(e);
        }
    }

    private void childHandler(MappedTreeNode node, Path entry, List<PooledTreeMapperTask> subTasks) {
        try {
            if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                PooledTreeMapperTask task = new PooledTreeMapperTask(entry);
                task.fork();
                subTasks.add(task);
            } else if (Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS) || Files.isExecutable(entry)) {
                node.addChild(new MappedTreeNode(entry));
            } else {
                LOGGER.info("Something else... " + entry);
                node.addUnreadable(entry);
            }
        } catch (IOException ioe) {
            // it's actually faster to let the read fail that it is to check if it's readable...
            // if we check each entry to see if it's readable, it drastically slows down the crawl.
            node.addUnreadable(entry);
        }
    }
}
