package com.sirius.sdk.agent.wallet.abstract_wallet.model;

import com.google.gson.Gson;
import com.sirius.sdk.base.JsonSerializable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AnonCredSchema implements JsonSerializable<AnonCredSchema> {


    String ver;
    String id;
    String name;
    String version;
    List<String> attrNames;

    public AnonCredSchema() {
    }

    public AnonCredSchema(String json) {
        AnonCredSchema anonCreds= deserialize(json);
        this.ver = anonCreds.ver;
        this.id = anonCreds.id;
        this.name = anonCreds.name;
        this.version = anonCreds.version;
        this.attrNames = anonCreds.attrNames;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnonCredSchema that = (AnonCredSchema) o;
        return id.equals(that.id) &&
                name.equals(that.name) &&
                version.equals(that.version) &&
                attrNames.equals(that.attrNames);
    }


    @Override
    public String serialize() {
        return new Gson().toJson(this,AnonCredSchema.class);
    }

    @Override
    public JSONObject serializeToJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ver", ver);
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        jsonObject.put("version", version);
        JSONArray array = new JSONArray();
        for (String attr : attrNames) {
            array.put(attr);
        }
        jsonObject.put("attrNames", array);
        return jsonObject;
    }

    @Override
    public AnonCredSchema deserialize(String string) {
        return new Gson().fromJson(string, AnonCredSchema.class);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getAttrNames() {
        return attrNames;
    }
}

