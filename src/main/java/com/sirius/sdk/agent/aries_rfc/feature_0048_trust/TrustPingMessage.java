package com.sirius.sdk.agent.aries_rfc.feature_0048_trust;

import com.sirius.sdk.agent.aries_rfc.AriesBaseMessage;

/**Implementation of Ping part for trust_ping protocol
 *  https://github.com/hyperledger/aries-rfcs/tree/master/features/0048-trust-ping
 */
public class TrustPingMessage  extends AriesBaseMessage {
    String comment;
    Boolean responseRequested;

    public TrustPingMessage(String message) {
        super(message);
    }

    public TrustPingMessage(String comment, Boolean responseRequested) {
        super();
        this.comment = comment;
        this.responseRequested = responseRequested;
    }

    @Override
    public String getProtocol() {
        return "trust_ping";
    }

    @Override
    public String getName() {
        return "ping";
    }
}

/*
class Ping(AriesProtocolMessage, metaclass=RegisterMessage):

        def __init__(self, comment: Optional[str]=None, response_requested: Optional[bool]=None, *args, **kwargs):
        super().__init__(*args, **kwargs)
        if comment is not None:
        self['comment'] = comment
        if response_requested is not None:
        self['response_requested'] = response_requested

@property
    def comment(self) -> Optional[str]:
            return self.get('comment', None)

@property
    def response_requested(self) -> Optional[bool]:
            return self.get('response_requested', None)*/
