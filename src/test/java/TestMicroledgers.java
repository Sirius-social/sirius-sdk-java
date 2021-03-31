import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.microledgers.AbstractMicroledger;
import com.sirius.sdk.agent.microledgers.MerkleInfo;
import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import helpers.ConfTest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class TestMicroledgers {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testInitLedger() {
        Agent agent4 = confTest.getAgent("agent4");
        String ledgerName = confTest.ledgerName();
        agent4.open();
        try {
            List<Transaction> genesisTxns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 1).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op1")),
                    new Transaction(new JSONObject().
                            put("reqId", 2).
                            put("identifier", "2btLJAAb1S3x6hZYdVyAePjqtQYi2ZBSRGy4569RZu8h").
                            put("op", "op2")),
                    new Transaction(new JSONObject().
                            put("reqId", 3).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op3"))
                    );
            Pair<AbstractMicroledger, List<Transaction>> createRes = agent4.getMicroledgers().create(ledgerName, genesisTxns);
            AbstractMicroledger ledger = createRes.first;
            List<Transaction> txns = createRes.second;

            Assert.assertEquals("3u8ZCezSXJq72H5CdEryyTuwAKzeZnCZyfftJVFr7y8U", ledger.rootHash());
        } finally {
            agent4.close();
        }
    }

    @Test
    public void testMerkleInfo() {
        Agent agent4 = confTest.getAgent("agent4");
        String ledgerName = confTest.ledgerName();
        agent4.open();
        try {
            List<Transaction> genesisTxns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 1).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op1")),
                    new Transaction(new JSONObject().
                            put("reqId", 2).
                            put("identifier", "2btLJAAb1S3x6hZYdVyAePjqtQYi2ZBSRGy4569RZu8h").
                            put("op", "op2")),
                    new Transaction(new JSONObject().
                            put("reqId", 3).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op3")),
                    new Transaction(new JSONObject().
                            put("reqId", 4).
                            put("identifier", "2btLJAAb1S3x6hZYdVyAePjqtQYi2ZBSRGy4569RZu8h").
                            put("op", "op4")),
                    new Transaction(new JSONObject().
                            put("reqId", 5).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op5"))
            );

            Pair<AbstractMicroledger, List<Transaction>> createRes = agent4.getMicroledgers().create(ledgerName, genesisTxns);
            AbstractMicroledger ledger = createRes.first;
            List<Transaction> txns = createRes.second;
            MerkleInfo merkleInfo = ledger.getMerkleInfo(4);
            Assert.assertEquals(merkleInfo.getRootHash(), "CwX1TRYKpejHmdnx3gMgHtSioDzhDGTASAD145kjyyRh");
            Assert.assertEquals(merkleInfo.getAuditPath(), Arrays.asList("46kxvYf7RjRERXdS56vUpFCzm2A3qRYSLaRr6tVT6tSd",
                    "3sgNJmsXpmin7P5C6jpHiqYfeWwej5L6uYdYoXTMc1XQ"));
        } finally {
            agent4.close();
        }
    }

    @Test
    public void testAppendOperations() {
        Agent agent4 = confTest.getAgent("agent4");
        String ledgerName = confTest.ledgerName();
        agent4.open();
        try {
            List<Transaction> genesisTxns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 1).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op1")));
            Pair<AbstractMicroledger, List<Transaction>> createRes = agent4.getMicroledgers().create(ledgerName, genesisTxns);
            AbstractMicroledger ledger = createRes.first;
            List<Transaction> txns = Arrays.asList(
                    new Transaction(new Transaction(new JSONObject().
                            put("reqId", 2).
                            put("identifier", "2btLJAAb1S3x6hZYdVyAePjqtQYi2ZBSRGy4569RZu8h").
                            put("op", "op2"))),
                    new Transaction(new JSONObject().
                            put("reqId", 3).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op3")));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String txnTime = df.format(new Date(System.currentTimeMillis()));
            Triple<Integer, Integer, List<Transaction>> appendRes = ledger.append(txns, txnTime);
            Assert.assertEquals(3, (int) appendRes.second);
            Assert.assertEquals(2, (int) appendRes.first);
            Assert.assertEquals(txnTime, appendRes.third.get(0).getTime());
            Assert.assertEquals(txnTime, appendRes.third.get(1).getTime());
        } finally {
            agent4.close();
        }
    }

    @Test
    public void testCommitDiscard() {
        Agent agent4 = confTest.getAgent("agent4");
        String ledgerName = confTest.ledgerName();
        agent4.open();
        try {
            List<Transaction> genesisTxns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 1).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op1")));
            Pair<AbstractMicroledger, List<Transaction>> createRes = agent4.getMicroledgers().create(ledgerName, genesisTxns);
            AbstractMicroledger ledger = createRes.first;
            List<Transaction> txns = Arrays.asList(
                    new Transaction(new Transaction(new JSONObject().
                            put("reqId", 2).
                            put("identifier", "2btLJAAb1S3x6hZYdVyAePjqtQYi2ZBSRGy4569RZu8h").
                            put("op", "op2"))),
                    new Transaction(new JSONObject().
                            put("reqId", 3).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op3")));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String txnTime = df.format(new Date(System.currentTimeMillis()));

            Assert.assertEquals(ledger.uncommittedRootHash(), ledger.rootHash());
            ledger.append(txns, txnTime);
            Assert.assertNotEquals(ledger.uncommittedRootHash(), ledger.rootHash());
            Assert.assertEquals(1, ledger.size());
            Assert.assertEquals(3, ledger.uncommittedSize());

            //commit
            ledger.commit(1);
            Assert.assertEquals(2, ledger.size());
            Assert.assertEquals(3, ledger.uncommittedSize());
            Assert.assertNotEquals(ledger.uncommittedRootHash(), ledger.rootHash());

            // discard
            ledger.discard(1);
            Assert.assertEquals(2, ledger.size());
            Assert.assertEquals(2, ledger.uncommittedSize());
            Assert.assertEquals(ledger.uncommittedRootHash(), ledger.rootHash());
        } finally {
            agent4.close();
        }
    }

}
