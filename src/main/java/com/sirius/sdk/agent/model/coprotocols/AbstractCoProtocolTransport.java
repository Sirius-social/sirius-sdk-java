package com.sirius.sdk.agent.model.coprotocols;

import com.sirius.sdk.agent.AgentRPC;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction application-level protocols in the context of interactions among agent-like things.
 *
 *         Sirius SDK protocol is high-level abstraction over Sirius transport architecture.
 *         Approach advantages:
 *           - developer build smart-contract logic in block-style that is easy to maintain and control
 *           - human-friendly source code of state machines in procedural style
 *           - program that is running in separate coroutine: lightweight abstraction to start/kill/state-detection work thread
 *         See details:
 *           - https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0003-protocols
 */
public abstract class AbstractCoProtocolTransport {

    public AbstractCoProtocolTransport(AgentRPC rpc) {
        this.rpc = rpc;
    }

    AgentRPC rpc;
    String theirVK;
    String myVerkey;
    String endpoint;
    List<String> routingKeys;
    boolean isSetup;
    /**
     * Should be called in Descendant
     * @param theirVerkey
     * @param endpoint
     * @param myVerkey
     * @param routing_keys
     */
    public void  setup(String theirVerkey, String endpoint, String myVerkey, List<String> routing_keys){
        this.theirVK = theirVerkey;
        this.myVerkey= myVerkey;
        this.endpoint = endpoint;
        this.routingKeys = routing_keys;
        if(routingKeys==null){
            routingKeys = new ArrayList<>();
        }
        isSetup = true;
    }



}

/*

         THREAD_DECORATOR = '~thread'
         PLEASE_ACK_DECORATOR = '~please_ack'

         SEC_PER_DAY = 86400
         SEC_PER_HOURS = 3600
         SEC_PER_MIN = 60

         def __init__(self, rpc: AgentRPC):
         """
        :param rpc: RPC (independent connection)
        """
         self.__time_to_live = None
         self._rpc = rpc
         self._check_protocols = True
         self._check_verkeys = False
         self.__default_timeout = rpc.timeout
         self.__wallet = DynamicWallet(self._rpc)
         self.__die_timestamp = None
         self.__their_vk = None
         self.__endpoint = None
         self.__my_vk = None
         self.__routing_keys = None
         self.__is_setup = False
         self.__protocols = []
         self.__please_ack_ids = []

@property
    def protocols(self) -> List[str]:
            return self.__protocols

@property
    def time_to_live(self) -> int:
            return self.__time_to_live

            def _setup(self, their_verkey: str, endpoint: str, my_verkey: str=None, routing_keys: List[str]=None):
            """Should be called in Descendant"""
            self.__their_vk = their_verkey
            self.__my_vk = my_verkey
            self.__endpoint = endpoint
            self.__routing_keys = routing_keys or []
            self.__is_setup = True

@property
    def wallet(self) -> DynamicWallet:
            return self.__wallet

@property
    def is_alive(self) -> bool:
            if self.__die_timestamp:
            return datetime.now() < self.__die_timestamp
        else:
        return False

        async def start(self, protocols: List[str], time_to_live: int=None):
        self.__protocols = protocols
        self.__time_to_live = time_to_live
        if self.__time_to_live:
        self.__die_timestamp = datetime.now() + timedelta(seconds=self.__time_to_live)
        else:
        self.__die_timestamp = None

        async def stop(self):
        self.__die_timestamp = None
        await self.__cleanup_context()

        async def switch(self, message: Message) -> (bool, Message):
        """Send Message to other-side of protocol and wait for response

        :param message: Protocol request
        :return: (success, Response)
        """
        if not self.__is_setup:
        raise SiriusPendingOperation('You must Setup protocol instance at first')
        try:
        self._rpc.timeout = self.__get_io_timeout()
        await self.__setup_context(message)
        try:
        event = await self._rpc.send_message(
        message=message,
        their_vk=self.__their_vk,
        endpoint=self.__endpoint,
        my_vk=self.__my_vk,
        routing_keys=self.__routing_keys,
        coprotocol=True
        )
        finally:
        await self.__cleanup_context(message)
        if self._check_verkeys:
        recipient_verkey = event.get('recipient_verkey', None)
        sender_verkey = event.get('sender_verkey')
        if recipient_verkey != self.__my_vk:
        raise SiriusInvalidPayloadStructure(f'Unexpected recipient_verkey: {recipient_verkey}')
        if sender_verkey != self.__their_vk:
        raise SiriusInvalidPayloadStructure(f'Unexpected sender_verkey: {sender_verkey}')
        payload = Message(event.get('message', {}))
        if payload:
        ok, message = restore_message_instance(payload)
        if not ok:
        message = Message(payload)
        if self._check_protocols:
        if Type.from_str(message.type).protocol not in self.protocols:
        raise SiriusInvalidMessage('@type has unexpected protocol "%s"' % message.type.protocol)
        return True, message
        else:
        return False, None
        except SiriusTimeoutIO:
        return False, None

        async def get_one(self) -> (Optional[Message], str, Optional[str]):
        self._rpc.timeout = self.__get_io_timeout()
        event = await self._rpc.read_protocol_message()
        if 'message' in event:
        ok, message = restore_message_instance(event['message'])
        if not ok:
        message = Message(event['message'])
        else:
        message = None
        sender_verkey = event.get('sender_verkey', None)
        recipient_verkey = event.get('recipient_verkey', None)
        return message, sender_verkey, recipient_verkey

        async def send(self, message: Message):
        """Send message and don't wait answer

        :param message:
        :return:
        """
        if not self.__is_setup:
        raise SiriusPendingOperation('You must Setup protocol instance at first')
        self._rpc.timeout = self.__get_io_timeout()
        await self.__setup_context(message)
        await self._rpc.send_message(
        message=message,
        their_vk=self.__their_vk,
        endpoint=self.__endpoint,
        my_vk=self.__my_vk,
        routing_keys=self.__routing_keys,
        coprotocol=False
        )

        async def send_many(self, message: Message, to: List[Pairwise]) -> List[Any]:
        batches = [
        RoutingBatch(p.their.verkey, p.their.endpoint, p.me.verkey, p.their.routing_keys)
        for p in to
        ]
        if not self.__is_setup:
        raise SiriusPendingOperation('You must Setup protocol instance at first')
        self._rpc.timeout = self.__get_io_timeout()
        await self.__setup_context(message)
        results = await self._rpc.send_message_batched(
        message, batches
        )
        return results

        async def __setup_context(self, message: Message):
        if self.PLEASE_ACK_DECORATOR in message:
        ack_message_id = message.get(self.PLEASE_ACK_DECORATOR, {}).get('message_id', None) or message.id
        ttl = self.__get_io_timeout() or 3600
        await self._rpc.start_protocol_with_threads(
        threads=[ack_message_id], ttl=ttl
        )
        self.__please_ack_ids.append(ack_message_id)

        async def __cleanup_context(self, message: Message=None):
        if message:
        if self.PLEASE_ACK_DECORATOR in message:
        ack_message_id = message.get(self.PLEASE_ACK_DECORATOR, {}).get('message_id', None) or message.id
        await self._rpc.stop_protocol_with_threads(
        threads=[ack_message_id], off_response=True
        )
        self.__please_ack_ids = [i for i in self.__please_ack_ids if i != ack_message_id]
        else:
        await self._rpc.stop_protocol_with_threads(
        threads=self.__please_ack_ids, off_response=True
        )
        self.__please_ack_ids.clear()

        def __get_io_timeout(self):
        if self.__die_timestamp:
        now = datetime.now()
        if now < self.__die_timestamp:
        delta = self.__die_timestamp - now
        return delta.days * self.SEC_PER_DAY + delta.seconds
        else:
        return 0
        else:
        return None
*/
