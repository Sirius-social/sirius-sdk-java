package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractPairwise;
import com.sirius.sdk.utils.Pair;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONObject;

import java.util.List;

public class PairwiseMobile extends AbstractPairwise {

    public PairwiseMobile(Wallet wallet) {

    }

    @Override
    public boolean isPairwiseExist(String theirDid) {
        return false;
    }

    @Override
    public void createPairwise(String theirDid, String myDid, JSONObject metadata, JSONObject tags) {

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
    public void setPairwiseMetadata(String theirDid, JSONObject metadata, JSONObject tags) {

    }

    @Override
    public Pair<List<String>, Integer> search(JSONObject tags, Integer limit) {
        return null;
    }
}
