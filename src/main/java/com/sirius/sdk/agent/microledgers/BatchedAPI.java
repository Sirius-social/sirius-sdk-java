package com.sirius.sdk.agent.microledgers;

import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.connections.RemoteCallWrapper;
import org.json.JSONObject;

import java.util.*;

public class BatchedAPI extends AbstractBatchedAPI {
    AgentRPC api;
    List<String> names = new ArrayList<>();
    Map<String, AbstractMicroledger> external = null;

    public BatchedAPI(AgentRPC api, Map<String, AbstractMicroledger> external) {
        this.api = api;
        this.external = external;
    }

    @Override
    public List<AbstractMicroledger> open(List<AbstractMicroledger> ledgers) {
        Set<String> namesToOpen = new HashSet<>();
        for (AbstractMicroledger ledger : ledgers) {
            namesToOpen.add(ledger.name());
        }

        try {
            api.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers-batched/1.0/open",
                    RemoteParams.RemoteParamsBuilder.create().
                            add("names", new ArrayList<>(namesToOpen)).
                            build());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.names = new ArrayList<>(namesToOpen);
        return getStates();
    }

    @Override
    public void close() {
        try {
            api.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers-batched/1.0/close");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<AbstractMicroledger> getStates() {
        JSONObject states = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers-batched/1.0/states");
        return returnLadgers(states);

    }

    @Override
    public List<AbstractMicroledger> append(List<Transaction> transactions, String txnTime) {
        JSONObject states = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers-batched/1.0/append_txns",
                        RemoteParams.RemoteParamsBuilder.create().
                                add("txns", transactions).
                                add("txn_time", txnTime));
        return returnLadgers(states);
    }

    @Override
    public List<AbstractMicroledger> commit() {
        JSONObject states = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers-batched/1.0/commit_txns");
        return returnLadgers(states);
    }

    @Override
    public List<AbstractMicroledger> resetUncommitted() {
        JSONObject states = new RemoteCallWrapper<JSONObject>(api){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/microledgers-batched/1.0/reset_uncommitted");
        return returnLadgers(states);
    }

    private List<AbstractMicroledger> returnLadgers(JSONObject states) {
        List<AbstractMicroledger> resp = new ArrayList<>();
        for (String name : this.names) {
            JSONObject state = states.optJSONObject(name);
            Microledger ledger = new Microledger(name, api, state);
            if (this.external != null) {
                if (this.external.containsKey(name)) {
                    ledger.assignTo(this.external.get(name));
                } else {
                    this.external.put(name, ledger);
                }
            }
            resp.add(ledger);
        }
        return resp;
    }
}
