package com.sirius.sdk.agent.n_wise.transactions;

import com.sirius.sdk.agent.n_wise.messages.BaseNWiseMessage;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnProtocolMessage.buildDidDoc;

public class GenesisTx extends BaseNWiseMessage {

    static {
        Message.registerMessageClass(GenesisTx.class, BaseNWiseMessage.PROTOCOL, "initial-message");
    }

    public GenesisTx(String msg) {
        super(msg);
    }

    public String getLabel() {
        return getMessageObj().optString("label");
    }

    public static GenesisTx.Builder<?> builder() {
        return new InitialMessageBuilder();
    }

    public static abstract class Builder<B extends GenesisTx.Builder<B>> extends BaseNWiseMessage.Builder<B> {
        String label = null;
        String creatorNickName = null;
        String creatorDid = null;
        String creatorVerkey = null;
        String creatorEndpoint = null;
        JSONObject creatorDidDocExtra = null;
        List<JSONObject> creatorConnectionServices = new ArrayList<>();

        public B setLabel(String label) {
            this.label = label;
            return self();
        }
        public B setCreatorNickName(String creatorNickName) {
            this.creatorNickName = creatorNickName;
            return self();
        }

        public B setCreatorDid(String creatorDid) {
            this.creatorDid = creatorDid;
            return self();
        }

        public B setCreatorVerkey(String creatorVerkey) {
            this.creatorVerkey = creatorVerkey;
            return self();
        }

        public B setCreatorEndpoint(String endpoint) {
            this.creatorEndpoint = endpoint;
            return self();
        }

        public B setCreatorDidDocExtra(JSONObject didDocExtra) {
            this.creatorDidDocExtra = didDocExtra;
            return self();
        }

        public B addConnectionService(JSONObject service) {
            this.creatorConnectionServices.add(service);
            return self();
        }
        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            put(label, "label", jsonObject);

            JSONObject extra = (creatorDidDocExtra != null) ? creatorDidDocExtra : new JSONObject();
            if (creatorNickName != null) {
                extra.put("nickname", creatorNickName);
            }

            if (creatorDid != null && creatorVerkey != null && creatorEndpoint != null) {
                jsonObject.put("connection", (new JSONObject().
                        put("DID", creatorDid).
                        put("DIDDoc", buildDidDoc(creatorDid, creatorVerkey, creatorEndpoint, extra))));
                for (JSONObject s : creatorConnectionServices) {
                    jsonObject.getJSONObject("connection").getJSONObject("DIDDoc").getJSONArray("service").put(s);
                }
            }

            return jsonObject;
        }

        public GenesisTx build() {
            return new GenesisTx(generateJSON().toString());
        }
    }

    private static class InitialMessageBuilder extends GenesisTx.Builder<InitialMessageBuilder> {
        @Override
        protected InitialMessageBuilder self() {
            return this;
        }
    }

}
