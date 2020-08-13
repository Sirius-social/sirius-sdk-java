package com.sirius.sdk.base;

import com.google.gson.JsonElement;

public interface JsonSerializable<T> {
    String serialize() ;
    T deserialize(String string);
}

