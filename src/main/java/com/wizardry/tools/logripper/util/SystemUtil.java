package com.wizardry.tools.logripper.util;

import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.*;
import java.util.List;

public final class SystemUtil {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private SystemUtil() {
        //private constructor
    }

    public static int calculateOptimalPartSize(long fileSize) {
        // Simple heuristic
        long partSize = (fileSize / (Runtime.getRuntime().availableProcessors() * 2L)) + 1L;
        if (partSize < Integer.MAX_VALUE) {
            return (int) partSize;
        }
        return Integer.MAX_VALUE;
    }

    public static List<String> readFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().toList();
        }
    }
}
