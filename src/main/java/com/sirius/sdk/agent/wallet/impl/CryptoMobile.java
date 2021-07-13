package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.crypto.CryptoJSONParameters;
import org.hyperledger.indy.sdk.crypto.CryptoResults;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        try {
            Crypto.setKeyMetadata(wallet, verkey, metadata).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getKeyMetadata(String verkey) {
        try {
            return Crypto.getKeyMetadata(wallet, verkey).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] cryptoSign(String signerVk, byte[] msg) {
        try {
            return Crypto.cryptoSign(wallet,signerVk,msg).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public boolean cryptoVerify(String signerVk, byte[] msg, byte[] signature) {
        try {
            return Crypto.cryptoVerify(signerVk,msg,signature).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public byte[] anonCrypt(String recipentVk, byte[] msg) {
        try {
            return Crypto.anonCrypt(recipentVk,msg).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public byte[] anonDecrypt(String recipientVk, byte[] encryptedMsg) {
        try {
            return  Crypto.anonDecrypt(wallet,recipientVk,encryptedMsg).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public byte[] packMessage(Object message, List<String> recipentVerkeys, String senderVerkey) {
        JSONArray jsonArray = new JSONArray();
        for (String key : recipentVerkeys) {
            jsonArray.put(key);
        }
        String messageString = message.toString();
        byte[] byteMessage = messageString.getBytes(StandardCharsets.UTF_8);
        try {
            return Crypto.packMessage(wallet, jsonArray.toString(), senderVerkey, byteMessage).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public String unpackMessage(byte[] jwe) {
        try {
            return new String(Crypto.unpackMessage(wallet, jwe).get(timeoutSec, TimeUnit.SECONDS), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
