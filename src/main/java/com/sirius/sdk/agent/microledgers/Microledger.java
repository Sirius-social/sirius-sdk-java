package com.sirius.sdk.agent.microledgers;

import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.connections.RemoteCallWrapper;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
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

        return null;
    }

    @Override
    public Triple<Integer, Integer, List<Transaction>> commit(int count) {
        return null;
    }

    @Override
    public void discard(int count) {

    }

    @Override
    public MerkleInfo getMerkleInfo(int seqNo) {
        return null;
    }

    @Override
    public AuditProof getAuditProof(int seqNo) {
        return null;
    }

    @Override
    public void resetUncommitted() {

    }

    @Override
    public Transaction getTransaction(int seqNo) {
        return null;
    }

    @Override
    public Transaction getUncommittedTransaction(int seqNo) {
        return null;
    }

    @Override
    public Transaction getLastTransaction() {
        return null;
    }

    @Override
    public Transaction getLastCommittedTransaction() {
        return null;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return null;
    }

    @Override
    public List<Transaction> getUncommittedTransactions() {
        return null;
    }

    private void checkStateIsExists() {
        if (state == null) {
            throw new SiriusContextError("Load state of Microledger at First!");
        }
    }
}
