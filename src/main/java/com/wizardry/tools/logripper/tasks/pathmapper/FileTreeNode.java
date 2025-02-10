package com.wizardry.tools.logripper.tasks.pathmapper;

import java.util.List;

public interface FileTreeNode<T> {
    T getPath();
    List<? extends FileTreeNode<T>> getChildren();
    void addChild(FileTreeNode<? extends T> child);
    void addChildren(FileTreeNode<? extends T>... children);
    boolean removeChild(FileTreeNode<? extends T> child);
    long getSize();
    String getReadableSize();
    String getName();
    void display(int level);
    boolean isDir();
    boolean isExe();
    boolean isFile();
    boolean isReadable();
    void sortChildrenBySize();
}
