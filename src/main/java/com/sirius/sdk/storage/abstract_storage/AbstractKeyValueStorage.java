package com.sirius.sdk.storage.abstract_storage;

public abstract class AbstractKeyValueStorage {
    public abstract void selectDb(String name);
    public abstract void set(String key,Object value);
    public abstract Object get(String key);
    public abstract void delete(String key);
}
