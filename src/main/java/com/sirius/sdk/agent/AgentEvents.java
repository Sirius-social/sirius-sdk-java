package com.sirius.sdk.agent;

import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.messaging.Message;

/**
 * RPC service.
 *
 *     Reactive nature of Smart-Contract design
 */
public class AgentEvents extends BaseAgentConnections{

    String tunnel;
    String balancingGroup;

    public AgentEvents(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout) {
        super(serverAddress, credentials, p2p, timeout);
    }

    @Override
    public String path() {
        return "/events";
    }

    @Override
    public void setup(Message context) {
        super.setup(context);
          /*   # Extract load balancing info
        balancing = context.get('~balancing', [])
        for balance in balancing:
        if balance['id'] == 'kafka':
        self.__balancing_group = balance['data']['json']['group_id']*/
    }
}
/*
"""
    """

        def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.__tunnel = None
        self.__balancing_group = None

@property
    def balancing_group(self) -> str:
            return self.__balancing_group

            async def pull(self, timeout: int=None) -> Message:
            if not self._connector.is_open:
            raise SiriusConnectionClosed('Open agent connection at first')
            data = await self._connector.read(timeout=timeout)
            try:
            payload = json.loads(data.decode(self._connector.ENC))
            except json.JSONDecodeError:
            raise SiriusInvalidPayloadStructure()
            if 'protected' in payload:
            message = self._p2p.unpack(payload)
            return Message(message)
            else:
            return Message(payload)

@classmethod
    def _path(cls):
            return '/events'

            async def _setup(self, context: Message):

*/
