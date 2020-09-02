package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractDID;
import com.sirius.sdk.errors.sirius_exceptions.*;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class DIDProxy extends AbstractDID {
    AgentRPC rpc;

    public DIDProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }


    @Override
    public Pair<String, String> createAndStoreMyDid(String did, String seed, Boolean cid) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            jsonObject.put("seed",seed);
            jsonObject.put("cid",cid);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/create_and_store_my_did",jsonObject.toString());
            System.out.println("response="+response);
            if (response instanceof Pair) {
                Pair<String, String> response1 = (Pair<String, String>) response;
                return response1;
            }
        } catch (SiriusConnectionClosed | SiriusRPCError
                | SiriusTimeoutRPC | SiriusInvalidType |
                SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public void storeTheirDid(String did, String verkey) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            jsonObject.put("verkey",verkey);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/store_their_did",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }

    }

    @Override
    public void setDidMetadata(String did, String metadata) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            jsonObject.put("metadata",metadata);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/set_did_metadata",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
    }

    @Override
    public List<Object> listMyDidsWithMeta() {
        try {

            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/list_my_dids_with_meta");
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String getDidMetadata(String did) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_did_metadata",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }

        return null;
    }

    @Override
    public String keyLocalDid(String did) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/key_for_local_did",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String keyForDid(String poolName, String did) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            jsonObject.put("pool_name",poolName);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/key_for_did",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String createKey(String seed) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("seed",seed);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/create_key__did",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String replaceKeysStart(String did, String seed) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            jsonObject.put("seed",seed);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/replace_keys_start",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public void replaceKeysApply(String did) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);

            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/replace_keys_apply",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
    }

    @Override
    public void setKeyMetadata(String verkey, String metadata) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("verkey",verkey);
            jsonObject.put("metadata",metadata);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/set_key_metadata__did",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
    }

    @Override
    public String getKeyMetadata(String verkey) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("verkey",verkey);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_key_metadata__did",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public void setEndpointForDid(String did, String address, String transportKey) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            jsonObject.put("address",address);
            jsonObject.put("transport_key",transportKey);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/set_endpoint_for_did",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
    }

    @Override
    public Pair<String, String> getEndpointForDid(String poolName, String did) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            jsonObject.put("pool_name",poolName);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_endpoint_for_did",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getMyDidMeta(String did) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_my_did_with_meta",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String abbreviateVerKey(String did, String fullVerkey) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/abbreviate_verkey",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String qualifyDid(String did, String method) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did",did);
            jsonObject.put("method",method);
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/qualify_did",jsonObject.toString());
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }
}
