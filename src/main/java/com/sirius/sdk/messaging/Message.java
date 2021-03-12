package com.sirius.sdk.messaging;

import com.google.gson.JsonObject;
import com.sirius.sdk.base.JsonSerializable;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;
import com.sirius.sdk.utils.GsonUtils;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class Message implements JsonSerializable<Message> {
    public static final String FIELD_TYPE = "@type";
    public static final String FIELD_ID = "@id";

    public static List<Triple<Class<? extends Message>, String, String>> MSG_REGISTRY = new ArrayList<>();

    public String getType() {
        return messageObj.optString(FIELD_TYPE, null);
    }

    public String getId() {
        return messageObj.optString(FIELD_ID);
    }

    public void setId(String id) {
        this.messageObj.put(FIELD_ID, id);
    }

    public JSONObject getMessageObj() {
        return messageObj;
    }

    JSONObject messageObj;
    Type typeOfType;

    public String getVersion() {
        return this.typeOfType.version;
    }

    public String getDocUri() {
        return typeOfType.docUri;
    }

    public Message(String message) {
        init(message);
    }

    public Message(JSONObject message) {
        init(message.toString());
    }

    private void init(String message) {
        messageObj = new JSONObject(message);
        if (!messageObjectHasKey(FIELD_TYPE)) {
            new SiriusInvalidMessage("No @type in message").printStackTrace();
        }

        try {
            typeOfType = Type.fromStr(messageObj.optString(FIELD_TYPE));
        } catch (SiriusInvalidType siriusInvalidType) {
            siriusInvalidType.printStackTrace();
        }

        if (!messageObj.has(FIELD_ID)) {
            messageObj.put(FIELD_ID, generateId());
        }
    }

    public Object getObjectFromJSON(String key) {
        if (messageObjectHasKey(key)) {
            if( messageObj.isNull(key)){
                return null;
            }
            return messageObj.get(key);
        }
        return null;
    }

    public String getStringFromJSON(String key) {
        if (messageObjectHasKey(key)) {
            String value = messageObj.getString(key);
            if (value == null || value.isEmpty()) {
                return "";
            }
            return value;
        }
        return "";
    }

    public Boolean getBooleanFromJSON(String key) {
        if (messageObjectHasKey(key)) {
            return messageObj.getBoolean(key);
        }
        return null;

    }

    public boolean messageObjectHasKey(String key) {
        return messageObj.has(key);
    }

    public JSONObject getJSONOBJECTFromJSON(String key) {
        if (messageObjectHasKey(key)) {
            return messageObj.optJSONObject(key);
        }
        return null;
    }

    public JSONObject getJSONOBJECTFromJSON(String key, JSONObject defaultValue) {
        if (messageObjectHasKey(key)) {
            return messageObj.optJSONObject(key);
        }
        return defaultValue;
    }

    public JSONObject getJSONOBJECTFromJSON(String key, String defaultValue) {
        if (messageObjectHasKey(key)) {
            return messageObj.optJSONObject(key);
        }
        return new JSONObject(defaultValue);
    }

    public JSONArray getJSONArrayFromJSON(String key, JSONArray defaultValue) {
        if (messageObjectHasKey(key)) {
           JSONArray object =  messageObj.getJSONArray(key);
           if(object == null) {
               return defaultValue;
           }
           return object;

        }
        return defaultValue;
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String serialize() {
        return messageObj.toString();
    }

    @Override
    public JSONObject serializeToJSONObject() {
        return new JSONObject(messageObj.toString());
    }


    @Override
    public Message deserialize(String string) {
        return new Message(string);
    }

    @Override
    public JsonObject serializeToJsonObject() {
       return GsonUtils.getDefaultGson().toJsonTree(this, Message.class).getAsJsonObject();
    }

    public static void registerMessageClass(Class<? extends Message> clas, String protocol, String name) {
        if (name == null)
            name = "*";
        for (int i = 0; i < MSG_REGISTRY.size(); i++) {
            if (MSG_REGISTRY.get(i).first.equals(clas)) {
                MSG_REGISTRY.set(i, new Triple<>(clas, protocol, name));
                return;
            }
        }

        MSG_REGISTRY.add(new Triple<>(clas, protocol, name));
    }

    public static void registerMessageClass(Class<? extends Message> clas, String protocol) {
        registerMessageClass(clas,protocol, "*");
    }

    public static Pair<String, String> getProtocolAndName(Class<? extends Message> clas) {
        for (Triple<Class<? extends Message>, String, String> e : MSG_REGISTRY) {
            if (e.first.equals(clas)) {
                return new Pair<>(e.second, e.third);
            }
        }
        return new Pair<>(null, null);
    }

    public static Pair<Boolean, Message> restoreMessageInstance(String payload) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Message message = new Message(payload);
        String protocol = message.typeOfType.protocol;
        String name = message.typeOfType.name;

        Class<? extends Message> clssTo = null;
        for (Triple<Class<? extends Message>, String, String> record : MSG_REGISTRY) {
            if (record.second.equals(protocol) && (record.third.equals(name) || record.third.equals("*"))) {
                clssTo = record.first;
            }
        }
        if (clssTo != null) {
            Constructor<? extends Message> constructor = clssTo.getConstructor(String.class);
            return new Pair<>(true, constructor.newInstance(payload));
        }

        return new Pair<>(false, null);
    }
}