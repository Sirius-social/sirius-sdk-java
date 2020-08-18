package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCache;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.CacheOptions;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.PurgeOptions;

public class CacheProxy extends AbstractCache {


    AgentRPC rpc;

    public CacheProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }



    @Override
    public String getSchema(String poolName, String submitter_did, String id, CacheOptions options) {
        return null;
    }

    @Override
    public String getCredDef(String poolName, String submitter_did, String id, CacheOptions options) {
        return null;
    }

    @Override
    public void purgeSchemaCache(PurgeOptions options) {

    }

    @Override
    public void purgeCredDefCache(PurgeOptions options) {

    }
}
