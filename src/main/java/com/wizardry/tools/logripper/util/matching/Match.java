package com.wizardry.tools.logripper.util.matching;

import com.wizardry.tools.logripper.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public record Match(String value, int index, boolean numbered) {
    private static final List<String> before = new ArrayList<>();
    private static final List<String> after = new ArrayList<>();

    public Match(String value, int index) {
        this(value, index, false);
        validate();
    }

    public void addBefore(String line) {
        if(!StringUtil.isEmpty(line)) {
            before.add(line);
        }
    }
    public void addAllBefore(List<String> list) {
        list.forEach(this::addBefore);
    }

    public void addAfter(String line) {
        if(!StringUtil.isEmpty(line)) {
            after.add(line);
        }
    }

    public void addAllAfter(List<String> list) {
        list.forEach(this::addAfter);
    }

    public List<String> getLinesBefore() {
        return List.copyOf(before);
    }

    public List<String> getLinesAfter() {
        return List.copyOf(after);
    }

    public void print(java.io.PrintStream out) {
        validate();
        before.forEach(out::println);
        out.println(index + ": " + value);
        after.forEach(out::println);
    }

    private void validate() {
        if(StringUtil.isEmpty(value)) {
            throw new IllegalArgumentException("The Match's value cannot be empty");
        }
        if((index < Integer.MIN_VALUE + before.size()) || (index > Integer.MAX_VALUE - after.size())) {
            throw new IndexOutOfBoundsException("Match index is potentially invalid.");
        }
    }

    public static Match of(String value, int index) {
        return new Match(value, index);
    }
    public static Match of(String value, int index, boolean numbered) {
        return new Match(value, index, numbered);
    }

}
