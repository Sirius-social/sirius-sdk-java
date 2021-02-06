package com.sirius.sdk.agent.wallet.impl;


import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.base.JsonSerializable;
import com.sirius.sdk.errors.sirius_exceptions.*;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

public  abstract class RemoteCallWrapper<T>  implements RemoteCall<T> {

    AgentRPC rpc;
    T myClassT;
    Class objectClass;
  //  TypeToken<T> type = new TypeToken<T>(getClass()) {};
  AbstractGenericType<T> abstractGenericType;
    AbstractDAO<T> abstractDAo;

    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) { };
    private final Type type = typeToken.getType();

    public Type getType() {
        return type;
    }

    public RemoteCallWrapper(AgentRPC rpc) {
        this.rpc = rpc;
        abstractGenericType = new AbstractGenericType<T>(){};
        abstractDAo = new AbstractDAO<T>(){};
    }


    public RemoteCallWrapper(AgentRPC rpc, Class objectClass) {
        this.rpc = rpc;
        this.objectClass = objectClass;
    }

    public T serializeResponse(Object object) {
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
            Object response;
            if(params == null){
                response = rpc.remoteCall(type);
            }else{
                response = rpc.remoteCall(type, params.build());
            }
            return serializeResponse(response);
        } catch (Exception siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    public T remoteCall(String type) {
        return remoteCall(type,null);
    }
}
