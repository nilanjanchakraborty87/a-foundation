package com.ajjpj.abase.function;

/**
 * Represents a function that takes two arguments and produces a result.
 *
 * @param <T> 1st parameter type
 * @param <X> 2nd parameter type
 * @param <R> return type
 *
 * @author bitmagier
 */
public interface AFunction2<T, X, R, E extends Exception> {
    R apply (T param1, X param2) throws E;
}