package com.wizardry.tools.logripper.tasks.pathmapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public interface FileTreeMapper<K extends Path,T extends FileTreeNode<K,T>> {
    T crawl(K rootPath) throws InterruptedException, ExecutionException, IOException;

    static <K extends Path,T extends FileTreeNode<K,T>> void printTree(T node, int level, boolean includeSize) {
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
        for (T child : node.getChildren()) {
            printTree(child, level + 1, includeSize);
        }
    }
}
