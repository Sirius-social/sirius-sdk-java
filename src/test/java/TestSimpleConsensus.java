import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.consensus.simple.messages.InitRequestLedgerMessage;
import com.sirius.sdk.agent.consensus.simple.messages.InitResponseLedgerMessage;
import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TestSimpleConsensus {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testInitLedgerMessaging() throws SiriusContextError, SiriusValidationError {
        Agent agentA = confTest.getAgent("agent1");
        Agent agentB = confTest.getAgent("agent2");
        String ledgerName = confTest.ledgerName();

        agentA.open();
        agentB.open();

        try {
            Pairwise a2b = confTest.getPairwise(agentA, agentB);
            Pairwise b2a = confTest.getPairwise(agentB, agentA);
            a2b.getMe().setDid("did:peer:" + a2b.getMe().getDid());
            b2a.getMe().setDid("did:peer:" + b2a.getMe().getDid());

            List<Transaction> genesisTxns = new ArrayList<>();
            genesisTxns.add(new Transaction(new JSONObject().
                    put("reqId", 1).
                    put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                    put("op", "op1")));

            InitRequestLedgerMessage request = InitRequestLedgerMessage.builder().
                    setParticipants(Arrays.asList(a2b.getMe().getDid(), b2a.getMe().getDid())).
                    setLedgerName(ledgerName).
                    setGenesis(genesisTxns).
                    setRootHash("xxx").
                    build();

            request.addSignature(agentA.getWallet().getCrypto(), a2b.getMe());
            request.addSignature(agentB.getWallet().getCrypto(), b2a.getMe());

            Assert.assertEquals(2, request.signatures().length());

            request.checkSignatures(agentA.getWallet().getCrypto(), a2b.getMe().getDid());
            request.checkSignatures(agentA.getWallet().getCrypto(), b2a.getMe().getDid());
            request.checkSignatures(agentA.getWallet().getCrypto());
            request.checkSignatures(agentB.getWallet().getCrypto(), a2b.getMe().getDid());
            request.checkSignatures(agentB.getWallet().getCrypto(), b2a.getMe().getDid());
            request.checkSignatures(agentB.getWallet().getCrypto());

            InitResponseLedgerMessage response = InitResponseLedgerMessage.builder().build();
            response.assignFrom(request);

            JSONObject payload1 = request.getMessageObj();
            JSONObject payload2 = response.getMessageObj();

            Assert.assertFalse(payload1.similar(payload2));

            payload1.remove("@id");
            payload1.remove("@type");
            payload2.remove("@id");
            payload2.remove("@type");

            Assert.assertTrue(payload1.similar(payload2));
        } finally {
            agentA.close();
            agentB.close();
        }
    }

    @Test
    public void testTransactionMessaging() {
        Agent agentA = confTest.getAgent("agent1");
        Agent agentB = confTest.getAgent("agent2");
        String ledgerName = confTest.ledgerName();

        agentA.open();
        agentB.open();

        try {
            Pairwise a2b = confTest.getPairwise(agentA, agentB);
            Pairwise b2a = confTest.getPairwise(agentB, agentA);
            a2b.getMe().setDid("did:peer:" + a2b.getMe().getDid());
            b2a.getMe().setDid("did:peer:" + b2a.getMe().getDid());

            List<Transaction> genesisTxns = new ArrayList<>();
            genesisTxns.add(new Transaction(new JSONObject().
                    put("reqId", 1).
                    put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                    put("op", "op1")));

            //agentA.getMicroledgers()

        } finally {
            agentA.close();
            agentB.close();
        }
    }
}
