package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.utils.Pair;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.non_secrets.WalletRecord;
import org.hyperledger.indy.sdk.non_secrets.WalletSearch;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NonSecretsMobile extends AbstractNonSecrets {

    Wallet wallet;
    int timeoutSec = 60;

    public NonSecretsMobile(Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public void addWalletRecord(String type, String id, String value, String tags) {
        try {
            WalletRecord.add(wallet,type,id,value,tags).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateWalletRecordValue(String type, String id, String value) {
        try {
            WalletRecord.updateValue(wallet,type,id,value).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateWalletRecordTags(String type, String id, String tags) {
        try {
            WalletRecord.updateTags(wallet,type,id,tags).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addWalletRecordTags(String type, String id, String tags) {
        try {
            WalletRecord.addTags(wallet,type,id,tags).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteWalletRecord(String type, String id, List<String> tagNames) {
        try {
            String arrayTag =  new  JSONArray(tagNames).toString();
            WalletRecord.deleteTags(wallet,type,id,arrayTag).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteWalletRecord(String type, String id) {
        try {
            WalletRecord.delete(wallet,type,id).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getWalletRecord(String type, String id, RetrieveRecordOptions options) {
        try {
            return WalletRecord.get(wallet,type,id,options.serialize()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (!e.getMessage().contains("WalletItemNotFoundException"))
                e.printStackTrace();
        }
        return null;
    }

    @Override
    public Pair<List<String>, Integer> walletSearch(String type, String query, RetrieveRecordOptions options, int limit) {

        options.setRetrieveRecords(true);
        options.setRetrieveTotalCount(true);
        String optionStr = options.serialize();
        try {
            WalletSearch search =  WalletSearch.open(wallet,type,query,optionStr).get(timeoutSec, TimeUnit.SECONDS);
           String searchListString =  WalletSearch.searchFetchNextRecords(wallet,search,limit).get(timeoutSec, TimeUnit.SECONDS);

           WalletSearch.closeSearch(search);

            if(searchListString == null){
                return new Pair<>(new ArrayList<>(),0);
            }else{
                JSONObject searchObj = new JSONObject(searchListString);
                JSONArray records = searchObj.optJSONArray("records");
                List<String> lis = new ArrayList<>();
                if(records!=null){
                    for(int i=0;i<records.length();i++){
                        Object object =  records.get(i);
                        if(object instanceof String){
                            lis.add((String) object);
                        }
                        if(object instanceof JSONObject){
                            lis.add(((JSONObject) object).toString());
                        }
                    }
                }
                Integer totalCount = searchObj.optInt("totalCount");
                return new Pair<>(lis,totalCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pair<>(new ArrayList<>(),0);

    }
}
