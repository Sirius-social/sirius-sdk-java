package com.sirius.sdk.rpc;

import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;

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

    AddressedTunnel tunnel;
    Date expirationTime;
    String id;
    String value;
    boolean readOk = false;
    Exception exception;

    /**
     * @param addressedTunnel communication tunnel for server-side cloud agent
     */
    public Future(AddressedTunnel addressedTunnel) {
        this.tunnel = addressedTunnel;
        id = UUID.randomUUID().toString();//TODO hex
    }

    /**
     * @param addressedTunnel communication tunnel for server-side cloud agent
     * @param expirationTime  time of response expiration
     */
    public Future(AddressedTunnel addressedTunnel, Date expirationTime) {
        this(addressedTunnel);
        this.expirationTime = expirationTime;

        this.tunnel = addressedTunnel;

    }

    /**
     * Promise info builder
     *
     * @return: serialized promise dump
     */
    public void promise() {

    }

    /**
     * "Wait for response
     *
     * @param timeout waiting timeout in seconds
     * @return True/False
     */
    public boolean waitPromise(int timeout) {
        if(readOk){
            return true;
        }
        if(timeout == 0){
            return false;
        }

        return false;
    }

    /**
     * Get response value.
     * @return: value
     *  @throws SiriusPendingOperation : response was not received yet. Call walt(0) to safely check value persists.
     */
    public String getValue() throws SiriusPendingOperation {
        if(readOk){
            return value;
        }else{
            throw new SiriusPendingOperation();
        }
    }

    public void  hasException(){

    }


}
