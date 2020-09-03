package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.errors.sirius_exceptions.*;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CryptoProxy extends AbstractCrypto {

    AgentRPC rpc;

    public CryptoProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }


    @Override
    public String createKey(String seed, String cryptoType) {
        RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                .add("seed", seed)
                .add("crypto_type", cryptoType)
                .build();
        try {
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/create_key", params);
            if (response instanceof String) {
                return (String) response;
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
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
        RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                .add("signer_vk", signerVk).add("msg", msg).build();

        try {
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/crypto_sign", params);
            if (response instanceof byte[]) {
                return ((byte[]) response);
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }

        return new byte[0];
    }

    @Override
    public boolean cryptoVerify(String signerVk, byte[] msg, byte[] signature) {
        RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                .add("signer_vk", signerVk)
                .add("msg", msg)
                .add("signature", signature).build();
        try {
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/crypto_verify", params);
            if (response instanceof Boolean) {
                return ((Boolean) response);
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
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
        RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                .add("message", message)
                .add("recipient_verkeys", recipentVerkeys)
                .add("sender_verkey", senderVerkey).build();
        try {
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/pack_message", params);
            if (response instanceof byte[]) {
                return (byte[]) response;
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public String unpackMessage(byte[] jwe) {
        RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                .add("jwe", jwe).build();
        try {
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/unpack_message", params);
            System.out.println("unpackMessage respone = " + response);
            System.out.println("unpackMessage respone = " + response.getClass());
            if (response != null) {
                return (String) response.toString();
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }
}
