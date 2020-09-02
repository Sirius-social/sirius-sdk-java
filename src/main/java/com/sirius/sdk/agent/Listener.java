package com.sirius.sdk.agent;

import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusConnectionClosed;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class Listener {

    AgentEvents source;
    AbstractPairwiseList pairwiseResolver;

    public Listener(AgentEvents source, AbstractPairwiseList pairwiseResolver) {
        this.source = source;
        this.pairwiseResolver = pairwiseResolver;
    }


    public Event getOne() {
        try {
            Message event = source.pull();
            if (event.messageObjectHasKey("message")) {
                JSONObject messObj = event.getJSONOBJECTFromJSON("message");
                Pair<Boolean, Message> result = Message.restoreMessageInstance(messObj.toString());
                if (result.first) {
                    //    event['message'] = message
                } else {
                    //    event['message'] = Message(event['message'])*/
                }
            }
            String theirVerkey = event.getStringFromJSON("sender_verkey");
            Pairwise pairwise = null;
            if (pairwiseResolver != null && theirVerkey != null) {
                pairwise = pairwiseResolver.loadForVerkey(theirVerkey);
            }
            return new Event(pairwise, event.toString());
        } catch (SiriusConnectionClosed | SiriusInvalidPayloadStructure
                | NoSuchMethodException | IllegalAccessException |
                InvocationTargetException | InstantiationException siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

}
