package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractAnonCreds;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.errors.sirius_exceptions.*;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;

import java.util.List;
import java.util.Objects;

public class AnonCredsProxy extends AbstractAnonCreds  {

    AgentRPC rpc;

    public AnonCredsProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }




    @Override
    public int hashCode() {
        return Objects.hash(rpc);
    }

    @Override
    public Pair<String, AnonCredSchema> issuerCreateSchema(String issuerDid, String name, String version, List<String> attrs) {
        Pair<String, String> response = new RemoteCallWrapper<Pair<String, String>>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/issuer_create_schema",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("issuer_did", issuerDid).
                        add("name", name).add("version", version).
                        add("attrs",attrs));
        AnonCredSchema anonCredSchema= new AnonCredSchema( response.second);
        return new Pair<>(response.first, anonCredSchema);

    }

    @Override
    public Pair<String, String> issuerCreateAndStoreCredentialDef(String issuerDid, Object schema, String tag, String signatureType, Object config) {
       return new RemoteCallWrapper<Pair<String, String>>(rpc){}.
               remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/issuer_create_and_store_credential_def",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("issuer_did", issuerDid)
                        .add("schema", schema)
                        .add("tag", tag)
                        .add("signature_type", signatureType)
                        .add("config", config));
    }

    @Override
    public String issuerRotateCredentialDefStart(String credDefId, String config) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/issuer_rotate_credential_def_start",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("cred_def_id", credDefId)
                                .add("config",config));
    }

    @Override
    public void issuerRotateCredentialDefApply(String credDefId) {
         new RemoteCallWrapper(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/issuer_rotate_credential_def_apply",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("cred_def_id", credDefId));
    }

    @Override
    public Triple<String, String, String> issuerCreateAndStoreRevocReg(String issuerDid, String revocDefType, String tag, String credDefId, String config, int tailsWriterHandle) {
        return new RemoteCallWrapper<Triple<String, String, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/issuer_create_and_store_revoc_reg",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("issuer_did", issuerDid)
                                .add("revoc_def_type",revocDefType)
                                .add("cred_def_id",credDefId)
                                .add("config",config)
                                .add("tails_writer_handle",tailsWriterHandle));

    }

    @Override
    public String issuerCreateCredentialOffer(String credDefId) {
      return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/issuer_create_credential_offer",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("cred_def_id", credDefId));
    }

    @Override
    public Triple<String, String, String> issuerCreateCredential(String credOffer, String credReq, String credValues, String revRegId, Integer blobStorageReaderHandle) {

        return   new RemoteCallWrapper<Triple<String, String, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/issuer_create_credential",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("cred_offer", credOffer)
                                .add("cred_req", credReq)
                                .add("cred_values", credValues)
                                .add("rev_reg_id", revRegId)
                                .add("blob_storage_reader_handle", blobStorageReaderHandle));
    }

    @Override
    public String issuerRevokeCredential(Integer blobStorageReaderHandle, String revRegId, String credRevocId) {
        return   new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/issuer_revoke_credential",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("blob_storage_reader_handle", blobStorageReaderHandle)
                                .add("rev_reg_id", revRegId)
                                .add("cred_revoc_id", credRevocId));
    }

    @Override
    public String issuerMergeRevocationRegistryDeltas(String revRegDelta, String otherRevRegDelta) {
        return   new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/issuer_merge_revocation_registry_deltas",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("rev_reg_delta", revRegDelta)
                                .add("other_rev_reg_delta", otherRevRegDelta));


    }

    @Override
    public String proverCreateMasterSecret(String masterSecretName) {
        return   new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_create_master_secret",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("master_secret_name", masterSecretName));
    }

    @Override
    public Pair<String, String> proverCreateCredentialReq(String proverDid, String credOffer, String credDef, String masterSecretId) {
        return   new RemoteCallWrapper<Pair<String, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_create_credential_req",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("prover_did", proverDid)
                                .add("cred_offer", credOffer)
                                .add("cred_def", credDef)
                                .add("master_secret_id", masterSecretId));
    }

    @Override
    public void proverSetCredentialAttrTagPolicy(String credDefId, String taAttrs, boolean retroactive) {
           new RemoteCallWrapper<Pair<String, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_set_credential_attr_tag_policy",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("cred_def_id", credDefId)
                                .add("tag_attrs", taAttrs)
                                .add("retroactive", retroactive)) ;
    }

    @Override
    public String proverGetCredentialAttrTagPolicy(String credDefId) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_get_credential_attr_tag_policy",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("cred_def_id", credDefId));

    }

    @Override
    public String proverStoreCredential(String credId, String credReqMetadata, String cred, String credDef, String revReqDef) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_store_credential",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("cred_id", credId)
                                .add("cred_req_metadata", credReqMetadata)
                                .add("cred", cred)
                                .add("cred_def", credDef)
                                .add("rev_reg_def", revReqDef));

    }

    @Override
    public String proverGetCredential(String credId) {
     return    new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_get_credential",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("cred_id", credId));
    }

    @Override
    public void proverDeleteCredential(String credId) {
           new RemoteCallWrapper(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_delete_credential",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("cred_id", credId));
    }

    @Override
    public List<String> proverGetCredentials(String filters) {
        return new RemoteCallWrapper<List<String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_get_credentials",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("filters", filters));
    }

    @Override
    public List<String> proverSearchCredential(String query) {
       return new RemoteCallWrapper<List<String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_search_credentials",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("query", query));
    }

    @Override
    public String proverGetCredentialsForProofReq(String proofRequest) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_get_credentials_for_proof_req",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("proof_request", proofRequest));
    }

    @Override
    public String proverSearchCredentialsForProofReq(String proofRequest, String extraQuery, int limitReferents) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_search_credentials_for_proof_req",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("proof_request", proofRequest)
                                .add("extra_query", extraQuery)
                                .add("limit_referents", limitReferents));
    }

    @Override
    public String proverCreateProof(String proofReq, String requestedCredentials, String masterSecretName, String schemas, String credentialDefs, String revStates) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prover_create_proof",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("proof_req", proofReq)
                                .add("requested_credentials", requestedCredentials)
                                .add("master_secret_name", masterSecretName)
                                .add("schemas", schemas)
                                .add("credential_defs", credentialDefs)
                                .add("rev_states", revStates));
    }

    @Override
    public boolean verifierVerifyProof(String proofRequest, String proof, String schemas, String credentialDefs, String revRegDefs, String revRegs) {
        return new RemoteCallWrapper<Boolean>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/verifier_verify_proof",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("proof_request", proofRequest)
                                .add("proof", proof)
                                .add("schemas", schemas)
                                .add("credential_defs", credentialDefs)
                                .add("rev_reg_defs", revRegDefs)
                                .add("rev_regs", revRegs));
    }

    @Override
    public String createRevocation(int blobStorageReaderHandle, String revRegDef, String revRegDelta, int timestamp, String credRevId) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/create_revocation_state",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("blob_storage_reader_handle", blobStorageReaderHandle)
                                .add("rev_reg_def", revRegDef)
                                .add("rev_reg_delta", revRegDelta)
                                .add("timestamp", timestamp)
                                .add("cred_rev_id", credRevId));
    }

    @Override
    public String updateRevocationState(int blobStorageReaderHandle, String revState, String revRegDef, String revRegDelta, int timestamp, String credRevId) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/update_revocation_state",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("blob_storage_reader_handle", blobStorageReaderHandle)
                                .add("rev_state", revState)
                                .add("rev_reg_def", revRegDef)
                                .add("rev_reg_delta", revRegDelta)
                                .add("timestamp", timestamp)
                                .add("cred_rev_id", credRevId));
    }

    @Override
    public String generateNonce() {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/generate_nonce");
    }

    @Override
    public String toUnqualified(String entity) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/to_unqualified",
                        RemoteParams.RemoteParamsBuilder.create()
                        .add("entity", entity));
    }


}
