package com.sirius.sdk.agent.wallet.impl;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

public abstract  class AbstractGenericType<T> {
    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) { };
    private final Type type = typeToken.getType();

    public Type getType() {
        return type;
    }
}
