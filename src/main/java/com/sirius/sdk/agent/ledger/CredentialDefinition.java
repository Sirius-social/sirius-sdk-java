package com.sirius.sdk.agent.ledger;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sirius.sdk.base.JsonSerializable;
import com.sirius.sdk.utils.GsonUtils;
import org.json.JSONObject;

public class CredentialDefinition implements JsonSerializable<CredentialDefinition> {
    public CredentialDefinition() {
    }

    public String getTag() {
        return tag;
    }

    public Schema getSchema() {
        return schema;
    }

    public Config getConfig() {
        return config;
    }

    public JsonObject getBody() {
        return body;
    }

    public int getSeqNo() {
        return seqNo;
    }
    @Expose
    String tag;
    Schema schema;
    CredentialDefinition.Config config;
    JsonObject body;
    @SerializedName("seq_no")
    Integer seqNo;

    public CredentialDefinition(String tag, Schema schema, Config config, JsonObject body, Integer seqNo) {
        this.tag = tag;
        this.schema = schema;
        this.config = config;
        this.body = body;
        this.seqNo = seqNo;
    }

    public CredentialDefinition(String tag, Schema schema) {
        this(tag,schema,new Config(),null);
    }

    public CredentialDefinition(String tag, Schema schema, Integer seqNo) {
        this(tag,schema,new Config(),seqNo);
    }

    public CredentialDefinition(String tag, Schema schema, Config config, Integer seqNo) {
        this(tag, schema, config,null,seqNo);
    }

    public static class Config implements JsonSerializable<Config> {
        @SerializedName("support_revocation")
        boolean supportRevocation;

        @Override
        public String serialize() {
            return GsonUtils.getDefaultGson().toJson(this, Config.class);
        }

        @Override
        public JSONObject serializeToJSONObject() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("support_revocation", supportRevocation);
            return jsonObject;
        }

        @Override
        public Config deserialize(String string) {
            return new Gson().fromJson(string, Config.class);
        }

        @Override
        public JsonObject serializeToJsonObject() {
            return null;
        }

    }

    @Override
    public String serialize() {
        return new Gson().toJson(this, CredentialDefinition.class);
    }

    @Override
    public JSONObject serializeToJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("support_revocation", true);
        return jsonObject;
    }

    @Override
    public CredentialDefinition deserialize(String string) {
        return new Gson().fromJson(string, CredentialDefinition.class);
    }

    @Override
    public JsonObject serializeToJsonObject() {
        return null;
    }


    public  String getId(){
        if (body != null) {
            if(body.has("id")) {
                return body.getAsJsonPrimitive("id").getAsString();
            }
        }
        return null;
    }

    public String getSubmitterDid(){
        String id = getId();
        if(id != null){
           return id.split(":")[0];
        }
        return null;
    }

}

