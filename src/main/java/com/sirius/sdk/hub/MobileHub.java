package com.sirius.sdk.hub;

import com.sirius.sdk.agent.AbstractAgent;
import com.sirius.sdk.agent.BaseSender;
import com.sirius.sdk.agent.MobileAgent;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.connections.Endpoint;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MobileHub extends AbstractHub {

    public static class Config extends AbstractHub.Config {
        public JSONObject walletConfig = null;
        public JSONObject walletCredentials = null;
        public Invitation mediatorInvitation = null;
        public String indyEndpoint = null;
        public String serverUri = null;
        public BaseSender sender = null;
    }
    public String serverUri = null;
    public MobileHub(MobileHub.Config config) {
        this.config = config;
        this.serverUri = ((Config) config).serverUri;
        createAgentInstance();
    }

    @Override
    public MobileAgent getAgent() {
        return (MobileAgent)super.getAgent();
    }

    @Override
    void createAgentInstance() {
        Config mobileConfig = (Config) config;
        agent = new MobileAgent(
                mobileConfig.walletConfig,
                mobileConfig.walletCredentials);
        List<Endpoint> points = new ArrayList<>();
        if(((Config) config).indyEndpoint != null && !((Config) config).indyEndpoint.isEmpty()){
            points.add(new Endpoint(((Config) config).indyEndpoint, new ArrayList<>(), true));
        }
        getAgent().setEndpoints(points);
        getAgent().setSender(((Config) config).sender);
        getAgent().create();
        getAgent().open();
    }
}
