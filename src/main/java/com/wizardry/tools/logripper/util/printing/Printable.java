package com.wizardry.tools.logripper.util.printing;

public interface Printable<T> {

    void print();

    void print(PrintOptions options);

    void printChildren();

    void printChildren(PrintOptions options);
}
