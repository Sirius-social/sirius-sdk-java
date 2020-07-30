package com.sirius.sdk;


import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;

public class Main {
    public static Sodium sodium;

    public static void main(String[] args) {
        sodium = NaCl.sodium();
    }
}
