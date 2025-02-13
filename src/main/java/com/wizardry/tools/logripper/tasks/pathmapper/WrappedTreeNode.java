package com.wizardry.tools.logripper.tasks.pathmapper;

import com.wizardry.tools.logripper.util.DataUtil;
import com.wizardry.tools.logripper.util.printing.PrintOptions;
import com.wizardry.tools.logripper.util.wrapping.WrappedPath;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class WrappedTreeNode implements FileTreeNode<WrappedPath,WrappedTreeNode> {

    private final static String UNREADABLE_NODE = "unreadable";

    private final WrappedPath path;
    private final List<WrappedTreeNode> children;
    private final int level;
    private long size;
    private final String name;

    public WrappedTreeNode(WrappedPath path, List<WrappedTreeNode> children, long size, String name, int level) {
        this.path = path;
        this.children = children;
        this.size = size;
        this.name = name;
        this.level = level;
    }

    public WrappedTreeNode(WrappedPath path, List<WrappedTreeNode> children, long size, String name) {
        this(path, children, size, name, 0);
    }

    public WrappedTreeNode(WrappedPath path, List<WrappedTreeNode> children) throws IOException {
        this(path, children, Files.size(path.unwrap()), String.valueOf(path.getFileName()), 0);
    }

    public WrappedTreeNode(WrappedPath path) throws IOException {
        this(path, new ArrayList<>());
    }

    public static WrappedTreeNode of(WrappedPath path) throws IOException {
        return new WrappedTreeNode(path);
    }

    public static WrappedTreeNode ofUnreadable(WrappedPath path, int level) {
        // Dummy Node that represents an unreadable path.
        return new WrappedTreeNode(path, new ArrayList<>(), 0L, UNREADABLE_NODE, level);
    }

    @Override
    public WrappedPath getPath() {
        return path;
    }

    @Override
    public List<WrappedTreeNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(WrappedTreeNode child) {
        children.add(child);
        this.size += child.getSize(); // add child size to the parent dir's size
    }

    public void addUnreadable(WrappedPath path) {
        children.add(WrappedTreeNode.ofUnreadable(path, level + 1));
    }

    @Override
    public final void addChildren(WrappedTreeNode... children) {
        Arrays.stream(children).forEach(this::addChild);
    }


    @Override
    public boolean removeChild(WrappedTreeNode child) {
        boolean success = children.remove((WrappedTreeNode) child);
        this.size -= child.getSize(); // remove child size from parent dir
        return success;
    }

    @Override
    public void display(int level) {
        StringBuilder indent = new StringBuilder();
        indent.append("  ".repeat(Math.max(0, level)));
        System.out.println(indent.append(path));
        for (WrappedTreeNode child : children) {
            child.display(level + 1);
        }
    }

    @Override
    public boolean isDir() {
        return path.isDir();
    }

    @Override
    public boolean isExe() {
        return path.isExe();
    }

    @Override
    public boolean isFile() {
        return path.isFile();
    }

    @Override
    public boolean isReadable() {
        return path.isReadable();
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
    public int getLevel() {
        return level;
    }

    @Override
    public String getName() {
        return name;
    }

    // Method to sort children by size
    public void sortChildrenBySize() {
        children.sort(Comparator.comparingLong(WrappedTreeNode::getSize));
        for (WrappedTreeNode child : children) {
            if (child.isDir()) {
                child.sortChildrenBySize();
            }
        }
    }

    @Override
    public void print() {
        if (isDir()) {
            System.out.println("/" + path.getFileName());
            return;
        }
        System.out.println(path.getFileName());
    }

    @Override
    public void print(PrintOptions options) {
        StringBuilder sb = new StringBuilder();
        if (options.linePrefix() != null) {
            sb.append(options.linePrefix());
        }
        if (options.includeSize()) {
            sb.append("[").append(getReadableSize()).append("]");
            // add additional spacing so that tree indentation accounts for short size strings.
            while(sb.length() < 10) {
                sb.append(" ");
            }
        }
        sb.append(" ".repeat(level * 2));
        if (isDir()) {
            sb.append("/");
        }
        sb.append(getPath().getFileName());
        if (options.lineSuffix() != null) {
            sb.append(options.lineSuffix());
        }
        System.out.println(sb);
    }

    @Override
    public void printChildren() {
        children.forEach(WrappedTreeNode::print);
    }

    @Override
    public void printChildren(PrintOptions options) {
        children.forEach(child -> child.print(options));
    }
}
