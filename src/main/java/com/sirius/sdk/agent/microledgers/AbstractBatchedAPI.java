package com.sirius.sdk.agent.microledgers;

import java.util.List;

public abstract class AbstractBatchedAPI {

    public abstract List<AbstractMicroledger> open(List<AbstractMicroledger> ledgers);

    public abstract void close();

    public abstract List<AbstractMicroledger> getStates();

    public abstract List<AbstractMicroledger> append(List<Transaction> transactions, String txnTime);

    public List<AbstractMicroledger> append(List<Transaction> transactions) {
        return append(transactions, null);
    }

    public abstract List<AbstractMicroledger> commit();

    public abstract List<AbstractMicroledger> resetUncommitted();
}
