package com.sirius.sdk.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JSONUtils {

    public static String JSONObjectToString(JSONObject obj) {
        List<String> keys = new ArrayList<>(obj.keySet());
        Collections.sort(keys);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        for (String key : keys) {
            stringBuilder.append("\"").append(key).append("\"").append(":");
            Object val = obj.get(key);
//            try {
//                val = new JSONObject(val.toString());
//            } catch (Exception ignored) {}
            if (val == null || val == JSONObject.NULL) {
                stringBuilder.append("null").append(",");
                continue;
            }
            if (!(val instanceof JSONObject || val instanceof JSONArray)) {
                boolean needQuotes = !(val instanceof Number || val instanceof Boolean);
                if (needQuotes) {
                    stringBuilder.append("\"").append(val.toString()).append("\"").append(",");
                } else {
                    stringBuilder.append(val.toString()).append(",");
                }
            }
            if (val instanceof JSONObject) {
                stringBuilder.append(JSONObjectToString((JSONObject) val)).append(",");
            }
            if (val instanceof JSONArray) {
                stringBuilder.append(JSONArrayToString((JSONArray) val)).append(",");
            }
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
            try {
                o = new JSONObject(o.toString());
            } catch (Exception ignored) {}
            if (o == null || o == JSONObject.NULL) {
                stringBuilder.append("null").append(",");
                continue;
            }
            if (!(o instanceof JSONObject || o instanceof JSONArray)) {
                boolean needQuotes = !(o instanceof Number || o instanceof Boolean);
                if (needQuotes) {
                    stringBuilder.append("\"").append(o.toString()).append("\"").append(",");
                } else {
                    stringBuilder.append(o.toString()).append(",");
                }
            }
            if (o instanceof JSONObject) {
                stringBuilder.append(JSONObjectToString((JSONObject) o)).append(",");
            }
            if (o instanceof JSONArray) {
                stringBuilder.append(JSONArrayToString((JSONArray) o)).append(",");
            }
        }
        if (stringBuilder.charAt(stringBuilder.length()-1) == ',') {
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
