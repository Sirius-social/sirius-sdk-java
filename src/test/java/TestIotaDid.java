import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.agent.diddoc.DidDocUtils;
import com.sirius.sdk.agent.diddoc.IotaPublicDidDoc;
import com.sirius.sdk.agent.diddoc.PublicDidDoc;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.IotaUtils;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestIotaDid {

    ConfTest confTest;

    static {
        IotaUtils.iotaNetwork = IotaUtils.TESTNET;
    }

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testOffChain() {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams issuerParams = testSuite.getAgentParams("agent1");
        Context context = CloudContext.builder().
                setServerUri(issuerParams.getServerAddress()).
                setCredentials(issuerParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
                setP2p(issuerParams.getConnection()).
                build();

        PublicDidDoc didDoc1 = new IotaPublicDidDoc(context.getCrypto());
        didDoc1.saveToWallet(context.getNonSecrets());

        PublicDidDoc didDoc2 = new IotaPublicDidDoc(context.getCrypto());
        didDoc2.saveToWallet(context.getNonSecrets());

        List<String> didList = DidDocUtils.publicDidList(context.getNonSecrets());

        Assert.assertTrue(didList.contains(didDoc1.getDid()));
        Assert.assertTrue(didList.contains(didDoc2.getDid()));

        DidDoc didDoc1fromWallet = DidDocUtils.fetchFromWallet(didDoc1.getDid(), context.getNonSecrets());
        Assert.assertEquals(didDoc1.getDid(), didDoc1fromWallet.getDid());

        DidDoc didDoc2fromWallet = DidDocUtils.fetchFromWallet(didDoc2.getDid(), context.getNonSecrets());
        Assert.assertEquals(didDoc2.getDid(), didDoc2fromWallet.getDid());

        didDoc1.addService("DIDCommMessaging", context.getEndpointWithEmptyRoutingKeys());
        didDoc1.saveToWallet(context.getNonSecrets());

        didDoc1fromWallet = DidDocUtils.fetchFromWallet(didDoc1.getDid(), context.getNonSecrets());
        Assert.assertEquals(
                didDoc1.extractService(true, "DIDCommMessaging").getString("serviceEndpoint"),
                didDoc1fromWallet.extractService(true, "DIDCommMessaging").getString("serviceEndpoint"));
    }

    @Test
    public void testOnChain() {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams issuerParams = testSuite.getAgentParams("agent1");
        Context context = CloudContext.builder().
                setServerUri(issuerParams.getServerAddress()).
                setCredentials(issuerParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
                setP2p(issuerParams.getConnection()).
                build();

        PublicDidDoc didDoc1 = new IotaPublicDidDoc(context.getCrypto());
        Assert.assertTrue(didDoc1.submitToLedger(context));

        PublicDidDoc didDoc1Resolved = DidDocUtils.resolve(didDoc1.getDid());
        Assert.assertNotNull(didDoc1Resolved);
        Assert.assertEquals(didDoc1.getDid(), didDoc1Resolved.getDid());

        didDoc1Resolved.addAgentServices(context);
        didDoc1Resolved.submitToLedger(context);

        didDoc1Resolved = DidDocUtils.resolve(didDoc1.getDid());
        Assert.assertNotNull(didDoc1Resolved);
        Assert.assertNotNull(didDoc1Resolved.extractService(true, "DIDCommMessaging"));
    }
}
