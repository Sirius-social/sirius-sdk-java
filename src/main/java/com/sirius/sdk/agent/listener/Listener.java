package com.sirius.sdk.agent.listener;

import com.sirius.sdk.agent.AbstractAgent;
import com.sirius.sdk.agent.connections.AgentEvents;
import com.sirius.sdk.agent.pairwise.AbstractPairwiseList;
import com.sirius.sdk.agent.connections.CloudAgentEvents;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusConnectionClosed;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import io.reactivex.rxjava3.core.Observable;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

public class Listener {

    AgentEvents source;
    AbstractPairwiseList pairwiseResolver;
    AbstractAgent agent;

    public Listener(AgentEvents source, AbstractAgent agent) {
        this.source = source;
        this.pairwiseResolver = agent.getPairwiseList();
        this.agent = agent;
    }

    public Observable<Event> listen() {
        return source.pull().map(msg -> {
            String theirVerkey = msg.getStringFromJSON("sender_verkey");
            Pairwise pairwise = null;
            if (pairwiseResolver != null && theirVerkey != null) {
                pairwise = pairwiseResolver.loadForVerkey(theirVerkey);
            }
            return new Event(pairwise, msg.serialize());
        });
    }

    public void unsubscribe() {
        agent.unsubscribe(this);
    }
}
