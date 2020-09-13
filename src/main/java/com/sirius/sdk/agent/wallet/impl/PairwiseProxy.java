package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.errors.sirius_exceptions.*;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractPairwise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PairwiseProxy extends AbstractPairwise  {
    AgentRPC rpc;

    public PairwiseProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }



    @Override
    public boolean isPairwiseExist(String theirDid) {
        return new RemoteCallWrapper<Boolean>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/is_pairwise_exists",
                        RemoteParams.RemoteParamsBuilder.create()
                .add("their_did", theirDid));

    }

    @Override
    public void createPairwise(String theirDid, String myDid, String metadata, String tags) {
         new RemoteCallWrapper(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/create_pairwise",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("their_did", theirDid)
                                .add("my_did", myDid)
                                .add("metadata", metadata)
                                .add("tags", tags));
    }

    @Override
    public List<Object> listPairwise() {
       return new RemoteCallWrapper<List<Object>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/list_pairwise");
    }

    @Override
    public String getPairwise(String thierDid) {
        return new RemoteCallWrapper<String>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_pairwise",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("their_did", thierDid));
    }

    @Override
    public void setPairwiseMetadata(String theirDid, String metadata, String tags) {
        new RemoteCallWrapper(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/set_pairwise_metadata",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("their_did", theirDid)
                                .add("metadata", metadata)
                                .add("tags", tags));
    }

    @Override
    public Pair<List<String>, Integer> search(String tags, Integer limit) {
       return  new RemoteCallWrapper<Pair<List<String>, Integer>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/search_pairwise",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("tags", tags)
                                .add("limit", limit));
    }

}

