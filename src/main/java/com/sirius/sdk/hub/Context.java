package com.sirius.sdk.hub;

import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.utils.Pair;

import java.util.List;

public class Context {
    public Agent agent = null;
    Hub currentHub = null;
    AbstractNonSecrets nonSecrets = new AbstractNonSecrets() {
        @Override
        public void addWalletRecord(String type, String id, String value, String tags) {
            AbstractNonSecrets service = currentHub.config.nonSecrets;
            service.addWalletRecord(type, id, value, tags);
        }

        @Override
        public void updateWalletRecordValue(String type, String id, String value) {
            AbstractNonSecrets service = currentHub.config.nonSecrets;
            service.updateWalletRecordValue(type, id, value);
        }

        @Override
        public void updateWalletRecordTags(String type, String id, String tags) {
            AbstractNonSecrets service = currentHub.config.nonSecrets;
            service.updateWalletRecordTags(type, id, tags);
        }

        @Override
        public void addWalletRecordTags(String type, String id, String tags) {
            AbstractNonSecrets service = currentHub.config.nonSecrets;
            service.addWalletRecordTags(type, id, tags);
        }

        @Override
        public void deleteWalletRecord(String type, String id, List<String> tagNames) {
            AbstractNonSecrets service = currentHub.config.nonSecrets;
            service.deleteWalletRecord(type, id, tagNames);
        }

        @Override
        public void deleteWalletRecord(String type, String id) {
            AbstractNonSecrets service = currentHub.config.nonSecrets;
            service.deleteWalletRecord(type, id);
        }

        @Override
        public String getWalletRecord(String type, String id, RetrieveRecordOptions options) {
            AbstractNonSecrets service = currentHub.config.nonSecrets;
            return service.getWalletRecord(type, id, options);
        }

        @Override
        public Pair<List<String>, Integer> walletSearch(String type, String query, RetrieveRecordOptions options, int limit) {
            AbstractNonSecrets service = currentHub.config.nonSecrets;
            return service.walletSearch(type, query, options, limit);
        }
    };

    public void init(Hub.Config config) {
        currentHub = new Hub(config);
    }

    public AbstractNonSecrets getNonSecrets() {
        return nonSecrets;
    }
}
