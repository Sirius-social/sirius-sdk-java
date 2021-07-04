package com.sirius.sdk.agent.wallet;

import com.sirius.sdk.agent.wallet.abstract_wallet.*;

public interface AbstractWallet {

    AbstractDID getDid();
    AbstractCrypto getCrypto();
    AbstractCache getCache();
    AbstractLedger getLedger();
    AbstractPairwise getPairwise();
    AbstractAnonCreds getAnoncreds();
    AbstractNonSecrets getNonSecrets();

}
