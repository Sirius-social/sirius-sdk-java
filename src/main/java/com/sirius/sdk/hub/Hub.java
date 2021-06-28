package com.sirius.sdk.hub;

import com.sirius.sdk.agent.pairwise.AbstractPairwiseList;
import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.connections.BaseAgentConnection;
import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.wallet.abstract_wallet.*;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;

import java.io.Closeable;

public class Hub extends AbstractHub {


    public Hub(Config config) {
        super(config);
    }

    public void createAgentInstance() {
        agent = new Agent(getConfig().serverUri, getConfig().credentials, getConfig().p2p, getConfig().ioTimeout, getConfig().storage);
        agent.open();
    }

    @Override
    public void close() {
        if (agent != null)
            agent.close();
    }

}
