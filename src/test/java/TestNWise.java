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
import com.sirius.sdk.hub.MobileHub;
import com.sirius.sdk.naclJava.LibSodium;
import com.sirius.sdk.utils.Base58;
import com.sirius.sdk.utils.IotaUtils;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import helpers.Smartphone;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import models.AgentParams;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;
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
                listener.listen().blockingSubscribe(new Consumer<Event>() {
                    @Override
                    public void accept(Event event) throws Throwable {
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
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {

                    }
                });
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

        try (Context context = getContext(bob)) {
            bobChat.leave(context);
        }
        carolChat.fetchFromLedger();
        Assert.assertEquals(2, carolChat.getParticipants().size());

        try (Context context = getContext(alice)) {
            aliceChat.removeParticipant(carolChat.getMyDid(), context);
        }
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
                listener.listen().timeout(15, TimeUnit.SECONDS).blockingSubscribe(new Observer<Event>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(Event event) {
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

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }


                });
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

        Assert.assertTrue(aliceFuture.get(30, TimeUnit.SECONDS));
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

        KeyPair carolInvitationKeyPair;
        try (Context context = getContext(alice)) {
            LazySodiumJava s = LibSodium.getInstance().getLazySodium();
            carolInvitationKeyPair = s.cryptoSignKeypair();
            InvitationTx invitationTx = new InvitationTx();
            invitationTx.setPublicKeys(Arrays.asList(carolInvitationKeyPair.getPublicKey().getAsBytes()));
            invitationTx.sign(context.getCrypto(), aliceDidVk.first, Base58.decode(aliceDidVk.second));
            Assert.assertTrue(stateMachine.check(invitationTx));
            stateMachine.append(invitationTx);
        }

        Pair<String, String> carolDidVk;
        JSONObject carolDidDoc;
        try (Context context = getContext(carol)) {
            AddParticipantTx addCarolTx = new AddParticipantTx();
            addCarolTx.setNickname("Carol");
            addCarolTx.setRole("user");
            carolDidVk = context.getDid().createAndStoreMyDid();
            carolDidDoc = ConnProtocolMessage.buildDidDoc(bobDidVk.first, bobDidVk.second, context.getEndpointAddressWithEmptyRoutingKeys());
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

    JSONObject aliceWalletConfig = new JSONObject().
            put("id", "alice6").
            put("storage_type", "default");
    JSONObject aliceWalletCredentials = new JSONObject().
            put("key", "8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY").
            put("key_derivation_method", "RAW");
    JSONObject bobWalletConfig = new JSONObject().
            put("id", "bob6").
            put("storage_type", "default");
    JSONObject bobWalletCredentials = new JSONObject().
            put("key", "8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY").
            put("key_derivation_method", "RAW");

    @Test
    public void testMobileAgent() throws InterruptedException, ExecutionException, TimeoutException {
        MobileHub.Config aliceConfig = new MobileHub.Config();
        aliceConfig.walletConfig = aliceWalletConfig;
        aliceConfig.walletCredentials = aliceWalletCredentials;
        aliceConfig.mediatorInvitation = ConfTest.getMediatorInvitation();
        Smartphone alice = new Smartphone(aliceConfig, "Alice");
        alice.start();

        MobileHub.Config bobConfig = new MobileHub.Config();
        bobConfig.walletConfig = bobWalletConfig;
        bobConfig.walletCredentials = bobWalletCredentials;
        bobConfig.mediatorInvitation = ConfTest.getMediatorInvitation();
        Smartphone bob = new Smartphone(bobConfig, "Bob");
        bob.start();

        String aliceNWiseInternalId = alice.createNWise("new n-wise");
        Assert.assertNotNull(aliceNWiseInternalId);

        FastInvitation fastInvitationAliceToBob = alice.createNWiseInvitation(aliceNWiseInternalId);
        Assert.assertNotNull(fastInvitationAliceToBob);
        String bobNWiseInternalId = bob.acceptInvitation(fastInvitationAliceToBob);
        Assert.assertNotNull(bobNWiseInternalId);

        Assert.assertTrue(alice.updateNWise(aliceNWiseInternalId));
        List<NWiseParticipant> aliceParticipants = alice.getNWiseParticipants(aliceNWiseInternalId);
        Assert.assertEquals(2, aliceParticipants.size());

        bob.sendNWiseMessage(bobNWiseInternalId, Message.builder().setContent("hello world!").build());
        alice.getMessage().get(30, TimeUnit.SECONDS);
        Assert.assertEquals(1, alice.getReceivedMessages().size());


        //alice.stop();
        //bob.stop();
    }

    //@After
    public void deleteWallet() throws Exception {
        Wallet.deleteWallet(aliceWalletConfig.toString(), aliceWalletCredentials.toString()).get();
    }
}
