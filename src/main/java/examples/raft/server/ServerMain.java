package examples.raft.server;

import com.baidu.brpc.server.RpcServer;
import com.github.wenweihu86.raft.RaftNode;
import com.github.wenweihu86.raft.RaftOptions;

import com.github.wenweihu86.raft.models.Endpoint;
import com.github.wenweihu86.raft.models.Server;
import com.github.wenweihu86.raft.proto.RaftProto;
import com.github.wenweihu86.raft.service.RaftClientService;
import com.github.wenweihu86.raft.service.RaftConsensusService;
import com.github.wenweihu86.raft.service.impl.RaftClientServiceImpl;
import com.github.wenweihu86.raft.service.impl.RaftConsensusServiceImpl;
import examples.raft.server.service.ExampleService;
import examples.raft.server.service.impl.ExampleServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenweihu86 on 2017/5/9.
 */
public class ServerMain {
    public static void main(String[] args) {
       if (args.length != 3) {
            System.out.printf("Usage: ./run_server.sh DATA_PATH CLUSTER CURRENT_NODE\n");
            System.exit(-1);
        }
        // parse args
        // raft data dir
        String dataPath = args[0];
        // peers, format is "host:port:serverId,host2:port2:serverId2"
        String servers = args[1];
        String[] splitArray = servers.split(",");
        List<Server> serverList = new ArrayList<>();
        for (String serverString : splitArray) {
            Server server = parseServer(serverString);
            serverList.add(server);
        }
        // local server
        Server localServer = parseServer(args[2]);


        RpcServer server = new RpcServer(localServer.getEndpoint().getPort());

        // just for test snapshot
        RaftOptions raftOptions = new RaftOptions();
        raftOptions.setDataDir(dataPath);
        raftOptions.setSnapshotMinLogSize(10 * 1024);
        raftOptions.setSnapshotPeriodSeconds(30);
        raftOptions.setMaxSegmentFileSize(1024 * 1024);

        ExampleStateMachine stateMachine = new ExampleStateMachine(raftOptions.getDataDir());

        RaftNode raftNode = new RaftNode(raftOptions, serverList, localServer, stateMachine);

        RaftConsensusService raftConsensusService = new RaftConsensusServiceImpl(raftNode);
        server.registerService(raftConsensusService);

        RaftClientService raftClientService = new RaftClientServiceImpl(raftNode);
        server.registerService(raftClientService);

        ExampleService exampleService = new ExampleServiceImpl(raftNode, stateMachine);
        server.registerService(exampleService);

        server.start();
        raftNode.init();
    }

    private static Server parseServer(String serverString) {
        String[] splitServer = serverString.split(":");
        String host = splitServer[0];
        Integer port = Integer.parseInt(splitServer[1]);
        Integer serverId = Integer.parseInt(splitServer[2]);
        Endpoint endPoint = new Endpoint()
                .setHost(host).setPort(port);
        Server serverBuilder = new Server();
        Server server = serverBuilder.setServerId(serverId.toString()).setEndpoint(endPoint);
        return server;
    }
}
