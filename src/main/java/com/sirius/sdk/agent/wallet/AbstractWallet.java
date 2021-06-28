package com.sirius.sdk.agent.wallet;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractAnonCreds;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCache;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractDID;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractLedger;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractPairwise;


public abstract class AbstractWallet {

    public abstract  AbstractDID getDid();

    public abstract AbstractCrypto getCrypto();

    public abstract AbstractCache getCache();

    public abstract AbstractLedger getLedger();


    public abstract AbstractPairwise getPairwise();

    public abstract AbstractAnonCreds getAnoncreds();

    public abstract AbstractNonSecrets getNonSecrets();
}
