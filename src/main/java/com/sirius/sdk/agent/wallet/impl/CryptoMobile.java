package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.crypto.CryptoJSONParameters;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CryptoMobile extends AbstractCrypto {
    Wallet wallet;
    int timeoutSec = 60;

    public CryptoMobile(Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public String createKey(String seed, String cryptoType) {
        try {
            return Crypto.createKey(wallet, new CryptoJSONParameters.CreateKeyJSONParameter(seed, cryptoType).toJson()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setKeyMetadata(String verkey, String metadata) {

    }

    @Override
    public String getKeyMetadata(String verkey) {
        return null;
    }

    @Override
    public byte[] cryptoSign(String signerVk, byte[] msg) {
        return new byte[0];
    }

    @Override
    public boolean cryptoVerify(String signerVk, byte[] msg, byte[] signature) {
        return false;
    }

    @Override
    public byte[] anonCrypt(String recipentVk, byte[] msg) {
        return new byte[0];
    }

    @Override
    public byte[] anonDecrypt(String recipientVk, byte[] encryptedMsg) {
        return new byte[0];
    }

    @Override
    public byte[] packMessage(Object message, List<String> recipentVerkeys, String senderVerkey) {
        return new byte[0];
    }

    @Override
    public String unpackMessage(byte[] jwe) {
        return null;
    }
}
