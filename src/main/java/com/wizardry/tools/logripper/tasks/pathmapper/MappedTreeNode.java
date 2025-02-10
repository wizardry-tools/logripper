package com.wizardry.tools.logripper.tasks.pathmapper;

import com.wizardry.tools.logripper.util.DataUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.*;

public class MappedTreeNode implements FileTreeNode<Path> {

    private final static String UNREADABLE_NODE = "unreadable";

    private final Path path;
    private final List<MappedTreeNode> children;
    private long size;
    private final String name;

    public MappedTreeNode(Path path, List<MappedTreeNode> children, long size, String name) {
        this.path = path;
        this.children = children;
        this.size = size;
        this.name = name;
    }

    public MappedTreeNode(Path path, List<MappedTreeNode> children) throws IOException {
        this.path = path;
        this.children = children;
        this.size = Files.size(path);
        this.name = String.valueOf(path.getFileName());
    }

    public MappedTreeNode(Path path) throws IOException {
        this(path, new ArrayList<>());
    }

    public static MappedTreeNode of(Path path) throws IOException {
        return new MappedTreeNode(path);
    }

    public static MappedTreeNode ofUnreadable(Path path) {
        // Dummy Node that represents an unreadable path.
        return new MappedTreeNode(path, new ArrayList<>(), 0L, UNREADABLE_NODE);
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public List<MappedTreeNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(FileTreeNode<? extends Path> child) {
        children.add((MappedTreeNode) child);
        this.size += child.getSize(); // add child size to the parent dir's size
    }

    public void addUnreadable(Path path) {
        children.add(MappedTreeNode.ofUnreadable(path));
    }

    @SafeVarargs
    @Override
    public final void addChildren(FileTreeNode<? extends Path>... children) {
        this.children.addAll(
                Arrays.stream(children)
                        .map(child -> (MappedTreeNode) child)
                        .toList());
        // update parent dir size
        this.size = this.children.stream().reduce(0L, (acc, node) -> acc + node.getSize(), Long::sum);
    }


    @Override
    public boolean removeChild(FileTreeNode<? extends Path> child) {
        boolean success = children.remove((MappedTreeNode) child);
        this.size -= child.getSize(); // remove child size from parent dir
        return success;
    }

    @Override
    public void display(int level) {
        StringBuilder indent = new StringBuilder();
        indent.append("  ".repeat(Math.max(0, level)));
        System.out.println(indent.append(path));
        for (MappedTreeNode child : children) {
            child.display(level + 1);
        }
    }

    @Override
    public boolean isDir() {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public boolean isExe() {
        return Files.isExecutable(path);
    }

    @Override
    public boolean isFile() {
        return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public boolean isReadable() {
        try {
            return Files.isReadable(path);
        } catch (SecurityException se) {
            return false;
        }
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String getReadableSize() {
        return DataUtil.humanReadableByteCountSI(size);
    }

    @Override
    public String getName() {
        return name;
    }

    // Method to sort children by size
    public void sortChildrenBySize() {
        children.sort(Comparator.comparingLong(MappedTreeNode::getSize));
        for (MappedTreeNode child : children) {
            if (child.isDir()) {
                child.sortChildrenBySize();
            }
        }
    }
}
