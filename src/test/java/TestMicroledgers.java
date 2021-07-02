import com.goterl.lazycode.lazysodium.LazySodium;
import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.microledgers.*;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import helpers.ConfTest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static helpers.ConfTest.getState;


public class TestMicroledgers {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testInitLedger() {
        CloudAgent agent4 = confTest.getAgent("agent4");
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
        CloudAgent agent4 = confTest.getAgent("agent4");
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
        CloudAgent agent4 = confTest.getAgent("agent4");
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
        CloudAgent agent4 = confTest.getAgent("agent4");
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

    @Test
    public void testResetUncommitted() {
        CloudAgent agent4 = confTest.getAgent("agent4");
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
            ledger.append(txns);
            int uncommittedSizeBefore = ledger.uncommittedSize();
            ledger.resetUncommitted();
            int uncommittedSizeAfter = ledger.uncommittedSize();
            Assert.assertNotEquals(uncommittedSizeAfter, uncommittedSizeBefore);
            Assert.assertEquals(1, uncommittedSizeAfter);
        } finally {
            agent4.close();
        }
    }

    @Test
    public void testGetOperations() {
        CloudAgent agent4 = confTest.getAgent("agent4");
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

            List<Transaction> txns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 4).
                            put("identifier", "2btLJAAb1S3x6hZYdVyAePjqtQYi2ZBSRGy4569RZu8h").
                            put("op", "op4")),
                    new Transaction(new JSONObject().
                            put("reqId", 5).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op5"))
            );
            ledger.append(txns);

            // 1 get_last_committed_txn
            Transaction txn = ledger.getLastCommittedTransaction();
            Assert.assertEquals(txn.optString("op"), "op3");

            // 2 get_last_txn
            txn = ledger.getLastTransaction();
            Assert.assertEquals(txn.optString("op"), "op5");

            //3 get_uncommitted_txns
            txns = ledger.getUncommittedTransactions();
            Assert.assertEquals(2, txns.size());
            //assert all(op in str(txns) for op in ['op4', 'op5']) is True
            //assert any(op in str(txns) for op in ['op1', 'op2', 'op3']) is False

            // 4 get_by_seq_no
            txn = ledger.getTransaction(1);
            Assert.assertEquals(txn.optString("op"), "op1");

            // 5 get_by_seq_no_uncommitted
            txn = ledger.getUncommittedTransaction(4);
            Assert.assertEquals(txn.optString("op"), "op4");
        } finally {
            agent4.close();
        }
    }

    @Test
    public void testReset() {
        CloudAgent agent4 = confTest.getAgent("agent4");
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

            Assert.assertEquals(5, ledger.size());
            Assert.assertTrue(agent4.getMicroledgers().isExists(ledgerName));

            agent4.getMicroledgers().reset(ledgerName);
            Assert.assertFalse(agent4.getMicroledgers().isExists(ledgerName));
        } finally {
            agent4.close();
        }
    }

    @Test
    public void testList() {
        CloudAgent agent4 = confTest.getAgent("agent4");
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

            List<LedgerMeta> collection = agent4.getMicroledgers().getList();
            boolean contains = false;
            for (LedgerMeta meta : collection) {
                if (meta.getName().equals(ledgerName)) {
                    contains = true;
                    break;
                }
            }
            Assert.assertTrue(contains);

            Assert.assertTrue(agent4.getMicroledgers().isExists(ledgerName));

            agent4.getMicroledgers().reset(ledgerName);

            collection = agent4.getMicroledgers().getList();
            contains = false;
            for (LedgerMeta meta : collection) {
                if (meta.getName().equals(ledgerName)) {
                    contains = true;
                    break;
                }
            }
            Assert.assertFalse(contains);

        } finally {
            agent4.close();
        }
    }

    @Test
    public void testGetAllTxns() {
        CloudAgent agent4 = confTest.getAgent("agent4");
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

            List<Transaction> txns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 4).
                            put("identifier", "2btLJAAb1S3x6hZYdVyAePjqtQYi2ZBSRGy4569RZu8h").
                            put("op", "op4")),
                    new Transaction(new JSONObject().
                            put("reqId", 5).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op5"))
            );
            ledger.append(txns);

            txns = ledger.getAllTransactions();
            Assert.assertEquals(3, txns.size());
        } finally {
            agent4.close();
        }
    }

    @Test
    public void testAuditProof() {
        CloudAgent agent4 = confTest.getAgent("agent4");
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
                            put("op", "op5")),
                    new Transaction(new JSONObject().
                            put("reqId", 6).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op6"))
            );
            Pair<AbstractMicroledger, List<Transaction>> createRes = agent4.getMicroledgers().create(ledgerName, genesisTxns);
            AbstractMicroledger ledger = createRes.first;

            List<Transaction> txns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 7).
                            put("identifier", "2btLJAAb1S3x6hZYdVyAePjqtQYi2ZBSRGy4569RZu8h").
                            put("op", "op7")),
                    new Transaction(new JSONObject().
                            put("reqId", 8).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op8")),
                    new Transaction(new JSONObject().
                            put("reqId", 9).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op9"))
            );
            ledger.append(txns);

            List<List<String>> auditPaths = new ArrayList<>();
            for (int seqNo : Arrays.asList(1, 2, 3, 4, 5, 6)) {
                AuditProof auditProof = ledger.getAuditProof(seqNo);
                Assert.assertEquals("3eDS4j8HgpAyRnuvfFG624KKvQBuNXKBenhqHmHtUgeq", auditProof.getRootHash());
                Assert.assertEquals(6, auditProof.getLedgerSize());
                Assert.assertFalse(auditPaths.contains(auditProof.getAuditPath()));
                auditPaths.add(auditProof.getAuditPath());
            }

            for (int seqNo : Arrays.asList(7, 8, 9)) {
                AuditProof auditProof = ledger.getAuditProof(seqNo);
                Assert.assertEquals("3eDS4j8HgpAyRnuvfFG624KKvQBuNXKBenhqHmHtUgeq", auditProof.getRootHash());
                Assert.assertEquals(6, auditProof.getLedgerSize());
                auditPaths.add(auditProof.getAuditPath());
            }

            Assert.assertEquals("Dkoca8Af15uMLBHAqbddwqmpiqsaDEtKDoFVfNRXt44g", ledger.uncommittedRootHash());
        }
        finally {
            agent4.close();
        }
    }

    @Test
    public void testLeafHash() {
        CloudAgent agent4 = confTest.getAgent("agent4");
        String ledgerName = confTest.ledgerName();
        agent4.open();
        try {
            List<Transaction> genesisTxns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 1).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op1"))
            );
            Pair<AbstractMicroledger, List<Transaction>> createRes = agent4.getMicroledgers().create(ledgerName, genesisTxns);
            AbstractMicroledger ledger = createRes.first;
            Transaction txn = createRes.second.get(0);
            byte[] leafHash = agent4.getMicroledgers().leafHash(txn);
            String leafHashHex = LazySodium.toHex(leafHash);
            Assert.assertEquals("79D9929FD1E7F16F099C26B6F44850DA044AD0FE51E92E582D9CA372F2B8B930", leafHashHex);
        } finally {
            agent4.close();
        }
    }

    @Test
    public void testRename() {
        CloudAgent agent4 = confTest.getAgent("agent4");
        String ledgerName = confTest.ledgerName();
        agent4.open();
        try {
            List<Transaction> genesisTxns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 1).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op1"))
            );
            Pair<AbstractMicroledger, List<Transaction>> createRes = agent4.getMicroledgers().create(ledgerName, genesisTxns);
            AbstractMicroledger ledger = createRes.first;

            String newName = "new_name_" + UUID.randomUUID();
            ledger.rename(newName);

            Assert.assertFalse(agent4.getMicroledgers().isExists(ledgerName));
            Assert.assertTrue(agent4.getMicroledgers().isExists(newName));
        } finally {
            agent4.close();
        }
    }

    @Test
    public void testBatchedOps() {
        CloudAgent agent4 = confTest.getAgent("agent4");
        List<String> ledgerNames = Arrays.asList("Ledger-" + UUID.randomUUID(), "Ledger-" + UUID.randomUUID());
        agent4.open();
        try {
            List<Transaction> genesisTxns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 1).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op1"))
            );
            List<Transaction> resetTxns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 2).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op2"))
            );
            List<Transaction> commitTxns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 3).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op3"))
            );
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String txnTime = df.format(new Date(System.currentTimeMillis()));

            for (String ledgerName : ledgerNames) {
                agent4.getMicroledgers().create(ledgerName, genesisTxns);
            }

            AbstractBatchedAPI batched = agent4.getMicroledgers().getBatched();
            List<AbstractMicroledger> ledgers = batched.openByLedgerNames(ledgerNames);
            try {
                Set<String> s1 = new HashSet<>();
                for (AbstractMicroledger l : ledgers) {
                    s1.add(l.name());
                }
                Assert.assertEquals(s1, new HashSet<>(ledgerNames));

                // Fetch states
                ledgers = batched.getStates();
                Map<String, JSONObject> statesBefore = new HashMap<>();
                for (AbstractMicroledger ledger : ledgers) {
                    statesBefore.put(ledger.name(), getState(ledger));
                }
                Assert.assertEquals(statesBefore.keySet(), new HashSet<>(ledgerNames));

                // Append
                ledgers = batched.append(resetTxns);
                Map<String, JSONObject> statesAfterAppend = new HashMap<>();
                for (AbstractMicroledger ledger : ledgers) {
                    statesAfterAppend.put(ledger.name(), getState(ledger));
                }
                Assert.assertEquals(statesAfterAppend.keySet(), new HashSet<>(ledgerNames));
                for (Map.Entry<String, JSONObject> e : statesAfterAppend.entrySet()) {
                    Assert.assertEquals(2,  e.getValue().optInt("uncommitted_size"));
                }

                // Reset uncommitted
                ledgers = batched.resetUncommitted();
                Map<String, JSONObject> statesAfterResetUncommitted = new HashMap<>();
                for (AbstractMicroledger ledger : ledgers) {
                    statesAfterResetUncommitted.put(ledger.name(), getState(ledger));
                }
                Assert.assertEquals(statesAfterResetUncommitted.keySet(), new HashSet<>(ledgerNames));
                for (Map.Entry<String, JSONObject> e : statesAfterResetUncommitted.entrySet()) {
                    Assert.assertEquals(1,  e.getValue().optInt("uncommitted_size"));
                }

                // Append + Commit
                batched.append(commitTxns, txnTime);
                ledgers = batched.commit();
                Map<String, JSONObject> statesAfterCommit = new HashMap<>();
                for (AbstractMicroledger ledger : ledgers) {
                    statesAfterCommit.put(ledger.name(), getState(ledger));
                }

                for (Map.Entry<String, JSONObject> e : statesAfterCommit.entrySet()) {
                    Assert.assertEquals(2,  e.getValue().optInt("uncommitted_size"));
                    Assert.assertEquals(2,  e.getValue().optInt("size"));
                }

                // Check all txns
                for (String ledgerName : ledgerNames) {
                    AbstractMicroledger ledger = agent4.getMicroledgers().getLedger(ledgerName);
                    List<Transaction> txns = ledger.getAllTransactions();
                    Assert.assertEquals(2, txns.size());
                    Assert.assertEquals("op1", txns.get(0).optString("op"));
                    Assert.assertEquals("op3", txns.get(1).optString("op"));
                    Assert.assertEquals(txnTime, txns.get(1).getTime());
                }
            } finally {
                batched.close();
            }

        } finally {
            agent4.close();
        }
    }
}
