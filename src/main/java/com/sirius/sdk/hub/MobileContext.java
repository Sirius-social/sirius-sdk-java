package com.sirius.sdk.hub;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Invitee;
import com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol.MediateGrant;
import com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol.MediateRequest;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2PAnon;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.ArrayList;

public class MobileContext extends Context {

    static final String MEDIATOR_ENDPOINTS = "MEDIATOR_ENDPOINTS";

    Pairwise mediatorPw = null;
    int timeToLiveSec = 60;

    public MobileContext(MobileHub.Config config) {
        super(new MobileHub(config));
        connectToMediator();

        Invitation invitation = ((MobileHub.Config) getCurrentHub().getConfig()).mediatorInvitation;
        String myEndpoint = getMyEndpoint(invitation.recipientKeys().get(0));
        if (myEndpoint != null) {
            Endpoint endpoint = new Endpoint(myEndpoint);
            getEndpoints().add(endpoint);
        } else {
            askForMediation();
        }
    }

    public static class MobileContextBuilder {
        MobileHub.Config config = new MobileHub.Config();

        public MobileContextBuilder setWalletConfig(JSONObject walletConfig) {
            config.walletConfig = walletConfig;
            return this;
        }

        public MobileContextBuilder setWalletCredentials(JSONObject walletCredentials) {
            config.walletCredentials = walletCredentials;
            return this;
        }

        public MobileContextBuilder setMediatorInvitation(Invitation invitation) {
            config.mediatorInvitation = invitation;
            return this;
        }

        public MobileContext build() {
            return new MobileContext(this.config);
        }
    }

    public static MobileContextBuilder builder() {
        return new MobileContext.MobileContextBuilder();
    }

    public void connectToMediator() {
        Invitation invitation = ((MobileHub.Config) getCurrentHub().getConfig()).mediatorInvitation;

        Pair<String, String> didVk = getDidVkForMediator(invitation.recipientKeys().get(0));
        if (didVk == null) {
            didVk = getDid().createAndStoreMyDid();
        }
        Pairwise.Me me = new Pairwise.Me(didVk.first, didVk.second);
        Endpoint endpoint = new Endpoint("ws://");
        Invitee invitee = new Invitee(this, me, endpoint);
        Pairwise pw = invitee.createConnection(invitation, "Edge agent");

        if (pw != null) {
            getPairwiseList().ensureExists(pw);
            mediatorPw = pw;
        }
    }

    private Pair<String, String> getDidVkForMediator(String mediatorRecipientKey) {
        String recordStr = getNonSecrets().getWalletRecord(MEDIATOR_ENDPOINTS, mediatorRecipientKey, new RetrieveRecordOptions(false, true, false));
        if (recordStr != null && !recordStr.isEmpty()) {
            JSONObject r = new JSONObject(recordStr);
            JSONObject v = new JSONObject(r.opt("value").toString());
            if (v.has("my_did")) {
                String myDid = v.optString("my_did");
                String myVk = getDid().keyForLocalDid(myDid);
                return new Pair<>(myDid, myVk);
            }
        }
        return null;
    }

    private String getMyEndpoint(String mediatorRecipientKey) {
        String recordStr = getNonSecrets().getWalletRecord(MEDIATOR_ENDPOINTS, mediatorRecipientKey, new RetrieveRecordOptions(false, true, false));
        if (recordStr != null && !recordStr.isEmpty()) {
            JSONObject r = new JSONObject(recordStr);
            JSONObject v = new JSONObject(r.opt("value").toString());
            if (v.has("endpoint")) {
                return v.optString("endpoint");
            }
        }
        return null;
    }

    private void saveEndpoint(String mediatorRecipientKey, String myDid, String endpoint) {
        getNonSecrets().addWalletRecord(MEDIATOR_ENDPOINTS, mediatorRecipientKey, new JSONObject().
                put("my_did", myDid).
                put("endpoint", endpoint).
                toString());
    }

    public boolean askForMediation() {
        try (AbstractP2PCoProtocol cp = new CoProtocolP2PAnon(
                this, mediatorPw.getMe().getVerkey(), mediatorPw.getTheir(), new ArrayList<>(), timeToLiveSec)) {
            MediateRequest request = MediateRequest.builder().build();
            Pair<Boolean, Message> res = cp.sendAndWait(request);
            if (res.first) {
                if (res.second instanceof MediateGrant) {
                    MediateGrant grant = (MediateGrant) res.second;
                    Endpoint endpoint = new Endpoint(grant.getEndpointAddress(), grant.getRoutingKeys());
                    getEndpoints().add(endpoint);
                    Invitation invitation = ((MobileHub.Config) getCurrentHub().getConfig()).mediatorInvitation;
                    saveEndpoint(invitation.recipientKeys().get(0), mediatorPw.getMe().getDid(), endpoint.getAddress());
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
