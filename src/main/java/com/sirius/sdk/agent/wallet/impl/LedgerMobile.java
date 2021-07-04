package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractLedger;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.NYMRole;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.List;

public class LedgerMobile extends AbstractLedger {

    public LedgerMobile(Wallet wallet) {

    }

    @Override
    public Pair<Boolean, String> readNym(String poolName, String submitterDid, String targetDid) {
        return null;
    }

    @Override
    public Pair<Boolean, String> readAttribute(String poolName, String submitterDid, String targetDid, String name) {
        return null;
    }

    @Override
    public Pair<Boolean, String> writeNum(String poolName, String submitterDid, String targetDid, String verKey, String alias, NYMRole role) {
        return null;
    }

    @Override
    public Pair<Boolean, String> registerSchema(String poolName, String submitterDid, AnonCredSchema data) {
        return null;
    }

    @Override
    public Pair<Boolean, String> registerCredDef(String poolName, String submitterDid, Object data) {
        return null;
    }

    @Override
    public Pair<Boolean, String> writeAttribute(String poolName, String submitterDid, String targetDid, String name, Object value) {
        return null;
    }

    @Override
    public String signAndSubmit(String poolName, String submitterDid, Object request) {
        return null;
    }

    @Override
    public String submitRequest(String poolName, Object request) {
        return null;
    }

    @Override
    public String submitAction(String poolName, String request, List<String> nodes, Integer timeout) {
        return null;
    }

    @Override
    public String signRequest(String submitterDid, Object request) {
        return null;
    }

    @Override
    public String multiSignRequest(String submitterDid, String request) {
        return null;
    }

    @Override
    public String buildGetDddoRequest(String submitterDid, String targetDid) {
        return null;
    }

    @Override
    public String buildNymRequest(String submitterDid, String targetDid, String verKey, String alias, NYMRole role) {
        return null;
    }

    @Override
    public String buildAttribRequest(String submitterDid, String targetDid, String xhash, String raw, String enc) {
        return null;
    }

    @Override
    public String buildGetAttribRequest(String submitterDid, String targetDid, String raw, String xhash, String enc) {
        return null;
    }

    @Override
    public String buildGetNymRequest(String submitterDid, String targetDid) {
        return null;
    }

    @Override
    public String parseGetNymResponse(Object response) {
        return null;
    }

    @Override
    public String buildSchemaRequest(String submitterDid, String data) {
        return null;
    }

    @Override
    public String buildGetSchemaRequest(String submitterDid, String id) {
        return null;
    }

    @Override
    public Pair<String, String> parseGetSchemaResponse(String getSchemaResponse) {
        return null;
    }

    @Override
    public String buildCredDefRequest(String submitterDid, Object data) {
        return null;
    }

    @Override
    public String buildGetCredDefRequest(String submitterDid, String id) {
        return null;
    }

    @Override
    public Pair<String, String> parseGetCredDefResponse(String getCredDefResponse) {
        return null;
    }

    @Override
    public String buildNodeRequest(String submitterDid, String targetDid, String data) {
        return null;
    }

    @Override
    public String buildGetValidatorInfoRequest(String submitterDid) {
        return null;
    }

    @Override
    public String buildGetTxnRequest(String submitterDid, String ledgerType, int seq_no) {
        return null;
    }

    @Override
    public String buildPoolConfigRequest(String submitterDid, boolean writes, boolean force) {
        return null;
    }

    @Override
    public String buildPoolRestart(String submitterDid, String action, String datetime) {
        return null;
    }

    @Override
    public String buildPoolUpgradeRequest(String submitter_did, String name, String version, String action, String sha256, Integer timeout, String schedule, String justification, boolean reinstall, boolean force, String packageString) {
        return null;
    }

    @Override
    public String buildRevocRegDefRequest(String submitter_did, String data) {
        return null;
    }

    @Override
    public String buildGetRevocRegDefRequest(String submitter_did, String revRegDefId) {
        return null;
    }

    @Override
    public Pair<String, String> parseGetRevocRegDefResponse(String getRevocRefDefResponse) {
        return null;
    }

    @Override
    public String buildRevocRegEntryRequest(String submitterDid, String revocRegDefId, String revDefType, String value) {
        return null;
    }

    @Override
    public String buildGetREvocRegRequest(String submitterDid, String revocRegDefId, int timestamp) {
        return null;
    }

    @Override
    public Triple<String, String, Integer> parseGetRevocRegResponse(String getRevocRegResponse) {
        return null;
    }

    @Override
    public String buildGetRevocRegDeltaRequest(String submitterDid, String revocRegDefId, Integer from, int to) {
        return null;
    }

    @Override
    public Triple<String, String, Integer> parseGetRevocRegDeltaResponse(String getRevocRegDeltaResponse) {
        return null;
    }

    @Override
    public String responseMetadata(String response) {
        return null;
    }

    @Override
    public String buildAuthRuleRequest(String submitterDid, String txnType, String action, String field, String old_value, String new_value, String constraint) {
        return null;
    }

    @Override
    public String buildAuthRulesRequest(String submitterDid, String data) {
        return null;
    }

    @Override
    public String buildGetAuthRuleRequest(String submitterDid, String txnType, String action, String field, String old_value, String new_value) {
        return null;
    }

    @Override
    public String buildTxnAuthorAgreementRequest(String submitterDid, String text, String version, Integer ratification_ts, Integer retirement_ts) {
        return null;
    }

    @Override
    public String buildDisableAllTxnAuthorAgreementsRequest(String submitter) {
        return null;
    }

    @Override
    public String buildGetTxnAuthorAgreementRequest(String submitterDid, String data) {
        return null;
    }

    @Override
    public String buildAcceptanceMechanismsRequest(String submitterDid, String aml, String version, String amlContext) {
        return null;
    }

    @Override
    public String buildGetAcceptanceMechanismsRequest(String submitterDid, Integer timestamp, String version) {
        return null;
    }

    @Override
    public String appendTxnAuthorAgreementAcceptanceToRequest(String request, String text, String version, String taa_digest, String mechanism, int time) {
        return null;
    }

    @Override
    public String appendRequestEndorser(String request, String endorserDid) {
        return null;
    }
}
