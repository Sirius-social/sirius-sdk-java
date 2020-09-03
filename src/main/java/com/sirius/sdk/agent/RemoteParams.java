package com.sirius.sdk.agent;

import java.util.HashMap;
import java.util.Map;

public class RemoteParams {
    private Map<String,Object> params = new HashMap<>();

    private RemoteParams(Map<String, Object> params) {
        this.params = params;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }


    public static final class RemoteParamsBuilder {
        private Map<String, Object> params = new HashMap<>();

        private RemoteParamsBuilder() {
        }

        public static RemoteParamsBuilder create() {
            return new RemoteParamsBuilder();
        }

        public RemoteParamsBuilder add(String name,Object object) {
            params.put(name,object);
            return this;
        }

        public RemoteParams build() {
            RemoteParams remoteParams = new RemoteParams(params);
            return remoteParams;
        }
    }
}
