package com.wizardry.tools.logripper.tasks.pathmapper;

import com.wizardry.tools.logripper.tasks.PooledRipperTask;
import com.wizardry.tools.logripper.util.FileTreeNode;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PathMappingTask extends PooledRipperTask<Path, FileTreeNode> {

    private final FileTreeNode node;
    private final long maxDepth;

    public PathMappingTask(Path path, long maxDepth) throws IOException {
        super(path);
        this.node = new FileTreeNode(path);
        this.maxDepth = maxDepth-1;
    }

    @Override
    protected FileTreeNode compute() {
        if (node.isDir() && maxDepth != 0) { // don't go deeper if maxDepth is equal to 0
            List<PathMappingTask> subTasks = new ArrayList<>();
            try {
                Files.walkFileTree(input, Set.of(), 0, new PathMappingVisitor(subTasks, maxDepth));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!subTasks.isEmpty()) {
                invokeAll(subTasks);  // Fork all the tasks
                for (PathMappingTask task : subTasks) {
                    node.addChild(task.join());  // Join and add child nodes
                }
            }
        }
        return node;
    }
}
