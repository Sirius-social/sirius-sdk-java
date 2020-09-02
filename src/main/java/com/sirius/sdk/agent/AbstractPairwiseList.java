package com.sirius.sdk.agent;

import com.sirius.sdk.agent.model.pairwise.Pairwise;

public abstract class AbstractPairwiseList {
    public abstract void create(Pairwise pairwise);
    public abstract void update(Pairwise pairwise);
    public abstract boolean isExists(String theirDid);
    public abstract void ensureExists(Pairwise pairwise);
    public abstract Pairwise loadForDid(String theirDid);
    public abstract Pairwise loadForVerkey(String theirVerkey);
}
