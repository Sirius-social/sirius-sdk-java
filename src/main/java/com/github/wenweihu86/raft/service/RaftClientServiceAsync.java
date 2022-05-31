package com.github.wenweihu86.raft.service;

import com.baidu.brpc.client.RpcCallback;
import com.github.wenweihu86.raft.models.*;


import java.util.concurrent.Future;

/**
 * 用于生成client异步调用所需的proxy
 * Created by wenweihu86 on 2017/5/14.
 */
public interface RaftClientServiceAsync extends RaftClientService {

    Future<GetLeaderResponse> getLeader(
            GetLeaderRequest request,
            RpcCallback<GetLeaderResponse> callback);

    Future<GetConfigurationResponse> getConfiguration(
            GetConfigurationRequest request,
            RpcCallback<GetConfigurationResponse> callback);

    Future<AddPeersResponse> addPeers(
            AddPeersRequest request,
            RpcCallback<AddPeersResponse> callback);

    Future<RemovePeersResponse> removePeers(
            RemovePeersRequest request,
            RpcCallback<RemovePeersResponse> callback);
}
