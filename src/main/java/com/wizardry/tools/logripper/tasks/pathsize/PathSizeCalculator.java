package com.wizardry.tools.logripper.tasks.pathsize;

import com.wizardry.tools.logripper.tasks.Ripper;
import com.wizardry.tools.logripper.util.DataUtil;
import com.wizardry.tools.logripper.util.Timestamp;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ForkJoinPool;

// TODO: This is failing
public class PathSizeCalculator implements Ripper<Path,Long> {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    @Override
    public Long rip(Path path) throws IOException {
        long size;
        Timestamp calculationTime = new Timestamp();
        try (ForkJoinPool pool = new ForkJoinPool()) {
            PathSizeTask task = new PathSizeTask(path);
            size = pool.invoke(task);
            String humanReadableSize = DataUtil.humanReadableByteCountSI(size);
            System.out.println("Total size: [" + humanReadableSize + "]");
        } catch (Exception e) {
            throw new IOException("Error calculating file size", e);
        }

        LOGGER.info("Calculated file size in [" + calculationTime.toMillis() + "] milliseconds");
        return size;
    }

}

