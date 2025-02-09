package com.wizardry.tools.logripper.tasks.pathmapper;

import com.wizardry.tools.logripper.tasks.PooledRipperTask;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PathMappingTask extends PooledRipperTask<Path, RippedTreeNode> {

    private final RippedTreeNode node;
    private final long maxDepth;

    public PathMappingTask(Path path, long maxDepth) throws IOException {
        super(path);
        this.node = new RippedTreeNode(path);
        this.maxDepth = maxDepth-1;
    }

    @Override
    protected RippedTreeNode compute() {
        if (node.isDir()) {
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
