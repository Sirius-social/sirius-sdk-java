package com.sirius.sdk.base;

import com.google.gson.JsonElement;
import org.json.JSONObject;

public interface JsonSerializable<T> {
    String serialize() ;
    JSONObject serializeToJSONObject() ;
    T deserialize(String string);
}

