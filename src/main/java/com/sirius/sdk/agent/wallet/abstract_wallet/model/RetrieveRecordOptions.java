package com.sirius.sdk.agent.wallet.abstract_wallet.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sirius.sdk.base.JsonSerializable;
import org.json.JSONObject;

public class RetrieveRecordOptions implements JsonSerializable<RetrieveRecordOptions> {

    boolean retrieveType;
    boolean retrieveValue;
    boolean retrieveTags;

    Boolean retrieveTotalCount;
    Boolean retrieveRecords;

    public void setRetrieveTotalCount(boolean retrieveTotalCount) {
        this.retrieveTotalCount = retrieveTotalCount;
    }

    public void setRetrieveRecords(boolean retrieveRecords) {
        this.retrieveRecords = retrieveRecords;
    }


    public RetrieveRecordOptions() {
    }

    public RetrieveRecordOptions(boolean retrieveType, boolean retrieveValue, boolean retrieveTags) {
        this.retrieveType = retrieveType;
        this.retrieveValue = retrieveValue;
        this.retrieveTags = retrieveTags;
    }



    @Override
    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);

    }

    @Override
    public JSONObject serializeToJSONObject() {
        String string = serialize();
        return new JSONObject(string);
    }

    @Override
    public RetrieveRecordOptions deserialize(String string) {
        Gson gson = new Gson();
        return gson.fromJson(string, RetrieveRecordOptions.class);
    }

    @Override
    public JsonObject serializeToJsonObject() {
        return null;
    }

    public void checkAll() {
        this.retrieveType = true;
        this.retrieveValue = true;
        this.retrieveTags = true;
    }
}

