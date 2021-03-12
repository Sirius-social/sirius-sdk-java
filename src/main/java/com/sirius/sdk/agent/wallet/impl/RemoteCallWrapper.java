package com.sirius.sdk.agent.wallet.impl;


import com.google.common.reflect.TypeToken;
import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class RemoteCallWrapper<T> implements RemoteCall<T> {

    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
    };
    private final Type type = typeToken.getType();
    AgentRPC rpc;
    T myClassT;
    Class objectClass;
    //  TypeToken<T> type = new TypeToken<T>(getClass()) {};
    AbstractGenericType<T> abstractGenericType;
    AbstractDAO<T> abstractDAo;

    public RemoteCallWrapper(AgentRPC rpc) {
        this.rpc = rpc;
        abstractGenericType = new AbstractGenericType<T>() {
        };
        abstractDAo = new AbstractDAO<T>() {
        };
    }

    public RemoteCallWrapper(AgentRPC rpc, Class objectClass) {
        this.rpc = rpc;
        this.objectClass = objectClass;
    }

    public Type getType() {
        return type;
    }

    public T serializeResponse(Object object) {
        if (object instanceof JSONObject) {
            return (T) object.toString();

        } else if (object instanceof JSONArray) {
            List<Object> objectList = new ArrayList<>();
            for (int i = 0; i < ((JSONArray) object).length(); i++) {
                objectList.add(serializeResponse(((JSONArray) object).get(i)));
            }
            return (T) objectList;
        } else if (object instanceof Pair) {
            Object firstObject = serializeResponse(((Pair) object).first);
            Object secondObject = serializeResponse(((Pair) object).second);
            return (T) new Pair(firstObject, secondObject);
        } else if (object instanceof Triple) {
            Object firstObject = serializeResponse(((Triple) object).first);
            Object secondObject = serializeResponse(((Triple) object).second);
            Object thirdObject = serializeResponse(((Triple) object).third);
            return (T) new Triple(firstObject, secondObject, thirdObject);
        }
        return (T) object;
    }

    @Override
    public T remoteCall(String type, RemoteParams.RemoteParamsBuilder params) {
        try {
            Object response;
            if (params == null) {
                response = rpc.remoteCall(type);
            } else {
                response = rpc.remoteCall(type, params.build());
            }
            return serializeResponse(response);
        } catch (Exception siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    public T remoteCall(String type) {
        return remoteCall(type, null);
    }
}
