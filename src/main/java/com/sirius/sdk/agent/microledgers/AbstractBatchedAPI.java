package com.sirius.sdk.agent.microledgers;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBatchedAPI {

    public List<AbstractMicroledger> open(List<AbstractMicroledger> ledgers) {
        List<String> namesToOpen = new ArrayList<>();
        for (AbstractMicroledger ledger : ledgers) {
            namesToOpen.add(ledger.name());
        }
        return openByLedgerNames(namesToOpen);
    }

    public abstract List<AbstractMicroledger> openByLedgerNames(List<String> ledgerNames);

    public abstract void close();

    public abstract List<AbstractMicroledger> getStates();

    public abstract List<AbstractMicroledger> append(List<Transaction> transactions, String txnTime);

    public List<AbstractMicroledger> append(List<Transaction> transactions) {
        return append(transactions, null);
    }

    public abstract List<AbstractMicroledger> commit();

    public abstract List<AbstractMicroledger> resetUncommitted();
}
