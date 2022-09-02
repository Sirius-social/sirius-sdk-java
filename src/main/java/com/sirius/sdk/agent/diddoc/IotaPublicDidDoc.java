package com.sirius.sdk.agent.diddoc;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.IotaUtils;
import com.sirius.sdk.utils.JcsEd25519Signature2020LdSigner;
import com.sirius.sdk.utils.JcsEd25519Signature2020LdVerifier;
import io.ipfs.multibase.Multibase;
import org.bitcoinj.core.Base58;
import org.iota.client.Client;
import org.iota.client.Message;
import org.iota.client.MessageId;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.sirius.sdk.utils.IotaUtils.generateTag;

public class IotaPublicDidDoc extends PublicDidDoc {
    Logger log = Logger.getLogger(IotaPublicDidDoc.class.getName());

    private JSONObject meta = new JSONObject();
    byte[] publicKey;
    String tag;
    String previousMessageId = "";

    public IotaPublicDidDoc(AbstractCrypto crypto) {
        this.publicKey = Base58.decode(crypto.createKey());
        this.tag = generateTag(this.publicKey);
        payload.put("id", "did:iota:" + tag);
    }

    private IotaPublicDidDoc(Message msg) {
        JSONObject obj = new JSONObject(new String(msg.payload().get().asIndexation().data()));
        this.payload = obj.optJSONObject("doc");
        this.meta = obj.optJSONObject("meta");
        this.previousMessageId = msg.id().toString();
        tag = this.payload.optString("id").substring("did:iota:".length());
        JSONObject verificationMethod = getVerificationMethod(obj);
        this.publicKey = Multibase.decode(verificationMethod.optString("publicKeyMultibase"));
    }

    public JSONObject getDidDoc() {
        return this.payload;
    }

    public String getTag() {
        return this.tag;
    }

    public static IotaPublicDidDoc load(String did) {
        Message msg = loadLastValidIntegrationMessage(did);
        if (msg == null || !msg.payload().isPresent())
            return null;
        return new IotaPublicDidDoc(msg);
    }

    private static Message loadLastValidIntegrationMessage(String did) {
        try {
            String tag = tagFromId(did);
            MessageId[] fetchedMessageIds = IotaUtils.node().getMessage().indexString(tag);
            HashMap<String, List<Message>> map = new HashMap<>();
            for (MessageId msgId : fetchedMessageIds) {
                Message msg = IotaUtils.node().getMessage().data(msgId);
                if (msg.payload().isPresent()) {
                    JSONObject obj = new JSONObject(new String(msg.payload().get().asIndexation().data()));
                    String previousMessageId = obj.optJSONObject("meta").optString("previousMessageId", "");
                    if (!map.containsKey(previousMessageId))
                        map.put(previousMessageId, Arrays.asList(msg));
                    else
                        map.get(previousMessageId).add(msg);
                }
            }

            if (!map.containsKey(""))
                return null;

            String prevMessageId = "";
            Message prevMessage = null;

            while (!map.isEmpty()) {
                if (map.containsKey(prevMessageId)) {
                    Message finalPrevMessage = prevMessage;
                    List<Message> list = map.get(prevMessageId).stream().
                            filter(m -> checkMessage(m, finalPrevMessage)).
                            sorted(IotaUtils.msgComparator).
                            collect(Collectors.toList());
                    if (list.isEmpty()) {
                        return prevMessage;
                    } else {
                        map.remove(prevMessageId);
                        prevMessage = list.get(0);
                        prevMessageId = prevMessage.id().toString();
                    }
                } else {
                    break;
                }
            }
            return prevMessage;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public boolean submitToLedger(Context context) {
        JSONObject o = generateIntegrationMessage(context.getCrypto());
        if (o == null)
            return false;
        Client iota = IotaUtils.node();
        try {
            Message message = iota.message().
                    withIndexString(tag).
                    withData(o.toString().getBytes(StandardCharsets.UTF_8)).
                    finish();
            previousMessageId = message.id().toString();
            saveToWallet(context.getNonSecrets());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private JSONObject generateIntegrationMessage(AbstractCrypto crypto) {
        JSONArray capabilityInvocation = new JSONArray();
        capabilityInvocation.put(new JSONObject()
                .put("id", this.payload.optString("id") + "#sign-0")
                .put("controller", this.payload.optString("id"))
                .put("type", "Ed25519VerificationKey2018")
                .put("publicKeyMultibase", Multibase.encode(Multibase.Base.Base58BTC, this.publicKey)));

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss'Z'");
        this.payload.put("capabilityInvocation", capabilityInvocation);

        if (previousMessageId.isEmpty()) {
            this.meta.put("created", dateFormat.format(new Date()));
        } else {
            this.meta.put("previousMessageId", previousMessageId);
        }
        this.meta.put("updated", dateFormat.format(new Date()));

        JcsEd25519Signature2020LdSigner signer = new JcsEd25519Signature2020LdSigner();
        signer.setVerificationMethod(URI.create(this.payload.optString("id") + "#sign-0"));
        JSONObject resMsg = new JSONObject().
                put("doc", payload).
                put("meta", meta);
        signer.sign(resMsg, publicKey, crypto);
        return resMsg;
    }

    private static JSONObject getVerificationMethod(JSONObject obj, String verificationMethodId) {
        JSONArray verificationMethods = obj.optJSONObject("doc").optJSONArray("capabilityInvocation");
        for (Object o : verificationMethods) {
            JSONObject verificationMethod = (JSONObject) o;
            if (verificationMethod.optString("id").equals(verificationMethodId)) {
                return verificationMethod;
            }
        }
        return null;
    }

    private static JSONObject getVerificationMethod(JSONObject obj) {
        String verificationMethodId = obj.optJSONObject("proof").optString("verificationMethod");
        return getVerificationMethod(obj, verificationMethodId);
    }

    private static String tagFromId(String id) {
        if (id.startsWith("did:iota:"))
            return id.substring("did:iota:".length());
        return id;
    }

    private static boolean isFirstMessage(JSONObject jsonMsg) {
        return jsonMsg.optJSONObject("meta").has("previousMessageId");
    }

    private static boolean checkFirstMessageTag(JSONObject jsonMsg) {
        JSONObject verificationMethod = getVerificationMethod(jsonMsg);
        if (verificationMethod == null) {
            return false;
        }
        String pubKeyMultibase = verificationMethod.optString("publicKeyMultibase");
        String tag = tagFromId(jsonMsg.optJSONObject("doc").optString("id"));
        return tag.equals(generateTag(Multibase.decode(pubKeyMultibase)));
    }

    private static boolean checkMessage(Message integrationMsg, Message prevIntegrationMsg) {
        if (prevIntegrationMsg == null)
            prevIntegrationMsg = integrationMsg;
        if (!integrationMsg.payload().isPresent() && ! prevIntegrationMsg.payload().isPresent())
            return false;
        JSONObject integrationMsgJson = new JSONObject(new String(integrationMsg.payload().get().asIndexation().data()));
        JSONObject prevIntegrationMsgJson = new JSONObject(new String(prevIntegrationMsg.payload().get().asIndexation().data()));

        if (isFirstMessage(integrationMsgJson) && !checkFirstMessageTag(integrationMsgJson))
            return false;

        String verificationMethodId = integrationMsgJson.optJSONObject("proof").optString("verificationMethod");
        JSONObject verificationMethod = getVerificationMethod(prevIntegrationMsgJson, verificationMethodId);
        if (verificationMethod == null) {
            return false;
        }
        String pubKeyMultibase = verificationMethod.optString("publicKeyMultibase");
        JcsEd25519Signature2020LdVerifier verifier = new JcsEd25519Signature2020LdVerifier(Multibase.decode(pubKeyMultibase));
        return verifier.verify(integrationMsgJson);
    }
}
