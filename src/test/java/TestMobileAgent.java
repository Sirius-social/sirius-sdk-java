//import com.sirius.sdk.agent.CloudAgent;
//import com.sirius.sdk.agent.MobileAgent;
//import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
//import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
//import com.sirius.sdk.agent.connections.Endpoint;
//import com.sirius.sdk.agent.listener.Event;
//import com.sirius.sdk.agent.listener.Listener;
//import com.sirius.sdk.agent.model.Entity;
//import com.sirius.sdk.agent.pairwise.Pairwise;
//import com.sirius.sdk.hub.CloudContext;
//import com.sirius.sdk.hub.Context;
//import com.sirius.sdk.hub.MobileContext;
//import com.sirius.sdk.utils.Pair;
//import helpers.ConfTest;
//import helpers.ServerTestSuite;
//import models.AgentParams;
//import org.json.JSONObject;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//public class TestMobileAgent {
//
//    ConfTest confTest;
//
//    JSONObject walletConfig = new JSONObject().
//            put("id", "Wallet1").
//            put("storage_type", "default");
//    JSONObject walletCredentials = new JSONObject().
//            put("key", "8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY").
//            put("key_derivation_method", "RAW");
//
//    @Before
//    public void configureTest() {
//        confTest = ConfTest.newInstance();
//    }
//
//    @Test
//    public void testSendMessage() throws InterruptedException, ExecutionException, TimeoutException {
//        ServerTestSuite testSuite = confTest.getSuiteSingleton();
//
//        CloudAgent cloudAgent = confTest.getAgent("agent1");
//        cloudAgent.open();
//        AgentParams cloudAgentParams = testSuite.getAgentParams("agent1");
//        List<Entity> entityList = cloudAgentParams.getEntitiesList();
//        Entity entity = entityList.get(0);
//
//        String cloudAgentEndpoint = "";
//        for (Endpoint e : cloudAgent.getEndpoints()) {
//            if (e.getRoutingKeys().size() == 0) {
//                cloudAgentEndpoint = e.getAddress();
//                break;
//            }
//        }
//
//        MobileAgent mobileAgent = new MobileAgent(walletConfig, walletCredentials);
//        mobileAgent.open();
//
//        Message msg = Message.builder().
//                setContent("Test").
//                setLocale("en").
//                build();
//
//        Listener cloudListener = cloudAgent.subscribe();
//        CompletableFuture<Event> cf = cloudListener.getOne();
//
//        mobileAgent.sendMessage(msg, Arrays.asList(entity.getVerkey()), cloudAgentEndpoint, null, new ArrayList<>());
//
//        Event event = cf.get(30, TimeUnit.SECONDS);
//        Assert.assertTrue(event.message() instanceof Message);
//        Assert.assertEquals(((Message) event.message()).getContent(), msg.getContent());
//
//        cloudAgent.close();
//    }
//
//    @Test
//    public void testListener() throws ExecutionException, InterruptedException, TimeoutException {
//        MobileAgent mobileAgent = new MobileAgent(walletConfig, walletCredentials);
//        mobileAgent.open();
//
//        Message msg = Message.builder().
//                setContent("Test").
//                setLocale("en").
//                build();
//
//        String myVerkey = mobileAgent.getWallet().getCrypto().createKey();
//        byte[] bytesMsg = mobileAgent.packMessage(msg, myVerkey);
//
//        Listener listener = mobileAgent.subscribe();
//        CompletableFuture<Event> cf = listener.getOne();
//
//        mobileAgent.receiveMsg(bytesMsg);
//
//        Event event = cf.get(60, TimeUnit.SECONDS);
//
//        Assert.assertEquals(myVerkey, event.getRecipientVerkey());
//    }
//
//    @Test
//    public void test0160MobileToCloud() throws InterruptedException, ExecutionException, TimeoutException {
//        ServerTestSuite testSuite = confTest.getSuiteSingleton();
//        AgentParams inviterParams = testSuite.getAgentParams("agent1");
//
//        // Get endpoints
//        String connectionKey = null;
//        Invitation invitation = null;
//        Pairwise.Me inviterMe = null;
//        try (Context context = CloudContext.builder().
//                setServerUri(inviterParams.getServerAddress()).
//                setCredentials(inviterParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
//                setP2p(inviterParams.getConnection()).
//                build()) {
//            String inviterEndpointAddress = context.getEndpointAddressWithEmptyRoutingKeys();
//            connectionKey = context.getCrypto().createKey();
//            invitation = Invitation.builder().
//                    setLabel("Inviter").
//                    setEndpoint(inviterEndpointAddress).
//                    setRecipientKeys(Collections.singletonList(connectionKey)).
//                    build();
//
//            Pair<String, String> didVerkey = context.getDid().createAndStoreMyDid();
//            inviterMe = new Pairwise.Me(didVerkey.first, didVerkey.second);
//        }
//
//        String finalConnectionKey = connectionKey;
//        Pairwise.Me finalInviterMe = inviterMe;
//        CompletableFuture<Boolean> runInviterFeature = CompletableFuture.supplyAsync(() -> {
//            try (Context context = CloudContext.builder().
//                    setServerUri(inviterParams.getServerAddress()).
//                    setCredentials(inviterParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
//                    setP2p(inviterParams.getConnection()).
//                    build()) {
//                TestAriesFeature0160.runInviter(context, finalConnectionKey, finalInviterMe);
//            }
//            return true;
//        }, r -> new Thread(r).start());
//
//        Pairwise.Me inviteeMe = null;
//        try (Context context = MobileContext.builder().
//                setWalletConfig(walletConfig).
//                setWalletCredentials(walletCredentials).
//                build()) {
//            Pair<String, String> didVerkey = context.getDid().createAndStoreMyDid();
//            inviteeMe = new Pairwise.Me(didVerkey.first, didVerkey.second);
//        }
//
//        Invitation finalInvitation = invitation;
//        Pairwise.Me finalInviteeMe = inviteeMe;
//        CompletableFuture<Boolean> runInviteeFeature = CompletableFuture.supplyAsync(() -> {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                Assert.fail();
//            }
//            try (Context context = MobileContext.builder().
//                    setWalletConfig(walletConfig).
//                    setWalletCredentials(walletCredentials).
//                    build()) {
//                TestAriesFeature0160.runInvitee(context, finalInvitation, "Invitee", finalInviteeMe);
//            }
//            return true;
//        }, r -> new Thread(r).start());
//
//        runInviterFeature.get(60, TimeUnit.SECONDS);
//        runInviteeFeature.get(60, TimeUnit.SECONDS);
//    }
//}
