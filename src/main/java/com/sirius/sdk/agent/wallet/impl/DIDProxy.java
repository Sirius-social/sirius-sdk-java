package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.agent.connections.RemoteCallWrapper;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractDID;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class DIDProxy extends AbstractDID  {
    AgentRPC rpc;

    public DIDProxy(AgentRPC rpc) {
        this.rpc = rpc;
    }


    @Override
    public Pair<String, String> createAndStoreMyDid(String did, String seed, Boolean cid) {
        return new RemoteCallWrapper< Pair<String, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/create_and_store_my_did",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("seed", seed)
                                .add("did", did)
                                .add("cid", cid));
    }

    @Override
    public void storeTheirDid(String did, String verkey) {
        new RemoteCallWrapper< Pair<String, String>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/store_their_did",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("did", did)
                                .add("verkey", verkey));
    }

    @Override
    public void setDidMetadata(String did, String metadata) {
        try {
            RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                    .add("did", did)
                    .add("metadata", metadata)
                    .build();
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/set_did_metadata", params);
        } catch (Exception siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
    }

    @Override
    public List<Object> listMyDidsWithMeta() {
        try {
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/list_my_dids_with_meta");
            if (response instanceof JSONArray) {
                List<Object> objectList = new ArrayList<>();
                for (int i = 0; i < ((JSONArray) response).length(); i++) {
                    Object object = ((JSONArray) response).get(i);
                    objectList.add(object);
                }
                return objectList;
            }
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
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
        } catch (Exception siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }


}
