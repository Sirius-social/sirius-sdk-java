package com.sirius.sdk.agent.wallet.impl;

import com.goterl.lazycode.lazysodium.models.Pair;
import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractPairwise;

import java.util.List;

public class PairwiseProxy extends AbstractPairwise {
    AgentRPC rpc;

    public PairwiseProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }


    @Override
    public boolean isPairwiseExist(String theirDid) {
        return false;
    }

    @Override
    public void createPairwise(String theirDid, String myDid, String metadata, String tags) {

    }

    @Override
    public List<Object> listPairwise() {
        return null;
    }

    @Override
    public String getPairwise(String thierDid) {
        return null;
    }

    @Override
    public void setPairwiseMetadata(String theirDid, String metadata, String tags) {

    }

    @Override
    public Pair<List<String>, Integer> search(String tags, int limit) {
        return null;
    }
}
