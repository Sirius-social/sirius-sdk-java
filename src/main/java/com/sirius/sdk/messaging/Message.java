package com.sirius.sdk.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.base.JsonSerializable;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessageClass;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static final class MessageBuilder {
        JSONObject jsonObject = new JSONObject();

        public MessageBuilder(String id, String type) {
            jsonObject.put("@id", id);
            jsonObject.put("@type", type);
        }

        public MessageBuilder(String type) {
            jsonObject.put("@id", Message.generateId());
            jsonObject.put("@type", type);
        }

        public MessageBuilder add(String key, Object value) {
            jsonObject.put(key,value);
            return this;
        }

        public Message build() {
            Message message = new Message(jsonObject.toString());
            return message;
        }


    }

    public static List<Triple<Class<? extends Message>, String, String>> MSG_REGISTRY = new ArrayList<>();

    public static final String FIELD_TYPE = "@type";
    public static final String FIELD_ID = "@id";

    @SerializedName("@type")
    private
    String type;
    @SerializedName("@id")
    private
    String id;

    public JSONObject getMessageObj() {
        return messageObj;
    }

    JSONObject messageObj;
    Type typeOfType;

    public String getVersion() {
        return this.typeOfType.version;
    }

    public String prettyPrint() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(this, this.getClass());
    }

    public String getDocUri() {
        return typeOfType.docUri;
    }

    public Message(String message) {
        messageObj = new JSONObject(message);
        if (!messageObjectHasKey(FIELD_TYPE)) {
            //   throw new SiriusInvalidMessage("No @type in message");
        }

        type = messageObj.getString(FIELD_TYPE);
        try {
            typeOfType = Type.fromStr(type);
        } catch (SiriusInvalidType siriusInvalidType) {
            siriusInvalidType.printStackTrace();
        }
        id = getStringFromJSON(FIELD_ID);
        if (id.isEmpty()) {
            this.id = generateId();
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
        Gson gson = new Gson();
      //  return gson.toJson(this, this.getClass());
        return messageObj.toString();
    }




    @Override
    public JSONObject serializeToJSONObject() {
        String string = serialize();
        return new JSONObject(string);
    }


    @Override
    public Message deserialize(String string) {
        Gson gson = new Gson();
        return gson.fromJson(string, this.getClass());
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
        for (int i = 0; i < MSG_REGISTRY.size(); i++) {
            if (MSG_REGISTRY.get(i).first.equals(clas)) {
                return new Pair<>(MSG_REGISTRY.get(i).second, MSG_REGISTRY.get(i).third);
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