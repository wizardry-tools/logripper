package com.wizardry.tools.logripper.tasks.pathsize;

import com.wizardry.tools.logripper.tasks.PooledRipper;
import com.wizardry.tools.logripper.util.DataUtil;
import com.wizardry.tools.logripper.util.Timestamp;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ForkJoinPool;

public class PathSizeCalculator implements PooledRipper<Path,Long> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    @Override
    public Long rip(Path path, boolean isDebug) throws IOException {
        long size = 0;
        Timestamp calculationTime = new Timestamp();
        try (ForkJoinPool pool = new ForkJoinPool()) {
            PathSizeTask task = new PathSizeTask(path, isDebug);
            size = pool.invoke(task);
            String humanReadableSize = DataUtil.humanReadableByteCountSI(size);
            LOGGER.info("Total size for [" + path.toAbsolutePath() + "] is [" + humanReadableSize + "]");
        } catch (Exception e) {
            throw new IOException("Error calculating file size", e);
        }
        if (isDebug) {
            LOGGER.info("Calculated file size in [" + calculationTime.toMillis() + "] milliseconds");
        }
        return size;
    }

}

