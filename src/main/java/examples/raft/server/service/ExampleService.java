package examples.raft.server.service;

import com.github.wenweihu86.raft.modelsExample.GetRequest;
import com.github.wenweihu86.raft.modelsExample.GetResponse;
import com.github.wenweihu86.raft.modelsExample.SetRequest;
import com.github.wenweihu86.raft.modelsExample.SetResponse;

/**
 * Created by wenweihu86 on 2017/5/9.
 */
public interface ExampleService {

    SetResponse set(SetRequest request);

    GetResponse get(GetRequest request);
}
