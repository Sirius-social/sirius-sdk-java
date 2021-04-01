package com.sirius.sdk.agent.microledgers;

import com.sirius.sdk.utils.Pair;

import java.util.List;

public abstract class AbstractMicroledgerList {

    public abstract Pair<AbstractMicroledger, List<Transaction>> create(String name, List<Transaction> genesis);

    public abstract AbstractMicroledger getLedger(String name);

    public abstract void reset(String name);

    public abstract boolean isExists(String name);

    public abstract byte[] leafHash(Transaction txn);

    public abstract List<LedgerMeta> getList();

    public AbstractBatchedAPI getBatched() {
        return null;
    }
}
