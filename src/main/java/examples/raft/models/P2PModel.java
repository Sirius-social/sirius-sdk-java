package examples.raft.models;

import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.rpc.AddressedTunnel;

public class P2PModel {
    P2PConnection p2p;
    AddressedTunnel tunnel;

    public P2PConnection getP2p() {
        return p2p;
    }

    public AddressedTunnel getTunnel() {
        return tunnel;
    }

    public P2PModel(P2PConnection p2p, AddressedTunnel tunnel) {
        this.p2p = p2p;
        this.tunnel = tunnel;
    }
}
