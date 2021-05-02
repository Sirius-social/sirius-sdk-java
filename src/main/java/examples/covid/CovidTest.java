package examples.covid;

import org.json.JSONObject;

public class CovidTest extends JSONObject {

    public CovidTest() {
        super();
    }

    public CovidTest(JSONObject json) {
        super(json.toString());
    }

    public CovidTest setFullName(String name) {
        put("full_name", name);
        return this;
    }

    public String getFullName() {
        return optString("full_name");
    }

    public CovidTest setLocation(String location) {
        put("location", location);
        return this;
    }

    public CovidTest setBioLocation(String bioLocation) {
        put("bio_location", bioLocation);
        return this;
    }

    public CovidTest setTimestamp(String timestamp) {
        put("timestamp", timestamp);
        return this;
    }

    public CovidTest setApproved(String approved) {
        put("approved", approved);
        return this;
    }

    public CovidTest setCovid(Boolean has) {
        put("has_covid", has.toString());
        return this;
    }

    public boolean hasCovid() {
        return optString("has_covid").equals("true");
    }

}
