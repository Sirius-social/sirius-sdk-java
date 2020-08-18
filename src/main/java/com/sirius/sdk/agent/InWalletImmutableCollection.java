package com.sirius.sdk.agent;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;
import com.sirius.sdk.utils.Pair;

import java.util.List;

public class InWalletImmutableCollection extends AbstractImmutableCollection {
    int DEFAULT_FETCH_LIMIT = 1000;

    public InWalletImmutableCollection(AbstractNonSecrets inWalletStorage) {
        this.inWalletStorage = inWalletStorage;
    }

    AbstractNonSecrets inWalletStorage;

    @Override
    public void selectDb(String name) {

    }

    @Override
    public void add(Object value, String tags) {

    }

    @Override
    public Pair<List<Object>, Integer> fetch(String tags, int limit) {
        return null;
    }
}
