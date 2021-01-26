package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractLedger;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.NYMRole;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;

import java.util.List;

public class LedgerProxy extends AbstractLedger  {


    AgentRPC rpc;

    public LedgerProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }


    @Override
    public Pair<Boolean, String> readNym(String poolName, String submitterDid, String targetDid) {
        return new RemoteCallWrapper<Pair<Boolean, String>>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/read_nym",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("pool_name", poolName).add("submitter_did", submitterDid).add("target_did", targetDid));
    }

    @Override
    public Pair<Boolean, String> readAttribute(String poolName, String submitterDid, String targetDid, String name) {
        return new RemoteCallWrapper<Pair<Boolean, String>>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/read_attribute",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("pool_name", poolName).add("submitter_did", submitterDid)
                        .add("target_did", targetDid).add("name",name));

    }

    @Override
    public Pair<Boolean, String> writeNum(String poolName, String submitterDid, String targetDid, String verKey, String alias, NYMRole role) {
        return new RemoteCallWrapper<Pair<Boolean, String>>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/write_nym",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("pool_name", poolName).add("submitter_did", submitterDid)
                        .add("target_did", targetDid).add("ver_key",verKey).
                        add("alias",alias).add("role",role));
    }

    @Override
    public Pair<Boolean, String> registerSchema(String poolName, String submitterDid, AnonCredSchema data) {
        return new RemoteCallWrapper<Pair<Boolean, String>>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/register_schema",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("pool_name", poolName).add("submitter_did", submitterDid).add("data", data));
    }

    @Override
    public Pair<Boolean, String> registerCredDef(String poolName, String submitterDid, Object data) {
        return new RemoteCallWrapper<Pair<Boolean, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/register_cred_def",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("pool_name", poolName).add("submitter_did", submitterDid)
                        .add("data", data));
    }

    @Override
    public Pair<Boolean, String> writeAttribute(String poolName, String submitterDid, String targetDid, String name, Object value) {
        return new RemoteCallWrapper<Pair<Boolean, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/write_attribute",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("pool_name", poolName).add("submitter_did", submitterDid)
                                .add("target_did", targetDid).add("name", name).add("value",value));
    }

    @Override
    public String signAndSubmit(String poolName, String submitterDid, Object request) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/sign_and_submit_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("pool_name", poolName).add("submitter_did", submitterDid)
                                .add("request", request));

    }

    @Override
    public String submitRequest(String poolName, String request) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin/1.0/submit_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("pool_name", poolName)
                                .add("request", request));
    }

    @Override
    public String submitAction(String poolName, String request, List<String> nodes, Integer timeout) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/submit_action",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("pool_name", poolName)
                                .add("request", request)
                                .add("nodes", nodes)
                                .add("timeout", timeout));
    }

    @Override
    public String signRequest(String submitterDid, String request) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/sign_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("request", request));
    }

    @Override
    public String multiSignRequest(String submitterDid, String request) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/multi_sign_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("request", request));
    }

    @Override
    public String buildGetDddoRequest(String submitterDid, String targetDid) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_ddo_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("target_did", targetDid));
    }

    @Override
    public String buildNymRequest(String submitterDid, String targetDid, String verKey, String alias, NYMRole role) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_nym_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("target_did", targetDid)
                                .add("ver_key", verKey)
                                .add("alias", alias)
                                .add("role", role));
    }

    @Override
    public String buildAttribRequest(String submitterDid, String targetDid, String xhash, String raw, String enc) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_attrib_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("target_did", targetDid)
                                .add("xhash", xhash)
                                .add("raw", raw)
                                .add("enc", enc));
    }

    @Override
    public String buildGetAttribRequest(String submitterDid, String targetDid, String raw, String xhash, String enc) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_attrib_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("target_did", targetDid)
                                .add("raw", raw)
                                .add("xhash", xhash)
                                .add("enc", enc));
    }

    @Override
    public String buildGetNymRequest(String submitterDid, String targetDid) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_nym_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("target_did", targetDid));
    }

    @Override
    public String parseGetNymResponse(Object response) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/parse_get_nym_response",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("response", response));
    }

    @Override
    public String buildSchemaRequest(String submitterDid, String data) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_schema_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("data", data));
    }

    @Override
    public String buildGetSchemaRequest(String submitterDid, String id) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_schema_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("id", id));
    }

    @Override
    public Pair<String, String> parseGetSchemaResponse(String getSchemaResponse) {
        return new RemoteCallWrapper<Pair<String, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/parse_get_schema_response",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("get_schema_response", getSchemaResponse));
    }

    @Override
    public String buildCredDefRequest(String submitterDid, Object data) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin/1.0/build_cred_def_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("data", data));
    }

    @Override
    public String buildGetCredDefRequest(String submitterDid, String id) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_cred_def_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("id", id));
    }

    @Override
    public Pair<String, String> parseGetCredDefResponse(String getCredDefResponse) {
        return new RemoteCallWrapper<Pair<String, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/parse_get_cred_def_response",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("get_cred_def_response", getCredDefResponse));
    }

    @Override
    public String buildNodeRequest(String submitterDid, String targetDid, String data) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_node_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("target_did", targetDid)
                                .add("data", data));
    }

    @Override
    public String buildGetValidatorInfoRequest(String submitterDid) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_validator_info_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid));
    }

    @Override
    public String buildGetTxnRequest(String submitterDid, String ledgerType, int seq_no) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_txn_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("ledger_type", ledgerType)
                                .add("seq_no", seq_no));
    }

    @Override
    public String buildPoolConfigRequest(String submitterDid, boolean writes, boolean force) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_pool_config_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("writes", writes)
                                .add("force", force));
    }

    @Override
    public String buildPoolRestart(String submitterDid, String action, String datetime) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_pool_restart_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("action", action)
                                .add("datetime", datetime));
    }

    @Override
    public String buildPoolUpgradeRequest(String submitter_did, String name, String version, String action,
                                          String sha256, Integer timeout, String schedule, String justification,
                                          boolean reinstall, boolean force, String packageString) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_pool_upgrade_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitter_did)
                                .add("name", name)
                                .add("version", version)
                                .add("action", action)
                                .add("_sha256", sha256)
                                .add("_timeout", timeout)
                                .add("schedule", schedule)
                                .add("justification", justification)
                                .add("reinstall", reinstall)
                                .add("force", force)
                                .add("packageString", packageString));
    }

    @Override
    public String buildRevocRegDefRequest(String submitter_did, String data) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_revoc_reg_def_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitter_did)
                                .add("data", data));
    }

    @Override
    public String buildGetRevocRegDefRequest(String submitter_did, String revRegDefId) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_revoc_reg_def_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitter_did)
                                .add("rev_reg_def_id", revRegDefId));
    }

    @Override
    public Pair<String, String> parseGetRevocRegDefResponse(String getRevocRefDefResponse) {
        return new RemoteCallWrapper<Pair<String, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/parse_get_revoc_reg_def_response",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("get_revoc_ref_def_response", getRevocRefDefResponse));
    }

    @Override
    public String buildRevocRegEntryRequest(String submitterDid, String revocRegDefId, String revDefType, String value) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_revoc_reg_entry_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("revoc_reg_def_id", revocRegDefId)
                                .add("rev_def_type", revDefType)
                                .add("value", value));
    }

    @Override
    public String buildGetREvocRegRequest(String submitterDid, String revocRegDefId, int timestamp) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_revoc_reg_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("revoc_reg_def_id", revocRegDefId)
                                .add("timestamp", timestamp));

    }

    @Override
    public Triple<String, String, Integer> parseGetRevocRegResponse(String getRevocRegResponse) {
        return new RemoteCallWrapper<Triple<String, String, Integer>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/parse_get_revoc_reg_response",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("get_revoc_reg_response", getRevocRegResponse));
    }

    @Override
    public String buildGetRevocRegDeltaRequest(String submitterDid, String revocRegDefId, Integer from, int to) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_revoc_reg_delta_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("revoc_reg_def_id", revocRegDefId)
                                .add("from_", from)
                                .add("to", to));

    }

    @Override
    public Triple<String, String, Integer> parseGetRevocRegDeltaResponse(String getRevocRegDeltaResponse) {
        return new RemoteCallWrapper<Triple<String, String, Integer>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/parse_get_revoc_reg_delta_response",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("get_revoc_reg_delta_response", getRevocRegDeltaResponse));
    }

    @Override
    public String responseMetadata(String response) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_response_metadata",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("response", response));
    }

    @Override
    public String buildAuthRuleRequest(String submitterDid, String txnType, String action,
                                       String field, String old_value, String new_value, String constraint) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_auth_rule_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("txn_type", txnType)
                                .add("action", action)
                                .add("field", field)
                                .add("old_value", old_value)
                                .add("new_value", new_value)
                                .add("constraint", constraint));
    }

    @Override
    public String buildAuthRulesRequest(String submitterDid, String data) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_auth_rules_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("data", data));
    }

    @Override
    public String buildGetAuthRuleRequest(String submitterDid, String txnType, String action,
                                          String field, String old_value, String new_value) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_auth_rule_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("txn_type", txnType)
                                .add("action", action)
                                .add("field", field)
                                .add("old_value", old_value)
                                .add("new_value", new_value));
    }

    @Override
    public String buildTxnAuthorAgreementRequest(String submitterDid, String text, String version,
                                                 Integer ratification_ts, Integer retirement_ts) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_txn_author_agreement_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("text", text)
                                .add("version", version)
                                .add("ratification_ts", ratification_ts)
                                .add("retirement_ts", retirement_ts));
    }

    @Override
    public String buildDisableAllTxnAuthorAgreementsRequest(String submitter_did) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_disable_all_txn_author_agreements_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitter_did));
    }

    @Override
    public String buildGetTxnAuthorAgreementRequest(String submitterDid, String data) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_txn_author_agreement_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("data", data));
    }

    @Override
    public String buildAcceptanceMechanismsRequest(String submitterDid, String aml, String version, String amlContext) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_acceptance_mechanisms_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("aml", aml)
                                .add("version", version)
                                .add("amlContext", amlContext));
    }

    @Override
    public String buildGetAcceptanceMechanismsRequest(String submitterDid, Integer timestamp, String version) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/build_get_acceptance_mechanisms_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("submitter_did", submitterDid)
                                .add("timestamp", timestamp)
                                .add("version", version));
    }

    @Override
    public String appendTxnAuthorAgreementAcceptanceToRequest(String request, String text, String version,
                                                              String taa_digest, String mechanism, int time) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/append_txn_author_agreement_acceptance_to_request",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("request", request)
                                .add("text", text)
                                .add("version", version)
                                .add("taa_digest", taa_digest)
                                .add("mechanism", mechanism)
                                .add("time", time));
    }

    @Override
    public String appendRequestEndorser(String request, String endorserDid) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/append_request_endorser",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("request", request)
                                .add("endorserDid", endorserDid));
    }


}
