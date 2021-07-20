package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractPairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
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
    final String STORAGE_TYPE = "pairwise";
    final String CONST_VALUE = "pairwise";
    final int DEFAULT_FETCH_LIMIT = 1000;
    NonSecretsMobile nonSecretsMobile;

    public PairwiseMobile(Wallet wallet, NonSecretsMobile nonSecretsMobile) {
        this.wallet = wallet;
        this.nonSecretsMobile = nonSecretsMobile;
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
            updateWalletRecordValueTagsSafely(STORAGE_TYPE, theirDid, tags);
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
            List<Object> pairwiseList = new ArrayList<>();
            for(int i=0;i<listPairwiseArray.length();i++){
                Object obect = listPairwiseArray.get(i);
                if(obect instanceof JSONObject){
                    String theirDid = ((JSONObject) obect).optString("their_did");
                    JSONObject tagsObject =   getWalletRecordTags(STORAGE_TYPE,theirDid);
                    ((JSONObject) obect).put("tags",tagsObject);
                }
                pairwiseList.add(obect);
            }
            return pairwiseList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getPairwise(String thierDid) {
        try {
            String pairwiseInfoJson = Pairwise.getPairwise(wallet, thierDid).get(timeoutSec, TimeUnit.SECONDS);
            JSONObject info = new JSONObject(pairwiseInfoJson);
            info.put("their_did",thierDid);
            JSONObject tagsObj = getWalletRecordTags(STORAGE_TYPE,thierDid);
            info.put("tags",tagsObj);
            return info.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPairwiseMetadata(String theirDid, JSONObject metadata, JSONObject tags) {
        try {
            updateWalletRecordValueTagsSafely(STORAGE_TYPE, theirDid, tags);
            Pairwise.setPairwiseMetadata(wallet, theirDid, metadata.toString()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Pair<List<String>, Integer> search(JSONObject tags, Integer limit) {
        RetrieveRecordOptions opts = new RetrieveRecordOptions(false, false, true);
        if (limit == null) {
            limit = DEFAULT_FETCH_LIMIT;
        }
        Pair<List<String>, Integer> searches = nonSecretsMobile.walletSearch(STORAGE_TYPE, tags.toString(), opts, limit);
        if (searches.first == null) {
            return new Pair<>(new ArrayList<String>(), searches.second);
        } else {
            List<String> pairwiseList = new ArrayList<>();
            for (String item : searches.first) {
                JSONObject itemObject = new JSONObject(item);
                String pw = getPairwise(itemObject.optString("id"));
                if (pw != null) {
                    pairwiseList.add(pw);
                }
            }
            return new Pair<>(pairwiseList, searches.second);
        }

    }


    public void updateWalletRecordValueTagsSafely(String type, String id, JSONObject tags) {
        if (tags == null) {
            tags = new JSONObject();
        }
        RetrieveRecordOptions opts = new RetrieveRecordOptions(false, false, true);
        String record = nonSecretsMobile.getWalletRecord(type, id, opts);
        if (record == null) {
            nonSecretsMobile.addWalletRecord(type, id, tags.toString());
        } else {
            nonSecretsMobile.updateWalletRecordTags(type, id, tags.toString());
        }
    }


    public JSONObject getWalletRecordTags(String type, String id) {
        RetrieveRecordOptions opts = new RetrieveRecordOptions(false, false, true);
        String record = nonSecretsMobile.getWalletRecord(type, id, opts);
        if (record == null) {
            return new JSONObject();
        } else {
            JSONObject recordObject = new JSONObject(record);
            JSONObject tags = recordObject.optJSONObject("tags");
            if (tags == null) {
                return new JSONObject();
            }
            return tags;
        }

    }
}
