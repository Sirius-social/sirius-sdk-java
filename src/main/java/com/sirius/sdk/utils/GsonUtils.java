package com.sirius.sdk.utils;

import com.google.gson.*;
import com.google.gson.annotations.Expose;

import java.util.Map;
import java.util.Set;

public class GsonUtils {

    public static Gson getDefaultGson() {
        return new GsonBuilder().setExclusionStrategies(new ExposeExcludeStrategy()).create();
    }

    public static JsonObject toJsonObject(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }

    public static JsonArray toJsonArray(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonArray();
    }

    public static JsonObject updateJsonObject(JsonObject originalJson, JsonObject updateObject) {
        return updateJsonObject(originalJson,updateObject,false);
    }

    public static JsonObject updateJsonObject(JsonObject originalJson, JsonObject updateObject, boolean withCopy) {
        JsonObject copyOrigin = originalJson;
        if(withCopy){
            copyOrigin =  originalJson.deepCopy();
        }
        Set<Map.Entry<String, JsonElement>> entrySet = updateObject.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            copyOrigin.add(entry.getKey(), entry.getValue());
        }
        return copyOrigin;
    }

    public static class ExposeExcludeStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes field) {
            Expose annotation = field.getAnnotation(Expose.class);
            if (annotation == null) {
                return false;
            }
            return true;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }
}
