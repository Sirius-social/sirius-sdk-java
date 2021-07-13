package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractAnonCreds;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.errors.indy_exceptions.DuplicateMasterSecretNameException;
import com.sirius.sdk.errors.indy_exceptions.WalletItemNotFoundException;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AnonCredsMobile extends AbstractAnonCreds {

    Wallet wallet;
    int timeoutSec = 60;

    public AnonCredsMobile(Wallet wallet) {
            this.wallet = wallet;
    }

    @Override
    public Pair<String, AnonCredSchema> issuerCreateSchema(String issuerDid, String name, String version, List<String> attrs) {
        return null;
    }

    @Override
    public Pair<String, String> issuerCreateAndStoreCredentialDef(String issuerDid, Object schema, String tag, String signatureType, Object config) {
        return null;
    }

    @Override
    public String issuerRotateCredentialDefStart(String credDefId, String config) {
        return null;
    }

    @Override
    public void issuerRotateCredentialDefApply(String credDefId) {

    }

    @Override
    public Triple<String, String, String> issuerCreateAndStoreRevocReg(String issuerDid, String revocDefType, String tag, String credDefId, String config, int tailsWriterHandle) {
        return null;
    }

    @Override
    public JSONObject issuerCreateCredentialOffer(String credDefId) {
        return null;
    }

    @Override
    public Triple<JSONObject, String, JSONObject> issuerCreateCredential(JSONObject credOffer, JSONObject credReq, JSONObject credValues, String revRegId, Integer blobStorageReaderHandle) {
        return null;
    }

    @Override
    public String issuerRevokeCredential(Integer blobStorageReaderHandle, String revRegId, String credRevocId) {
        return null;
    }

    @Override
    public String issuerMergeRevocationRegistryDeltas(String revRegDelta, String otherRevRegDelta) {
        return null;
    }

    @Override
    public String proverCreateMasterSecret(String masterSecretName) throws DuplicateMasterSecretNameException {
        return null;
    }

    @Override
    public Pair<JSONObject, JSONObject> proverCreateCredentialReq(String proverDid, JSONObject credOffer, JSONObject credDef, String masterSecretId) {
        return null;
    }

    @Override
    public void proverSetCredentialAttrTagPolicy(String credDefId, String taAttrs, boolean retroactive) {

    }

    @Override
    public String proverGetCredentialAttrTagPolicy(String credDefId) {
        return null;
    }

    @Override
    public String proverStoreCredential(String credId, JSONObject credReqMetadata, JSONObject cred, JSONObject credDef, String revReqDef) {
        return null;
    }

    @Override
    public String proverGetCredential(String credDefId) throws WalletItemNotFoundException {
        return null;
    }

    @Override
    public void proverDeleteCredential(String credId) {

    }

    @Override
    public List<String> proverGetCredentials(String filters) {
        return null;
    }

    @Override
    public List<String> proverSearchCredential(String query) {
        return null;
    }

    @Override
    public String proverGetCredentialsForProofReq(String proofRequest) {
        return null;
    }

    @Override
    public JSONObject proverSearchCredentialsForProofReq(JSONObject proofRequest, String extraQuery, int limitReferents) {
        return null;
    }

    @Override
    public JSONObject proverCreateProof(JSONObject proofReq, JSONObject requestedCredentials, String masterSecretName, JSONObject schemas, JSONObject credentialDefs, JSONObject revStates) {
        return null;
    }

    @Override
    public boolean verifierVerifyProof(JSONObject proofRequest, JSONObject proof, JSONObject schemas, JSONObject credentialDefs, JSONObject revRegDefs, JSONObject revRegs) {
        return false;
    }

    @Override
    public String createRevocation(int blobStorageReaderHandle, String revRegDef, String revRegDelta, int timestamp, String credRevId) {
        return null;
    }

    @Override
    public String updateRevocationState(int blobStorageReaderHandle, String revState, String revRegDef, String revRegDelta, int timestamp, String credRevId) {
        return null;
    }

    @Override
    public String generateNonce() {
        try {
            return  Anoncreds.generateNonce().get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toUnqualified(String entity) {
        try {
            return Anoncreds.toUnqualified(entity).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
