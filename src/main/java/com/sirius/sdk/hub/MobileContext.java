package com.sirius.sdk.hub;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Invitee;
import com.sirius.sdk.agent.BaseSender;

import com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol.MediateGrant;
import com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol.MediateRequest;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2PAnon;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.ArrayList;

public class MobileContext extends Context {

    Pairwise mediatorPw = null;
    int timeToLiveSec = 60;

    public MobileContext(MobileHub.Config config) {
        super(new MobileHub(config));
        connectToMediator();
        askForMediation();
    }


    @Override
    public MobileHub getCurrentHub() {
        return (MobileHub) super.getCurrentHub();
    }

    public static class MobileContextBuilder {
        MobileHub.Config config = new MobileHub.Config();

        public MobileContextBuilder setWalletConfig(JSONObject walletConfig) {
            config.walletConfig = walletConfig;
            return this;
        }
        public MobileContextBuilder setIndyEndpoint(String indyEndpoint) {
            config.indyEndpoint = indyEndpoint;
            return this;
        }

        public MobileContextBuilder setServerUri(String serverUri) {
            config.serverUri = serverUri;
            return this;
        }

        public MobileContextBuilder setWalletCredentials(JSONObject walletCredentials) {
            config.walletCredentials = walletCredentials;
            return this;
        }

        public MobileContextBuilder setSender(BaseSender sender) {
            config.sender = sender;
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

        Pair<String, String> didVk = getDid().createAndStoreMyDid();
        Pairwise.Me me = new Pairwise.Me(didVk.first, didVk.second);
        Endpoint endpoint = new Endpoint("ws://");
        Invitee invitee = new Invitee(this, me, endpoint);
        Pairwise pw = invitee.createConnection(invitation, "Edge agent");

        if (pw != null) {
            getPairwiseList().ensureExists(pw);
            pw = mediatorPw;
        }
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
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
