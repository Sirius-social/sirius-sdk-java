package com.sirius.sdk.agent.wallet;


import java.util.HashMap;
import java.util.Map;

public enum KeyDerivationMethod {
    DEFAULT("ARGON2I_MOD"),
    FAST("ARGON2I_INT"),
    RAW("RAW");

    private static Map<String, KeyDerivationMethod> map = new HashMap<String, KeyDerivationMethod>();

    private String value;

    static {
        for (KeyDerivationMethod errorCode : KeyDerivationMethod.values()) {
            map.put(String.valueOf(errorCode.value), errorCode);
        }
    }

    KeyDerivationMethod(String value) {
        this.value = value;
    }

    public static KeyDerivationMethod valueOfVal(String value) {
        return map.get(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
