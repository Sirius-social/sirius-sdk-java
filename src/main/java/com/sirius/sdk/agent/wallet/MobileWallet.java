package com.sirius.sdk.agent.wallet;

import com.sirius.sdk.agent.wallet.abstract_wallet.*;
import com.sirius.sdk.agent.wallet.impl.*;
import org.hyperledger.indy.sdk.wallet.Wallet;

public class MobileWallet implements AbstractWallet {
    AbstractAnonCreds anoncreds;
    AbstractDID did;
    AbstractCrypto crypto;
    AbstractCache cache;
    AbstractLedger ledger;
    AbstractPairwise pairwise;
    AbstractNonSecrets nonSecrets;

    public MobileWallet(Wallet wallet) {
        anoncreds = new AnonCredsMobile(wallet);
        did = new DIDMobile(wallet);
        crypto = new CryptoMobile(wallet);
        cache = new CacheMobile(wallet);
        ledger = new LedgerMobile(wallet);
        nonSecrets = new NonSecretsMobile(wallet);
        pairwise = new PairwiseMobile(wallet,nonSecrets);

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

}
