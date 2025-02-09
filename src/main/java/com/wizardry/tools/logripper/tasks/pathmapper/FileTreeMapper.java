package com.wizardry.tools.logripper.tasks.pathmapper;

import com.wizardry.tools.logripper.util.FileTreeNode;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;

public interface FileTreeMapper<T extends FileTreeNode<Path>> {

    T crawl(Path rootPath) throws InterruptedException, ExecutionException, IOException;

    void shutdown();
}
