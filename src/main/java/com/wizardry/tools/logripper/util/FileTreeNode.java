package com.wizardry.tools.logripper.util;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

public class FileTreeNode extends TreeNode<Path> {

    private final long size;

    public FileTreeNode(Path value) throws IOException {
        super(value);
        this.size = Files.size(value);
    }

    public FileTreeNode(Path value, List<TreeNode<Path>> children) throws IOException {
        super(value, children);
        this.size = Files.size(value);
    }

    public static FileTreeNode of(Path value) throws IOException {
        return new FileTreeNode(value);
    }

    public boolean isDir() {
        return Files.isDirectory(getValue(), LinkOption.NOFOLLOW_LINKS);
    }

    public boolean isExe() {
        return Files.isExecutable(getValue());
    }

    public boolean isFile() {
        return Files.isRegularFile(getValue(), LinkOption.NOFOLLOW_LINKS);
    }

    public boolean isReadable() {
        try {
            return Files.isReadable(getValue());
        } catch (SecurityException se) {
            return false;
        }
    }

    public long getSize() {
        return size;
    }

    public String getReadableSize() {
        return DataUtil.humanReadableByteCountSI(size);
    }
}
