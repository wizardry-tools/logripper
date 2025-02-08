package com.wizardry.tools.logripper.functions;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<T, U, V> {

    void accept(T t, U u, V v);

    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);

        return (l, u, v) -> {
            accept(l, u, v);
            after.accept(l, u, v);
        };
    }
}
