package examples.covid;

import org.json.JSONObject;

class BoardingPass extends JSONObject {

    public BoardingPass() {
        super();
    }

    public BoardingPass setFullName(String name) {
        put("full_name", name);
        return this;
    }

    public String getFullName() {
        return optString("full_name");
    }

    public BoardingPass setFlight(String flight) {
        put("flight", flight);
        return this;
    }

    public BoardingPass setDeparture(String departure) {
        put("departure", departure);
        return this;
    }

    public BoardingPass setArrival(String arrival) {
        put("arrival", arrival);
        return this;
    }

    public BoardingPass setDate(String date) {
        put("date", date);
        return this;
    }

    public BoardingPass setClass(String cls) {
        put("class", cls);
        return this;
    }

    public BoardingPass setSeat(String seat) {
        put("seat", seat);
        return this;
    }
}