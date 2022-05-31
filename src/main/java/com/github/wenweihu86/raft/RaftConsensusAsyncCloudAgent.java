package com.github.wenweihu86.raft;

import com.baidu.brpc.client.RpcCallback;
import com.github.wenweihu86.raft.models.*;

import com.github.wenweihu86.raft.service.RaftConsensusServiceAsync;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.utils.GsonUtils;

import java.util.ArrayList;
import java.util.concurrent.*;

public class RaftConsensusAsyncCloudAgent implements RaftConsensusServiceAsync {


    public void sendMessage(String type, String messageString,Server localServer, RpcCallback callback) {
        Message message = Message.builder().setContent(messageString).build();
        ArrayList list = new ArrayList<String>();
        message.getMessageObj().put("type",type );
        list.add(peerAgent.getCloudAgentVerkey());
        localServer.getCloudAgent().sendMessage(message, list,
                peerAgent.getEndpoint().getHost(), localServer.getCloudAgentVerkey(), new ArrayList<>());

        try {
            com.sirius.sdk.messaging.Message message1 =  read().get(10,TimeUnit.SECONDS);
            if (message1 instanceof Message) {
                if("VoteResponse".equals(message1.getMessageObj().getString("type"))){
                    String messageString2 = ((Message) message1).getContent();
                    VoteResponse voteResponse = GsonUtils.getDefaultGson().fromJson(messageString2, VoteResponse.class);
                    System.out.println("preVote VoteResponse success=" + voteResponse.tosGson());
                    if(callback!=null){
                        callback.success(voteResponse);
                    }
                    return;
                }

                if("AppendEntriesResponse".equals(message1.getMessageObj().getString("type"))){
                    String messageString2 = ((Message) message1).getContent();
                    AppendEntriesResponse voteResponse = GsonUtils.getDefaultGson().fromJson(messageString2, AppendEntriesResponse.class);
                    System.out.println("preVote AppendEntriesResponse success=" + voteResponse.tosGson());
                    if(callback!=null){
                        callback.success(voteResponse);
                    }
                    return;
                }

                if("InstallSnapshotResponse".equals(message1.getMessageObj().getString("type"))){
                    String messageString2 = ((Message) message1).getContent();
                    InstallSnapshotResponse voteResponse = GsonUtils.getDefaultGson().fromJson(messageString2, InstallSnapshotResponse.class);
                    System.out.println("preVote InstallSnapshotResponse success=" + voteResponse.tosGson());
                    if(callback!=null){
                        callback.success(voteResponse);
                    }
                    return;
                }

            }
        } catch (InterruptedException e) {
         //   e.printStackTrace();
        } catch (ExecutionException e) {
         //   e.printStackTrace();
        } catch (TimeoutException e) {
           // e.printStackTrace();
        }
        System.out.println("preVote async response fail=");
        if(callback!=null){
            callback.fail(new Exception("ERROR"));
        }

    }

    Server peerAgent;

    public RaftConsensusAsyncCloudAgent(Server server) {
        this.peerAgent = server;
    }

    @Override
    public VoteResponse preVote(VoteRequest request) {
        System.out.println("preVote request=" + request.toString());
        sendMessage("preVote", request.tosGson(),request.getServer(),null );
        return null;
    }

    @Override
    public VoteResponse requestVote(VoteRequest request) {
        System.out.println("requestVote request=" + request.toString());
        sendMessage("requestVote", request.tosGson(), request.getServer(),null);
        return null;
    }

    @Override
    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {
        System.out.println("appendEntries request=" + request.toString());
        sendMessage("appendEntries", request.tosGson(), request.getServer(),null);
        return null;
    }

    @Override
    public InstallSnapshotResponse installSnapshot(InstallSnapshotRequest request) {
        System.out.println("installSnapshot request=" + request.toString());
        sendMessage("installSnapshot", request.tosGson(), request.getServer(),null);

        return null;
    }

    CompletableFuture<com.sirius.sdk.messaging.Message> readFuture = new CompletableFuture<>();

    public CompletableFuture<com.sirius.sdk.messaging.Message> read() {
        readFuture = new CompletableFuture<>();
        return readFuture;
    }

    public void complete(com.sirius.sdk.messaging.Message data){
        readFuture.complete(data);
    }


    @Override
    public Future<VoteResponse> preVote(VoteRequest request, RpcCallback<VoteResponse> callback) {
        System.out.println("preVote async request=" + request.toString());
        sendMessage("preVote", request.tosGson(), request.getServer(),callback);

        return null;
    }

    @Override
    public Future<VoteResponse> requestVote(VoteRequest request, RpcCallback<VoteResponse> callback) {
        System.out.println("requestVote async request=" + request.toString());
        sendMessage("requestVote", request.tosGson(), request.getServer(),callback);
        return null;
    }

    @Override
    public Future<AppendEntriesResponse> appendEntries(AppendEntriesRequest request, RpcCallback<AppendEntriesResponse> callback) {
        System.out.println("appendEntries async request=" + request.toString());
        sendMessage("appendEntries", request.tosGson(), request.getServer(),callback);
        return null;
    }

    @Override
    public Future<InstallSnapshotResponse> installSnapshot(InstallSnapshotRequest request, RpcCallback<InstallSnapshotResponse> callback) {
        System.out.println("installSnapshot async request=" + request.toString());
        sendMessage("installSnapshot", request.tosGson(), request.getServer(),callback);
        return null;
    }
}
