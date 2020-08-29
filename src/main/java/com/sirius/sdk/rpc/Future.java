package com.sirius.sdk.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.goterl.lazycode.lazysodium.LazySodium;
import com.sirius.sdk.base.JsonMessage;
import com.sirius.sdk.base.JsonSerializable;
import com.sirius.sdk.encryption.Custom;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValueEmpty;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.StringUtils;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Date;
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
    Exception exception;

    /**
     * @param addressedTunnel communication tunnel for server-side cloud agent
     */
    public Future(AddressedTunnel addressedTunnel) {
        this.tunnel = addressedTunnel;
        String uuid = UUID.randomUUID().toString();
        id = convertStringToHex(uuid);//TODO hex
    }

    public static String convertStringToHex(String str) {

        // display in uppercase
        //char[] chars = Hex.encodeHex(str.getBytes(StandardCharsets.UTF_8), false);

        // display in lowercase, default
        char[] chars = Hex.encodeHex(str.getBytes(StandardCharsets.UTF_8));

        return String.valueOf(chars);
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


    public static class FuturePromise implements JsonSerializable<FuturePromise> {
        String id;
        String channel_address;
        long expiration_stamp;

        public FuturePromise(String id, String channel_address, long expiration_stamp) {
            this.id = id;
            this.channel_address = channel_address;
            this.expiration_stamp = expiration_stamp;
        }


        public JSONObject serializeToObj() {
            JSONObject jsonObject = new JSONObject();
             jsonObject.put("id",id);
             jsonObject.put("channel_address",channel_address);
             jsonObject.put("expiration_stamp",expiration_stamp);
            return jsonObject;
        }

        @Override
        public String serialize() {
            Gson gson = new GsonBuilder().create();
            return gson.toJson(this, FuturePromise.class);
        }

        @Override
        public FuturePromise deserialize(String string) {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(string, FuturePromise.class);
        }
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
                Object exception = message.getObjectFromJSON("exception");
                if (exception == null) {
                    Object value =   message.getObjectFromJSON("value");
                    boolean is_tuple =   message.getBooleanFromJSON("is_tuple");
                    boolean is_bytes =   message.getBooleanFromJSON("is_bytes");
                    if(is_tuple){
                        //TODO make Tuple
                    }else if(is_bytes){
                        Custom custom = new Custom();
                        this.value =  custom.b64ToBytes(value.toString(),false);
                    }else{
                        this.value = value;
                    }
                    readOk  =true;
                    return true;

                } else {
                    //   exception()
                }
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
        return false;
    }

    /**
     * Get exception that have interrupted response routine on server-side.
     *
     * @return Exception instance or None if it does not exists
     */
    public Exception exception() {
        try {
            if (hasException()) {

            }
        } catch (SiriusPendingOperation siriusPendingOperation) {
            siriusPendingOperation.printStackTrace();
        }
        return null;

    }
      /*      if self.has_exception():
            if self.__exception['indy']:
    indy_exc = self.__exception['indy']
    exc_class = errorcode_to_exception(errorcode=indy_exc['error_code'])
    exc = exc_class(
            error_code=indy_exc['error_code'],
            error_details=dict(message=indy_exc['message'], indy_backtrace=None)
                )
                        return exc
            else:
                    return SiriusPromiseContextException(
            class_name=self.__exception['class_name'], printable=self.__exception['printable']
    )
        else:
                return None*/


    /**
     * Raise exception if exists
     *
     * @throws SiriusValueEmpty: raises if exception is empty
     */
    public void raiseException() throws SiriusValueEmpty {
        try {
            if (hasException()) {

            }
        } catch (SiriusPendingOperation siriusPendingOperation) {
            siriusPendingOperation.printStackTrace();
        }
    }


}
