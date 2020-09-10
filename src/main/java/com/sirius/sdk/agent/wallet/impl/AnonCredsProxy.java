package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractAnonCreds;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.errors.sirius_exceptions.*;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;

import java.util.List;

public class AnonCredsProxy extends AbstractAnonCreds  {

    AgentRPC rpc;

    public AnonCredsProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }




    @Override
    public Pair<String, AnonCredSchema> issuerCreateSchema(String issuerDid, String name, String version, List<String> attrs) {
        return null;
    }

    @Override
    public Pair<String, String> issuerCreateAndStoreCredentialDef(String issuerDid, String schema, String tag, String signatureType, String config) {
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
    public String issuerCreateCredentialOffer(String credDefId) {
        return null;
    }

    @Override
    public Triple<String, String, String> issuerCreateCredential(String credOffer, String credReq, String credValues, String revRegId, Integer blobStorageReaderHandle) {
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
    public String proverCreateMasterSecret(String masterSecretName) {
        return null;
    }

    @Override
    public Pair<String, String> proverCreateCredentialReq(String proverDid, String credOffer, String credDef, String masterSecretId) {
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
    public String proverStoreCredential(String credId, String credReqMetadata, String cred, String credDef, String revReqDef) {
        return null;
    }

    @Override
    public String proverGetCredential(String credDefId) {
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
    public String proverSearchCredentialsForProofReq(String proofRequest, String extraQuery, int limitReferents) {
        return null;
    }

    @Override
    public String proverCreateProof(String proofReq, String requestedCredentials, String masterSecretName, String schemas, String credentialDefs, String revStates) {
        return null;
    }

    @Override
    public boolean verifierVerifyProof(String proofRequest, String proof, String schemas, String credentialDefs, String revRegDefs, String revRegs) {
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
        return null;
    }

    @Override
    public String toUnqualified(String entity) {
        return null;
    }


}
