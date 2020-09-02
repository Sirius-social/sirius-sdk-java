package com.sirius.sdk.agent;

import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractPairwise;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class WalletPairwiseList extends AbstractPairwiseList {
    AbstractPairwise api;

    public WalletPairwiseList(AbstractPairwise api) {
        this.api = api;
    }

    public static String buildTags(Pairwise pairwise) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("my_did", pairwise.getMe().getDid());
        jsonObject.put("my_verkey", pairwise.getMe().getVerkey());
        jsonObject.put("their_verkey", pairwise.getTheir().getVerkey());
        return jsonObject.toString();
    }

    public static Pairwise restorePairwise(String metadata) {
        JSONObject metaObject = new JSONObject(metadata);
        JSONObject meObj = metaObject.optJSONObject("me");
        String meDid = null;
        String meVerKey = null;
        if (meObj != null) {
            meDid = meObj.getString("did");
            meVerKey = meObj.getString("verkey");
        }
        Pairwise.Me me = new Pairwise.Me(meDid, meVerKey);
        JSONObject theirObj = metaObject.optJSONObject("their");
        String theirDid = null;
        String theirVerKey = null;
        String theirLabel = null;
        String theirEndpoint = null;
        List<String> theirRoutingKeys = null;
        if (theirObj != null) {
            theirDid = theirObj.getString("did");
            theirVerKey = theirObj.getString("verkey");
            theirLabel = theirObj.getString("label");
            JSONObject endpointObj = theirObj.optJSONObject("endpoint");
            if (endpointObj != null) {
                theirEndpoint = endpointObj.getString("address");
                JSONArray routingArray = endpointObj.getJSONArray("routing_keys");
                if (routingArray != null) {
                    for (int i = 0; i < routingArray.length(); i++) {
                        String key = routingArray.getString(i);
                        theirRoutingKeys.add(key);
                    }
                }
            }
        }
        Pairwise.Their their = new Pairwise.Their(theirDid, theirLabel, theirEndpoint, theirVerKey, theirRoutingKeys);
        return new Pairwise(me, their, metadata);
    }


    @Override
    public void create(Pairwise pairwise) {
        api.createPairwise(pairwise.getTheir().getDid(), pairwise.getMe().getDid(), pairwise.getMetadata(), buildTags(pairwise));
    }

    @Override
    public void update(Pairwise pairwise) {
        api.setPairwiseMetadata(pairwise.getTheir().getDid(), pairwise.getMetadata(), buildTags(pairwise));
    }

    @Override
    public boolean isExists(String theirDid) {
        return api.isPairwiseExist(theirDid);
    }

    @Override
    public void ensureExists(Pairwise pairwise) {
        if (isExists(pairwise.getTheir().getDid())) {
            update(pairwise);
        } else {
            create(pairwise);
        }
    }

    @Override
    public Pairwise loadForDid(String theirDid) {
        if (isExists(theirDid)) {
            String raw = api.getPairwise(theirDid);
            JSONObject metadataObj = new JSONObject(raw);
            JSONObject metadata = metadataObj.getJSONObject("metadata");
            return restorePairwise(metadata.toString());
        } else {
            return null;
        }
    }

    @Override
    public Pairwise loadForVerkey(String theirVerkey) {
        JSONObject tagsObj = new JSONObject();
        tagsObj.put("their_verkey", theirVerkey);
        Pair<List<String>, Integer> results = api.search(tagsObj.toString(), 1);
        if (results.first != null) {
            if (results.first.size() > 0) {
                String raw = results.first.get(0);
                JSONObject metadataObj = new JSONObject(raw);
                JSONObject metadata = metadataObj.getJSONObject("metadata");
                return restorePairwise(metadata.toString());
            }
        }
        return null;
    }
}
