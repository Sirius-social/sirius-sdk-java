import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnProtocolMessage;
import com.sirius.sdk.agent.n_wise.*;
import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.agent.n_wise.transactions.*;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.CloudHub;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.MobileHub;
import com.sirius.sdk.naclJava.LibSodium;
import com.sirius.sdk.utils.IotaUtils;
import com.sirius.sdk.utils.Pair;
import helpers.*;
import models.AgentParams;
import org.bitcoinj.core.Base58;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TestNWise {
    ConfTest confTest;

    static {
        IotaUtils.iotaNetwork = IotaUtils.TESTNET;
    }

    static final String alice = "agent1";
    static final String bob = "agent2";
    static final String carol = "agent3";

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    public Context getContext(String agentName) {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams agent = testSuite.getAgentParams(agentName);
        return CloudContext.builder().
                setServerUri(agent.getServerAddress()).
                setCredentials(agent.getCredentials().getBytes(StandardCharsets.UTF_8)).
                setP2p(agent.getConnection()).
                build();
    }

    public CloudHub.Config getCloudConfig(String agentName) {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams agent = testSuite.getAgentParams(agentName);
        CloudHub.Config config = new CloudHub.Config();
        config.serverUri = agent.getServerAddress();
        config.credentials = agent.getCredentials().getBytes(StandardCharsets.UTF_8);
        config.p2p = agent.getConnection();
        return config;
    }

    @Test
    public void testRestoreIotaChats() {
        try (Context context = getContext(alice)) {
            new NWiseList(context.getNonSecrets()).clearList();
            IotaNWise aliceChat1 = IotaNWise.createChat("chat1", "Alice", context);
            IotaNWise aliceChat2 = IotaNWise.createChat("chat2", "Alice", context);
            new NWiseList(context.getNonSecrets()).add(aliceChat1);
            new NWiseList(context.getNonSecrets()).add(aliceChat2);
        }

        try (Context context = getContext(alice)) {
            List<NWiseList.NWiseInfo> nWiseList = new NWiseList(context.getNonSecrets()).getNWiseInfoList();
            Assert.assertEquals(2, nWiseList.size());
            NWise restoredNWise1 = NWise.restore(nWiseList.get(0));
            NWise restoredNWise2 = NWise.restore(nWiseList.get(1));
            Assert.assertTrue(restoredNWise1 instanceof IotaNWise);
            Assert.assertTrue(restoredNWise2 instanceof IotaNWise);
            Assert.assertTrue(Arrays.asList("chat1", "chat2").contains(restoredNWise1.getChatName()));
            Assert.assertTrue(Arrays.asList("chat1", "chat2").contains(restoredNWise2.getChatName()));
            Assert.assertNotEquals(restoredNWise1.getChatName(), restoredNWise2.getChatName());
        }
    }

    @Test
    public void testRestoreIotaChat() {
        IotaNWise nWise;
        try (Context context = getContext(alice)) {
            new NWiseList(context.getNonSecrets()).clearList();
            nWise = IotaNWise.createChat("chat1", "Alice", context);
            new NWiseList(context.getNonSecrets()).add(nWise);
        }

        try (Context context = getContext(alice)) {
            List<NWiseList.NWiseInfo> nWiseList = new NWiseList(context.getNonSecrets()).getNWiseInfoList();
            Assert.assertEquals(1, nWiseList.size());
            NWise restoredNWise = NWise.restore(nWiseList.get(0));
            Assert.assertTrue(restoredNWise instanceof IotaNWise);
            Assert.assertEquals(nWise.getChatName(), restoredNWise.getChatName());
            Assert.assertEquals(nWise.getLedgerType(), restoredNWise.getLedgerType());
            Assert.assertEquals(nWise.getMyDid(), restoredNWise.getMyDid());
        }
    }

    @Test
    public void testNWiseStateMachine() throws SodiumException {
        Pair<String, String> aliceDidVk;
        NWiseStateMachine stateMachine = new NWiseStateMachine();
        // Alice creates n-wise
        try (Context context = getContext(alice)) {
            GenesisTx genesisTx = new GenesisTx();
            genesisTx.setCreatorNickname("Alice");
            String chatName = "Alice chat";
            genesisTx.setLabel(chatName);
            aliceDidVk = context.getDid().createAndStoreMyDid();
            genesisTx.setCreatorDidDocParams(
                    aliceDidVk.first,
                    Base58.decode(aliceDidVk.second),
                    context.getEndpointAddressWithEmptyRoutingKeys()
            );
            genesisTx.sign(context.getCrypto(), aliceDidVk.first, Base58.decode(aliceDidVk.second));
            Assert.assertTrue(stateMachine.check(genesisTx));

            stateMachine.append(genesisTx);
            Assert.assertEquals(chatName, stateMachine.getLabel());
            Assert.assertArrayEquals(Base58.decode(aliceDidVk.second), stateMachine.getGenesisCreatorVerkey());
            Assert.assertEquals(aliceDidVk.first, stateMachine.resolveDid(Base58.decode(aliceDidVk.second)));
            Assert.assertEquals(1, stateMachine.getParticipants().size());
        }

        Pair<String, String> bobDidVk;
        JSONObject bobDidDoc;
        // Bob creates his DID and DIDDoc for n-wise
        try (Context context = getContext(bob)) {
            bobDidVk = context.getDid().createAndStoreMyDid();
            bobDidDoc = ConnProtocolMessage.buildDidDoc(bobDidVk.first, bobDidVk.second, context.getEndpointAddressWithEmptyRoutingKeys());
        }

        // Alice adds Bob's DID to the n-wise
        try (Context context = getContext(alice)) {
            AddParticipantTx addBobTx = new AddParticipantTx();
            addBobTx.setNickname("Bob");
            addBobTx.setDid(bobDidVk.first);
            addBobTx.setDidDoc(bobDidDoc);
            addBobTx.sign(context.getCrypto(), aliceDidVk.first, Base58.decode(aliceDidVk.second));
            Assert.assertTrue(stateMachine.check(addBobTx));

            stateMachine.append(addBobTx);
            Assert.assertEquals(2, stateMachine.getParticipants().size());
        }

        KeyPair carolInvitationKeyPair;
        // Bob creates invitation to Carol
        try (Context context = getContext(bob)) {
            LazySodiumJava s = LibSodium.getInstance().getLazySodium();
            carolInvitationKeyPair = s.cryptoSignKeypair();
            InvitationTx invitationTx = new InvitationTx();
            invitationTx.setPublicKeys(Arrays.asList(carolInvitationKeyPair.getPublicKey().getAsBytes()));
            invitationTx.sign(context.getCrypto(), bobDidVk.first, Base58.decode(bobDidVk.second));
            Assert.assertTrue(stateMachine.check(invitationTx));
            stateMachine.append(invitationTx);
        }

        Pair<String, String> carolDidVk;
        JSONObject carolDidDoc;
        // Carol accepts invitation and adds herself to the n-wise
        try (Context context = getContext(carol)) {
            AddParticipantTx addCarolTx = new AddParticipantTx();
            addCarolTx.setNickname("Carol");
            carolDidVk = context.getDid().createAndStoreMyDid();
            carolDidDoc = ConnProtocolMessage.buildDidDoc(carolDidVk.first, carolDidVk.second, context.getEndpointAddressWithEmptyRoutingKeys());
            addCarolTx.setDid(carolDidVk.first);
            addCarolTx.setDidDoc(carolDidDoc);
            addCarolTx.sign(Base58.encode(carolInvitationKeyPair.getPublicKey().getAsBytes()), carolInvitationKeyPair.getSecretKey().getAsBytes());
            Assert.assertTrue(stateMachine.check(addCarolTx));
            stateMachine.append(addCarolTx);

            Assert.assertEquals(3, stateMachine.getParticipants().size());
            List<NWiseParticipant> participants = stateMachine.getParticipants();
            Set<String> actualDidSet = new HashSet<>();
            for (NWiseParticipant p : participants) {
                actualDidSet.add(p.did);
            }
            Set<String> expectedDidSet = Set.of(aliceDidVk.first, bobDidVk.first, carolDidVk.first);
            Assert.assertEquals(expectedDidSet, actualDidSet);

            Assert.assertFalse(stateMachine.check(addCarolTx));
        }

        // Alice transfers her ownership over n-wise to Carol
        try (Context context = getContext(alice)) {
            Assert.assertEquals(aliceDidVk.first, stateMachine.getCurrentOwner().did);
            NewOwnerTx newOwnerTx = new NewOwnerTx();
            newOwnerTx.setDid(carolDidVk.first);
            newOwnerTx.sign(context.getCrypto(), aliceDidVk.first, Base58.decode(aliceDidVk.second));
            Assert.assertTrue(stateMachine.check(newOwnerTx));
            stateMachine.append(newOwnerTx);

            Assert.assertEquals(carolDidVk.first, stateMachine.getCurrentOwner().did);
        }

        // Alice tries to remove bob from n-wise, but fails, since she is not an owner anymore
        try (Context context = getContext(alice)) {
            Assert.assertEquals(3, stateMachine.getParticipants().size());
            RemoveParticipantTx removeParticipantTx = new RemoveParticipantTx();
            removeParticipantTx.setDid(bobDidVk.first);
            removeParticipantTx.sign(context.getCrypto(), aliceDidVk.first, Base58.decode(aliceDidVk.second));
            Assert.assertFalse(stateMachine.check(removeParticipantTx));
            stateMachine.append(removeParticipantTx);
            Assert.assertEquals(3, stateMachine.getParticipants().size());
        }

        // Alice removes herself from the n-wise
        try (Context context = getContext(alice)) {
            RemoveParticipantTx removeParticipantTx = new RemoveParticipantTx();
            removeParticipantTx.setDid(aliceDidVk.first);
            removeParticipantTx.sign(context.getCrypto(), aliceDidVk.first, Base58.decode(aliceDidVk.second));
            Assert.assertTrue(stateMachine.check(removeParticipantTx));
            stateMachine.append(removeParticipantTx);
            Assert.assertEquals(2, stateMachine.getParticipants().size());
        }

        // Carol removes bob from the n-wise
        try (Context context = getContext(carol)) {
            Assert.assertEquals(2, stateMachine.getParticipants().size());
            RemoveParticipantTx removeParticipantTx = new RemoveParticipantTx();
            removeParticipantTx.setDid(bobDidVk.first);
            removeParticipantTx.sign(context.getCrypto(), carolDidVk.first, Base58.decode(carolDidVk.second));
            Assert.assertTrue(stateMachine.check(removeParticipantTx));
            stateMachine.append(removeParticipantTx);
            Assert.assertEquals(1, stateMachine.getParticipants().size());
        }
    }

    @Test
    public void testNWiseStateMachine_badGenesisSignature() {
        try (Context context = getContext(alice)) {
            NWiseStateMachine stateMachine = new NWiseStateMachine();
            GenesisTx genesisTx = new GenesisTx();
            genesisTx.setCreatorNickname("Alice");
            Pair<String, String> aliceDidVk = context.getDid().createAndStoreMyDid();
            genesisTx.setCreatorDidDocParams(
                    aliceDidVk.first,
                    Base58.decode(aliceDidVk.second),
                    context.getEndpointAddressWithEmptyRoutingKeys()
            );
            genesisTx.sign(context.getCrypto(), aliceDidVk.first, Base58.decode(aliceDidVk.second));
            String signatureValue = genesisTx.optJSONObject("proof").optString("signatureValue");
            signatureValue = new StringBuilder(signatureValue).reverse().toString();
            genesisTx.optJSONObject("proof").put("signatureValue", signatureValue);

            Assert.assertFalse(stateMachine.check(genesisTx));
        }
    }

    public MobileNWiseClient createSmartphone(String nickname) {
        JSONObject walletConfig = new JSONObject().
                put("id", UUID.randomUUID()).
                put("storage_type", "default");
        JSONObject walletCredentials = new JSONObject().
                put("key", "8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY").
                put("key_derivation_method", "RAW");

        MobileHub.Config config = new MobileHub.Config();
        config.walletConfig = walletConfig;
        config.walletCredentials = walletCredentials;
        config.mediatorInvitation = ConfTest.getMediatorInvitation();
        return new MobileNWiseClient(config, nickname);
    }

    @Test
    public void testCloudNWiseClient() {
        CloudNWiseClient aliceClient = new CloudNWiseClient(getCloudConfig(alice), "Alice");
        CloudNWiseClient bobClient = new CloudNWiseClient(getCloudConfig(bob), "Bob");
        CloudNWiseClient carolClient = new CloudNWiseClient(getCloudConfig(carol), "Carol");
        testNWiseClient(aliceClient, bobClient, carolClient);
    }

    public void testMobileNWiseClient() {
        AbstractNWiseClient aliceClient = createSmartphone("Alice");
        AbstractNWiseClient bobClient = createSmartphone("Bob");
        AbstractNWiseClient carolClient = createSmartphone("Carol");
        testNWiseClient(aliceClient, bobClient, carolClient);
    }

    public void testNWiseClient(AbstractNWiseClient alice, AbstractNWiseClient bob, AbstractNWiseClient carol) {
        alice.start();
        bob.start();
        carol.start();

        String aliceNWiseInternalId = alice.createNWise("new n-wise");
        Assert.assertNotNull(aliceNWiseInternalId);

        Invitation invitationAliceToBob = alice.createNWiseInvitation(aliceNWiseInternalId);
        Assert.assertNotNull(invitationAliceToBob);
        String bobNWiseInternalId = bob.acceptInvitation(invitationAliceToBob);
        Assert.assertNotNull(bobNWiseInternalId);

        Assert.assertTrue(alice.updateNWise(aliceNWiseInternalId));
        Assert.assertEquals(2, alice.getNWiseParticipants(aliceNWiseInternalId).size());

        Message bobToAlice = Message.builder().setContent("hello world!").build();
        bob.sendNWiseMessage(bobNWiseInternalId, bobToAlice);
        alice.getEvents().filter(e -> {
            return e.message().getId().equals(bobToAlice.getId());
        }).timeout(30, TimeUnit.SECONDS).blockingFirst();
        Assert.assertEquals(1, alice.getReceivedMessages().size());

        Invitation invitationBobToCarol = bob.createNWiseInvitation(bobNWiseInternalId);
        Assert.assertNotNull(invitationBobToCarol);
        String carolNWiseInternalId = carol.acceptInvitation(invitationBobToCarol);
        Assert.assertNotNull(carolNWiseInternalId);

        bob.getEvents().filter(e -> e.getSenderVerkey().equals(Base58.encode(carol.getMe(carolNWiseInternalId).getVerkey()))).
                timeout(30, TimeUnit.SECONDS).blockingFirst();

        Assert.assertEquals(3, bob.getNWiseParticipants(bobNWiseInternalId).size());

        String bobVkBase58 = Base58.encode(bob.getMe(bobNWiseInternalId).getVerkey());
        bob.leave(bobNWiseInternalId);

        carol.getEvents().filter(e -> e.getSenderVerkey().equals(bobVkBase58)).
                timeout(30, TimeUnit.SECONDS).blockingFirst();

        Assert.assertEquals(2, carol.getNWiseParticipants(carolNWiseInternalId).size());
    }

}
