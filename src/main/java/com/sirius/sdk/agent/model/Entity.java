package com.sirius.sdk.agent.model;

public class Entity {

    String label;
    String seed;
    String verkey;
    String did;

    public String getLabel() {
        return label;
    }

    public String getSeed() {
        return seed;
    }

    public String getVerkey() {
        return verkey;
    }

    public String getDid() {
        return did;
    }

    public Entity(String label, String seed, String verkey, String did) {
        this.label = label;
        this.seed = seed;
        this.verkey = verkey;
        this.did = did;
    }
}
