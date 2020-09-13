package com.sirius.sdk.utils;

public final class Pair<T, U> {

    public Pair(T first, U second) {
        this.second = second;
        this.first = first;
    }

    public final T first;
    public final U second;

    // Because 'pair()' is shorter than 'new Pair<>()'.
    // Sometimes this difference might be very significant (especially in a
    // 80-ish characters boundary). Sorry diamond operator.
    public static <T, U> Pair<T, U> pair(T first, U second) {
        return new Pair<>(first, second);
    }


    public Class getFirstClass() {
        return first.getClass();
    }

    public Class getSecondClass() {
        return second.getClass();
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
