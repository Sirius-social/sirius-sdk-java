package examples.iota;

import com.danubetech.keyformats.crypto.ByteSigner;
import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.naclJava.LibSodium;
import com.sirius.sdk.utils.StringUtils;
import foundation.identity.jsonld.JsonLDException;
import foundation.identity.jsonld.JsonLDObject;
import info.weboftrust.ldsignatures.LdProof;
import info.weboftrust.ldsignatures.signer.JcsEd25519Signature2020LdSigner;
import info.weboftrust.ldsignatures.signer.LdSigner;
import info.weboftrust.ldsignatures.suites.JcsEd25519Signature2020SignatureSuite;
import info.weboftrust.ldsignatures.verifier.JcsEd25519Signature2020LdVerifier;
import info.weboftrust.ldsignatures.verifier.LdVerifier;
import org.bitcoinj.core.Base58;
import org.iota.client.Client;
import org.iota.client.Message;
import org.iota.client.MessageId;
import org.iota.client.MessageMetadata;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class IotaPublicDidDoc {

    Logger log = Logger.getLogger(IotaPublicDidDoc.class.getName());

    private JSONObject doc = new JSONObject();
    private JSONObject meta = new JSONObject();
    AbstractCrypto crypto;
    String publicKeyBase58;
    String tag;
    String previousMessageId = "";

    static Comparator<Message> msgComparator = new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            MessageMetadata meta1 = node().getMessage().metadata(o1.id());
            MessageMetadata meta2 = node().getMessage().metadata(o2.id());
            if (meta1.milestoneIndex() < meta2.milestoneIndex())
                return -1;
            else if (meta1.milestoneIndex() > meta2.milestoneIndex())
                return 1;
            else
                return o1.id().toString().compareTo(o2.id().toString());
        }
    };

    public IotaPublicDidDoc(AbstractCrypto crypto) {
        this.crypto = crypto;
        this.publicKeyBase58 = crypto.createKey();
        this.tag = generateTag(publicKeyBase58);
        doc.put("id", "did:iota:" + tag);
    }

    private static String generateTag(String publicKeyBase58) {
        LazySodiumJava s = LibSodium.getInstance().getLazySodium();
        byte[] inputBytes = s.bytes(publicKeyBase58);
        byte[] outputBytes = new byte[32];
        s.cryptoGenericHash(outputBytes, 32, inputBytes, inputBytes.length, null, 0);
        return StringUtils.bytesToBase58String(outputBytes);
    }

    private IotaPublicDidDoc(Message msg, AbstractCrypto crypto) {
        this.crypto = crypto;
        JSONObject obj = new JSONObject(new String(msg.payload().get().asIndexation().data()));
        this.doc = obj.optJSONObject("doc");
        this.meta = obj.optJSONObject("meta");
        this.previousMessageId = msg.id().toString();
        tag = this.doc.optString("id").substring("did:iota:".length());
        JSONArray authentications = this.doc.getJSONArray("authentication");
        JSONObject authentication = (JSONObject) authentications.get(0);
        this.publicKeyBase58 = authentication.optString("publicKeyBase58");
    }

    public JSONObject getDidDoc() {
        return this.doc;
    }

    public String getDid() {
        return this.doc.optString("id");
    }

    public String getTag() {
        return this.tag;
    }

    public static IotaPublicDidDoc load(String did, AbstractCrypto crypto) {
        Message msg = loadLastValidIntegrationMessage(did);
        if (msg == null || !msg.payload().isPresent())
            return null;
        return new IotaPublicDidDoc(msg, crypto);
    }

    private static Message loadLastValidIntegrationMessage(String did) {
        String tag = tagFromId(did);
        MessageId[] fetchedMessageIds = node().getMessage().indexString(tag);
        HashMap<String, List<Message>> map = new HashMap<>();
        for (MessageId msgId : fetchedMessageIds) {
            Message msg = node().getMessage().data(msgId);
            if (msg.payload().isPresent()) {
                JSONObject obj = new JSONObject(new String (msg.payload().get().asIndexation().data()));
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
                        sorted(msgComparator).
                        collect(Collectors.toList());
                if (list.isEmpty()) {
                    return prevMessage;
                } else {
                    map.remove(prevMessageId);
                    prevMessage = list.get(list.size() - 1);
                    prevMessageId = prevMessage.id().toString();
                }
            } else {
                break;
            }
        }
        return prevMessage;
    }

    public void setEndpoint(Endpoint endpoint) {
        JSONArray services = new JSONArray();
        this.doc.put("service", services);
        JSONObject service = new JSONObject().
                put("type", "DIDCommMessaging").
                put("serviceEndpoint", endpoint.getAddress()).
                put("routingKeys", endpoint.getRoutingKeys());
        services.put(service);
    }

    public Endpoint getEndpoint() {
        if (this.doc.has("service")) {
            JSONArray services = this.doc.getJSONArray("service");
            for (Object o : services) {
                JSONObject jo = (JSONObject) o;
                if (jo.optString("type").equals("DIDCommMessaging")) {
                    String uri = jo.optString("serviceEndpoint");
                    List<String> routingKeys = new ArrayList<>();
                    if (jo.has("routingKeys")) {
                        JSONArray routingKeysJ = jo.getJSONArray("routingKeys");
                        for (Object rko : routingKeysJ) {
                            routingKeys.add((String) rko);
                        }
                    }
                    return new Endpoint(uri, routingKeys);
                }
            }
        }
        return null;
    }

    public void submit() {
        JSONObject o = generateIntegrationMessage();
        if (o == null)
            return;
        Client iota = node();
        Message message = iota.message().
                withIndexString(tag).
                withDataString(o.toString()).
                finish();
        previousMessageId = message.id().toString();
        System.out.println(
                "Did message sent: https://explorer.iota.org/mainnet/message/" + message.id());
    }

    private JSONObject generateIntegrationMessage() {
        ByteSigner byteSigner = new IndyWalletSigner(crypto, publicKeyBase58);

        JSONArray authentication = new JSONArray();
        authentication.put(new JSONObject()
                .put("id", this.doc.optString("id") + "#sign-0")
                .put("controller", this.doc.optString("id"))
                .put("type", "Ed25519VerificationKey2018")
                .put("publicKeyBase58", this.publicKeyBase58));

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss'Z'");
        this.doc.put("authentication", authentication);

        if (previousMessageId.isEmpty()) {
            this.meta.put("created", dateFormat.format(new Date()));
        } else {
            this.meta.put("previousMessageId", previousMessageId);
        }
        this.meta.put("updated", dateFormat.format(new Date()));

        LdSigner ldSigner = new JcsEd25519Signature2020LdSigner(byteSigner);
        ldSigner.setVerificationMethod(URI.create(this.doc.optString("id") + "#sign-0"));

        JSONObject resMsg = new JSONObject().
                put("doc", doc).
                put("meta", meta);
        JsonLDObject jsonLdObject = JsonLDObject.fromJson(resMsg.toString());
        JSONObject proof = null;
        try {
            proof = new JSONObject(ldSigner.sign(jsonLdObject).toJson());
            resMsg.put("proof", proof);
            return resMsg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONObject getVerificationMethod(JSONObject obj, String verificationMethodId) {
        JSONArray verificationMethods = obj.optJSONObject("doc").optJSONArray("authentication");
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
        String b58 = verificationMethod.optString("publicKeyBase58");
        String tag = tagFromId(jsonMsg.optJSONObject("doc").optString("id"));
        return tag.equals(generateTag(b58));
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
        String pubKeyB58 = verificationMethod.optString("publicKeyBase58");
        LdVerifier<JcsEd25519Signature2020SignatureSuite> ldVerifier = new JcsEd25519Signature2020LdVerifier(Base58.decode(pubKeyB58));
        JsonLDObject ldObject = JsonLDObject.fromJson(integrationMsgJson.toString());
        try {
            return ldVerifier.verify(ldObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final String MAINNET = "https://chrysalis-nodes.iota.cafe:443";
    private static Client node() {
        return Client.Builder().withNode(MAINNET).finish();
    }
}
