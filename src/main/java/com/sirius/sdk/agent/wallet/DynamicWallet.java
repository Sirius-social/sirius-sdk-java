package com.sirius.sdk.agent.wallet;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.wallet.impl.*;
import com.sirius.sdk.errors.sirius_exceptions.*;
import org.json.JSONObject;

public class DynamicWallet {
    AnonCredsProxy anoncreds;
    DIDProxy did;
    CryptoProxy crypto;
    CacheProxy cache;
    LedgerProxy ledger;
    AgentRPC rpc;
    PairwiseProxy pairwise;
    NonSecretsProxy nonSecrets;

    public DynamicWallet(AgentRPC agentRPC) {
        this.rpc = agentRPC;
        did = new DIDProxy(rpc);
        crypto = new CryptoProxy(rpc);
        cache = new CacheProxy(rpc);
        pairwise = new PairwiseProxy(rpc);
        nonSecrets = new NonSecretsProxy(rpc);
        ledger = new LedgerProxy(rpc);
        anoncreds = new AnonCredsProxy(rpc);
    }



    public DIDProxy getDid() {
        return did;
    }

    public CryptoProxy getCrypto() {
        return crypto;
    }

    public CacheProxy getCache() {
        return cache;
    }

    public LedgerProxy getLedger() {
        return ledger;
    }


    public PairwiseProxy getPairwise() {
        return pairwise;
    }

    public AnonCredsProxy getAnoncreds() {
        return anoncreds;
    }

    public NonSecretsProxy getNonSecrets() {
        return nonSecrets;
    }

    public Object generateWalletKey(String seed){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("seed",seed);
        try {
            return rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/generate_wallet_key",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }
}

