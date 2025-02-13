package com.wizardry.tools.logripper.util.wrapping;

public interface Wrappable<T> {

    default T unwrap() {
        return null;
    }
}
