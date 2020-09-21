package com.sirius.sdk.rpc;

import com.sirius.sdk.base.ReadOnlyChannel;
import com.sirius.sdk.base.WriteOnlyChannel;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.messaging.Message;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Transport abstraction that help build tunnels (p2p pairwise relationships) over channel layer.
 */
public class AddressedTunnel {
    Charset ENC = StandardCharsets.UTF_8;
    String address;
    ReadOnlyChannel input;
    WriteOnlyChannel output;
    P2PConnection p2p;
    AddressedTunnel.Context context;

    /**
     * @param address communication address of transport environment on server-side
     * @param input   channel of input stream
     * @param output  channel of output stream
     * @param p2p     pairwise connection that configured and prepared outside
     */
    public AddressedTunnel(String address, ReadOnlyChannel input, WriteOnlyChannel output, P2PConnection p2p) {
        this.address = address;
        this.input = input;
        this.output = output;
        this.p2p = p2p;
        this.context = new Context();
    }

    public String getAddress() {
        return address;
    }


    public Context getContext() {
        return context;
    }


    /**
     * Read message.
     * <p>
     * Tunnel allows to receive non-encrypted messages, high-level logic may control message encryption flag
     * via context.encrypted field
     *
     * @param timeout timeout in seconds
     * @return received packet
     */
    public Message receive(int timeout) throws SiriusInvalidPayloadStructure {
        byte[] payload = input.read(timeout);
        try {
         String payloadString =    new String(payload,StandardCharsets.US_ASCII);
         System.out.println("payloadString="+payloadString);
            JSONObject jsonObject = new JSONObject(payloadString);
            if (jsonObject.has("protected")) {
                String unpacked = p2p.unpack(new String(payload, StandardCharsets.US_ASCII));
                System.out.println("unpacked="+unpacked);
                context.setEncrypted(true);
                return new Message(unpacked);
            } else {
                context.setEncrypted(false);
                return new Message(new String(payload,StandardCharsets.US_ASCII));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SiriusInvalidPayloadStructure("Invalid packed message");
        }
    }

    public boolean post(Message message) {
        return post(message,true);
    }
    /**
     * Write message
     *
     * @param message message to send
     * @param encrypt do encryption
     */
    public boolean post(Message message, boolean encrypt) {
        String payload = null;
        if (encrypt) {
            payload = p2p.pack(message.serialize());
        } else {
            payload = message.serialize();
        }
        return output.write(payload.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Tunnel instance context
     */
    static class Context {

        public boolean isEncrypted() {
            return encrypted;
        }

        public void setEncrypted(boolean encrypted) {
            this.encrypted = encrypted;
        }

        boolean encrypted = false;


    }
}

