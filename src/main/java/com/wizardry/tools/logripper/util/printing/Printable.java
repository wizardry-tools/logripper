package com.wizardry.tools.logripper.util.printing;

public interface Printable {

    default void print() {}

    default void print(PrintOptions options) {}

    default void printChildren() {}

    default void printChildren(PrintOptions options) {}
}
