package com.wizardry.tools.logripper.util.functions;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);

    default <X> TriFunction<T, U, V, X> andThen(Function<? super R, ? extends X> after) {
        Objects.requireNonNull(after);

        return (t, u, v) -> after.apply(apply(t, u, v));
    }
}
