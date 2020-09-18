package com.sirius.sdk.agent.model.pairwise;

import org.json.JSONObject;

import java.util.List;

public class Pairwise {
    Me me;
    Their their;
    JSONObject metadata;


    public Me getMe() {
        return me;
    }

    public Their getTheir() {
        return their;
    }

    public JSONObject getMetadata() {
        return metadata;
    }


    public Pairwise(Me me, Their their, JSONObject metadata) {
        this.me = me;
        this.their = their;
        this.metadata = metadata;
    }

    public Pairwise(Me me, Their their) {
        this.me = me;
        this.their = their;
        this.metadata = null;
    }


   public static class Their extends  TheirEndpoint{
        String did;



       public String getDid() {
            return did;
        }

        public String getLabel() {
            return label;
        }

        String label;
        public Their(String did, String label,String endpoint, String verkey, List<String> routingKeys) {
            super(endpoint, verkey, routingKeys);
            this.did = did;
            this.label = label;
        }
       public Their(String did, String label,String endpoint, String verkey) {
           super(endpoint, verkey);
           this.did = did;
           this.label = label;
       }
    }


    public static class Me{

        String did;
        String verkey;

        public String getDid() {
            return did;
        }

        public String getVerkey() {
            return verkey;
        }



        public Me(String did, String verkey) {
            this.did = did;
            this.verkey = verkey;
        }

    }
}
