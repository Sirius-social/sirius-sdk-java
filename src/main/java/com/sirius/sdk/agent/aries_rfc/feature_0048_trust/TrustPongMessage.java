package com.sirius.sdk.agent.aries_rfc.feature_0048_trust;

import com.sirius.sdk.agent.aries_rfc.AriesBaseMessage;

/**
 * Implementation of Pong part for trust_ping protocol
 * https://github.com/hyperledger/aries-rfcs/tree/master/features/0048-trust-ping
 */
public class TrustPongMessage extends AriesBaseMessage {

    String comment;
    String threadId;

    public TrustPongMessage(String message) {
        super(message);
        comment = getStringFromJSON("comment");
        threadId = getJSONOBJECTFromJSON(THREAD_DECORATOR).getString("thid");
    }



    @Override
    public String getProtocol() {
        return "trust_ping";
    }

    @Override
    public String getName() {
        return "ping_response";
    }

    public String getThreadId() {
        try {
            return getJSONOBJECTFromJSON(THREAD_DECORATOR).getString("thid");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}

/*
class Pong(AriesProtocolMessage, metaclass=RegisterMessage):

    def __init__(self, ping_id: str=None, comment: Optional[str]=None, *args, **kwargs):
        super().__init__(*args, **kwargs)
        if ping_id is not None:
            self.get(THREAD_DECORATOR, {}).update({'thid': ping_id})
        if comment is not None:
            self['comment'] = comment

    @property
    def comment(self) -> Optional[str]:
        return self.get('comment', None)

    @property
    def ping_id(self):
        return self.get(THREAD_DECORATOR, {}).get('thid', None)

*/
