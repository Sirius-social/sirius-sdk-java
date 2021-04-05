import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.consensus.simple.messages.*;
import com.sirius.sdk.agent.microledgers.AbstractMicroledger;
import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
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
    public void testTransactionMessaging() throws SiriusValidationError {
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

            Pair<AbstractMicroledger, List<Transaction>> t1 = agentA.getMicroledgers().create(ledgerName, genesisTxns);
            AbstractMicroledger ledgerForA = t1.first;
            Pair<AbstractMicroledger, List<Transaction>> t2 = agentB.getMicroledgers().create(ledgerName, genesisTxns);
            AbstractMicroledger ledgerForB = t2.first;

            List<Transaction> newTransactions = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 2).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op2")),
                    new Transaction(new JSONObject().
                            put("reqId", 3).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op3"))
            );
            Triple<Integer, Integer, List<Transaction>> t3 = ledgerForA.append(newTransactions);
            List<Transaction> newTxns = t3.third;

            // A->B
            MicroLedgerState stateA = new MicroLedgerState(ConfTest.getState(ledgerForA));
            MicroLedgerState x = MicroLedgerState.fromLedger(ledgerForA);
            Assert.assertTrue(stateA.similar(x));
            Assert.assertEquals(stateA.getHash(), x.getHash());

            ProposeTransactionsMessage propose = ProposeTransactionsMessage.builder().
                    setTransactions(newTxns).
                    setState(stateA).
                    build();
            propose.validate();

            // B -> A
            ledgerForB.append(propose.transactions());
            PreCommitTransactionsMessage preCommit = PreCommitTransactionsMessage.builder().
                    setState(new MicroLedgerState(ConfTest.getState(ledgerForA))).
                    build();
            preCommit.signState(agentB.getWallet().getCrypto(), b2a.getMe());
            preCommit.validate();
            Pair<Boolean, String> t4 = preCommit.verifyState(agentA.getWallet().getCrypto(), a2b.getTheir().getVerkey());
            Assert.assertTrue(t4.first);
            Assert.assertEquals(t4.second, stateA.getHash());

            // A -> B
            CommitTransactionsMessage commit = CommitTransactionsMessage.builder().build();
            commit.addPreCommit(a2b.getTheir().getDid(), preCommit);
            commit.validate();
            JSONObject states = commit.verifyPreCommits(agentA.getWallet().getCrypto(), stateA);
            Assert.assertTrue(states.toString().contains(a2b.getTheir().getDid()));
            Assert.assertTrue(states.toString().contains(a2b.getTheir().getVerkey()));

            // B -> A (post commit)
            PostCommitTransactionsMessage postCommit = PostCommitTransactionsMessage.builder().build();
            postCommit.addCommitSign(agentB.getWallet().getCrypto(), commit, b2a.getMe());
            postCommit.validate();
            Assert.assertTrue(postCommit.verifyCommits(agentA.getWallet().getCrypto(), commit, Arrays.asList(a2b.getTheir().getVerkey())));
        } finally {
            agentA.close();
            agentB.close();
        }
    }

    private Runnable routineOfLedgerCreator(String uri, byte[] credentials, P2PConnection p2p, Pairwise.Me me,
                                            List<String> participants, String ledgerName, List<JSONObject> genesis) {
        try (Context c = Context.builder().build()) {

        }
        return null;
    }

    @Test
    public void testSimpleConsensusInitLedger() {
        Agent agentA = confTest.getAgent("agent1");
        Agent agentB = confTest.getAgent("agent2");
        Agent agentC = confTest.getAgent("agent3");
        String ledgerName = confTest.ledgerName();

        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams aParams = testSuite.getAgentParams("agent1");
        AgentParams bParams = testSuite.getAgentParams("agent2");
        AgentParams cParams = testSuite.getAgentParams("agent3");

        agentA.open();
        agentB.open();
        agentC.open();
        try {
            Pairwise a2b = confTest.getPairwise(agentA, agentB);
            Pairwise a2c = confTest.getPairwise(agentA, agentC);
            Assert.assertEquals(a2b.getMe(), a2c.getMe());
            Pairwise b2a = confTest.getPairwise(agentB, agentA);
            Pairwise b2c = confTest.getPairwise(agentB, agentC);
            Assert.assertEquals(b2a.getMe(), b2c.getMe());
            Pairwise c2a = confTest.getPairwise(agentC, agentA);
            Pairwise c2b = confTest.getPairwise(agentC, agentB);
            Assert.assertEquals(c2a.getMe(), c2b.getMe());

            List<String> participants = Arrays.asList(a2b.getMe().getDid(), a2b.getTheir().getDid(), a2c.getTheir().getDid());
            List<Transaction> genesis = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 1).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op1")),
                    new Transaction(new JSONObject().
                            put("reqId", 2).
                            put("identifier", "2btLJAAb1S3x6hZYdVyAePjqtQYi2ZBSRGy4569RZu8h").
                            put("op", "op2"))
            );


        } finally {
            agentA.close();
            agentB.close();
            agentC.close();
        }
    }
}
