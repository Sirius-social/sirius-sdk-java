package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;

import java.util.List;

public class CryptoProxy extends AbstractCrypto {

    AgentRPC rpc;

    public CryptoProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }


    @Override
    public String createKey(String seed, String cryptoType) {
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
