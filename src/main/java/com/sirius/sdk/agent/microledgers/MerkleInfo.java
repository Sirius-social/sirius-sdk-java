package com.sirius.sdk.agent.microledgers;

import java.util.List;

public class MerkleInfo {
    String rootHash;
    List<String> auditPath;

    public MerkleInfo(String rootHash, List<String> auditPath) {
        this.rootHash = rootHash;
        this.auditPath = auditPath;
    }

    public String getRootHash() {
        return rootHash;
    }

    public List<String> getAuditPath() {
        return auditPath;
    }
}
