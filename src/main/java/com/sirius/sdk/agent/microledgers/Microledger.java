package com.sirius.sdk.agent.microledgers;

import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.connections.RemoteCallWrapper;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Microledger extends AbstractMicroledger {
    String name;
    AgentRPC api;
    JSONObject state = null;

    public Microledger(String name, AgentRPC api, JSONObject state) {
        this.name = name;
        this.api = api;
        this.state = state;
    }

    public Microledger(String name, AgentRPC api) {
        this.name = name;
        this.api = api;
    }

    public void assignTo(AbstractMicroledger other) {
        if (other instanceof Microledger) {
            ((Microledger) other).state = this.state;
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int size() {
        checkStateIsExists();
        return state.getInt("size");
    }

    @Override
    public int uncommittedSize() {
        checkStateIsExists();
        return state.getInt("uncommitted_size");
    }

    @Override
    public String rootHash() {
        checkStateIsExists();
        return state.getString("root_hash");
    }

    @Override
    public String uncommittedRootHash() {
        checkStateIsExists();
        return state.getString("uncommitted_root_hash");
    }

    @Override
    public int seqNo() {
        checkStateIsExists();
        return state.getInt("seqNo");
    }

    @Override
    public void reload() {
        JSONObject state = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/state",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name));
        this.state = state;
    }

    @Override
    public void rename(String newName) {
        new RemoteCallWrapper<Void>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/rename",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name).
                                add("new_name", newName));
    }

    @Override
    public List<Transaction> init(List<Transaction> genesis) {
        Pair<JSONObject, List<JSONObject>> res = new RemoteCallWrapper<Pair<JSONObject, List<JSONObject>>>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/rename",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name).
                                add("genesis_txns", genesis));
        List<Transaction> txns = null;
        if (res != null) {
            txns = new ArrayList<>();
            state = res.first;
            for (JSONObject txn : res.second) {
                txns.add(new Transaction(txn));
            }
        }
        return txns;
    }

    @Override
    public Triple<Integer, Integer, List<Transaction>> append(List<Transaction> transactions, String txnTime) {
        Object transactionsWithMeta = new RemoteCallWrapper<Object>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/append_txns_metadata",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name).
                                add("txns", transactions).
                                add("txn_time", txnTime));

        JSONArray appendTxns = new RemoteCallWrapper<JSONArray>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/append_txns",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name).
                                add("txns", transactionsWithMeta));

        this.state = appendTxns.getJSONObject(0);
        List<Transaction> appendedTxns = new ArrayList<>();
        JSONArray appendedTxnsJson = appendTxns.getJSONArray(3);
        for (Object o : appendedTxnsJson) {
            appendedTxns.add(new Transaction((JSONObject) o));
        }
        return new Triple<>(appendTxns.getInt(1), appendTxns.getInt(2), appendedTxns);
    }

    @Override
    public Triple<Integer, Integer, List<Transaction>> commit(int count) {
        JSONArray commitTxns = new RemoteCallWrapper<JSONArray>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/commit_txns",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name).
                                add("count", count));

        this.state = commitTxns.getJSONObject(0);
        List<Transaction> appendedTxns = new ArrayList<>();
        JSONArray commitTxnsJson = commitTxns.getJSONArray(3);
        for (Object o : commitTxnsJson) {
            appendedTxns.add(new Transaction((JSONObject) o));
        }
        return new Triple<>(commitTxns.getInt(1), commitTxns.getInt(2), appendedTxns);
    }

    @Override
    public void discard(int count) {
        new RemoteCallWrapper<Void>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/discard_txns",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name).
                                add("count", count));
    }

    @Override
    public MerkleInfo getMerkleInfo(int seqNo) {
        JSONObject merkleInfoJson = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/merkle_info",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name).
                                add("seqNo", seqNo));
        JSONArray auditPathJson = merkleInfoJson.getJSONArray("auditPath");
        List<String> auditPath = new ArrayList<>();
        for (Object o : auditPathJson) {
            auditPath.add((String) o);
        }
        return new MerkleInfo(merkleInfoJson.getString("rootHash"), auditPath);
    }

    @Override
    public AuditProof getAuditProof(int seqNo) {
        JSONObject merkleInfoJson = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/audit_proof",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name).
                                add("seqNo", seqNo));
        JSONArray auditPathJson = merkleInfoJson.getJSONArray("auditPath");
        List<String> auditPath = new ArrayList<>();
        for (Object o : auditPathJson) {
            auditPath.add((String) o);
        }
        return new AuditProof(merkleInfoJson.getString("rootHash"), auditPath, merkleInfoJson.getInt("ledgerSize"));
    }

    @Override
    public void resetUncommitted() {
        new RemoteCallWrapper<Void>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/reset_uncommitted",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name));
    }

    @Override
    public Transaction getTransaction(int seqNo) {
        JSONObject txn = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/get_by_seq_no",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name).
                                add("seqNo", seqNo));
        return new Transaction(txn);
    }

    @Override
    public Transaction getUncommittedTransaction(int seqNo) {
        JSONObject txn = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/get_by_seq_no_uncommitted",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name).
                                add("seqNo", seqNo));
        return new Transaction(txn);
    }

    @Override
    public Transaction getLastTransaction() {
        JSONObject txn = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/get_last_txn",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name));
        return new Transaction(txn);
    }

    @Override
    public Transaction getLastCommittedTransaction() {
        JSONObject txn = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/get_last_committed_txn",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name));
        return new Transaction(txn);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        JSONArray txns = new RemoteCallWrapper<JSONArray>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/get_all_txns",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name));
        List<Transaction> res = new ArrayList<>();
        for (Object o : txns) {
            res.add(new Transaction(((JSONArray) o).getJSONObject(1)));
        }
        return res;
    }

    @Override
    public List<Transaction> getUncommittedTransactions() {
        JSONArray txns = new RemoteCallWrapper<JSONArray>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/get_uncommitted_txns",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name));
        List<Transaction> res = new ArrayList<>();
        for (Object o : txns) {
            res.add(new Transaction((JSONObject) o));
        }
        return res;
    }

    private void checkStateIsExists() {
        if (state == null) {
            throw new SiriusContextError("Load state of Microledger at First!");
        }
    }
}
