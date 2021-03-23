package com.sirius.sdk.agent.consensus.simple.messages;

import com.goterl.lazycode.lazysodium.LazySodium;
import com.sirius.sdk.agent.microledgers.AbstractMicroledger;
import com.sirius.sdk.utils.JSONUtils;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MicroLedgerState {
    JSONObject payload;

    public MicroLedgerState(JSONObject obj) {
        this.payload = obj;
    }

    public static MicroLedgerState fromLedger(AbstractMicroledger ledger) {
        return new MicroLedgerState(new JSONObject().
                put("name", ledger.name()).
                put("seq_no", ledger.seqNo()).
                put("size", ledger.size()).
                put("uncommitted_size", ledger.uncommittedSize()).
                put("root_hash", ledger.rootHash()).
                put("uncommitted_root_hash", ledger.uncommittedRootHash()));
    }

    public JSONObject getJSONObject() {
        return this.payload;
    }

    public boolean isFilled() {
        return payload.keySet().containsAll(Arrays.asList("name", "seq_no", "size", "uncommitted_size", "root_hash", "uncommitted_root_hash"));
    }

    public String getName() {
        return payload.optString("name");
    }

    public void setName(String name) {
        payload.put("name", name);
    }

    public int getSeqNo() {
        return payload.optInt("seq_no");
    }

    public void setSeqNo(int seqNo) {
        payload.put("seq_no", seqNo);
    }

    public int getSize() {
        return payload.optInt("size");
    }

    public void setSize(int size) {
        payload.put("size", size);
    }

    public int getUncommittedSize() {
        return payload.optInt("uncommitted_size");
    }

    public void setUncommittedSize(int uncommittedSize) {
        payload.put("uncommitted_size", uncommittedSize);
    }

    public String getRootHash() {
        return payload.optString("root_hash");
    }

    public void setRootHash(String rootHash) {
        payload.put("root_hash", rootHash);
    }

    public String getUncommittedRootHash() {
        return payload.optString("uncommitted_root_hash");
    }

    public void setUncommittedRootHash(String uncommittedRootHash) {
        payload.put("uncommitted_root_hash", uncommittedRootHash);
    }

    public String getHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(JSONUtils.JSONObjectToString(payload, true).getBytes());
            byte[] digest = md.digest();
            return LazySodium.toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


}
