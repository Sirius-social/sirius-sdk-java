package com.sirius.sdk.hub;
import com.sirius.sdk.agent.MobileAgent;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Invitee;
import com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol.KeylistUpdate;
import com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol.KeylistUpdateResponse;
import com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol.MediateGrant;
import com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol.MediateRequest;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.agent.wallet.impl.PoolMobile;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2PAnon;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MobileContext extends Context {

    static final String MEDIATOR_ENDPOINTS = "MEDIATOR_ENDPOINTS";

    Pairwise mediatorPw = null;
    int timeToLiveSec = 60;

    public static void addPool(String name, String txnPath) {
        PoolMobile.registerPool(name, txnPath);
    }

    public MobileContext(MobileHub.Config config) {
        super(new MobileHub(config));
        connectToMediator();
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

        String mediatorDid = getMediatorDid(invitation.recipientKeys().get(0));
        if (mediatorDid == null) {
            Pair<String, String> didVk = getDid().createAndStoreMyDid();
            Pairwise.Me me = new Pairwise.Me(didVk.first, didVk.second);
            Endpoint endpoint = new Endpoint("ws://");
            Invitee invitee = new Invitee(this, me, endpoint);
            Pairwise pw = invitee.createConnection(invitation, "Edge agent");

            if (pw != null) {
                getPairwiseList().ensureExists(pw);
                mediatorPw = pw;
            }

            askForMediation();
        } else {
            mediatorPw = getPairwiseList().loadForDid(mediatorDid);
            getEndpoints().add(getMyMediatorEndpoint(invitation.recipientKeys().get(0)));
        }

        JSONArray services = mediatorPw.getTheir().getDidDoc().optJSONArray("service");
        JSONObject mediatorService = new JSONObject();
        for (Object o : services) {
            JSONObject service = (JSONObject) o;
            if (service.optString("type").equals("MediatorService")) {
                mediatorService = service;
                break;
            }
        }

        String myWsEndpoint = mediatorService.optString("serviceEndpoint");
        ((MobileAgent) currentHub.getAgentConnectionLazy()).connect(myWsEndpoint);
    }

    private String getMediatorDid(String mediatorRecipientKey) {
        String recordStr = getNonSecrets().getWalletRecord(MEDIATOR_ENDPOINTS, mediatorRecipientKey, new RetrieveRecordOptions(false, true, false));
        if (recordStr != null && !recordStr.isEmpty()) {
            JSONObject r = new JSONObject(recordStr);
            JSONObject v = new JSONObject(r.opt("value").toString());
            if (v.has("their_did")) {
                return v.optString("their_did");
            }
        }
        return null;
    }

    private Endpoint getMyMediatorEndpoint(String mediatorRecipientKey) {
        String recordStr = getNonSecrets().getWalletRecord(MEDIATOR_ENDPOINTS, mediatorRecipientKey, new RetrieveRecordOptions(false, true, false));
        if (recordStr != null && !recordStr.isEmpty()) {
            JSONObject r = new JSONObject(recordStr);
            JSONObject v = new JSONObject(r.opt("value").toString());
            if (v.has("endpoint_address")) {
                return new Endpoint(v.optString("endpoint_address"));
            }
        }
        return null;
    }

    private void saveMediatorInfo(String mediatorRecipientKey, String theirDid, Endpoint endpoint) {
        getNonSecrets().addWalletRecord(MEDIATOR_ENDPOINTS, mediatorRecipientKey, new JSONObject().
                put("their_did", theirDid).
                put("endpoint_address", endpoint.getAddress()).
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
                    saveMediatorInfo(invitation.recipientKeys().get(0), mediatorPw.getTheir().getDid(), endpoint);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addMediatorKeys(List<String> keys) {
        try (AbstractP2PCoProtocol cp = new CoProtocolP2PAnon(
                this, mediatorPw.getMe().getVerkey(), mediatorPw.getTheir(), new ArrayList<>(), timeToLiveSec)) {
            KeylistUpdate keylistUpdate = KeylistUpdate.builder().
                    addKeys(keys).
                    build();
            Pair<Boolean, Message> res = cp.sendAndWait(keylistUpdate);
            if (res.first) {
                if (res.second instanceof KeylistUpdateResponse) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addMediatorKey(String key) {
        return addMediatorKeys(Arrays.asList(key));
    }
}
