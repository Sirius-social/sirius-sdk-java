package com.sirius.sdk.agent.ledger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.utils.GsonUtils;
import org.json.JSONObject;

public class Schema extends AnonCredSchema {
    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public int getSeqNo() {
        return seqNo;
    }

    int seqNo;

   public String getIssuerDid() {
        if (getId() != null) {
            return getId().split(":")[0];
        }
        return null;
    }

    public Schema() {
        super();
    }

    public Schema(String json) {
        super(json);
        Schema schema = deserialize(json);
        seqNo = schema.seqNo;
    }

    @Override
    public boolean equals(Object o) {
       boolean isEqueals =  super.equals(o);
        Schema schema = (Schema) o;
        return seqNo == schema.seqNo && isEqueals;
    }

    @Override
    public Schema deserialize(String string) {
        return new Gson().fromJson(string, Schema.class);
    }
    @Override
    public String serialize() {
        return new Gson().toJson(this,Schema.class);
    }

    @Override
    public JSONObject serializeToJSONObject() {
        JSONObject jsonObject = super.serializeToJSONObject();
        jsonObject.put("seqNo", seqNo);
        return jsonObject;
    }

    @Override
    public JsonObject serializeToJsonObject() {
        return GsonUtils.getDefaultGson().toJsonTree(this,Schema.class).getAsJsonObject();
    }

}


