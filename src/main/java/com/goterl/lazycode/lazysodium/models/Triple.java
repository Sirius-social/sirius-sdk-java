package com.goterl.lazycode.lazysodium.models;

public final class Triple<T, U, Z> {

    public Triple(T first, U second,Z third) {
        this.second = second;
        this.first = first;
        this.third = third;
    }

    public final T first;
    public final U second;
    public final Z third;

    // Because 'pair()' is shorter than 'new Pair<>()'.
    // Sometimes this difference might be very significant (especially in a
    // 80-ish characters boundary). Sorry diamond operator.
    public static <T, U,Z> Triple<T, U,Z> triple(T first, U second,Z third) {
        return new Triple<>(first, second,third);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second +", " +third +")";
    }
}
