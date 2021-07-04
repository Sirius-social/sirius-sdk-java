package com.sirius.sdk.agent.wallet;

import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.wallet.abstract_wallet.*;
import com.sirius.sdk.agent.wallet.impl.*;

public class CloudWallet implements AbstractWallet {
    AbstractAnonCreds anoncreds;
    AbstractDID did;
    AbstractCrypto crypto;
    AbstractCache cache;
    AbstractLedger ledger;
    AbstractPairwise pairwise;
    AbstractNonSecrets nonSecrets;
    AgentRPC rpc;

    public CloudWallet(AgentRPC agentRPC) {
        this.rpc = agentRPC;
        did = new DIDProxy(rpc);
        crypto = new CryptoProxy(rpc);
        cache = new CacheProxy(rpc);
        pairwise = new PairwiseProxy(rpc);
        nonSecrets = new NonSecretsProxy(rpc);
        ledger = new LedgerProxy(rpc);
        anoncreds = new AnonCredsProxy(rpc);
    }

    @Override
    public AbstractDID getDid() {
        return did;
    }

    @Override
    public AbstractCrypto getCrypto() {
        return crypto;
    }

    @Override
    public AbstractCache getCache() {
        return cache;
    }

    @Override
    public AbstractLedger getLedger() {
        return ledger;
    }

    @Override
    public AbstractPairwise getPairwise() {
        return pairwise;
    }

    @Override
    public AbstractAnonCreds getAnoncreds() {
        return anoncreds;
    }

    @Override
    public AbstractNonSecrets getNonSecrets() {
        return nonSecrets;
    }

    public Object generateWalletKey(String seed){
        RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                .add("seed",seed)
                  .build();
        try {
            return rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/generate_wallet_key",params);
        } catch (Exception siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }
}

