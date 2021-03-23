package com.sirius.sdk.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JSONUtils {

    public static String JSONObjectToString(JSONObject obj) {
        return JSONObjectToString(obj, false);
    }

    public static String JSONObjectToString(JSONObject obj, boolean sortKeys) {
        List<String> keys = new ArrayList<>(obj.keySet());
        if (sortKeys)
            Collections.sort(keys);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        for (String key : keys) {
            stringBuilder.append("\"").append(key).append("\"").append(":");
            Object val = obj.get(key);
            stringBuilder.append(JSONFieldToString(val)).append(',');
        }
        if (stringBuilder.charAt(stringBuilder.length()-1) == ',') {
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private static String JSONArrayToString(JSONArray arr) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (Object o : arr) {
            stringBuilder.append(JSONFieldToString(o)).append(',');
        }
        if (stringBuilder.charAt(stringBuilder.length()-1) == ',') {
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private static String JSONFieldToString(Object o) {
        if (o == null || o == JSONObject.NULL) {
            return "null";
        }
        if (!(o instanceof JSONObject || o instanceof JSONArray)) {
            boolean needQuotes = !(o instanceof Number || o instanceof Boolean);
            if (needQuotes) {
                return JSONObject.quote(o.toString());
            } else {
                return o.toString();
            }
        }
        if (o instanceof JSONObject) {
            return JSONObjectToString((JSONObject) o);
        }
        if (o instanceof JSONArray) {
            return JSONArrayToString((JSONArray) o);
        }
        return "";
    }
}
