package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.errors.sirius_exceptions.*;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RemoteCallWrapper<T> implements RemoteCall<T> {

    AgentRPC rpc;
    T myClassT;

    public RemoteCallWrapper(AgentRPC rpc) {
        this.rpc = rpc;
    }


    public T serializeResponse(Object object) {
        if(object!=null){
            System.out.println("serializeResponse="+object.getClass());
        }
        if (object instanceof JSONObject) {
            return (T) ((JSONObject) object).toString();
        }else if(object instanceof JSONArray){
            List<Object> objectList = new ArrayList<>();
            for(int i=0;i<((JSONArray) object).length();i++){
                objectList.add(serializeResponse(((JSONArray) object).get(i)));
            }
            return (T)objectList;
        }else if(object instanceof Pair){
           Object firstObject =  serializeResponse(((Pair) object).first);
           Object secondObject =  serializeResponse(((Pair) object).second);
           return  (T) new Pair(firstObject,secondObject);
        }
        return (T) object;
    }

    @Override
    public T remoteCall(String type, RemoteParams.RemoteParamsBuilder params) {
        try {
            Object response = rpc.remoteCall(type, params.build());
            return serializeResponse(response);
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }
}
