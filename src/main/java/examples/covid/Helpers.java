package examples.covid;

import com.sirius.sdk.agent.model.Entity;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.CloudHub;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Helpers {

    public static Pairwise establishConnection(CloudHub.Config myConf, Entity myEntity, CloudHub.Config theirConf, Entity theirEntity) {
        Context me = new Context(myConf);
        Context their = new Context(theirConf);

        {
            Pairwise pairwise = me.getPairwiseList().loadForDid(theirEntity.getDid());
            boolean isFilled = (pairwise != null) && (pairwise.getMetadata() != null);
            if (!isFilled) {
                Pairwise.Me me_ = new Pairwise.Me(myEntity.getDid(), myEntity.getVerkey());
                Pairwise.Their their_ = new Pairwise.Their(theirEntity.getDid(), theirEntity.getLabel(),
                        their.getEndpointAddressWithEmptyRoutingKeys(), theirEntity.getVerkey(), new ArrayList<>());

                JSONObject metadata = (new JSONObject()).
                        put("me", (new JSONObject()).
                                put("did", myEntity.getDid()).
                                put("verkey", myEntity.getVerkey())).
                        put("their", (new JSONObject()).
                                put("did", theirEntity.getDid()).
                                put("verkey", theirEntity.getVerkey()).
                                put("label", theirEntity.getLabel()).
                                put("endpoint", (new JSONObject()).
                                        put("address", their.getEndpointAddressWithEmptyRoutingKeys()).
                                        put("routing_keys", new JSONArray())));

                pairwise = new Pairwise(me_, their_, metadata);
                me.getDid().storeTheirDid(theirEntity.getDid(), theirEntity.getVerkey());
                me.getPairwiseList().ensureExists(pairwise);
            }
        }

        {
            Pairwise pairwise = their.getPairwiseList().loadForDid(theirEntity.getDid());
            boolean isFilled = (pairwise != null) && (pairwise.getMetadata() != null);
            if (!isFilled) {
                Pairwise.Me me_ = new Pairwise.Me(theirEntity.getDid(), theirEntity.getVerkey());
                Pairwise.Their their_ = new Pairwise.Their(myEntity.getDid(), myEntity.getLabel(),
                        me.getEndpointAddressWithEmptyRoutingKeys(), myEntity.getVerkey(), new ArrayList<>());

                JSONObject metadata = (new JSONObject()).
                        put("me", (new JSONObject()).
                                put("did", theirEntity.getDid()).
                                put("verkey", theirEntity.getVerkey())).
                        put("their", (new JSONObject()).
                                put("did", myEntity.getDid()).
                                put("verkey", myEntity.getVerkey()).
                                put("label", myEntity.getLabel()).
                                put("endpoint", (new JSONObject()).
                                        put("address", me.getEndpointAddressWithEmptyRoutingKeys()).
                                        put("routing_keys", new JSONArray())));

                pairwise = new Pairwise(me_, their_, metadata);
                their.getDid().storeTheirDid(myEntity.getDid(), myEntity.getVerkey());
                their.getPairwiseList().ensureExists(pairwise);
            }
        }

        Pairwise res = me.getPairwiseList().loadForDid(theirEntity.getDid());
        me.close();
        their.close();
        return res;
    }
}
