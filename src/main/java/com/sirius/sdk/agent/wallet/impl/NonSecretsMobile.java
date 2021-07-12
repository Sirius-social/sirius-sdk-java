package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.utils.Pair;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.non_secrets.WalletRecord;
import org.hyperledger.indy.sdk.non_secrets.WalletSearch;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NonSecretsMobile extends AbstractNonSecrets {

    Wallet wallet;
    int timeoutSec = 60;

    public NonSecretsMobile(Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public void addWalletRecord(String type, String id, String value, String tags) {
        try {
            WalletRecord.add(wallet,type,id,value,tags).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateWalletRecordValue(String type, String id, String value) {
        try {
            WalletRecord.updateValue(wallet,type,id,value).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateWalletRecordTags(String type, String id, String tags) {
        try {
            WalletRecord.updateTags(wallet,type,id,tags).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addWalletRecordTags(String type, String id, String tags) {
        try {
            WalletRecord.addTags(wallet,type,id,tags).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteWalletRecord(String type, String id, List<String> tagNames) {

    }

    @Override
    public void deleteWalletRecord(String type, String id) {
        try {
            WalletRecord.delete(wallet,type,id).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getWalletRecord(String type, String id, RetrieveRecordOptions options) {
        try {
            return WalletRecord.get(wallet,type,id,options.serialize()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Pair<List<String>, Integer> walletSearch(String type, String query, RetrieveRecordOptions options, int limit) {
        return null;
    }
}
