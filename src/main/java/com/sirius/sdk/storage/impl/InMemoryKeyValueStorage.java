package com.sirius.sdk.storage.impl;

import com.sirius.sdk.storage.abstract_storage.AbstractKeyValueStorage;

import java.util.HashMap;
import java.util.Map;

public class InMemoryKeyValueStorage extends AbstractKeyValueStorage {
    Map<String, Map<String, Object>> databases = new HashMap<>();
    Map<String, Object> selectedDb;
    @Override
    public void selectDb(String name) {
        if (databases.containsKey(name)) {
            selectedDb = databases.get(name);
        } else {
            Map<String, Object> newDb = new HashMap<>();
            databases.put(name, newDb);
            selectedDb = newDb;
        }
    }

    @Override
    public void set(String key, Object value) {
        selectedDb.put(key, value);
    }

    @Override
    public Object get(String key) {
        return selectedDb.get(key);
    }

    @Override
    public void delete(String key) {
        selectedDb.remove(key);
    }
}
