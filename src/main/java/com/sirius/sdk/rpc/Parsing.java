package com.sirius.sdk.rpc;

import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.wallet.KeyDerivationMethod;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.*;
import com.sirius.sdk.base.JsonMessage;
import com.sirius.sdk.base.JsonSerializable;
import com.sirius.sdk.encryption.Custom;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.messaging.Type;
import com.sirius.sdk.utils.Pair;
import com.sun.deploy.ref.AppModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Parsing {
    /**
     * @param msgType Aries RFCs attribute
     *                https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0020-message-types
     * @param future  Future to check response routine is completed
     * @param params  RPC call params
     * @return RPC service packet
     */
    public static Message buildRequest(String msgType, Future future, RemoteParams params) {
        try {
            Type type = Type.fromStr(msgType);
            if (!"sirius_rpc".equals(type.getProtocol()) && !"admin".equals(type.getProtocol()) && !"microledgers".equals(type.getProtocol())) {
                throw new SiriusInvalidType("Expect sirius_rpc protocol");
            }
        } catch (SiriusInvalidType siriusInvalidType) {
            siriusInvalidType.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("@type", msgType);
        jsonObject.put("@id", UUID.randomUUID().toString());
        jsonObject.put("@promise", future.promise().serializeToJSONObject());
        JSONObject paramsObject = incapsulateParam(params);
        jsonObject.put("params", paramsObject);
        return new Message(jsonObject.toString());
    }


    public static final Map<String, Class> CLS_MAP = new HashMap<String, Class>() {
        {
            put("application/cache-options", CacheOptions.class);
            put("application/purge-options", PurgeOptions.class);
            put("application/retrieve-record-options", RetrieveRecordOptions.class);
            put("application/nym-role", NYMRole.class);
            put("application/pool-action", PoolAction.class);
            put("application/key-derivation-method", KeyDerivationMethod.class);
        }
    };
    public static final Map<Class, String> CLS_MAP_REVERT = new HashMap<Class, String>() {
        {
            put(CacheOptions.class, "application/cache-options");
            put(PurgeOptions.class, "application/purge-options");
            put(RetrieveRecordOptions.class, "application/retrieve-record-options");
            put(NYMRole.class, "application/nym-role");
            put(PoolAction.class, "application/pool-action");
            put(KeyDerivationMethod.class, "application/key-derivation-method");
        }
    };

    //  CLS_MAP_REVERT = {v: k for k, v in CLS_MAP.items()}


    public static JSONObject incapsulateParam(RemoteParams params) {
        JSONObject paramsObject = new JSONObject();
        if (params == null) {
            return paramsObject;
        }
        Map<String, Object> paramsMap = params.getParams();
        Set<String> keys = paramsMap.keySet();
        for (String key : keys) {
            Pair<String, Object> pair = serializeVariable(paramsMap.get(key));
            JSONObject oneParamObject = new JSONObject();
            if (pair.first == null) {
                oneParamObject.put("mime_type", JSONObject.NULL);
            } else {
                oneParamObject.put("mime_type", pair.first);
            }
            if (pair.second == null) {
                oneParamObject.put("payload", JSONObject.NULL);
            } else {
                oneParamObject.put("payload", pair.second);
            }
            paramsObject.put(key, oneParamObject);
        }

        return paramsObject;
    }

    public static Object serializeObject(Object param) {
        Object varParam = null;
        String mimeType = CLS_MAP_REVERT.get(param.getClass());
        if (mimeType != null && param instanceof JsonSerializable) {
            varParam = ((JsonSerializable) param).serialize();
        }else if (param instanceof Collection) {
            JSONArray jsonArray = new JSONArray();
            for (Object oneParam : (Collection) param) {
                Object oneParamObject = serializeObject(oneParam);
                jsonArray.put(oneParamObject);
            }
            varParam = jsonArray;
        } else if (param instanceof byte[]) {
            Custom custom = new Custom();
            varParam = custom.bytesToB64((byte[]) param, false);
        } else if (param instanceof JsonSerializable) {
            varParam = ((JsonSerializable) param).serializeToJSONObject();
        }else if(param instanceof  JSONObject){
            varParam = param;
        } else if(param instanceof  Integer){
            varParam = param;
        } else {
            varParam = param.toString();
        }
        return varParam;
    }

    /**
     * Serialize input variable to JSON-compatible string
     *
     * @param param input variable
     * @return tuple (type, variable serialized dump)
     */
    public static Pair<String, Object> serializeVariable(Object param) {
        if (param == null) {
            return new Pair<>(null, null);
        }
        String mimeType = CLS_MAP_REVERT.get(param.getClass());
        if (param instanceof byte[]) {
            mimeType = "application/base64";
        }
        Object varParam = serializeObject(param);
        return new Pair<>(mimeType, varParam);
        /*if isinstance(var, CacheOptions):
        return CLS_MAP_REVERT[CacheOptions], var.serialize()
        elif isinstance(var, PurgeOptions):
        return CLS_MAP_REVERT[PurgeOptions], var.serialize()
        elif isinstance(var, RetrieveRecordOptions):
        return CLS_MAP_REVERT[RetrieveRecordOptions], var.serialize()
        elif isinstance(var, NYMRole):
        return CLS_MAP_REVERT[NYMRole], var.serialize()
        elif isinstance(var, PoolAction):
        return CLS_MAP_REVERT[PoolAction], var.serialize()
        elif isinstance(var, KeyDerivationMethod):
        return CLS_MAP_REVERT[KeyDerivationMethod], var.serialize()
        elif isinstance(var, bytes):
        return 'application/base64', base64.b64encode(var).decode('ascii')
    else:
        return None, var*/


    }


}
