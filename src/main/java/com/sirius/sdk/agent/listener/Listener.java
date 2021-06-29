package com.sirius.sdk.agent.listener;

import com.sirius.sdk.agent.pairwise.AbstractPairwiseList;
import com.sirius.sdk.agent.connections.CloudAgentEvents;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusConnectionClosed;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

public class Listener {

    CloudAgentEvents source;
    AbstractPairwiseList pairwiseResolver;

    public Listener(CloudAgentEvents source, AbstractPairwiseList pairwiseResolver) {
        this.source = source;
        this.pairwiseResolver = pairwiseResolver;
    }


    public CompletableFuture<Event> getOne() {
        try {
            return source.pull().thenApply(msg -> {
                if (msg.messageObjectHasKey("message")) {
                    JSONObject messObj = msg.getJSONOBJECTFromJSON("message");
                    Pair<Boolean, Message> result = null;
                    try {
                        result = Message.restoreMessageInstance(messObj.toString());
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                    if (result.first) {
                        //    msg['message'] = message
                    } else {
                        //    msg['message'] = Message(msg['message'])*/
                    }
                }
                String theirVerkey = msg.getStringFromJSON("sender_verkey");
                Pairwise pairwise = null;
                if (pairwiseResolver != null && theirVerkey != null) {
                    pairwise = pairwiseResolver.loadForVerkey(theirVerkey);
                }
                return new Event(pairwise, msg.serialize());
            });
        } catch (SiriusConnectionClosed siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        } catch (SiriusInvalidPayloadStructure siriusInvalidPayloadStructure) {
            siriusInvalidPayloadStructure.printStackTrace();
        }

        return null;
    }

}
