package com.sirius.sdk.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

public class JsonMessage {

    JSONObject messageObj;

    public JsonMessage(String message) {
        this.messageObj = new JSONObject(message);
    }

    public String prettyPrint() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(this, this.getClass());
    }



    public String getStringFromJSON(String key){
        if(messageObjectHasKey(key)){
            String value =  messageObj.getString(key);
            if(value == null || value.isEmpty()){
                return "";
            }
            return value;
        }
        return "";
    }

    public  boolean messageObjectHasKey(String key){
        return messageObj.has(key);
    }

    public JSONObject getJSONOBJECTFromJSON(String key){
        if(messageObjectHasKey(key)){
            return messageObj.getJSONObject(key);
        }
        return null;
    }



}
