package com.sirius.sdk.agent.listener;

import com.sirius.sdk.agent.connections.AgentEvents;
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

    AgentEvents source;
    AbstractPairwiseList pairwiseResolver;

    public Listener(AgentEvents source, AbstractPairwiseList pairwiseResolver) {
        this.source = source;
        this.pairwiseResolver = pairwiseResolver;
    }

    public CompletableFuture<Event> getOne() {
        try {
            return source.pull().thenApply(msg -> {
                String theirVerkey = msg.getStringFromJSON("sender_verkey");
                Pairwise pairwise = null;
                if (pairwiseResolver != null && theirVerkey != null) {
                    pairwise = pairwiseResolver.loadForVerkey(theirVerkey);
                }
                return new Event(pairwise, msg.serialize());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
