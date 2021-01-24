package com.sirius.sdk.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.base.JsonSerializable;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessageClass;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;
import com.sirius.sdk.utils.GsonUtils;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class Message implements JsonSerializable<Message> {


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static final class MessageBuilder {
        JSONObject jsonObject =new JSONObject();

        public MessageBuilder(String id, String type) {
            jsonObject.put("@id", id);
            jsonObject.put("@type", type);
        }

        public MessageBuilder add(String key, Object value){
            jsonObject.put(key,value);
            return this;
        }

        public Message build() {
            Message message = new Message(jsonObject.toString());
            return message;
        }


    }
/*
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
            return self._type.name*/


    public static Map<String, Map<String, Class<? extends Message>>> MSG_REGISTRY = new HashMap<>();

    public static final String FIELD_TYPE = "@type";
    public static final String FIELD_ID = "@id";

    @SerializedName("@type")
    private
    String type;
    @SerializedName("@id")
    private
    String id;

    public JSONObject getMessageObj() {
        return messageObj;
    }

    JSONObject messageObj;
    Type typeOfType;



    public String prettyPrint() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(this, this.getClass());
    }

    public Message(String message) {
        System.out.println("message="+message);
        messageObj = new JSONObject(message);
        if (!messageObjectHasKey(FIELD_TYPE)) {
            //   throw new SiriusInvalidMessage("No @type in message");
        }

        type = messageObj.getString(FIELD_TYPE);
        try {
            typeOfType = Type.fromStr(type);
        } catch (SiriusInvalidType siriusInvalidType) {
            siriusInvalidType.printStackTrace();
        }
        id = getStringFromJSON(FIELD_ID);
        if (id.isEmpty()) {
            this.id = generateId();
        }

    }

    public Object getObjectFromJSON(String key) {
        if (messageObjectHasKey(key)) {
            if( messageObj.isNull(key)){
                return null;
            }
            return messageObj.get(key);
        }
        return null;
    }

    public String getStringFromJSON(String key) {
        if (messageObjectHasKey(key)) {
            String value = messageObj.getString(key);
            if (value == null || value.isEmpty()) {
                return "";
            }
            return value;
        }
        return "";
    }

    public Boolean getBooleanFromJSON(String key) {
        if (messageObjectHasKey(key)) {
            return messageObj.getBoolean(key);
        }
        return null;

    }


    public boolean messageObjectHasKey(String key) {
        return messageObj.has(key);
    }

    public JSONObject getJSONOBJECTFromJSON(String key) {
        if (messageObjectHasKey(key)) {
            return messageObj.optJSONObject(key);
        }
        return null;
    }

    public JSONArray getJSONArrayFromJSON(String key,JSONArray defaultValue) {
        if (messageObjectHasKey(key)) {
           JSONArray object =  messageObj.getJSONArray(key);
           if(object == null){
               return defaultValue;
           }
           return object;

        }
        return defaultValue;
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String serialize() {
        Gson gson = new Gson();
      //  return gson.toJson(this, this.getClass());
        return messageObj.toString();

    }




    @Override
    public JSONObject serializeToJSONObject() {
        String string = serialize();
        return new JSONObject(string);
    }


    @Override
    public Message deserialize(String string) {
        Gson gson = new Gson();
        return gson.fromJson(string, this.getClass());
    }

    @Override
    public JsonObject serializeToJsonObject() {
       return GsonUtils.getDefaultGson().toJsonTree(this, Message.class).getAsJsonObject();
    }

    public static void registerMessageClass(Class<? extends Message> clas, String protocol, String name) {
        Map<String, Class<? extends Message>> descriptor = MSG_REGISTRY.getOrDefault(protocol, new HashMap<>());
        if (name != null && !name.isEmpty()) {
            descriptor.put(name, clas);
        } else {
            descriptor.put("*", clas);
        }
        MSG_REGISTRY.put(protocol, descriptor);
    }

    public static void registerMessageClass(Class<? extends Message> clas, String protocol) {
        registerMessageClass(clas,protocol, null);
    }

    public static Pair<Boolean, Message> restoreMessageInstance(String payload) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Pair<Boolean, Message> pair = new Pair<>(false, null);
        Message message = new Message(payload);
        String protocol = message.typeOfType.protocol;
        String name = message.typeOfType.name;
        Map<String, Class<? extends Message>> descriptor = MSG_REGISTRY.get(protocol);
        if (descriptor != null) {
            Class<? extends Message> clssTo = null;
            if (descriptor.containsKey(name)) {
                clssTo = descriptor.get(name);
            } else if (descriptor.containsKey("*")) {
                clssTo = descriptor.get("*");
            }
            if (clssTo != null) {
                Constructor<? extends Message> constructor = clssTo.getConstructor(String.class);
                return new Pair<>(true, constructor.newInstance(payload));
            }

        }
        return pair;

    }

/*    def restore_message_instance(payload:dict)->(bool,Message):
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
                    return False,None*/

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


*/
