package com.sirius.sdk.agent;

import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Codec {
    static final long I32_BOUND = 2^31;
    static final Map<String, Integer> ENCODE_PREFIX;
    static {
        ENCODE_PREFIX = new HashMap<>();
        ENCODE_PREFIX.put(String.class.getSimpleName(), 1);
        ENCODE_PREFIX.put(Boolean.class.getSimpleName(), 2);
        ENCODE_PREFIX.put(Integer.class.getSimpleName(), 3);
        ENCODE_PREFIX.put(Long.class.getSimpleName(), 3);
        ENCODE_PREFIX.put(Double.class.getSimpleName(), 4);
        ENCODE_PREFIX.put("", 9);
    }

    public static String encode(Object raw) {
        if (raw == null) {
            return String.valueOf(I32_BOUND);
        }

        if (raw instanceof Boolean) {
            return String.format("%d%d", ENCODE_PREFIX.get(Boolean.class.getSimpleName()), (Boolean) raw ? I32_BOUND + 2 : I32_BOUND + 1);
        }

        if (raw instanceof Integer && (Integer) raw >= -I32_BOUND && (Integer) raw < I32_BOUND) {
            return String.format("%d", (Integer) raw);
        }

        if (raw instanceof Long && (Long) raw >= -I32_BOUND && (Long) raw < I32_BOUND) {
            return String.format("%d", (Long) raw);
        }

        String hexed = String.format("%d%s",
                ENCODE_PREFIX.getOrDefault(raw.getClass().getSimpleName(), ENCODE_PREFIX.get("")),
                (new BigInteger(raw.toString().getBytes(StandardCharsets.UTF_8))).add(BigInteger.valueOf(I32_BOUND)).toString()
                );
        return hexed;
    }
}
