package com.sirius.sdk.encryption;

import com.danubetech.keyformats.crypto.ByteSigner;
import com.danubetech.keyformats.jose.JWSAlgorithm;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;

import java.security.GeneralSecurityException;

public class IndyWalletSigner extends ByteSigner {
    AbstractCrypto crypto;
    String verkey;

    public IndyWalletSigner(AbstractCrypto crypto, String verkey) {
        super(JWSAlgorithm.EdDSA);
        this.crypto = crypto;
        this.verkey = verkey;
    }

    @Override
    protected byte[] sign(byte[] bytes) throws GeneralSecurityException {
        return this.crypto.cryptoSign(this.verkey, bytes);
    }
}
