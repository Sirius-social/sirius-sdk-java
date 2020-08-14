package com.sirius.sdk.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.sirius.sdk.base.JsonSerializable;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import org.json.JSONObject;

import java.util.UUID;


public class Message implements JsonSerializable<Message> {


    public static final String FIELD_TYPE = "@type";
    public static final String FIELD_ID = "@id";
    @SerializedName("@type")
    String type;
    @SerializedName("@id")
    String id;

    JSONObject messageObj;
    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }


    public String prettyPrint() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(this, this.getClass());
    }

    public Message(String message) {
        messageObj = new JSONObject(message);
        if (!messageObjectHasKey(FIELD_TYPE)) {
            //   throw new SiriusInvalidMessage("No @type in message");
        }

        type = messageObj.getString(FIELD_TYPE);

        id = getStringFromJSON(FIELD_ID);
        if (id.isEmpty()) {
            this.id = generateId();
        }

    }

    public String getStringFromJSON(String key){
        if(messageObjectHasKey(key)){
           String value =  messageObj.getString(key);
            if(value == null || value.isEmpty()){
                return "";
            }
            return value;
        }
        return "";
    }

    public  boolean messageObjectHasKey(String key){
        return messageObj.has(key);
    }

    public JSONObject getJSONOBJECTFromJSON(String key){
        if(messageObjectHasKey(key)){
            return messageObj.getJSONObject(key);
        }
        return null;
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this, this.getClass());

    }

    @Override
    public Message deserialize(String string) {
        Gson gson = new Gson();
        return gson.fromJson(string, this.getClass());
    }
}
/*

# Registry for restoring message instance from payload
        MSG_REGISTRY={}


        def generate_id():
        """ Generate a message id. """
        return str(uuid.uuid4())


        Message(dict):
        """ Message base class.
        Inherits from dict meaning it behaves like a dictionary.
    """
        __slots__=(
        'mtc',
        '_type'
        )e

        def __init__(self,*args,**kwargs):
        super().__init__(*args,**kwargs)

        if'@type'not in self:
        raise SiriusInvalidMessage('No @type in message')

        if'@id'not in self:
        self['@id']=generate_id()
        elif not isinstance(self['@id'],str):
        raise SiriusInvalidMessage('Message @id is invalid; must be str')

        if isinstance(self['@type'],Type):
        self._type=self['@type']
        self['@type']=str(self._type)
        else:
        self._type=Type.from_str(self.type)

@property
    def type(self):
            """ Shortcut for msg['@type'] """
            return self['@type']

@property
    def id(self):  # pylint:disable=invalid-name
            """ Shortcut for msg['@id'] """
            return self['@id']

@property
    def doc_uri(self)->str:
            """ Get type doc_uri """
            return self._type.doc_uri

@property
    def protocol(self)->str:
            """ Get type protocol """
            return self._type.protocol

@property
    def version(self)->str:
            """ Get type version """
            return self._type.version

@property
    def version_info(self)->Semver:
            """ Get type version info """
            return self._type.version_info

@property
    def name(self)->str:
            """ Get type name """
            return self._type.name

@property
    def normalized_version(self)->str:
            """ Get type normalized version """
            return str(self._type.version_info)

            # Serialization
@classmethod
    def deserialize(cls,serialized:str):
            """ Deserialize a message from a json string. """
            try:
            return cls(json.loads(serialized))
            except json.decoder.JSONDecodeError as err:
            raise SiriusInvalidMessage('Could not deserialize message')from err

            def serialize(self):
            """ Serialize a message into a json string. """
            return json.dumps(self)

            def pretty_print(self):
            """ return a 'pretty print' representation of this message. """
            return json.dumps(self,indent=2)

            def __eq__(self,other):
            if not isinstance(other,Message):
            return False

            return super().__eq__(other)

            def __hash__(self):
            return hash(self.id)


            def register_message_class(cls,protocol:str,name:str=None):
            if issubclass(cls,Message):
            descriptor=MSG_REGISTRY.get(protocol,{})
            if name:
            descriptor[name]=cls
            else:
            descriptor['*']=cls
            MSG_REGISTRY[protocol]=descriptor
            else:
            raise SiriusInvalidMessageClass()


            def restore_message_instance(payload:dict)->(bool,Message):
            if'@type'in payload:
            typ=Type.from_str(payload['@type'])
            descriptor=MSG_REGISTRY.get(typ.protocol,None)
            if descriptor:
            if typ.name in descriptor:
            cls=descriptor[typ.name]
            elif'*'in descriptor:
            cls=descriptor['*']
            else:
            cls=None
            else:
            cls=None
            if cls is not None:
            return True,cls(**payload)
            else:
            return False,None
            else:
            return False,None
*/
