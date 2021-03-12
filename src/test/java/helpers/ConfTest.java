package helpers;

import com.goterl.lazycode.lazysodium.LazySodium;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.model.Entity;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.encryption.Custom;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.SiriusCryptoError;
import com.sirius.sdk.rpc.AddressedTunnel;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.StringUtils;
import models.AgentParams;
import models.P2PModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class ConfTest {

    String test_suite_baseurl;
    String test_suite_overlay_address;
    String old_agent_address;
    String old_agent_overlay_address;
    String old_agent_root;
    Custom custom = new Custom();
    private static ConfTest instance;
    public static ConfTest newInstance() {
        ConfTest confTest = new ConfTest();
        confTest.configureTestEnv();
        return confTest;
    }

    public static String proverMasterSecretName = "prover_master_secret_name";

    public static ConfTest getSingletonInstance() {
        if(instance==null){
            instance = newInstance();
        }
        return instance;
    }

    public void configureTestEnv() {
        test_suite_baseurl = System.getenv("TEST_SUITE_BASE_URL");
        if (test_suite_baseurl == null || test_suite_baseurl.isEmpty()) {
            test_suite_baseurl = "http://localhost";
        }
        test_suite_overlay_address = "http://10.0.0.90";

        old_agent_address = System.getenv("INDY_AGENT_BASE_URL");
        if (old_agent_address == null || old_agent_address.isEmpty()) {
            old_agent_address = "http://127.0.0.1:88";
        }
        old_agent_overlay_address = "http://10.0.0.52:8888";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", "root");
        jsonObject.put("password", "root");
        old_agent_root = jsonObject.toString();
    }

    public Pair<P2PModel, P2PModel> createP2P() {
        try {
            KeyPair keysAgent = custom.createKeypair("000000000000000000000000000AGENT".getBytes(StandardCharsets.US_ASCII));
            KeyPair keysSdk = custom.createKeypair("00000000000000000000000000000SDK".getBytes(StandardCharsets.US_ASCII));

            P2PConnection agent = new P2PConnection(
                    StringUtils.bytesToBase58String(keysAgent.getPublicKey().getAsBytes()),
                    StringUtils.bytesToBase58String(keysAgent.getSecretKey().getAsBytes()),
                    StringUtils.bytesToBase58String(keysSdk.getPublicKey().getAsBytes()));

            P2PConnection smartContract = new P2PConnection(
                    StringUtils.bytesToBase58String(keysSdk.getPublicKey().getAsBytes()),
                    StringUtils.bytesToBase58String(keysSdk.getSecretKey().getAsBytes()),
                    StringUtils.bytesToBase58String(keysAgent.getPublicKey().getAsBytes()));

            InMemoryChannel downstream = new InMemoryChannel();
            InMemoryChannel upstream = new InMemoryChannel();
            AddressedTunnel agentTunnel = new AddressedTunnel("memory://agent->sdk",upstream,downstream,agent);
            AddressedTunnel sdkTunnel = new AddressedTunnel("memory://sdk->agent",downstream,upstream,smartContract);
            P2PModel agentModel = new P2PModel(agent,agentTunnel);
            P2PModel sdkModel = new P2PModel(smartContract,sdkTunnel);
            return new Pair<>(agentModel,sdkModel);
        } catch (SiriusCryptoError siriusCryptoError) {
            siriusCryptoError.printStackTrace();
        } catch (SodiumException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ServerTestSuite getSuiteSingleton() {
        ServerTestSuite serverTestSuite = ServerTestSuite.newInstance();
        serverTestSuite.ensureIsAlive();
        return serverTestSuite;
    }

    public IndyAgent getIndyAgentSingleton() {
        return new IndyAgent();
    }

    public Agent getAgent(String name) {
        AgentParams params = getSuiteSingleton().getAgentParams(name);
        Agent agent = new Agent(params.getServerAddress(), params.getCredentials().getBytes(StandardCharsets.US_ASCII),
                params.getConnection(), 60, null, name);
        return agent;
    }

    public ServerTestSuite testSuite() {
        return getSuiteSingleton();
    }

    public IndyAgent indyAgent() {
        return getIndyAgentSingleton();
    }

    public Agent agent1() {
        return getAgent("agent1");
    }

    public Agent agent2() {
        return getAgent("agent2");
    }

    public Agent agent3() {
        return getAgent("agent3");
    }

    public Agent agent4() {
        return getAgent("agent4");
    }

    public Agent A() {
        return getAgent("agent1");
    }

    public Agent B() {
        return getAgent("agent2");
    }

    public Agent C() {
        return getAgent("agent3");
    }

    public Agent D() {
        return getAgent("agent4");
    }

    public String ledgerName() {
        return "Ledger-" + LazySodium.toHex(UUID.randomUUID().toString().getBytes(StandardCharsets.US_ASCII));
    }

    public String defaultNetwork() {
        return "default";
    }

    public Pairwise getPairwise(Agent me, Agent their) {
        ServerTestSuite suite = getSuiteSingleton();
        AgentParams myParams = suite.getAgentParams(me.getName());
        AgentParams theirParams = suite.getAgentParams(their.getName());
        Entity myEntity = myParams.getEntitiesList().get(0);
        Entity theirEntity = theirParams.getEntitiesList().get(0);
        String myEndpointAddress = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(me);
        String theirEndpointAddress = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(their);

        {
            Pairwise pairwise = me.getPairwiseList().loadForDid(theirEntity.getDid());
            boolean isFilled = (pairwise != null) && (pairwise.getMetadata() != null);
            if (!isFilled) {
                Pairwise.Me me_ = new Pairwise.Me(myEntity.getDid(), myEntity.getVerkey());
                Pairwise.Their their_ = new Pairwise.Their(theirEntity.getDid(), theirEntity.getLabel(), theirEndpointAddress, theirEntity.getVerkey(), new ArrayList<String>());

                JSONObject metadata = (new JSONObject()).
                        put("me", (new JSONObject()).
                                put("did", myEntity.getDid()).
                                put("verkey", myEntity.getVerkey())).
                        put("their", (new JSONObject()).
                                put("did", theirEntity.getDid()).
                                put("verkey", theirEntity.getVerkey()).
                                put("label", theirEntity.getLabel()).
                                put("endpoint", (new JSONObject()).
                                        put("address", theirEndpointAddress).
                                        put("routing_keys", new JSONArray())));

                pairwise = new Pairwise(me_, their_, metadata);
                me.getWallet().getDid().storeTheirDid(theirEntity.getDid(), theirEntity.getVerkey());
                me.getPairwiseList().ensureExists(pairwise);
            }
        }

        {
            Pairwise pairwise = their.getPairwiseList().loadForDid(theirEntity.getDid());
            boolean isFilled = (pairwise != null) && (pairwise.getMetadata() != null);
            if (!isFilled) {
                Pairwise.Me me_ = new Pairwise.Me(theirEntity.getDid(), theirEntity.getVerkey());
                Pairwise.Their their_ = new Pairwise.Their(myEntity.getDid(), myEntity.getLabel(), myEndpointAddress, myEntity.getVerkey(), new ArrayList<String>());

                JSONObject metadata = (new JSONObject()).
                        put("me", (new JSONObject()).
                                put("did", theirEntity.getDid()).
                                put("verkey", theirEntity.getVerkey())).
                        put("their", (new JSONObject()).
                                put("did", myEntity.getDid()).
                                put("verkey", myEntity.getVerkey()).
                                put("label", myEntity.getLabel()).
                                put("endpoint", (new JSONObject()).
                                        put("address", myEndpointAddress).
                                        put("routing_keys", new JSONArray())));

                pairwise = new Pairwise(me_, their_, metadata);
                their.getWallet().getDid().storeTheirDid(myEntity.getDid(), myEntity.getVerkey());
                their.getPairwiseList().ensureExists(pairwise);
            }
        }

        return me.getPairwiseList().loadForDid(theirEntity.getDid());
    }
}
