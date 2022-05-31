package examples.raft;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.server.RpcServer;
import com.github.wenweihu86.raft.Peer;
import com.github.wenweihu86.raft.RaftNode;
import com.github.wenweihu86.raft.RaftOptions;

import com.github.wenweihu86.raft.models.*;
import com.github.wenweihu86.raft.modelsExample.GetRequest;
import com.github.wenweihu86.raft.modelsExample.GetResponse;
import com.github.wenweihu86.raft.modelsExample.SetRequest;
import com.github.wenweihu86.raft.modelsExample.SetResponse;
import com.github.wenweihu86.raft.service.RaftClientService;
import com.github.wenweihu86.raft.service.RaftConsensusService;
import com.github.wenweihu86.raft.service.impl.RaftClientServiceImpl;
import com.github.wenweihu86.raft.service.impl.RaftConsensusServiceImpl;
import com.googlecode.protobuf.format.JsonFormat;
import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.model.Entity;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.CloudHub;
import com.sirius.sdk.hub.MobileHub;
import com.sirius.sdk.utils.GsonUtils;
import com.sirius.sdk.utils.Pair;
import examples.raft.helpers.ConfTest;
import examples.raft.helpers.ServerTestSuite;
import examples.raft.models.AgentParams;
import examples.raft.server.ExampleStateMachine;
import examples.raft.server.service.ExampleService;
import examples.raft.server.service.impl.ExampleServiceImpl;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;


public class Main {
    static final String DKMS_NAME = "test_network";
    static final String COVID_MICROLEDGER_NAME = "covid_ledger_test3";

    static CloudHub.Config steward = new CloudHub.Config();
    static CloudHub.Config labConfig = new CloudHub.Config();
    static CloudHub.Config airCompanyConfig = new CloudHub.Config();
    static CloudHub.Config airportConfig = new CloudHub.Config();

    static final String LAB_DID = "X1YdguoHBaY1udFQMbbKKG";
    static final String AIRCOMPANY_DID = "XwVCkzM6sMxk87M2GKtya6";
    static final String AIRPORT_DID = "Ap29nQ3Kf2bGJdWEV3m4AG";


    public static MobileHub.Config mobileConfig = new MobileHub.Config();
    public static MobileHub.Config mobileConfig2 = new MobileHub.Config();
    public static MobileHub.Config mobileConfig3 = new MobileHub.Config();

    public static final String MEDIATOR_ADDRESS = "ws://mediator.socialsirius.com:8000";
    public static final JSONObject walletConfig = new JSONObject().
            put("id", "Wallet9").
            put("storage_type", "default");
    public static final JSONObject walletCredentials = new JSONObject().
            put("key", "8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY").
            put("key_derivation_method", "RAW");

    static {

        mobileConfig.walletConfig = walletConfig;
        mobileConfig.walletCredentials = walletCredentials;
        mobileConfig.mediatorInvitation = Invitation.builder().
                setLabel("Mediator").
                setEndpoint("ws://mediator.socialsirius.com:8000/ws").
                setRecipientKeys(Collections.singletonList("DjgWN49cXQ6M6JayBkRCwFsywNhomn8gdAXHJ4bb98im")).
                build();


        mobileConfig2.walletConfig = walletConfig;
        mobileConfig2.walletCredentials = walletCredentials;
        mobileConfig2.mediatorInvitation = Invitation.builder().
                setLabel("Mediator").
                setEndpoint("ws://mediator.socialsirius.com:8000/ws").
                setRecipientKeys(Collections.singletonList("BNxpmTgs9B3yMURa1ta7avKuBA5wcBp5ZmXfqPFPYGAP")).
                build();

        mobileConfig3.walletConfig = walletConfig;
        mobileConfig3.walletCredentials = walletCredentials;
        mobileConfig3.mediatorInvitation = Invitation.builder().
                setLabel("Mediator").
                setEndpoint("ws://mediator.socialsirius.com:8000/ws").
                setRecipientKeys(Collections.singletonList("8VNHw79eMTZJBasgjzdwyKyCYA88ajm9gvP98KGcjaBt")).
                build();

    }


    private static RaftNode generateRaftAgent(List<Server> serverList, Server localServer) {
        RaftOptions raftOptions = new RaftOptions();
        raftOptions.setDataDir("D:\\new");
        raftOptions.setSnapshotMinLogSize(10 * 1024);
        raftOptions.setSnapshotPeriodSeconds(30);
        raftOptions.setMaxSegmentFileSize(1024 * 1024);

        ExampleStateMachine stateMachine = new ExampleStateMachine(raftOptions.getDataDir());
        RaftNode raftNode = new RaftNode(raftOptions, serverList, localServer, stateMachine);
        RaftConsensusService raftConsensusService = new RaftConsensusServiceImpl(raftNode);
        raftNode.raftConsensusService = raftConsensusService;
        //  server.registerService(raftConsensusService);
        RaftClientService raftClientService = new RaftClientServiceImpl(raftNode);
        raftNode.raftClientService = raftClientService;
        //  server.registerService(raftClientService);
        ExampleService exampleService = new ExampleServiceImpl(raftNode, stateMachine);
        //  server.registerService(exampleService);
        raftNode.exampleService = exampleService;
        //   server.start();
        raftNode.init();
        return raftNode;
    }

    private static Server generateServerByName( String name) {
        ConfTest confTest = ConfTest.getSingletonInstance();
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams agent1params = testSuite.getAgentParams(name);
        List<Entity> entityList1 = agent1params.getEntitiesList();
        Entity entity1 = entityList1.get(0);

        CloudAgent agent1 = new CloudAgent(agent1params.getServerAddress(), agent1params.getCredentials().getBytes(StandardCharsets.US_ASCII),
                agent1params.getConnection(), 10);
        agent1.open();
        String agent1Endpoint = "";
        for (Endpoint e : agent1.getEndpoints()) {
            if (e.getRoutingKeys().size() == 0) {
                agent1Endpoint = e.getAddress();
                break;
            }
        }
    //    Pair<String,String> keys = agent1.getWallet().getDid().createAndStoreMyDid();
        Server server1 = new Server().setServerId(entity1.getDid()).setCloudAgent(agent1).setCloudAgentVerkey(entity1.getVerkey()).
                setEndpoint(new com.github.wenweihu86.raft.models.Endpoint().setHost(agent1Endpoint));
        return server1;
    }

    private static void createPairwisesWithEachOther(List<Server> serverList, Server localServer){
        System.out.println("--isExist2 START localServer="+localServer.getServerId());
        for(int i=0;i<serverList.size();i++){
            Server  server = serverList.get(i);
            if(localServer.getServerId().equals(server.getServerId())){
                continue;
            }
            localServer.getCloudAgent().getWallet().getDid().storeTheirDid(server.getServerId(), server.getCloudAgentVerkey());
            boolean isExist2 = localServer.getCloudAgent().getWallet().getPairwise().isPairwiseExist(server.getServerId());
            System.out.println("isExist2 server="+server.getServerId() + "isExist2="+isExist2);

            if (!isExist2) {
                System.out.println("createPairwisesWithEachOther");
                localServer.getCloudAgent().getWallet().getPairwise().createPairwise(server.getServerId(), localServer.getServerId());
            }
        }
        System.out.println("--isExist2 ENDlocalServer="+localServer.getServerId());
    }

    private static void parseMessage(Event event, RaftNode agent){
        if(event==null){
            return;
        }
        if (event.message() instanceof Message){
            String message =  ((Message) event.message()).getContent();


           String type = ((Message) event.message()).getMessageObj().getString("type");
            System.out.println("RECEIVED agent: "+agent.getLocalServer().getServerId()+" type="+type+" message="+message);
           //REQUEST
           if("preVote".equals(type)){
               VoteRequest voteRequest =   GsonUtils.getDefaultGson().fromJson(message, VoteRequest.class);
               VoteResponse voteResponse= agent.raftConsensusService.preVote(voteRequest);
               Message messageResponse = Message.builder().setContent(voteResponse.tosGson()).build();
               messageResponse.getMessageObj().put("type","VoteResponse");
               agent.getLocalServer().getCloudAgent().sendTo(messageResponse,event.getPairwise());
           }

            if("requestVote".equals(type)){
                VoteRequest voteRequest =   GsonUtils.getDefaultGson().fromJson(message, VoteRequest.class);
                VoteResponse voteResponse= agent.raftConsensusService.requestVote(voteRequest);
                Message messageResponse = Message.builder().setContent(voteResponse.tosGson()).build();
                messageResponse.getMessageObj().put("type","VoteResponse");
                agent.getLocalServer().getCloudAgent().sendTo(messageResponse,event.getPairwise());
            }

            if("appendEntries".equals(type)){
                AppendEntriesRequest appendRequest =   GsonUtils.getDefaultGson().fromJson(message, AppendEntriesRequest.class);
                AppendEntriesResponse appendResponse= agent.raftConsensusService.appendEntries(appendRequest);
                Message messageResponse = Message.builder().setContent(appendResponse.tosGson()).build();
                messageResponse.getMessageObj().put("type","AppendEntriesResponse");
                agent.getLocalServer().getCloudAgent().sendTo(messageResponse,event.getPairwise());
            }

            if("installSnapshot".equals(type)){
                InstallSnapshotRequest installSnapshotRequest =   GsonUtils.getDefaultGson().fromJson(message, InstallSnapshotRequest.class);
                InstallSnapshotResponse installSnapshotResponse= agent.raftConsensusService.installSnapshot(installSnapshotRequest);
                Message messageResponse = Message.builder().setContent(installSnapshotResponse.tosGson()).build();
                messageResponse.getMessageObj().put("type","InstallSnapshotResponse");
                agent.getLocalServer().getCloudAgent().sendTo(messageResponse,event.getPairwise());
            }

            if("getLeader".equals(type)){
                GetLeaderRequest getLeaderRequest =   GsonUtils.getDefaultGson().fromJson(message, GetLeaderRequest.class);
                GetLeaderResponse getLeaderResponse= agent.raftClientService.getLeader(getLeaderRequest);
                Message messageResponse = Message.builder().setContent(getLeaderResponse.tosGson()).build();
                messageResponse.getMessageObj().put("type","GetLeaderResponseClient");
                agent.getLocalServer().getCloudAgent().sendTo(messageResponse,event.getPairwise());
            }
            if("getConfiguration".equals(type)){
                GetConfigurationRequest getConfigurationRequest =   GsonUtils.getDefaultGson().fromJson(message, GetConfigurationRequest.class);
                GetConfigurationResponse getConfigurationResponse= agent.raftClientService.getConfiguration(getConfigurationRequest);
                Message messageResponse = Message.builder().setContent(getConfigurationResponse.tosGson()).build();
                messageResponse.getMessageObj().put("type","GetConfigurationResponseClient");
                agent.getLocalServer().getCloudAgent().sendTo(messageResponse,event.getPairwise());
            }
            if("addPeers".equals(type)){
                AddPeersRequest addPeersRequest =   GsonUtils.getDefaultGson().fromJson(message, AddPeersRequest.class);
                AddPeersResponse addPeersResponse= agent.raftClientService.addPeers(addPeersRequest);
                Message messageResponse = Message.builder().setContent(addPeersResponse.tosGson()).build();
                messageResponse.getMessageObj().put("type","AddPeersResponseClient");
                agent.getLocalServer().getCloudAgent().sendTo(messageResponse,event.getPairwise());
            }

            if("removePeers".equals(type)){
                RemovePeersRequest removePeersRequest =   GsonUtils.getDefaultGson().fromJson(message, RemovePeersRequest.class);
                RemovePeersResponse removePeersResponse= agent.raftClientService.removePeers(removePeersRequest);
                Message messageResponse = Message.builder().setContent(removePeersResponse.tosGson()).build();
                messageResponse.getMessageObj().put("type","RemovePeersResponseClient");
                agent.getLocalServer().getCloudAgent().sendTo(messageResponse,event.getPairwise());
            }

            //RESPONSE
            if(type.contains("ResponseClient")){

                //  VoteResponse voteResponse =   GsonUtils.getDefaultGson().fromJson(message, VoteResponse.class);
                //voteResponse.getTerm()
               /* agent.getPeerMap().forEach(new BiConsumer<String, Peer>() {
                    @Override
                    public void accept(String s, Peer peer) {
                        peer.getRaftClionsensusServiceAsync().complete(event.message());
                    }
                });*/
            }else if(type.contains("Response")){
              //  VoteResponse voteResponse =   GsonUtils.getDefaultGson().fromJson(message, VoteResponse.class);
                //voteResponse.getTerm()
                agent.getPeerMap().forEach(new BiConsumer<String, Peer>() {
                    @Override
                    public void accept(String s, Peer peer) {
                        peer.getRaftConsensusServiceAsync().complete(event.message());
                    }
                });
            }

        }

    }


    private static void addPeers(Server server, RaftNode agent){

        new Thread(new Runnable() {
            @Override
            public void run() {
                AddPeersRequest request =    new AddPeersRequest();
                ArrayList list = new ArrayList<>();
                list.add(server);
                request.setServers(list);
                agent.raftClientService.addPeers(request);
            }
        }).start();
    }

    private static void startConsoleAppChoose(){
        Scanner in = new Scanner(System.in);
        boolean loop = true;
        while (loop) {
            System.out.println("Enter your option:");
            System.out.println("1 - Get Leader");
            System.out.println("2 - Add peers");
            System.out.println("3 - Remove peers");
            System.out.println("4 - Get Configuration");
            System.out.println("5 - preVote");
            System.out.println("14 - Exit");

            int option = in.nextInt();
            switch (option) {
                case 1: {
                    agent1.raftClientService.getLeader(new GetLeaderRequest());
                    break;
                }

                case 2: {
                    agent1.raftClientService.addPeers(new AddPeersRequest());
                   // addPeers(server3);
                    break;
                }

                case 3: {
                    agent1.raftClientService.removePeers(new RemovePeersRequest());
                    break;
                }
                case 4: {
                    agent1.raftClientService.getConfiguration(new GetConfigurationRequest());
                    break;
                }

                case 5: {
                    agent1.resetElectionTimer();
                    agent2.resetElectionTimer();
                    agent3.resetElectionTimer();
                    break;
                }


                case 14: {
                    loop = false;
                    break;
                }

            }
        }
    }


    private static void listen(RaftNode agent){
        Listener listener = agent.getLocalServer().getCloudAgent().subscribe();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Event event = null;
                    try {
                        event = listener.getOne().get();
                        if(event!=null){
                         //   System.out.println("received 1: " + event.message().getMessageObj().toString());
                            parseMessage(event,agent);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }

            }
        }).start();
    }

   private static Server server1;
    private static Server server2;
    private static Server server3;


    private static  RaftNode agent1;
    private static  RaftNode agent2;
    private static  RaftNode agent3;

    public static void main(String[] args) {
        System.out.println("Запускаем агентов");

         server1 =  generateServerByName("agent1");
         server2 =  generateServerByName("agent2");
        server3 =  generateServerByName("agent3");
       // Server server4 =  generateServerByName("agent4");

        List<Server> serverList = new ArrayList<>();
        serverList.add(server1);
        serverList.add(server2);
        serverList.add(server3);

        //FIRST AGENT
        System.out.println("Создаем Pairwise каждый локальный с каждым из списка");
        createPairwisesWithEachOther(serverList,server1);
        createPairwisesWithEachOther(serverList,server2);
        createPairwisesWithEachOther(serverList,server3);

        System.out.println("Создаем node");

         agent1 =  generateRaftAgent(serverList,server1);
         agent2 =  generateRaftAgent(serverList,server2);
         agent3 =  generateRaftAgent(serverList,server3);


        System.out.println("Слушаем запросы");

        listen(agent1);
        listen(agent2);
        listen(agent3);


        startConsoleAppChoose();
    }

}
