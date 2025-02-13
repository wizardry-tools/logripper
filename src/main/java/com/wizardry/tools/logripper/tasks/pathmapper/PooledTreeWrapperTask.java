package com.wizardry.tools.logripper.tasks.pathmapper;

import com.wizardry.tools.logripper.util.wrapping.WrappedPath;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class PooledTreeWrapperTask extends RecursiveTask<WrappedTreeNode> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private final WrappedPath path;

    public PooledTreeWrapperTask(WrappedPath path) {
        this.path = path;
    }

    @Override
    protected WrappedTreeNode compute() throws SecurityException{
        WrappedTreeNode node = getWrappedTreeNode(path);
        try (DirectoryStream<Path> stream = path.dirStream()) {
            List<PooledTreeWrapperTask> subTasks = new ArrayList<>();
            stream.forEach(child -> {
                childHandler(node, WrappedPath.of(child), subTasks);
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

    private WrappedTreeNode getWrappedTreeNode(WrappedPath path) {
        try {
            return new WrappedTreeNode(path);
        } catch (IOException e) {
            LOGGER.info("Can't map node: "+path.toAbsolutePath());
            throw new RuntimeException(e);
        }
    }

    private void childHandler(WrappedTreeNode node, WrappedPath entry, List<PooledTreeWrapperTask> subTasks) {
        try {
            if (entry.isDir()) {
                PooledTreeWrapperTask task = new PooledTreeWrapperTask(entry);
                task.fork();
                subTasks.add(task);
            } else if (entry.isFile() || entry.isExe()) {
                node.addChild(new WrappedTreeNode(entry));
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
