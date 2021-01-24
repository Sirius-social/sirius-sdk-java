package com.sirius.sdk.agent.aries_rfc;

import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.messaging.Message;

public abstract class AriesProtocolMessage extends Message {

    public static final String ARIES_DOC_URI = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/";
    public static final String THREAD_DECORATOR = "~thread";

    public AriesProtocolMessage(String message) {
        super(message);
        Message.registerMessageClass(this.getClass(), getProtocol(), getName());
    }

    public AriesProtocolMessage() {
        super("{}");
        Message.registerMessageClass(this.getClass(), getProtocol(), getName());
    }

    public abstract String getProtocol();

    public abstract String getName();


}

/*   PROTOCOL = None
           NAME = None

           def __init__(self, id_: str=None, version: str='1.0', *args, **kwargs):
           if self.NAME and ('@type' not in dict(*args, **kwargs)):
           kwargs['@type'] = str(
           Type(
           doc_uri=ARIES_DOC_URI, protocol=self.PROTOCOL,
           name=self.NAME, version=version
           )
           )
           super().__init__(*args, **kwargs)
           if id_ is not None:
           self['@id'] = id_
           if self.doc_uri != ARIES_DOC_URI:
           raise SiriusValidationError('Unexpected doc_uri "%s"' % self.doc_uri)
           if self.protocol != self.PROTOCOL:
           raise SiriusValidationError('Unexpected protocol "%s"' % self.protocol)
           if self.name != self.NAME:
           raise SiriusValidationError('Unexpected name "%s"' % self.name)

           def validate(self):
           validate_common_blocks(self)*/
