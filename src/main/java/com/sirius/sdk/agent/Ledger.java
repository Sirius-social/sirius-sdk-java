package com.sirius.sdk.agent;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractAnonCreds;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCache;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractLedger;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;

public class Ledger {
    String name;
    AbstractLedger api;
    AbstractAnonCreds issuer;
    AbstractCache cache;
    AbstractImmutableCollection storage;
    String db;

    public Ledger(String name, AbstractLedger api, AbstractAnonCreds issuer,
                  AbstractCache cache, AbstractImmutableCollection storage) {
        this.name = name;
        this.api = api;
        this.issuer = issuer;
        this.cache = cache;
        this.storage = storage;
        db = String.format("ledger_storage_%s", name);
    }

}
