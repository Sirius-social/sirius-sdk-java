import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnProtocolMessage;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.n_wise.*;
import com.sirius.sdk.agent.n_wise.messages.FastInvitation;
import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.agent.n_wise.messages.Request;
import com.sirius.sdk.agent.n_wise.transactions.AddParticipantTx;
import com.sirius.sdk.agent.n_wise.transactions.GenesisTx;
import com.sirius.sdk.agent.n_wise.transactions.InvitationTx;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.MobileContext;
import com.sirius.sdk.naclJava.LibSodium;
import com.sirius.sdk.utils.Base58;
import com.sirius.sdk.utils.IotaUtils;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    @Test
    public void testIotaChat() {
        String chatName = "test chat";

        Invitation invitationForBob;
        Invitation invitationForCarol;

        IotaNWise aliceChat = null;
        try (Context context = getContext(alice)) {
            aliceChat = IotaNWise.createChat(chatName, "Alice", context);
            String internalId = new NWiseList(context.getNonSecrets()).add(aliceChat);
            invitationForBob = aliceChat.createInvitation(context);
            new NWiseList(context.getNonSecrets()).addInvitationKey(internalId, invitationForBob.getInviterVerkey());
            invitationForCarol = aliceChat.createInvitation(context);
            new NWiseList(context.getNonSecrets()).addInvitationKey(internalId, invitationForCarol.getInviterVerkey());
        }

        IotaNWise finalAliceChat = aliceChat;
        Thread aliceThread = new Thread(() -> {
            Listener listener = null;
            try (Context context = getContext(alice)) {
                listener = context.subscribe();
                System.out.println("Start listening...");
                for (int i = 0; i < 3; i++) {
                    Event event = listener.getOne().get(30, TimeUnit.SECONDS);
                    System.out.println("Event:" + event.message());
                    if (finalAliceChat.getCurrentParticipantsVerkeysBase58().contains(event.getSenderVerkey())) {
                        if (event.message() instanceof Message) {
                            Message message = (Message) event.message();
                            finalAliceChat.fetchFromLedger();
                            String nick = finalAliceChat.resolveNickname(event.getSenderVerkey());
                            Assert.assertEquals("Carol", nick);
                            System.out.println("New message from " + nick + " : " + message.getContent());
                        }
                    }
                    if (event.message() instanceof Request) {
                        Assert.assertTrue(finalAliceChat.acceptRequest((Request) event.message(), context));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                listener.unsubscribe();
            }
        });
        aliceThread.start();

        IotaNWise bobChat = null;
        try (Context context = getContext(bob)) {
            bobChat = IotaNWise.acceptInvitation(invitationForBob, "Bob", context);
            Assert.assertNotNull(bobChat);
        }

        IotaNWise carolChat = null;
        try (Context context = getContext(carol)) {
            carolChat = IotaNWise.acceptInvitation(invitationForCarol, "Carol", context);
            Assert.assertNotNull(carolChat);
            carolChat.send(Message.builder().setContent("Hello world").build(), context);
        }

        Assert.assertEquals(3, aliceChat.getParticipants().size());
        Assert.assertEquals(3, bobChat.getParticipants().size());
        Assert.assertEquals(3, carolChat.getParticipants().size());

        Assert.assertEquals(chatName, aliceChat.getChatName());
        Assert.assertEquals(chatName, bobChat.getChatName());
        Assert.assertEquals(chatName, carolChat.getChatName());

        bobChat.leave();
        carolChat.fetchFromLedger();
        Assert.assertEquals(2, carolChat.getParticipants().size());

        aliceChat.removeParticipant(carolChat.getMyDid());
        Assert.assertEquals(1, aliceChat.getParticipants().size());

        aliceThread.interrupt();
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
    public void testNWiseManager() throws ExecutionException, InterruptedException, TimeoutException {
        try (Context context = getContext(alice)) {
            new NWiseList(context.getNonSecrets()).clearList();
        }

        try (Context context = getContext(bob)) {
            new NWiseList(context.getNonSecrets()).clearList();
        }


        String nWise1AliceInternalId;
        Invitation invitationForBob;
        try (Context context = getContext(alice)) {
            nWise1AliceInternalId = context.getNWiseManager().create("NWise1", "Alice");
            invitationForBob = context.getNWiseManager().createPrivateInvitation(nWise1AliceInternalId);
        }

        CompletableFuture<Boolean> aliceFuture = CompletableFuture.supplyAsync(() -> {
            Listener listener = null;
            try (Context context = getContext(alice)) {
                listener = context.subscribe();
                System.out.println("Start listening...");
                for (int i = 0; i < 2; i++) {
                    Event event = listener.getOne().get(30, TimeUnit.SECONDS);
                    System.out.println("Event:" + event.message());
                    if (event.message() instanceof Request) {
                        context.getNWiseManager().acceptRequest((Request) event.message(), event.getRecipientVerkey());
                    } else if (event.message() instanceof Message) {
                        String nWiseId = context.getNWiseManager().resolveNWiseId(event.getSenderVerkey());
                        Assert.assertEquals(nWise1AliceInternalId, nWiseId);
                        String nick = context.getNWiseManager().resolveParticipant(event.getSenderVerkey()).nickname;
                        Assert.assertEquals("Bob", nick);
                        Message message = (Message) event.message();
                        System.out.println("New message from " + nick + " : " + message.getContent());
                    } else {
                        Assert.fail("Unexpected message to Alice");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                listener.unsubscribe();
            }
            return true;
        });


        try (Context context = getContext(bob)) {
            String internalId = context.getNWiseManager().acceptInvitation(invitationForBob, "Bob");
            Assert.assertNotNull(internalId);
            Assert.assertTrue(context.getNWiseManager().send(internalId, Message.builder().setContent("Hello world").build()));
        }

        Assert.assertTrue(aliceFuture.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void testNWiseStateMachine() throws SodiumException {
        Pair<String, String> aliceDidVk;
        NWiseStateMachine stateMachine = new NWiseStateMachine();
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
        try (Context context = getContext(bob)) {
            bobDidVk = context.getDid().createAndStoreMyDid();
            bobDidDoc = ConnProtocolMessage.buildDidDoc(bobDidVk.first, bobDidVk.second, context.getEndpointAddressWithEmptyRoutingKeys());
        }

        try (Context context = getContext(alice)) {
            AddParticipantTx addBobTx = new AddParticipantTx();
            addBobTx.setNickname("Bob");
            addBobTx.setDid(bobDidVk.first);
            addBobTx.setDidDoc(bobDidDoc);
            addBobTx.setRole("user");
            addBobTx.sign(context.getCrypto(), aliceDidVk.first, Base58.decode(aliceDidVk.second));
            Assert.assertTrue(stateMachine.check(addBobTx));

            stateMachine.append(addBobTx);
            Assert.assertEquals(2, stateMachine.getParticipants().size());
        }

        try (Context context = getContext(alice)) {
            LazySodiumJava s = LibSodium.getInstance().getLazySodium();
            KeyPair keyPair = s.cryptoSignKeypair();
            InvitationTx invitationTx = new InvitationTx();
            invitationTx.setPublicKeys(Arrays.asList(keyPair.getPublicKey().getAsBytes()));
            invitationTx.sign(context.getCrypto(), aliceDidVk.first, Base58.decode(aliceDidVk.second));
            Assert.assertTrue(stateMachine.check(invitationTx));
            stateMachine.append(invitationTx);
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

    @Test
    public void testMobileAgent() {
        JSONObject walletConfig = new JSONObject().
                put("id", "Wallet9").
                put("storage_type", "default");
        JSONObject walletCredentials = new JSONObject().
                put("key", "8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY").
                put("key_derivation_method", "RAW");

        try (Context context = MobileContext.builder().
                setWalletConfig(walletConfig).
                setWalletCredentials(walletCredentials).
                setMediatorInvitation(ConfTest.getMediatorInvitation()).
                build()) {
            String internalId = context.getNWiseManager().create("nwise", "Alice");
            context.getNWiseManager().createFastInvitation(internalId);

        }
    }
}
