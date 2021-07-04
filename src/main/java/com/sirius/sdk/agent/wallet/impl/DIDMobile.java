package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractDID;
import com.sirius.sdk.utils.Pair;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.List;

public class DIDMobile extends AbstractDID {

    public DIDMobile(Wallet wallet) {

    }

    @Override
    public Pair<String, String> createAndStoreMyDid(String did, String seed, Boolean cid) {
        return null;
    }

    @Override
    public void storeTheirDid(String did, String verkey) {

    }

    @Override
    public void setDidMetadata(String did, String metadata) {

    }

    @Override
    public List<Object> listMyDidsWithMeta() {
        return null;
    }

    @Override
    public String getDidMetadata(String did) {
        return null;
    }

    @Override
    public String keyForLocalDid(String did) {
        return null;
    }

    @Override
    public String keyForDid(String poolName, String did) {
        return null;
    }

    @Override
    public String createKey(String seed) {
        return null;
    }

    @Override
    public String replaceKeysStart(String did, String seed) {
        return null;
    }

    @Override
    public void replaceKeysApply(String did) {

    }

    @Override
    public void setKeyMetadata(String verkey, String metadata) {

    }

    @Override
    public String getKeyMetadata(String verkey) {
        return null;
    }

    @Override
    public void setEndpointForDid(String did, String address, String transportKey) {

    }

    @Override
    public Pair<String, String> getEndpointForDid(String pooName, String did) {
        return null;
    }

    @Override
    public Object getMyDidMeta(String did) {
        return null;
    }

    @Override
    public String abbreviateVerKey(String did, String fullVerkey) {
        return null;
    }

    @Override
    public String qualifyDid(String did, String method) {
        return null;
    }
}
