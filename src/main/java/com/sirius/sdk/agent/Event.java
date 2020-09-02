package com.sirius.sdk.agent;

import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.messaging.Message;

public class Event extends Message {

    Pairwise pairwise;
    public Event(Pairwise pairwise,String message) {
        super(message);
        this.pairwise = pairwise;
    }
}
