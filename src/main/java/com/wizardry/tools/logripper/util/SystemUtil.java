package com.wizardry.tools.logripper.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SystemUtil {

    public static int calculateOptimalPartSize(int fileSize) {
        // Simple heuristic
        return (fileSize / (Runtime.getRuntime().availableProcessors() * 2)) + 1;
    }

    public static List<String> readFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return new ArrayList<>(reader.lines().toList());
        }
    }

}
