package com.sirius.sdk.agent.connections;

import com.sirius.sdk.errors.sirius_exceptions.SiriusConnectionClosed;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.messaging.Message;

import java.util.concurrent.CompletableFuture;

public interface AgentEvents {

    CompletableFuture<Message> pull() throws SiriusConnectionClosed, SiriusInvalidPayloadStructure;
}