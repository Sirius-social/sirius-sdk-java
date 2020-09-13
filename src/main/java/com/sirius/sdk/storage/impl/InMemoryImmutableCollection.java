package com.sirius.sdk.storage.impl;

import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryImmutableCollection extends AbstractImmutableCollection {

    Map<String, List<Pair<String, Object>>> databases = new HashMap<>();
    List<Pair<String, Object>> selectedDb;

    @Override
    public void selectDb(String name) {
        if (databases.containsKey(name)) {
            selectedDb = databases.get(name);
        } else {
            List<Pair<String, Object>> newDb = new ArrayList<>();
            databases.put(name, newDb);
            selectedDb = newDb;
        }
    }

    @Override
    public void add(Object value, String tags) {
        Pair<String ,Object> item= new Pair<>(tags,value);
        selectedDb.add(item);
    }

    @Override
    public Pair<List<Object>, Integer> fetch(String tags, Integer limit) {
        List<Object> result = new ArrayList<>();
        for(int i=0;i<selectedDb.size();i++){
            Pair<String,Object> item = selectedDb.get(i);
            JSONObject tagsObj = new JSONObject(tags);
            JSONObject tagsItemObj = new JSONObject(item.first);
            for(String key : tagsObj.keySet()){
                String tag = tagsObj.getString(key);
                if(tagsItemObj.has(key)){
                    String itemTag = tagsItemObj.getString(key);
                    if(itemTag.equals(tag)){
                        result.add(item.second);
                    }
                }
            }
        }
        return new Pair<>(result,result.size());
    }
}

