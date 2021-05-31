package com.sirius.sdk.agent.aries_rfc.concept_0017_attachments;

import org.json.JSONObject;
import java.util.Base64;

public class Attach extends JSONObject {

    public Attach(JSONObject obj) {
        super(obj.toString());
    }

    public Attach() {
        super();
    }

    public String getId() {
        return optString("@id");
    }

    public Attach setId(String id) {
        put("@id", id);
        return this;
    }

    public String getMimeType() {
        return optString("mime_type");
    }

    public Attach setMimeType(String mimeType) {
        put("mime_type", mimeType);
        return this;
    }

    public String getFileName() {
        return optString("filename");
    }

    public Attach setFileName(String fileName) {
        put("filename", fileName);
        return this;
    }

    public String getLastModTime() {
        return optString("lastmod_time");
    }

    public Attach setLastModTime(String lastModTime) {
        put("lastmod_time", lastModTime);
        return this;
    }

    public String getDescription() {
        return optString("description");
    }

    public Attach setDescription(String description) {
        put("description", description);
        return this;
    }

    public byte[] getData() {
        if (!has("data"))
            return null;
        return Base64.getDecoder().decode(getJSONObject("data").optString("base64"));
    }

    public Attach setData(byte[] data) {
        put("data", new JSONObject().
                put("base64", new String(Base64.getEncoder().encode(data))));
        return this;
    }
}
