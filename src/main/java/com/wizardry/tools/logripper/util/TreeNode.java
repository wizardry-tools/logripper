package com.wizardry.tools.logripper.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreeNode<T> {
    private T value;
    private final List<TreeNode<T>> children;

    public TreeNode(T value) {
        this.value = value;
        this.children = new ArrayList<>();
    }

    public TreeNode(T value, List<TreeNode<T>> children) {
        this.value = value;
        this.children = children;
    }

    public static <T> TreeNode<T> of(T value) {
        return new TreeNode<T>(value);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public void addChild(TreeNode<T> child) {
        children.add(child);
    }

    public void addChildren(TreeNode<T>... children) {
        this.children.addAll(Arrays.stream(children).toList());
    }

    public boolean removeChild(TreeNode<T> child) {
        return children.remove(child);
    }

    public void display(int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("  ");
        }
        System.out.println(indent.append(value));
        for (TreeNode<T> child : children) {
            child.display(level + 1);
        }
    }

}

