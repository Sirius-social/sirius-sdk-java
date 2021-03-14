package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.connections.RemoteCallWrapper;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.List;

public class NonSecretsProxy extends AbstractNonSecrets  {

    AgentRPC rpc;

    public NonSecretsProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }


    @Override
    public void addWalletRecord(String type, String id, String value, String tags) {
        JSONObject tagObject = null;
        if(tags!=null){
            tagObject = new JSONObject(tags);
        }
        new RemoteCallWrapper<Void>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/add_wallet_record",
                RemoteParams.RemoteParamsBuilder.create()
                .add("type_", type).add("id_", id).add("value",value).add("tags", tagObject));
    }

    @Override
    public void updateWalletRecordValue(String type, String id, String value) {
        new RemoteCallWrapper<Void>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/update_wallet_record_value",
                RemoteParams.RemoteParamsBuilder.create()
                .add("type_", type).add("id_", id).add("value", value));
    }

    @Override
    public void updateWalletRecordTags(String type, String id, String tags) {
        JSONObject tagObject = null;
        if(tags!=null){
            tagObject = new JSONObject(tags);
        }
        new RemoteCallWrapper<Void>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/update_wallet_record_tags",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("type_", type).add("id_", id).add("tags", tagObject));
    }

    @Override
    public void addWalletRecordTags(String type, String id, String tags) {
        JSONObject tagObject = null;
        if(tags!=null){
            tagObject = new JSONObject(tags);
        }
        new RemoteCallWrapper<Void>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/add_wallet_record_tags",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("type_", type).add("id_", id).add("tags", tagObject));
    }

    @Override
    public void deleteWalletRecord(String type, String id, List<String> tagNames) {
        new RemoteCallWrapper<Void>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/delete_wallet_record_tags",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("type_", type).add("id_", id).add("tag_names", tagNames));
    }

    @Override
    public void deleteWalletRecord(String type, String id) {
        new RemoteCallWrapper<Void>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/delete_wallet_record",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("type_", type).add("id_", id));
    }

    @Override
    public String getWalletRecord(String type, String id, RetrieveRecordOptions options) {
       return new RemoteCallWrapper<String>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_wallet_record",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("type_", type).add("id_", id).add("options", options));
    }

    @Override
    public Pair<List<String>, Integer> walletSearch(String type, String query, RetrieveRecordOptions options, int limit) {
        JSONObject queryObject = null;
        if(query!=null){
            queryObject = new JSONObject(query);
        }
        return new RemoteCallWrapper<Pair<List<String>, Integer>>(rpc){}.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/wallet_search",
                RemoteParams.RemoteParamsBuilder.create()
                        .add("type_", type).add("query", queryObject).add("options", options).add("limit",limit));
    }


}
