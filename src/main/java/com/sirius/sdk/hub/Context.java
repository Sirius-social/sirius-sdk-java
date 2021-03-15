package com.sirius.sdk.hub;

import com.sirius.sdk.agent.pairwise.AbstractPairwiseList;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.*;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.CacheOptions;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.PurgeOptions;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.indy_exceptions.DuplicateMasterSecretNameException;
import com.sirius.sdk.errors.indy_exceptions.WalletItemNotFoundException;
import com.sirius.sdk.errors.sirius_exceptions.SiriusRPCError;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

public class Context implements Closeable {

    // loading all Message classes to force their registration in static block
    static {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("com.sirius.sdk"))
                        .setScanners(new SubTypesScanner())
        );

        for (Class<?> cl : reflections.getSubTypesOf(com.sirius.sdk.messaging.Message.class)) {
            try {
                Class.forName(cl.getName(), true, cl.getClassLoader());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

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

    AbstractAnonCreds anonCreds = new AbstractAnonCreds() {
        @Override
        public Pair<String, AnonCredSchema> issuerCreateSchema(String issuerDid, String name, String version, List<String> attrs) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.issuerCreateSchema(issuerDid, name, version, attrs);
        }

        @Override
        public Pair<String, String> issuerCreateAndStoreCredentialDef(String issuerDid, Object schema, String tag, String signatureType, Object config) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.issuerCreateAndStoreCredentialDef(issuerDid, schema, tag, signatureType, config);
        }

        @Override
        public String issuerRotateCredentialDefStart(String credDefId, String config) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.issuerRotateCredentialDefStart(credDefId, config);
        }

        @Override
        public void issuerRotateCredentialDefApply(String credDefId) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            service.issuerRotateCredentialDefApply(credDefId);
        }

        @Override
        public Triple<String, String, String> issuerCreateAndStoreRevocReg(String issuerDid, String revocDefType, String tag, String credDefId, String config, int tailsWriterHandle) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.issuerCreateAndStoreRevocReg(issuerDid, revocDefType, tag, credDefId, config, tailsWriterHandle);
        }

        @Override
        public JSONObject issuerCreateCredentialOffer(String credDefId) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.issuerCreateCredentialOffer(credDefId);
        }

        @Override
        public Triple<JSONObject, String, JSONObject> issuerCreateCredential(JSONObject credOffer, JSONObject credReq, JSONObject credValues, String revRegId, Integer blobStorageReaderHandle) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.issuerCreateCredential(credOffer, credReq, credValues, revRegId, blobStorageReaderHandle);
        }

        @Override
        public String issuerRevokeCredential(Integer blobStorageReaderHandle, String revRegId, String credRevocId) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.issuerRevokeCredential(blobStorageReaderHandle, revRegId, credRevocId);
        }

        @Override
        public String issuerMergeRevocationRegistryDeltas(String revRegDelta, String otherRevRegDelta) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.issuerMergeRevocationRegistryDeltas(revRegDelta, otherRevRegDelta);
        }

        @Override
        public String proverCreateMasterSecret(String masterSecretName) throws DuplicateMasterSecretNameException {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.proverCreateMasterSecret(masterSecretName);
        }

        @Override
        public Pair<JSONObject, JSONObject> proverCreateCredentialReq(String proverDid, JSONObject credOffer, JSONObject credDef, String masterSecretId) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.proverCreateCredentialReq(proverDid, credOffer, credDef, masterSecretId);
        }

        @Override
        public void proverSetCredentialAttrTagPolicy(String credDefId, String taAttrs, boolean retroactive) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            service.proverSetCredentialAttrTagPolicy(credDefId, taAttrs, retroactive);
        }

        @Override
        public String proverGetCredentialAttrTagPolicy(String credDefId) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.proverGetCredentialAttrTagPolicy(credDefId);
        }

        @Override
        public String proverStoreCredential(String credId, JSONObject credReqMetadata, JSONObject cred, JSONObject credDef, String revReqDef) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.proverStoreCredential(credId, credReqMetadata, cred, credDef, revReqDef);
        }

        @Override
        public String proverGetCredential(String credDefId) throws WalletItemNotFoundException {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.proverGetCredential(credDefId);
        }

        @Override
        public void proverDeleteCredential(String credId) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            service.proverDeleteCredential(credId);
        }

        @Override
        public List<String> proverGetCredentials(String filters) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.proverGetCredentials(filters);
        }

        @Override
        public List<String> proverSearchCredential(String query) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.proverSearchCredential(query);
        }

        @Override
        public String proverGetCredentialsForProofReq(String proofRequest) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.proverGetCredentialsForProofReq(proofRequest);
        }

        @Override
        public JSONObject proverSearchCredentialsForProofReq(JSONObject proofRequest, String extraQuery, int limitReferents) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.proverSearchCredentialsForProofReq(proofRequest, extraQuery, limitReferents);
        }

        @Override
        public JSONObject proverCreateProof(JSONObject proofReq, JSONObject requestedCredentials, String masterSecretName, JSONObject schemas, JSONObject credentialDefs, JSONObject revStates) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.proverCreateProof(proofReq, requestedCredentials, masterSecretName, schemas, credentialDefs, revStates);
        }

        @Override
        public boolean verifierVerifyProof(JSONObject proofRequest, JSONObject proof, JSONObject schemas, JSONObject credentialDefs, JSONObject revRegDefs, JSONObject revRegs) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.verifierVerifyProof(proofRequest, proof, schemas, credentialDefs, revRegDefs, revRegs);
        }

        @Override
        public String createRevocation(int blobStorageReaderHandle, String revRegDef, String revRegDelta, int timestamp, String credRevId) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.createRevocation(blobStorageReaderHandle, revRegDef, revRegDelta, timestamp, credRevId);
        }

        @Override
        public String updateRevocationState(int blobStorageReaderHandle, String revState, String revRegDef, String revRegDelta, int timestamp, String credRevId) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.updateRevocationState(blobStorageReaderHandle, revState, revRegDef, revRegDelta, timestamp, credRevId);
        }

        @Override
        public String generateNonce() {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.generateNonce();
        }

        @Override
        public String toUnqualified(String entity) {
            AbstractAnonCreds service = currentHub.getAnonCreds();
            return service.toUnqualified(entity);
        }
    };

    AbstractCache cache = new AbstractCache() {
        @Override
        public String getSchema(String poolName, String submitter_did, String id, CacheOptions options) {
            AbstractCache service = currentHub.getCache();
            return service.getSchema(poolName, submitter_did, id, options);
        }

        @Override
        public String getCredDef(String poolName, String submitter_did, String id, CacheOptions options) {
            AbstractCache service = currentHub.getCache();
            return service.getCredDef(poolName, submitter_did, id, options);
        }

        @Override
        public void purgeSchemaCache(PurgeOptions options) {
            AbstractCache service = currentHub.getCache();
            service.purgeSchemaCache(options);
        }

        @Override
        public void purgeCredDefCache(PurgeOptions options) {
            AbstractCache service = currentHub.getCache();
            service.purgeCredDefCache(options);
        }
    };

    public static class Builder {
        Hub.Config config = new Hub.Config();

        //public AbstractCrypto crypto = null;
        //        public AbstractMicroledgerList microledgers = null;
        //        public AbstractPairwiseList pairwiseStorage = null;
        //        public AbstractDID did = null;
        //        public AbstractAnonCreds anoncreds = null;
        //        public AbstractNonSecrets nonSecrets = null;
        //        public String serverUri = null;
        //        public byte[] credentials;
        //        public P2PConnection p2p;
        //        public int ioTimeout = BaseAgentConnection.IO_TIMEOUT;
        //        public AbstractImmutableCollection storage = null;

        public Builder setCrypto(AbstractCrypto crypto) {
            this.config.crypto = crypto;
            return this;
        }

        public Builder setMicroledgers(AbstractMicroledgerList microledgers) {
            this.config.microledgers = microledgers;
            return this;
        }

        public Builder setServerUri(String serverUri) {
            this.config.serverUri = serverUri;
            return this;
        }

        public Builder setCredentials(byte[] credentials) {
            this.config.credentials = credentials;
            return this;
        }

        public Builder setP2p(P2PConnection p2p) {
            this.config.p2p = p2p;
            return this;
        }

        public Builder setTimeoutSec(int timeoutSec) {
            this.config.ioTimeout = timeoutSec;
            return this;
        }

        public Context build() {
            return new Context(this.config);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Context(Hub.Config config) {
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

    public AbstractAnonCreds getAnonCreds() {
        return anonCreds;
    }

    public AbstractCache getCache() {
        return cache;
    }

    public Map<String, Ledger> getLedgers() {
        return currentHub.getAgentConnectionLazy().getLedgers();
    }

    public String generateQrCode(String value) {
        return currentHub.getAgentConnectionLazy().generateQrCode(value);
    }

    public Endpoint getEndpointWithEmptyRoutingKeys() {
        for (Endpoint e : getEndpoints()) {
            if (e.getRoutingKeys().size() == 0) {
                return e;
            }
        }
        return null;
    }

    public String getEndpointAddressWithEmptyRoutingKeys() {
        Endpoint e = getEndpointWithEmptyRoutingKeys();
        if (e != null)
            return e.getAddress();
        else
            return "";
    }

    public Listener subscribe() {
        return currentHub.getAgentConnectionLazy().subscribe();
    }

    public Hub getCurrentHub() {
        return currentHub;
    }

    public void sendTo(Message message, Pairwise to) {
        try {
            currentHub.getAgentConnectionLazy().sendTo(message, to);
        } catch (SiriusRPCError siriusRPCError) {
            siriusRPCError.printStackTrace();
        }
    }

    @Override
    public void close() {
        currentHub.close();
    }
}
