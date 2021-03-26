package com.sirius.sdk.agent.microledgers;

import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicroledgerList extends AbstractMicroledgerList {
    AgentRPC api;
    Map<String, AbstractMicroledger> instances = new HashMap<>();
    BatchedAPI batchedAPI;

    @Override
    public Pair<AbstractMicroledger, List<Transaction>> create(String name, List<Transaction> genesis) {
        Microledger instance = new Microledger(name, api);
        List<Transaction> txns = instance.init(genesis);
        this.instances.put(name, instance);
        return new Pair<>(instance, txns);
    }

    @Override
    public AbstractMicroledger getLegder(String name) {
        return null;
    }

    @Override
    public void reset(String name) {

    }

    @Override
    public boolean isExists(String name) {
        return false;
    }

    @Override
    public byte[] leafHash(Transaction txn) {
        return new byte[0];
    }

    @Override
    public List<LedgerMeta> getList() {
        return null;
    }

    @Override
    public AbstractBatchedAPI getBatched() {
        return this.batchedAPI;
    }

    public MicroledgerList(AgentRPC api) {
        this.api = api;
        this.batchedAPI = new BatchedAPI(api, this.instances);
    }
}
