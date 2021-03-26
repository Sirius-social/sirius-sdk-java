package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.agent.microledgers.Utils;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Base58;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.sirius.sdk.agent.aries_rfc.Utils.verifySigned;

public class BaseInitLedgerMessage extends SimpleConsensusMessage {

    static {
        Message.registerMessageClass(BaseInitLedgerMessage.class, SimpleConsensusMessage.PROTOCOL, "initialize");
    }

    public BaseInitLedgerMessage(String msg) {
        super(msg);
    }

    public JSONObject ledgerHash() {
        return getMessageObj().optJSONObject("ledger~hash");
    }

    public JSONObject getLedger() {
        return getMessageObj().optJSONObject("ledger");
    }

    public JSONArray signatures() {
        JSONArray res = getMessageObj().optJSONArray("signatures");
        return res != null ? res : new JSONArray();
    }

    public JSONObject checkSignatures(AbstractCrypto api, String participant) throws SiriusContextError, SiriusValidationError {
        if (ledgerHash() == null) {
            throw new SiriusContextError("Ledger Hash description is empty");
        }

        JSONArray signatures;
        if (participant.isEmpty()) {
            signatures = signatures();
        } else {
            signatures = new JSONArray();
            for(Object s : signatures()) {
                if (((JSONObject)s).optString("participant").equals(participant)) {
                    signatures.put(s);
                }
            }
        }

        if (signatures.isEmpty()) {
            throw new SiriusContextError("Signatures list is empty!");
        }

        JSONObject response = new JSONObject();
        for (Object o : signatures) {
            JSONObject item = (JSONObject) o;
            Pair<JSONObject, Boolean> regSignRes = verifySigned(api, item.optJSONObject("signature"));
            JSONObject signedLedgerHash = regSignRes.first;
            if (!regSignRes.second) {
                throw new SiriusValidationError("Invalid Sign for participant: " + item.optString("participant"));
            }
            if (!signedLedgerHash.similar(this.ledgerHash())) {
                throw new SiriusValidationError("NonConsistent Ledger hash for participant: " + item.optString("participant"));
            }
            response.put(item.optString("participant"), signedLedgerHash);
        }
        return response;
    }

    public JSONObject checkSignatures(AbstractCrypto api) throws SiriusContextError, SiriusValidationError {
        return checkSignatures(api, "");
    }

    public static abstract class Builder<B extends Builder<B>> extends SimpleConsensusMessage.Builder<B> {
        String ledgerName = null;
        String rootHash = null;
        List<Transaction> genesis = null;

        public B setLedgerName(String ledgerName) {
            this.ledgerName = ledgerName;
            return self();
        }

        public B setRootHash(String rootHash) {
            this.rootHash = rootHash;
            return self();
        }

        public B setGenesis(List<Transaction> genesis) {
            this.genesis = genesis;
            return self();
        }

        protected Builder() {}

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            JSONObject ledger = new JSONObject();
            if (ledgerName != null) {
                ledger.put("name", ledgerName);
            }
            if (rootHash != null) {
                ledger.put("root_hash", rootHash);
            }

            if (genesis != null) {
                JSONArray gArr = new JSONArray();
                for (Transaction tr : genesis) {
                    gArr.put(tr);
                }
                ledger.put("genesis", gArr);
            }

            if (!ledger.isEmpty()) {
                jsonObject.put("ledger", ledger);
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    String base58 = Base58.encode(digest.digest(Utils.serializeOrdering(ledger)));
                    jsonObject.put("ledger~hash", new JSONObject().
                            put("func", "sha256").
                            put("base58", base58));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            return jsonObject;
        }
    }
}
