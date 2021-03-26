package com.sirius.sdk.agent.microledgers;

import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.utils.Triple;
import org.json.JSONObject;

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
        return 0;
    }

    @Override
    public int uncommittedSize() {
        return 0;
    }

    @Override
    public String rootHash() {
        return null;
    }

    @Override
    public String uncommittedRootHash() {
        return null;
    }

    @Override
    public int seqNo() {
        return 0;
    }

    @Override
    public void reload() {

    }

    @Override
    public void rename(String newName) {

    }

    @Override
    public List<Transaction> init(List<Transaction> genesis) {
        return null;
    }

    @Override
    public Triple<Integer, Integer, List<Transaction>> append(List<Transaction> transactions, String txnTime) {
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
}
