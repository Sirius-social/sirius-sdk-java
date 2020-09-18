package com.sirius.sdk.agent;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractStateMachine {
    TransportLayer transportLayer;
    int timeToLive;
    Logger logger;

    /**
     * @param transportLayer aries-rfc transports factory
     * @param timeToLive     state machine time to live to finish progress
     * @param logger
     */
    public AbstractStateMachine(TransportLayer transportLayer, int timeToLive, Logger logger) {
        this.transportLayer = transportLayer;
        this.timeToLive = timeToLive;
        this.logger = logger;
    }

    public AbstractStateMachine(TransportLayer transportLayer, int timeToLive) {
        this(transportLayer, timeToLive, null);
    }

    public AbstractStateMachine(TransportLayer transportLayer) {
        this(transportLayer, 60, null);
    }

    public abstract List<String> protocols();


    public void log(Object messages) {
        if (logger != null) {
            logger.log(Level.INFO, "state_machine_id= " + this + "message: " + messages.toString());
        }
    }
}
