package com.sirius.sdk.agent.model;

public class Entity {

    String seed;
    String verkey;
    String did;

    public String getSeed() {
        return seed;
    }

    public String getVerkey() {
        return verkey;
    }

    public String getDid() {
        return did;
    }

    public Entity(String seed, String verkey, String did) {
        this.seed = seed;
        this.verkey = verkey;
        this.did = did;
    }
}
