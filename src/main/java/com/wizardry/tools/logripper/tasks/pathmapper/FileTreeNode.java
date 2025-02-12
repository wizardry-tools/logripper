package com.wizardry.tools.logripper.tasks.pathmapper;

import java.util.List;
import com.wizardry.tools.logripper.util.filesystem.Readable;
import com.wizardry.tools.logripper.util.filesystem.Sizable;

public interface FileTreeNode<T> extends Readable,Sizable {
    T getPath();
    List<? extends FileTreeNode<T>> getChildren();
    void addChild(FileTreeNode<? extends T> child);
    void addChildren(FileTreeNode<? extends T>... children);
    boolean removeChild(FileTreeNode<? extends T> child);
    String getName();
    void display(int level);
    void sortChildrenBySize();
}
