package com.sirius.sdk.storage.abstract_storage;

import com.sirius.sdk.utils.Pair;

import java.util.List;

public abstract class AbstractImmutableCollection {
    public abstract void selectDb(String name);
    public abstract void add(Object value, String tags);
    public abstract Pair<List<Object>,Integer> fetch (String tags,int limit);
}
