package com.wizardry.tools.logripper.tasks.pathmapper;

import com.wizardry.tools.logripper.tasks.PooledRipper;
import com.wizardry.tools.logripper.util.Timestamp;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;

public class PathMapper implements PooledRipper<Path, RippedTreeNode> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();
    private final long maxDepth;
    private final boolean printSize;


    public PathMapper(long maxDepth, boolean printSize) {
        this.maxDepth = maxDepth;
        this.printSize = printSize;
    }

    @Override
    public RippedTreeNode rip(Path start) throws IOException {
        RippedTreeNode root;
        Timestamp calculationTime = Timestamp.now();
        try (ForkJoinPool pool = new ForkJoinPool()) {
            PathMappingTask task = new PathMappingTask(start, maxDepth);
            root =  pool.invoke(task);
        } catch (Exception e) {
            throw new IOException("Error calculating file size", e);
        }
        LOGGER.info("Mapped Paths in [" + calculationTime.toMillis() + "] milliseconds");
        return root;
    }

}

