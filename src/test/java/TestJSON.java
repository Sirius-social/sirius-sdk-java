import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestJSON {

    @Test
    public void testMany(){
        JSONObject requestedAttributes = new JSONObject().put("test1","test1").put("test2","test2").put("test3","test3");



        JSONObject result = new JSONObject().
                put("self_attested_attributes", new JSONObject()).
                put("requested_attributes", new JSONObject()).
                put("requested_predicates", new JSONObject());

        for (String attrReferent : requestedAttributes.keySet()) {
            List<String> credForAttrStr = new ArrayList<>();
            credForAttrStr.add("testname");
            credForAttrStr.add("testvalue");
            JSONArray credForAttr = new JSONArray(credForAttrStr);
            JSONArray collection = result.optJSONObject("requested_attributes").optJSONArray(attrReferent);
            collection = collection != null ? collection : new JSONArray();
            for (Object o : credForAttr)
                collection.put(o);
            result.optJSONObject("requested_attributes").put(attrReferent, collection);
        }
        System.out.println("result="+result);
    }

    @Test
    public void testMany2(){
        JSONObject requestedAttributes = new JSONObject().put("test1","test1").put("test2","test2").put("test3","test3");



        JSONObject result = new JSONObject().
                put("self_attested_attributes", new JSONObject()).
                put("requested_attributes", new JSONObject().put("inside", new JSONObject())).
                put("requested_predicates", new JSONObject());

        for (String attrReferent : requestedAttributes.keySet()) {
            List<String> credForAttrStr = new ArrayList<>();
            credForAttrStr.add("testname");
            credForAttrStr.add("testvalue");
            JSONArray credForAttr = new JSONArray(credForAttrStr);
            JSONArray collection = result.optJSONObject("requested_attributes").optJSONArray(attrReferent);
            collection = collection != null ? collection : new JSONArray();
            for (Object o : credForAttr)
                collection.put(o);
            result.optJSONObject("requested_attributes").optJSONObject("inside").put(attrReferent, collection);
        }
        System.out.println("result="+result);
    }
}
