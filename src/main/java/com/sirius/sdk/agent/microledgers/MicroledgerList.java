package com.sirius.sdk.agent.microledgers;

import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.connections.BaseAgentConnection;
import com.sirius.sdk.agent.connections.RemoteCallWrapper;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.utils.JSONUtils;
import com.sirius.sdk.utils.Pair;
import org.checkerframework.checker.units.qual.A;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicroledgerList extends AbstractMicroledgerList {
    BaseAgentConnection api;
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
    public AbstractMicroledger getLedger(String name) {
        if (!this.instances.containsKey(name)) {
            checkIsExists(name);
            Microledger instance = new Microledger(name, api);
            this.instances.put(name, instance);
        }
        return this.instances.get(name);
    }

    @Override
    public void reset(String name) {
        checkIsExists(name);
        new RemoteCallWrapper<Void>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/reset",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name));
        if (this.instances.containsKey(name))
            this.instances.remove(name);

    }

    @Override
    public boolean isExists(String name) {
        return new RemoteCallWrapper<Boolean>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/is_exists",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", name));
    }

    @Override
    public byte[] leafHash(Transaction txn) {
        byte[] data = JSONUtils.JSONObjectToString(txn, true).getBytes(StandardCharsets.UTF_8);
        return new RemoteCallWrapper<byte[]>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/leaf_hash",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("data", data));
    }

    @Override
    public List<LedgerMeta> getList() {
        List<String> collection = new RemoteCallWrapper<List<String>>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers/1.0/list",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("name", "*"));
        List<LedgerMeta> res = new ArrayList<>();
        for (String s : collection) {
            res.add(new LedgerMeta(new JSONObject(s)));
        }
        return res;
    }

    @Override
    public AbstractBatchedAPI getBatched() {
        return this.batchedAPI;
    }

    public MicroledgerList(BaseAgentConnection api) {
        this.api = api;
        this.batchedAPI = new BatchedAPI(api, this.instances);
    }

    private void checkIsExists(String name) {
        if (!this.instances.containsKey(name)) {
            boolean isExists = isExists(name);
            if (!isExists)
                throw new SiriusContextError("MicroLedger with name " + name + " does not exists");
        }
    }
}
