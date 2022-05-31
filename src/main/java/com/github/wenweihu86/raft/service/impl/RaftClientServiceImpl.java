package com.github.wenweihu86.raft.service.impl;

import com.github.wenweihu86.raft.Peer;
import com.github.wenweihu86.raft.RaftNode;
import com.github.wenweihu86.raft.models.Server;
import com.github.wenweihu86.raft.models.*;

import com.github.wenweihu86.raft.service.RaftClientService;
import com.github.wenweihu86.raft.util.ConfigurationUtils;
import com.googlecode.protobuf.format.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenweihu86 on 2017/5/14.
 */
public class RaftClientServiceImpl implements RaftClientService {
    private static final Logger LOG = LoggerFactory.getLogger(RaftClientServiceImpl.class);
    private static final JsonFormat jsonFormat = new JsonFormat();

    private RaftNode raftNode;

    public RaftClientServiceImpl(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    @Override
    public GetLeaderResponse getLeader(GetLeaderRequest request) {
        LOG.info("RaftClientServiceImpl getLeader request");
        GetLeaderResponse responseBuilder = new GetLeaderResponse();
        responseBuilder.setResCode(ResCode.RES_CODE_SUCCESS);
        Endpoint endPointBuilder = new Endpoint();
        raftNode.getLock().lock();
        try {
            String leaderId = raftNode.getLeaderId();
            if (leaderId == "") {
                responseBuilder.setResCode(ResCode.RES_CODE_FAIL);
            } else if (leaderId == raftNode.getLocalServer().getServerId()) {
                endPointBuilder.setHost(raftNode.getLocalServer().getEndpoint().getHost());
                endPointBuilder.setPort(raftNode.getLocalServer().getEndpoint().getPort());
            } else {
                Configuration configuration = raftNode.getConfiguration();
                for (Server server : configuration.getServersList()) {
                    if (server.getServerId() == leaderId) {
                        endPointBuilder.setHost(server.getEndpoint().getHost());
                        endPointBuilder.setPort(server.getEndpoint().getPort());
                        break;
                    }
                }
            }
        } finally {
            raftNode.getLock().unlock();
        }
        responseBuilder.setLeader(endPointBuilder);
        GetLeaderResponse response = responseBuilder;
        LOG.info("getLeader response={}", response.toString());
        return responseBuilder;
    }

    @Override
    public GetConfigurationResponse getConfiguration(GetConfigurationRequest request) {
        LOG.info("getConfiguration");
        GetConfigurationResponse responseBuilder
                = new GetConfigurationResponse();
        responseBuilder.setResCode(ResCode.RES_CODE_SUCCESS);
        raftNode.getLock().lock();
        try {
            Configuration configuration = raftNode.getConfiguration();
            Server leader = ConfigurationUtils.getServer(configuration, raftNode.getLeaderId());
            responseBuilder.setLeader(leader);
            responseBuilder.addAllServers(configuration.getServersList());
        } finally {
            raftNode.getLock().unlock();
        }
        GetConfigurationResponse response = responseBuilder;
        LOG.info("getConfiguration request={} response={}",
                request, response.toString());

        return response;
    }

    @Override
    public AddPeersResponse addPeers(AddPeersRequest request) {
        LOG.info("RaftClientServiceImpl addPeers request");
        AddPeersResponse responseBuilder = new AddPeersResponse();
        responseBuilder.setResCode(ResCode.RES_CODE_FAIL);
        if (request.getServersCount() == 0
                 ) {
            LOG.warn("added server's size can only multiple of 2");
            responseBuilder.setResMsg("added server's size can only multiple of 2");
            return responseBuilder;
        }
        for (Server server : request.getServersList()) {
            if (raftNode.getPeerMap().containsKey(server.getServerId())) {
                LOG.warn("already be added/adding to configuration");
                responseBuilder.setResMsg("already be added/adding to configuration");
                return responseBuilder;
            }
        }
        List<Peer> requestPeers = new ArrayList<>(request.getServersCount());
        for (Server server : request.getServersList()) {
            final Peer peer = new Peer(server);
            peer.setNextIndex(1);
            requestPeers.add(peer);
            raftNode.getPeerMap().putIfAbsent(server.getServerId(), peer);
            raftNode.getExecutorService().submit(new Runnable() {
                @Override
                public void run() {
                    raftNode.appendEntries(peer);
                }
            });
        }

        int catchUpNum = 0;
        raftNode.getLock().lock();
        try {
            while (catchUpNum < requestPeers.size()) {
                try {
                    raftNode.getCatchUpCondition().await();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                catchUpNum = 0;
                for (Peer peer : requestPeers) {
                    if (peer.isCatchUp()) {
                        catchUpNum++;
                    }
                }
                if (catchUpNum == requestPeers.size()) {
                    break;
                }
            }
        } finally {
            raftNode.getLock().unlock();
        }

        if (catchUpNum == requestPeers.size()) {
            raftNode.getLock().lock();
            byte[] configurationData;
            Configuration newConfiguration;
            try {
                newConfiguration = new Configuration(raftNode.getConfiguration())
                        .addAllServers(request.getServersList());
               //TODO configurationData = newConfiguration.toByteArray();
                configurationData = new byte[]{};
            } finally {
                raftNode.getLock().unlock();
            }
            boolean success = raftNode.replicate(configurationData, EntryType.ENTRY_TYPE_CONFIGURATION);
            if (success) {
                responseBuilder.setResCode(ResCode.RES_CODE_SUCCESS);
            }
        }
        if (responseBuilder.getResCode() != ResCode.RES_CODE_SUCCESS) {
            raftNode.getLock().lock();
            try {
                for (Peer peer : requestPeers) {
                    //peer.getRpcClient().stop();
                   // peer.getRpcClient().close();
                    //TODO
                    raftNode.getPeerMap().remove(peer.getServer().getServerId());
                }
            } finally {
                raftNode.getLock().unlock();
            }
        }

        AddPeersResponse response = responseBuilder;
        LOG.info("addPeers request={} resCode={}",
               request.toString(), response.getResCode());

        return response;
    }

    @Override
    public RemovePeersResponse removePeers(RemovePeersRequest request) {
        LOG.info("RaftClientServiceImpl removePeers request");
        RemovePeersResponse responseBuilder = new RemovePeersResponse();
        responseBuilder.setResCode(ResCode.RES_CODE_FAIL);

        if (request.getServersCount() == 0
                || request.getServersCount() % 2 != 0) {
            LOG.warn("removed server's size can only multiple of 2");
            responseBuilder.setResMsg("removed server's size can only multiple of 2");
            return responseBuilder;
        }

        // check request peers exist
        raftNode.getLock().lock();
        try {
            for (Server server : request.getServersList()) {
                if (!ConfigurationUtils.containsServer(raftNode.getConfiguration(), server.getServerId())) {
                    return responseBuilder;
                }
            }
        } finally {
            raftNode.getLock().unlock();
        }

        raftNode.getLock().lock();
        Configuration newConfiguration;
        byte[] configurationData;
        try {
            newConfiguration = ConfigurationUtils.removeServers(
                    raftNode.getConfiguration(), request.getServersList());
            LOG.debug("newConfiguration={}", newConfiguration);
            //TODO    configurationData = newConfiguration.toByteArray();
            configurationData = new byte[]{};
        } finally {
            raftNode.getLock().unlock();
        }
        boolean success = raftNode.replicate(configurationData, EntryType.ENTRY_TYPE_CONFIGURATION);
        if (success) {
            responseBuilder.setResCode(ResCode.RES_CODE_SUCCESS);
        }

        LOG.info("removePeers request={} resCode={}",
                request.toString(), responseBuilder.getResCode());

        return responseBuilder;
    }

}
