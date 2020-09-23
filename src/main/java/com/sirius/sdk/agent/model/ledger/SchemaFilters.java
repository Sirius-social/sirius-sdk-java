package com.sirius.sdk.agent.model.ledger;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.sirius.sdk.base.JsonSerializable;
import com.sirius.sdk.utils.GsonUtils;
import org.json.JSONObject;

public class SchemaFilters {

    public Tags getTags() {
        return tags;
    }

    SchemaFilters.Tags tags;

    public String getId() {
        return getTags().getId();
    }

    public void setId(String id) {
        getTags().setId(id);
    }

    public String getName() {
        return getTags().getName();
    }

    public void setName(String name) {
        this.getTags().setName(name);
    }

    public String getVersion() {
        return getTags().getVersion();
    }

    public void setVersion(String version) {
        this.getTags().setVersion(version);
    }

    public String getSubmitterDid() {
        return getTags().getSubmitterDid();
    }

    public void setSubmitterDid(String submitterDid) {
        this.getTags().setSubmitterDid(submitterDid);
    }


    public SchemaFilters() {
        this.tags = new Tags("schema");
    }

    public static class Tags implements JsonSerializable<Tags> {
        String category ;
        String id;
        String name;
        String version;
        @SerializedName("submitter_did")
        String submitterDid;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSubmitterDid() {
            return submitterDid;
        }

        public void setSubmitterDid(String submitterDid) {
            this.submitterDid = submitterDid;
        }

        public Tags(String category) {
            this.category = category;
        }

        @Override
        public String serialize() {
            return GsonUtils.getDefaultGson().toJson(this,Tags.class);
        }

        @Override
        public JSONObject serializeToJSONObject() {
            return null;
        }

        @Override
        public Tags deserialize(String string) {
            return GsonUtils.getDefaultGson().fromJson(string,Tags.class);
        }

        @Override
        public JsonObject serializeToJsonObject() {
            return GsonUtils.toJsonObject(serialize());
        }
    }
}
