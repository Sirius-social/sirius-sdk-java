package com.sirius.sdk.agent.consensus.simple.messages;

import com.goterl.lazycode.lazysodium.LazySodium;
import com.sirius.sdk.agent.microledgers.AbstractMicroledger;
import com.sirius.sdk.utils.JSONUtils;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MicroLedgerState extends JSONObject {

    public MicroLedgerState(JSONObject obj) {
        super(obj.toString());
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

    public boolean isFilled() {
        return this.keySet().containsAll(Arrays.asList("name", "seq_no", "size", "uncommitted_size", "root_hash", "uncommitted_root_hash"));
    }

    public String getName() {
        return optString("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public int getSeqNo() {
        return optInt("seq_no");
    }

    public void setSeqNo(int seqNo) {
        put("seq_no", seqNo);
    }

    public int getSize() {
        return optInt("size");
    }

    public void setSize(int size) {
        put("size", size);
    }

    public int getUncommittedSize() {
        return optInt("uncommitted_size");
    }

    public void setUncommittedSize(int uncommittedSize) {
        put("uncommitted_size", uncommittedSize);
    }

    public String getRootHash() {
        return optString("root_hash");
    }

    public void setRootHash(String rootHash) {
        put("root_hash", rootHash);
    }

    public String getUncommittedRootHash() {
        return optString("uncommitted_root_hash");
    }

    public void setUncommittedRootHash(String uncommittedRootHash) {
        put("uncommitted_root_hash", uncommittedRootHash);
    }

    public String getHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(JSONUtils.JSONObjectToString(this, true).getBytes());
            byte[] digest = md.digest();
            return LazySodium.toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


}
