package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractDID;
import com.sirius.sdk.errors.sirius_exceptions.*;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DIDProxy extends AbstractDID  {
    AgentRPC rpc;

    public DIDProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }


    @Override
    public Pair<String, String> createAndStoreMyDid(String did, String seed, Boolean cid) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("seed", seed)
                    .add("did", did)
                    .add("cid", cid)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/create_and_store_my_did", params);
            System.out.println("response=" + response);
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
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .add("verkey", verkey)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/store_their_did", params);
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }

    }

    @Override
    public void setDidMetadata(String did, String metadata) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .add("metadata", metadata)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/set_did_metadata", params);
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
    }

    @Override
    public List<Object> listMyDidsWithMeta() {
        try {
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/list_my_dids_with_meta");
            System.out.println("response" + response);
            if (response instanceof JSONArray) {
                List<Object> objectList = new ArrayList<>();
                for (int i = 0; i < ((JSONArray) response).length(); i++) {
                    Object object = ((JSONArray) response).get(i);
                    objectList.add(object);
                }
                return objectList;
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String getDidMetadata(String did) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_did_metadata", params);
            if(response!=null){
                return response.toString();
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }

        return null;
    }

    @Override
    public String keyForLocalDid(String did) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/key_for_local_did", params);
            if (response != null) {
                return response.toString();
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String keyForDid(String poolName, String did) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .add("pool_name", poolName)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/key_for_did", params);
            if(response!=null){
                return response.toString();
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String createKey(String seed) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("seed", seed)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/create_key__did", params);
            if (response != null) {
                return response.toString();
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String replaceKeysStart(String did, String seed) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("seed", seed)
                    .add("did", did)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/replace_keys_start", params);
            if (response != null) {
                return response.toString();
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public void replaceKeysApply(String did) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/replace_keys_apply", params);
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
    }

    @Override
    public void setKeyMetadata(String verkey, String metadata) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("verkey", verkey)
                    .add("metadata", metadata)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/set_key_metadata__did", params);
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
    }

    @Override
    public String getKeyMetadata(String verkey) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("verkey", verkey)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_key_metadata__did", params);
            if (response != null) {
                return response.toString();
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public void setEndpointForDid(String did, String address, String transportKey) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .add("address", address)
                    .add("transport_key", transportKey)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/set_endpoint_for_did", params);
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
    }

    @Override
    public Pair<String, String> getEndpointForDid(String poolName, String did) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .add("pool_name", poolName)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_endpoint_for_did", params);
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getMyDidMeta(String did) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/get_my_did_with_meta", params);
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String abbreviateVerKey(String did, String fullVerkey) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/abbreviate_verkey", params);
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public String qualifyDid(String did, String method) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .add("method", method)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/qualify_did", params);
            if (response != null) {
                return response.toString();
            }
        } catch (SiriusConnectionClosed | SiriusRPCError | SiriusTimeoutRPC | SiriusInvalidType | SiriusPendingOperation siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }


}
