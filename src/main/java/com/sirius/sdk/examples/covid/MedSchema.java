package com.sirius.sdk.examples.covid;

import org.json.JSONObject;

public class MedSchema extends JSONObject {

    public MedSchema() {
        super();
    }

    public MedSchema setFullName(String name) {
        put("full_name", name);
        return this;
    }

    public MedSchema setLocation(String location) {
        put("location", location);
        return this;
    }

    public MedSchema setBioLocation(String bioLocation) {
        put("bio_location", bioLocation);
        return this;
    }

    public MedSchema setTimestamp(String timestamp) {
        put("timestamp", timestamp);
        return this;
    }

    public MedSchema setApproved(String approved) {
        put("approved", approved);
        return this;
    }

    public MedSchema setSarsCov2Igm(Boolean has) {
        put("sars_cov_2_igm", has.toString());
        return this;
    }

    public MedSchema setSarsCov2Igg(Boolean has) {
        put("sars_cov_2_igg", has.toString());
        return this;
    }
}