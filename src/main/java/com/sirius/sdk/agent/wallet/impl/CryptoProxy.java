package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.connections.RemoteCallWrapper;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;

import java.util.List;

public class CryptoProxy extends AbstractCrypto   {

    AgentRPC rpc;

    public CryptoProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }


    @Override
    public String createKey(String seed, String cryptoType) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/create_key",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("seed", seed)
                                .add("crypto_type", cryptoType));
    }

    @Override
    public void setKeyMetadata(String verkey, String metadata) {
         new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/set_key_metadata",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("verkey", verkey).
                                add("metadata", metadata));
    }

    @Override
    public String getKeyMetadata(String verkey) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_key_metadata",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("verkey", verkey));
    }

    @Override
    public byte[] cryptoSign(String signerVk, byte[] msg) {
        return new RemoteCallWrapper<byte[]>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/crypto_sign",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("signer_vk", signerVk)
                                .add("msg", msg));
    }

    @Override
    public boolean cryptoVerify(String signerVk, byte[] msg, byte[] signature) {
        return new RemoteCallWrapper<Boolean>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/crypto_verify",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("signer_vk", signerVk)
                                .add("msg", msg)
                                .add("signature", signature));
    }

    @Override
    public byte[] anonCrypt(String recipentVk, byte[] msg) {
        return new RemoteCallWrapper<byte[]>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/anon_crypt",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("recipient_vk", recipentVk).add("msg",msg));
    }

    @Override
    public byte[] anonDecrypt(String recipientVk, byte[] encryptedMsg) {
        return new RemoteCallWrapper<byte[]>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/anon_decrypt",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("recipient_vk", recipientVk).add("encrypted_msg",encryptedMsg));
    }

    @Override
    public byte[] packMessage(Object message, List<String> recipentVerkeys, String senderVerkey) {
        return new RemoteCallWrapper<byte[]>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/pack_message",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("message", message)
                                .add("recipient_verkeys", recipentVerkeys)
                                .add("sender_verkey", senderVerkey));
    }

    @Override
    public String unpackMessage(byte[] jwe) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/unpack_message",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("jwe", jwe));
    }
}
