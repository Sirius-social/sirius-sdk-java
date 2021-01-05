package helpers;

import com.goterl.lazycode.lazysodium.LazySodium;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.encryption.Custom;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.SiriusCryptoError;
import com.sirius.sdk.rpc.AddressedTunnel;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.StringUtils;
import models.AgentParams;
import models.P2PModel;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
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
}

/*



@pytest.fixture()
def p2p() -> dict:
        keys_agent = create_keypair(b'000000000000000000000000000AGENT')
        keys_sdk = create_keypair(b'00000000000000000000000000000SDK')
        agent = P2PConnection(
        my_keys=(
        bytes_to_b58(keys_agent[0]),
        bytes_to_b58(keys_agent[1])
        ),
        their_verkey=bytes_to_b58(keys_sdk[0])
        )
        smart_contract = P2PConnection(
        my_keys=(
        bytes_to_b58(keys_sdk[0]),
        bytes_to_b58(keys_sdk[1])
        ),
        their_verkey=bytes_to_b58(keys_agent[0])
        )
        downstream = InMemoryChannel()
        upstream = InMemoryChannel()
        return {
        'agent': {
        'p2p': agent,
        'tunnel': AddressedTunnel('memory://agent->sdk', upstream, downstream, agent)
        },
        'sdk': {
        'p2p': smart_contract,
        'tunnel': AddressedTunnel('memory://sdk->agent', downstream, upstream, smart_contract)
        }
        }


        def get_suite_singleton() -> ServerTestSuite:
        global SERVER_SUITE
        if not isinstance(SERVER_SUITE, ServerTestSuite):
        suite = ServerTestSuite()
        asyncio.get_event_loop().run_until_complete(suite.ensure_is_alive())
        SERVER_SUITE = suite
        return SERVER_SUITE


        def get_indy_agent_singleton() -> IndyAgent:
        global INDY_AGENT
        if not isinstance(INDY_AGENT, IndyAgent):
        agent = IndyAgent()
        asyncio.get_event_loop().run_until_complete(agent.ensure_is_alive())
        INDY_AGENT = agent
        return INDY_AGENT


        def get_agent(name: str) -> Agent:
        params = get_suite_singleton().get_agent_params(name)
        agent = Agent(
        server_address=params['server_address'],
        credentials=params['credentials'],
        p2p=params['p2p'],
        timeout=30,
        name=name
        )
        return agent


@pytest.fixture()
def test_suite() -> ServerTestSuite:
        return get_suite_singleton()


@pytest.fixture()
def indy_agent() -> IndyAgent:
        return get_indy_agent_singleton()


@pytest.fixture()
def agent1() -> Agent:
        return get_agent('agent1')


@pytest.fixture()
def agent2() -> Agent:
        return get_agent('agent2')


@pytest.fixture()
def agent3() -> Agent:
        return get_agent('agent3')


@pytest.fixture()
def agent4() -> Agent:
        return get_agent('agent4')


@pytest.fixture()
def A() -> Agent:
        return get_agent('agent1')


@pytest.fixture()
def B() -> Agent:
        return get_agent('agent2')


@pytest.fixture()
def C() -> Agent:
        return get_agent('agent3')


@pytest.fixture()
def D() -> Agent:
        return get_agent('agent4')


@pytest.fixture()
def ledger_name() -> str:
        return 'Ledger-' + uuid.uuid4().hex


        async def get_pairwise(me: Agent, their: Agent):
        suite = get_suite_singleton()
        me_params = suite.get_agent_params(me.name)
        their_params = suite.get_agent_params(their.name)
        me_label, me_entity = list(me_params['entities'].keys())[0], list(me_params['entities'].items())[0][1]
        their_label, their_entity = list(their_params['entities'].keys())[0], list(their_params['entities'].items())[0][1]
        me_endpoint_address = [e for e in me.endpoints if e.routing_keys == []][0].address
        their_endpoint_address = [e for e in their.endpoints if e.routing_keys == []][0].address
        self = me
        for agent, entity_me, entity_their, label_their, endpoint_their in [
        (me, me_entity, their_entity, their_label, their_endpoint_address),
        (their, their_entity, me_entity, me_label, me_endpoint_address)
        ]:
        pairwise = await agent.pairwise_list.load_for_did(their_did=their_entity['did'])
        is_filled = pairwise and pairwise.metadata
        if not is_filled:
        me_ = Pairwise.Me(entity_me['did'], entity_me['verkey'])
        their_ = Pairwise.Their(entity_their['did'], their_label, endpoint_their, entity_their['verkey'])
        metadata = {
        'me': {
        'did': entity_me['did'],
        'verkey': entity_me['verkey'],
        'did_doc': None
        },
        'their': {
        'did': entity_their['did'],
        'verkey': entity_their['verkey'],
        'label': label_their,
        'endpoint': {
        'address': endpoint_their,
        'routing_keys': []
        },
        'did_doc': None
        }
        }
        pairwise = Pairwise(me=me_, their=their_, metadata=metadata)
        await agent.wallet.did.store_their_did(entity_their['did'], entity_their['verkey'])
        await agent.pairwise_list.ensure_exists(pairwise)
        pairwise = await self.pairwise_list.load_for_did(their_did=their_entity['did'])
        return pairwise
*/
