package com.sirius.sdk.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sirius.sdk.base.JsonSerializable;
import com.sirius.sdk.encryption.Custom;
import com.sirius.sdk.errors.IndyException;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPromiseContextException;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValueEmpty;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

/**
 * "Futures and Promises pattern.
 * (http://dist-prog-book.com/chapter/2/futures.html)
 * <p>
 * <p>
 * Server point has internal communication schemas and communication addresses for
 * Aries super-protocol/sub-protocol behaviour
 * (https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0003-protocols).
 * <p>
 * Future hide communication addresses specifics of server-side service (cloud agent) and pairwise configuration
 * of communication between sdk-side and agent-side logic, allowing to take attention on
 * response awaiting routines.
 */
public class Future {
    public static final String MSG_TYPE = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/future";

    AddressedTunnel tunnel;
    long expirationTime;
    String id;
    Object value;
    boolean readOk = false;
    JSONObject exception;

    /**
     * @param addressedTunnel communication tunnel for server-side cloud agent
     */
    public Future(AddressedTunnel addressedTunnel) {
        this.tunnel = addressedTunnel;
        id = UUID.randomUUID().toString();
    }


    /**
     * @param addressedTunnel communication tunnel for server-side cloud agent
     * @param expirationTime  time of response expiration
     */
    public Future(AddressedTunnel addressedTunnel, long expirationTime) {
        this(addressedTunnel);
        this.expirationTime = expirationTime;

        this.tunnel = addressedTunnel;

    }

    /**
     * Promise info builder
     *
     * @return: serialized promise dump
     */
    public FuturePromise promise() {
        return new FuturePromise(id, tunnel.address, expirationTime);
    }

    /**
     * "Wait for response
     *
     * @param timeout waiting timeout in seconds
     * @return True/False
     */
    public boolean waitPromise(int timeout) {
        if (readOk) {
            return true;
        }
        if (timeout == 0) {
            return false;
        }

        try {
            Message message = tunnel.receive(timeout);
            String payload = message.serialize();
            //(payload.get('~thread', {}).get('thid', None) == self.__id
            JSONObject threadObj = message.getJSONOBJECTFromJSON("~thread");
            String threadId = null;
            if (threadObj != null) {
                threadId = threadObj.getString("thid");
            }
            boolean isType = MSG_TYPE.equals(message.getType());
            boolean isTypeId = id.equals(threadId);
            if (MSG_TYPE.equals(message.getType()) && id.equals(threadId)) {
                JSONObject exception = message.getJSONOBJECTFromJSON("exception");
                if (exception == null) {
                    Object value = message.getObjectFromJSON("value");
                    boolean is_tuple = message.getBooleanFromJSON("is_tuple");
                    boolean is_bytes = message.getBooleanFromJSON("is_bytes");
                    if (is_tuple) {
                        if (((JSONArray) value).length() == 2) {
                            this.value = new Pair<Object, Object>(((JSONArray) value).get(0), ((JSONArray) value).get(1));
                        } else if (((JSONArray) value).length() == 3) {
                            this.value = new Triple<Object, Object, Object>(((JSONArray) value).get(0), ((JSONArray) value).get(1), ((JSONArray) value).get(2));
                        } else {
                            this.value = value;
                        }
                    } else if (is_bytes) {
                        Custom custom = new Custom();
                        this.value = custom.b64ToBytes(value.toString(), false);
                    } else {
                        this.value = value;
                    }
                } else {
                    this.exception = exception;
                }
                readOk = true;
                return true;
            } else {
                System.out.println("Unexpected payload" + message.serialize() + "Expected id: " + id);
            }


        } catch (SiriusInvalidPayloadStructure siriusInvalidPayloadStructure) {
            siriusInvalidPayloadStructure.printStackTrace();
        }

        return false;
    }

    /**
     * Get response value.
     *
     * @throws SiriusPendingOperation : response was not received yet. Call walt(0) to safely check value persists.
     * @return: value
     */
    public Object getValue() throws SiriusPendingOperation {
        if (readOk) {
            return value;
        } else {
            throw new SiriusPendingOperation();
        }
    }

    /*

     */
/**
 * "Wait for response
 *
 * @param timeout waiting timeout in seconds
 * @return True/False
 *//*

    public boolean waitPromise(int timeout)  {
        if (readOk) {
            return true;
        }
        if (timeout == 0) {
            return false;
        }

        try {
            Message message = tunnel.receive(timeout);
            JsonObject messageObject = message.serializeToJsonObject();
            JSONObject threadObj = message.getJSONOBJECTFromJSON("~thread");
            String threadId = null;
            if (threadObj != null) {
                threadId = threadObj.getString("thid");
            }
            boolean isType = MSG_TYPE.equals(message.getType());
            boolean isTypeId = id.equals(threadId);
            if (MSG_TYPE.equals(message.getType()) && id.equals(threadId)) {
                JSONObject exception = message.getJSONOBJECTFromJSON("exception");
                if (exception == null) {
                    JsonElement valueElement =  messageObject.get("value");
                    Object value =   message.getObjectFromJSON("value");

                    boolean is_tuple =    messageObject.getAsJsonPrimitive("is_tuple").getAsBoolean();
                    boolean is_bytes =     messageObject.getAsJsonPrimitive("is_bytes").getAsBoolean();
                    if(is_tuple){
                        JsonArray valueArray =  messageObject.getAsJsonArray();
                        if (valueArray.size() == 2) {
                            this.value = new Pair<JsonElement,JsonElement>(valueArray.get(0),valueArray.get(1));
                        */
/*}else if(valueArray.size() == 3){
                            this.value = new Triple<JsonElement,JsonElement,JsonElement>(valueArray.get(0),valueArray.get(1),valueArray.get(2));
                        *//*
}else {
                            this.value = value;
                        }
                    }else if(is_bytes){
                        Custom custom = new Custom();
                        this.value =  custom.b64ToBytes(valueElement.getAsString(),false);
                    }else{
                        this.value = value;
                    }
                } else {
                    this.exception = exception;
                }
                readOk  =true;
                return true;
            } else {
                System.out.println("Unexpected payload" + message.serialize() + "Expected id: " + id);
            }


        } catch (SiriusInvalidPayloadStructure  siriusInvalidPayloadStructure) {
            siriusInvalidPayloadStructure.printStackTrace();
        }

        return false;
    }
*/

    /**
     * Check if response was interrupted with exception
     *
     * @throws SiriusPendingOperation: response was not received yet. Call walt(0) to safely check value persists.
     * @return: True if request have done with exception
     */
    public boolean hasException() throws SiriusPendingOperation {
        if (!readOk) {
            throw new SiriusPendingOperation();
        }
        return exception != null;
    }

    /**
     * Get exception that have interrupted response routine on server-side.
     *
     * @return Exception instance or None if it does not exists
     */
    public Exception getFutureException() {
        try {
            if (hasException()) {
                if (exception.optJSONObject("indy") != null) {
                    JSONObject indy_exc = exception.getJSONObject("indy");

                    IndyException exc_class = IndyException.fromSdkError(indy_exc.getInt("error_code"), indy_exc);
                    return exc_class;
                } else {
                    return new SiriusPromiseContextException(exception.optString("class_name"), exception.optString("printable"));
                }

            }
        } catch (SiriusPendingOperation siriusPendingOperation) {
            siriusPendingOperation.printStackTrace();
        }
        return null;

    }

    /**
     * Raise exception if exists
     *
     * @throws SiriusValueEmpty: raises if exception is empty
     */
    public void raiseException() throws Exception {
        try {
            if (hasException()) {
                throw getFutureException();
            }
        } catch (SiriusPendingOperation siriusPendingOperation) {
            siriusPendingOperation.printStackTrace();
        }
    }

    public static class FuturePromise implements JsonSerializable<FuturePromise> {
        String id;
        String channel_address;
        long expiration_stamp;

        public FuturePromise(String id, String channel_address, long expiration_stamp) {
            this.id = id;
            this.channel_address = channel_address;
            this.expiration_stamp = expiration_stamp;
        }

        public String getId() {
            return id;
        }

        public String getChannel_address() {
            return channel_address;
        }

        public long getExpiration_stamp() {
            return expiration_stamp;
        }

        @Override
        public String serialize() {
            Gson gson = new GsonBuilder().create();
            return gson.toJson(this, FuturePromise.class);
        }

        @Override
        public JSONObject serializeToJSONObject() {
            String object = serialize();
            return new JSONObject(object);
        }

        @Override
        public FuturePromise deserialize(String string) {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(string, FuturePromise.class);
        }

        @Override
        public JsonObject serializeToJsonObject() {
            return null;
        }
    }


}
