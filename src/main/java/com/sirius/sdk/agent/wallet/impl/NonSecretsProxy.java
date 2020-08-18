package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.utils.Pair;

import java.util.List;

public class NonSecretsProxy extends AbstractNonSecrets {

    AgentRPC rpc;

    public NonSecretsProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }



    @Override
    public void addWalletRecord(String type, String id, String value, String tags) {

    }

    @Override
    public void updateWalletRecordValue(String type, String id, String value) {

    }

    @Override
    public void updateWalletRecordTags(String type, String id, String tags) {

    }

    @Override
    public void addWalletRecordTags(String type, String id, String tags) {

    }

    @Override
    public void deleteWalletRecord(String type, String id, List<String> tagNames) {

    }

    @Override
    public void deleteWalletRecord(String type, String id) {

    }

    @Override
    public String getWalletRecord(String type, String id, RetrieveRecordOptions options) {
        return null;
    }

    @Override
    public Pair<List<String>, Integer> walletSearch(String type, String query, RetrieveRecordOptions options, int limit) {
        return null;
    }
}
