package examples.raft.server.service.impl;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.RpcClientOptions;
import com.baidu.brpc.client.instance.Endpoint;
import com.github.wenweihu86.raft.Peer;


import com.github.wenweihu86.raft.RaftNode;
import com.github.wenweihu86.raft.models.EntryType;
import com.github.wenweihu86.raft.modelsExample.GetRequest;
import com.github.wenweihu86.raft.modelsExample.GetResponse;
import com.github.wenweihu86.raft.modelsExample.SetRequest;
import com.github.wenweihu86.raft.modelsExample.SetResponse;
import com.googlecode.protobuf.format.JsonFormat;
import examples.raft.server.ExampleStateMachine;
import examples.raft.server.service.ExampleProto;
import examples.raft.server.service.ExampleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wenweihu86 on 2017/5/9.
 */
public class ExampleServiceImpl implements ExampleService {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleServiceImpl.class);
    private static JsonFormat jsonFormat = new JsonFormat();

    private RaftNode raftNode;
    private ExampleStateMachine stateMachine;
    private String leaderId = null;
    private RpcClient leaderRpcClient = null;
    private Lock leaderLock = new ReentrantLock();

    public ExampleServiceImpl(RaftNode raftNode, ExampleStateMachine stateMachine) {
        this.raftNode = raftNode;
        this.stateMachine = stateMachine;
    }

    private void onLeaderChangeEvent() {
        LOG.info("ExampleServiceImpl onLeaderChangeEvent ");
        //TODO FIX ""
        if (raftNode.getLeaderId() != null
                && raftNode.getLeaderId() != raftNode.getLocalServer().getServerId()
                && leaderId != raftNode.getLeaderId()) {
            leaderLock.lock();
            if (leaderId != null && leaderRpcClient != null) {
                leaderRpcClient.stop();
                leaderRpcClient = null;
                leaderId = null;
            }
            leaderId = raftNode.getLeaderId();
            Peer peer = raftNode.getPeerMap().get(leaderId);
            Endpoint endpoint = new Endpoint(peer.getServer().getEndpoint().getHost(),
                    peer.getServer().getEndpoint().getPort());
            RpcClientOptions rpcClientOptions = new RpcClientOptions();
            rpcClientOptions.setGlobalThreadPoolSharing(true);
            leaderRpcClient = new RpcClient(endpoint, rpcClientOptions);
            leaderLock.unlock();
        }
    }

    @Override
    public SetResponse set(SetRequest request) {
        LOG.info("ExampleServiceImpl SetResponse ");
        SetResponse responseBuilder = new SetResponse();

        if (raftNode.getLeaderId() !=null && raftNode.getLeaderId()!="") {
            responseBuilder.setSuccess(false);
        } else if (raftNode.getLeaderId() != raftNode.getLocalServer().getServerId()) {
            onLeaderChangeEvent();
            ExampleService exampleService = BrpcProxy.getProxy(leaderRpcClient, ExampleService.class);
            SetResponse responseFromLeader = exampleService.set(request);
           //TODO responseBuilder.mergeFrom(responseFromLeader);
        } else {
            // 数据同步写入raft集群
          //TODO  byte[] data = request.toByteArray();
            byte[] data = new byte[]{};
            boolean success = raftNode.replicate(data, EntryType.ENTRY_TYPE_DATA);
            responseBuilder.setSuccess(success);
        }

        SetResponse response = responseBuilder;
        LOG.info("set request, request={}, response={}", request.toString(),
                response.toString());
        return response;
    }

    @Override
    public GetResponse get(GetRequest request) {
        LOG.info("ExampleServiceImpl GetResponse ");
        GetResponse response = stateMachine.get(request);
        LOG.info("get request, request={}, response={}", request.toString(),
                response.toString());
        return response;
    }

}
