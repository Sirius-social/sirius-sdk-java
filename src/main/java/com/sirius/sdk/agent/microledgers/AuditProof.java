package com.sirius.sdk.agent.microledgers;

import java.util.List;

public class AuditProof extends MerkleInfo {
    int ledgerSize;

    public AuditProof(String rootHash, List<String> auditPath, int ledgerSize) {
        super(rootHash, auditPath);
        this.ledgerSize = ledgerSize;
    }

    public int getLedgerSize() {
        return ledgerSize;
    }
}
