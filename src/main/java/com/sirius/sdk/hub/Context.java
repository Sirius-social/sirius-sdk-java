package com.sirius.sdk.hub;

import com.sirius.sdk.agent.AbstractPairwiseList;
import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.Listener;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.model.Endpoint;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractDID;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.errors.sirius_exceptions.SiriusRPCError;
import com.sirius.sdk.utils.Pair;

import java.util.List;

public class Context {
    public Agent agent = null;
    Hub currentHub = null;
    AbstractNonSecrets nonSecrets = new AbstractNonSecrets() {
        @Override
        public void addWalletRecord(String type, String id, String value, String tags) {
            AbstractNonSecrets service = currentHub.getNonSecrets();
            service.addWalletRecord(type, id, value, tags);
        }

        @Override
        public void updateWalletRecordValue(String type, String id, String value) {
            AbstractNonSecrets service = currentHub.getNonSecrets();
            service.updateWalletRecordValue(type, id, value);
        }

        @Override
        public void updateWalletRecordTags(String type, String id, String tags) {
            AbstractNonSecrets service = currentHub.getNonSecrets();
            service.updateWalletRecordTags(type, id, tags);
        }

        @Override
        public void addWalletRecordTags(String type, String id, String tags) {
            AbstractNonSecrets service = currentHub.getNonSecrets();
            service.addWalletRecordTags(type, id, tags);
        }

        @Override
        public void deleteWalletRecord(String type, String id, List<String> tagNames) {
            AbstractNonSecrets service = currentHub.getNonSecrets();
            service.deleteWalletRecord(type, id, tagNames);
        }

        @Override
        public void deleteWalletRecord(String type, String id) {
            AbstractNonSecrets service = currentHub.getNonSecrets();
            service.deleteWalletRecord(type, id);
        }

        @Override
        public String getWalletRecord(String type, String id, RetrieveRecordOptions options) {
            AbstractNonSecrets service = currentHub.getNonSecrets();
            return service.getWalletRecord(type, id, options);
        }

        @Override
        public Pair<List<String>, Integer> walletSearch(String type, String query, RetrieveRecordOptions options, int limit) {
            AbstractNonSecrets service = currentHub.getNonSecrets();
            return service.walletSearch(type, query, options, limit);
        }
    };

    AbstractCrypto crypto = new AbstractCrypto() {
        @Override
        public String createKey(String seed, String cryptoType) {
            AbstractCrypto service = currentHub.getCrypto();
            return service.createKey(seed, cryptoType);
        }

        @Override
        public void setKeyMetadata(String verkey, String metadata) {
            AbstractCrypto service = currentHub.getCrypto();
            service.setKeyMetadata(verkey, metadata);
        }

        @Override
        public String getKeyMetadata(String verkey) {
            AbstractCrypto service = currentHub.getCrypto();
            return service.getKeyMetadata(verkey);
        }

        @Override
        public byte[] cryptoSign(String signerVk, byte[] msg) {
            AbstractCrypto service = currentHub.getCrypto();
            return service.cryptoSign(signerVk, msg);
        }

        @Override
        public boolean cryptoVerify(String signerVk, byte[] msg, byte[] signature) {
            AbstractCrypto service = currentHub.getCrypto();
            return service.cryptoVerify(signerVk, msg, signature);
        }

        @Override
        public byte[] anonCrypt(String recipentVk, byte[] msg) {
            AbstractCrypto service = currentHub.getCrypto();
            return service.anonDecrypt(recipentVk, msg);
        }

        @Override
        public byte[] anonDecrypt(String recipientVk, byte[] encryptedMsg) {
            AbstractCrypto service = currentHub.getCrypto();
            return service.anonDecrypt(recipientVk, encryptedMsg);
        }

        @Override
        public byte[] packMessage(Object message, List<String> recipentVerkeys, String senderVerkey) {
            AbstractCrypto service = currentHub.getCrypto();
            return service.packMessage(message, recipentVerkeys, senderVerkey);
        }

        @Override
        public String unpackMessage(byte[] jwe) {
            AbstractCrypto service = currentHub.getCrypto();
            return service.unpackMessage(jwe);
        }
    };

    AbstractDID did = new AbstractDID() {
        @Override
        public Pair<String, String> createAndStoreMyDid(String did, String seed, Boolean cid) {
            AbstractDID service = currentHub.getDid();
            return service.createAndStoreMyDid(did, seed, cid);
        }

        @Override
        public void storeTheirDid(String did, String verkey) {
            AbstractDID service = currentHub.getDid();
            service.storeTheirDid(did, verkey);
        }

        @Override
        public void setDidMetadata(String did, String metadata) {
            AbstractDID service = currentHub.getDid();
            service.setDidMetadata(did, metadata);
        }

        @Override
        public List<Object> listMyDidsWithMeta() {
            AbstractDID service = currentHub.getDid();
            return service.listMyDidsWithMeta();
        }

        @Override
        public String getDidMetadata(String did) {
            AbstractDID service = currentHub.getDid();
            return service.getDidMetadata(did);
        }

        @Override
        public String keyForLocalDid(String did) {
            AbstractDID service = currentHub.getDid();
            return service.keyForLocalDid(did);
        }

        @Override
        public String keyForDid(String poolName, String did) {
            AbstractDID service = currentHub.getDid();
            return service.keyForDid(poolName, did);
        }

        @Override
        public String createKey(String seed) {
            AbstractDID service = currentHub.getDid();
            return service.createKey(seed);
        }

        @Override
        public String replaceKeysStart(String did, String seed) {
            AbstractDID service = currentHub.getDid();
            return service.replaceKeysStart(did, seed);
        }

        @Override
        public void replaceKeysApply(String did) {
            AbstractDID service = currentHub.getDid();
            service.replaceKeysStart(did);
        }

        @Override
        public void setKeyMetadata(String verkey, String metadata) {
            AbstractDID service = currentHub.getDid();
            service.setKeyMetadata(verkey, metadata);
        }

        @Override
        public String getKeyMetadata(String verkey) {
            AbstractDID service = currentHub.getDid();
            return service.getKeyMetadata(verkey);
        }

        @Override
        public void setEndpointForDid(String did, String address, String transportKey) {
            AbstractDID service = currentHub.getDid();
            service.setEndpointForDid(did, address, transportKey);
        }

        @Override
        public Pair<String, String> getEndpointForDid(String pooName, String did) {
            AbstractDID service = currentHub.getDid();
            return service.getEndpointForDid(pooName, did);
        }

        @Override
        public Object getMyDidMeta(String did) {
            AbstractDID service = currentHub.getDid();
            return service.getMyDidMeta(did);
        }

        @Override
        public String abbreviateVerKey(String did, String fullVerkey) {
            AbstractDID service = currentHub.getDid();
            return service.abbreviateVerKey(did, fullVerkey);
        }

        @Override
        public String qualifyDid(String did, String method) {
            AbstractDID service = currentHub.getDid();
            return service.qualifyDid(did, method);
        }
    };

    AbstractPairwiseList pairwiseList = new AbstractPairwiseList() {
        @Override
        public void create(Pairwise pairwise) {
            AbstractPairwiseList service = currentHub.getPairwiseList();
            service.create(pairwise);
        }

        @Override
        public void update(Pairwise pairwise) {
            AbstractPairwiseList service = currentHub.getPairwiseList();
            service.update(pairwise);
        }

        @Override
        public boolean isExists(String theirDid) {
            AbstractPairwiseList service = currentHub.getPairwiseList();
            return service.isExists(theirDid);
        }

        @Override
        public void ensureExists(Pairwise pairwise) {
            AbstractPairwiseList service = currentHub.getPairwiseList();
            service.ensureExists(pairwise);
        }

        @Override
        public Pairwise loadForDid(String theirDid) {
            AbstractPairwiseList service = currentHub.getPairwiseList();
            return service.loadForDid(theirDid);
        }

        @Override
        public Pairwise loadForVerkey(String theirVerkey) {
            AbstractPairwiseList service = currentHub.getPairwiseList();
            return service.loadForVerkey(theirVerkey);
        }
    };

    public void init(Hub.Config config) {
        currentHub = new Hub(config);
    }

    public AbstractNonSecrets getNonSecrets() {
        return nonSecrets;
    }

    public List<Endpoint> getEndpoints() {
        return currentHub.getAgentConnectionLazy().getEndpoints();
    }

    public AbstractCrypto getCrypto() {
        return crypto;
    }

    public AbstractDID getDid() {
        return did;
    }

    public AbstractPairwiseList getPairwiseList() {
        return pairwiseList;
    }

    public String generateQrCode(String value) {
        return currentHub.getAgentConnectionLazy().generateQrCode(value);
    }

    public Listener subscribe() {
        return currentHub.getAgentConnectionLazy().subscribe();
    }

    public void sendTo(Message message, Pairwise to) {
        try {
            currentHub.getAgentConnectionLazy().sendTo(message, to);
        } catch (SiriusRPCError siriusRPCError) {
            siriusRPCError.printStackTrace();
        }
    }
}
