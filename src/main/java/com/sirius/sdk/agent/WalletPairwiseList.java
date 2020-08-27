package com.sirius.sdk.agent;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractPairwise;

public class WalletPairwiseList extends AbstractPairwiseList{
    AbstractPairwise api;

    public WalletPairwiseList(AbstractPairwise api) {
        this.api = api;
    }

}
