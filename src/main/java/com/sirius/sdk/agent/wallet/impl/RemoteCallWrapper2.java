package com.sirius.sdk.agent.wallet.impl;


import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public   class RemoteCallWrapper2  implements RemoteCall {

    AgentRPC rpc;
    Object response;
    public RemoteCallWrapper2(AgentRPC rpc) {
        this.rpc = rpc;
    }


 /*   public Object  serializeResponse(Object... objects) {
        if(response!=null){
            System.out.println("serializeResponse="+response.getClass());
        }
        if (response instanceof JSONObject) {
            return ((JSONObject) response).toString();
        }else if(response instanceof JSONArray){
            List<Object> objectList = new ArrayList<>();
            for(int i=0;i<((JSONArray) response).length();i++){
                objectList.add(serializeResponseTo(((JSONArray) response).get(i),object));
            }
            return objectList;
        }else if(response instanceof Pair && objects.length==2){
            Object firstObject =  serializeResponse(((Pair) response).first);
            Object secondObject =  serializeResponseTo(((Pair) response).second);
            return   new Pair(firstObject,secondObject);
        }

        return  object;
    }*/



    public Object  serializeResponseTo(Object response, Object object) {

        if(object!=null){
            System.out.println("serializeResponse="+response.getClass());
        }
        if (response instanceof JSONObject) {
              return ((JSONObject) response).toString();
        }else if(response instanceof JSONArray){
            List<Object> objectList = new ArrayList<>();
            for(int i=0;i<((JSONArray) response).length();i++){
                objectList.add(serializeResponseTo(((JSONArray) response).get(i),object));
            }
          return objectList;
        }else if(response instanceof Pair){
           Object firstObject =  serializeResponseTo(((Pair) response).first,object);
           Object secondObject =  serializeResponseTo(((Pair) response).second,object);
           return   new Pair(firstObject,secondObject);
        }

        return  object;
    }

    @Override
    public RemoteCallWrapper2 remoteCall(String type, RemoteParams.RemoteParamsBuilder params) {
        try {
            response = rpc.remoteCall(type, params.build());
            return this;
        } catch (Exception siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }
}
