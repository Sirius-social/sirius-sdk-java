package com.sirius.sdk.agent.storages;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InWalletImmutableCollection extends AbstractImmutableCollection {
    int DEFAULT_FETCH_LIMIT = 1000;
    String selectedDb;

    public InWalletImmutableCollection(AbstractNonSecrets storage) {
        this.storage = storage;
    }

    AbstractNonSecrets storage;

    @Override
    public void selectDb(String name) {
        selectedDb = name;
    }

    @Override
    public void add(Object value, String tags) {
        storage.addWalletRecord(selectedDb, UUID.randomUUID().toString(), value.toString(), tags);
    }

    @Override
    public Pair<List<Object>, Integer> fetch(String tags, Integer limit) {
       if(limit == null){
        limit=    DEFAULT_FETCH_LIMIT;
       }
        Pair<List<String>,Integer> result = storage.walletSearch(selectedDb,tags,
                new RetrieveRecordOptions(false,true,false),limit);
        if(result.first!=null && result.first!= JSONObject.NULL){
            List<Object> listValue = new ArrayList<>();
            for(int i=0;i<result.first.size();i++){
                Object object = result.first.get(i);
                JSONObject jsonObject =  new JSONObject(object.toString());
                 String values =  jsonObject.optString("value");
                 if(values!=null){
                     listValue.add(values);
                 }
            }
            return new Pair<>(listValue, result.second);
        }else{
            return new Pair<>(new ArrayList<>(),result.second);
        }
    }
}

