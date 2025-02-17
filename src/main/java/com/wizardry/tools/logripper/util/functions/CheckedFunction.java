package com.wizardry.tools.logripper.util.functions;

import java.util.function.Function;

/**
 * Represents a function that accepts one argument and produces a result.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {

    static <T,R> CheckedFunction<T,R> from(Function<T,R> function) {
        return function == null ? null : function::apply;
    }
    
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws Exception
     */
    R apply(T t) throws Exception;

    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies this function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of input to the {@code before} function, and to the
     *           composed function
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before}
     * function and then applies this function
     * @throws NullPointerException if before is null
     *
     */
    default <V> CheckedFunction<V, R> compose(final CheckedFunction<? super V, ? extends T> before) {
        if (before == null) {
            throw new NullPointerException();
        }
        return (V t) -> apply(before.apply(t));
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     *
     */
    default <V> CheckedFunction<T, V> andThen(final CheckedFunction<? super R, ? extends V> after) {
        if (after == null) {
            throw new NullPointerException();
        }
        return (T t) -> after.apply(apply(t));
    }

    /**
     * Returns a function that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the function
     * @return a function that always returns its input argument
     */
    static <T> CheckedFunction<T, T> identity() {
        return (T t) -> t;
    }

    @SafeVarargs
    static <T> CheckedFunction<T, Boolean> or(CheckedFunction<T, Boolean>... functions) {
        return t -> {
            for (CheckedFunction<T, Boolean> f : functions) {
                if (f.apply(t)) {
                    return true;
                }
            }
            return false;
        };
    }    
}