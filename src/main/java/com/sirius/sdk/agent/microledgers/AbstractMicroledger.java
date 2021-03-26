package com.sirius.sdk.agent.microledgers;

import com.sirius.sdk.utils.Triple;

import java.util.List;

public abstract class AbstractMicroledger {

    public abstract String name();

    public abstract int size();

    public abstract int uncommittedSize();

    public abstract String rootHash();

    public abstract String uncommittedRootHash();

    public abstract int seqNo();

    public abstract void reload();

    public abstract void rename(String newName);

    public abstract List<Transaction> init(List<Transaction> genesis);

    public abstract Triple<Integer, Integer, List<Transaction>> append(List<Transaction> transactions, String txnTime);

    public abstract Triple<Integer, Integer, List<Transaction>> commit(int count);

    public abstract void discard(int count);

    public abstract MerkleInfo getMerkleInfo(int seqNo);

    public abstract AuditProof getAuditProof(int seqNo);

    public abstract void resetUncommitted();

    public abstract Transaction getTransaction(int seqNo);

    public abstract Transaction getUncommittedTransaction(int seqNo);

    public abstract Transaction getLastTransaction();

    public abstract Transaction getLastCommittedTransaction();

    public abstract List<Transaction> getAllTransactions();

    public abstract List<Transaction> getUncommittedTransactions();
}
