package examples.iota;

import com.danubetech.keyformats.crypto.ByteSigner;
import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.naclJava.LibSodium;
import com.sirius.sdk.utils.StringUtils;
import foundation.identity.jsonld.JsonLDObject;
import info.weboftrust.ldsignatures.signer.JcsEd25519Signature2020LdSigner;
import info.weboftrust.ldsignatures.signer.LdSigner;
import org.iota.client.Client;
import org.iota.client.Message;
import org.iota.client.MessageId;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IotaPublicDidDoc {

    private JSONObject payload = new JSONObject();
    Context context;
    String publicKeyBase58;
    String tag;

    public IotaPublicDidDoc(Context context) {
        this.context = context;
        publicKeyBase58 = context.getCrypto().createKey();
        LazySodiumJava s = LibSodium.getInstance().getLazySodium();
        byte[] inputBytes = s.bytes(publicKeyBase58);
        byte[] outputBytes = new byte[32];
        s.cryptoGenericHash(outputBytes, 32, inputBytes, inputBytes.length, null, 0);
        tag = StringUtils.bytesToBase58String(outputBytes);
        payload.put("id", "did:iota:" + tag);
    }

    private IotaPublicDidDoc(JSONObject payload) {
        this.payload = payload;
        tag = this.payload.optString("id").substring("did:iota:".length());
        JSONArray authentications = this.payload.getJSONArray("authentication");
        JSONObject authentication = (JSONObject) authentications.get(0);
        this.publicKeyBase58 = authentication.optString("publicKeyBase58");
    }

    public JSONObject getPayload() {
        return this.payload;
    }

    public String getDid() {
        return this.payload.optString("id");
    }

    public String getTag() {
        return this.tag;
    }

    public static IotaPublicDidDoc load(String did) {
        String tag;
        if (did.startsWith("did:iota:"))
            tag = did.substring("did:iota:".length());
        else
            tag = did;
        MessageId[] fetchedMessageIds = node().getMessage().indexString(tag);
        if (fetchedMessageIds.length > 0) {
            Message msg = node().getMessage().data(fetchedMessageIds[0]);
            if (msg.payload().isPresent()) {
                JSONObject obj = new JSONObject(new String (msg.payload().get().asIndexation().data()));
                System.out.println(obj);
                //TODO: check proof
                return new IotaPublicDidDoc(obj);
            }
        }
        return null;
    }

    public void setEndpoint(Endpoint endpoint) {
        JSONArray services = new JSONArray();
        this.payload.put("service", services);
        JSONObject service = new JSONObject().
                put("type", "DIDCommMessaging").
                put("serviceEndpoint", endpoint.getAddress()).
                put("routingKeys", endpoint.getRoutingKeys());
        services.put(service);
    }

    public Endpoint getEndpoint() {
        if (this.payload.has("service")) {
            JSONArray services = this.payload.getJSONArray("service");
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
        proof();
        Client iota = node();
        iota.message().
                withIndexString(tag).
                withDataString(this.payload.toString()).
                finish();
    }

    private void proof() {
        ByteSigner byteSigner = new IndyWalletSigner(context.getCrypto(), publicKeyBase58);

        JSONArray authentication = new JSONArray();
        authentication.put(new JSONObject()
                .put("id", this.payload.optString("id") + "#sign-0")
                .put("controller", this.payload.optString("id"))
                .put("type", "Ed25519VerificationKey2018")
                .put("publicKeyBase58", this.publicKeyBase58));

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss'Z'");
        this.payload.put("id", this.payload.optString("id"));
        this.payload.put("authentication", authentication);
        this.payload.put("created", dateFormat.format(new Date()));
        this.payload.put("updated", dateFormat.format(new Date()));

        LdSigner ldSigner = new JcsEd25519Signature2020LdSigner(byteSigner);
        ldSigner.setVerificationMethod(URI.create(this.payload.optString("id") + "#sign-0"));

        JsonLDObject jsonLdObject = JsonLDObject.fromJson(this.payload.toString());
        JSONObject proof = null;
        try {
            proof = new JSONObject(ldSigner.sign(jsonLdObject).toJson());
            this.payload.put("proof", proof);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String MAINNET = "https://chrysalis-nodes.iota.cafe:443";
    private static Client node() {
        return Client.Builder().withNode(MAINNET).finish();
    }
}
