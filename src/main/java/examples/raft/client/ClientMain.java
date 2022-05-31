package examples.raft.client;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;

import com.github.wenweihu86.raft.modelsExample.GetRequest;
import com.github.wenweihu86.raft.modelsExample.GetResponse;
import com.github.wenweihu86.raft.modelsExample.SetRequest;
import com.github.wenweihu86.raft.modelsExample.SetResponse;
import com.googlecode.protobuf.format.JsonFormat;
import examples.raft.server.service.ExampleProto;
import examples.raft.server.service.ExampleService;

/**
 * Created by wenweihu86 on 2017/5/14.
 */
public class ClientMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.printf("Usage: ./run_client.sh CLUSTER KEY [VALUE]\n");
            System.exit(-1);
        }

        // parse args
        String ipPorts = args[0];
        String key = args[1];
        String value = null;
        if (args.length > 2) {
            value = args[2];
        }

        // init rpc client
        RpcClient rpcClient = new RpcClient(ipPorts);
        ExampleService exampleService = BrpcProxy.getProxy(rpcClient, ExampleService.class);
        final JsonFormat jsonFormat = new JsonFormat();

        // set
        if (value != null) {
            SetRequest setRequest = new SetRequest()
                    .setKey(key).setValue(value);
            SetResponse setResponse = exampleService.set(setRequest);
            System.out.printf("set request, key=%s value=%s response=%s\n",
                    key, value, setResponse.toString());
        } else {
            // get
            GetRequest getRequest = new GetRequest()
                    .setKey(key);
            GetResponse getResponse = exampleService.get(getRequest);
            System.out.printf("get request, key=%s, response=%s\n",
                    key, getResponse.toString());
        }

        rpcClient.stop();
    }
}
