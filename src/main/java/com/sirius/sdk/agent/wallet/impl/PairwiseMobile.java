package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractPairwise;
import com.sirius.sdk.utils.Pair;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pairwise.Pairwise;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PairwiseMobile extends AbstractPairwise {

    Wallet wallet;
    int timeoutSec = 60;

    public PairwiseMobile(Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public boolean isPairwiseExist(String theirDid) {
        try {
            return Pairwise.isPairwiseExists(wallet, theirDid).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void createPairwise(String theirDid, String myDid, JSONObject metadata, JSONObject tags) {
        try {
            Pairwise.createPairwise(wallet, theirDid, myDid, metadata.toString()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Object> listPairwise() {
        try {
            String listPairwise = Pairwise.listPairwise(wallet).get(timeoutSec, TimeUnit.SECONDS);
            JSONArray listPairwiseArray = new JSONArray(listPairwise);
            return listPairwiseArray.toList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getPairwise(String thierDid) {
        try {
            String pairwiseInfoJson = Pairwise.getPairwise(wallet, thierDid).get(timeoutSec, TimeUnit.SECONDS);
            return pairwiseInfoJson;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPairwiseMetadata(String theirDid, JSONObject metadata, JSONObject tags) {
        try {
            Pairwise.setPairwiseMetadata(wallet, theirDid, metadata.toString()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Pair<List<String>, Integer> search(JSONObject tags, Integer limit) {
        List<Object> list = listPairwise();
        List<String> res = new ArrayList<>();
        for (Object s : list) {
            JSONObject pw = new JSONObject(s.toString());
            boolean b = true;
            for (String k : tags.keySet()) {
                if (!pw.has(k) || !pw.optString(k).equals(tags.optString(k))) {
                    b = false;
                    continue;
                }
            }
            if (b) {
                res.add(s.toString());
            }
        }

        return new Pair<>(res, list.size());
    }
}
