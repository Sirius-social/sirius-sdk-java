package com.sirius.sdk.rpc;

import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.messaging.Type;
import com.sun.deploy.ref.AppModel;
import org.json.JSONObject;

import java.util.UUID;

public class Parsing {
    /**
     *
     * @param msgType Aries RFCs attribute
     *         https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0020-message-types
     * @param future Future to check response routine is completed
     * @param params RPC call params
     * @return RPC service packet
     */
    public static Message buildRequest(String msgType, Future future, String params){
        try {
            Type type =  Type.fromStr(msgType);
            if(!"sirius_rpc".equals(type.getProtocol() ) && !"admin".equals(type.getProtocol()) && !"microledgers".equals(type.getProtocol())){
                throw new SiriusInvalidType("Expect sirius_rpc protocol");
            }
        } catch (SiriusInvalidType siriusInvalidType) {
            siriusInvalidType.printStackTrace();
        }
        JSONObject jsonObject= new JSONObject();
        jsonObject.put("@type",msgType);
        jsonObject.put("@id", UUID.randomUUID().toString());
        jsonObject.put("@promise",future.promise().serializeToObj());
        JSONObject paramsObject = new JSONObject();
        if(params!=null && !params.isEmpty() && params.startsWith("{")){
            paramsObject = new JSONObject(params);
        }
        jsonObject.put("params",paramsObject);
        return new Message(jsonObject.toString());

    }
/*    def build_request(msg_type: str, future: Future, params: dict) -> Message:

    typ = Type.from_str(msg_type)
            if typ.protocol not in ['sirius_rpc', 'admin', 'microledgers']:
    raise SiriusInvalidType('Expect sirius_rpc protocol')
    return Message({
        '@type': msg_type,
                '@promise': future.promise,
                'params': {k: incapsulate_param(v) for k, v in params.items()}
    })*/

}
