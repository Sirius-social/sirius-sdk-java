package com.sirius.sdk.agent.ledger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.sirius.sdk.utils.GsonUtils;

import java.util.Map;
import java.util.Set;

public class CredentialDefinitionFilters {

    public Tags getTags() {
        return tags;
    }

    CredentialDefinitionFilters.Tags tags;
    JsonObject extras;

    public JsonObject getTagsObject() {
        JsonObject tagobj = GsonUtils.getDefaultGson().toJsonTree(tags, CredentialDefinitionFilters.Tags.class).getAsJsonObject();

        Set<Map.Entry<String, JsonElement>> entrySet = extras.entrySet();
        for (Map.Entry<String, JsonElement> obj : entrySet) {
            tagobj.add(obj.getKey(),obj.getValue());
        }
        return tagobj;
    }

    public void setExtras(JsonObject extras) {
        this.extras = extras;
    }

    public JsonObject getExtras() {
        return extras;
    }

    public void addExtra(String name, String value) {
        extras.addProperty(name, value);
    }


    public CredentialDefinitionFilters() {
        this.tags = new CredentialDefinitionFilters.Tags("cred_def");
        this.extras = new JsonObject();
    }

    public String getTag(){
       return getTags().getTag();
    }

    public void setTag(String tag){
         getTags().setTag(tag);
    }
    public void setId(String id){
        getTags().setId(id);
    }
    public String getSubmitterDid() {
        return getTags().getSubmitterDid();
    }

    public void setSubmitterDid(String submitterDid) {
        this.getTags().setSubmitterDid( submitterDid);
    }

    public String getSchemaId() {
        return getTags().getSchemaId();
    }

    public void setSchemaId(String schemaId) {
        this.getTags().setSchemaId(schemaId);
    }

    public int getSeqNo() {
        return getTags().getSeqNo();
    }

    public void setSeqNo(int seqNo) {
        this.getTags().setSeqNo(seqNo);
    }


    public static class Tags {
        String category;
        String id;
        String tag;
        @SerializedName("submitter_did")
        String submitterDid;
        @SerializedName("schema_id")
        String schemaId;
        @SerializedName("seq_no")
        int seqNo;

        public int getSeqNo() {
            return seqNo;
        }

        public void setSeqNo(int seqNo) {
            this.seqNo = seqNo;
        }



        public String getSchemaId() {
            return schemaId;
        }

        public void setSchemaId(String schemaId) {
            this.schemaId = schemaId;
        }

        public String getSubmitterDid() {
            return submitterDid;
        }

        public void setSubmitterDid(String submitterDid) {
            this.submitterDid = submitterDid;
        }


        public void setId(String id) {
            this.id = id;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getId() {
            return id;
        }

        public String getTag() {
            return tag;
        }


        public Tags(String category) {
            this.category = category;
        }
    }

}
