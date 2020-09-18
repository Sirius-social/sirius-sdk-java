package com.sirius.sdk.agent.model.pairwise;

import java.util.ArrayList;
import java.util.List;

public class TheirEndpoint {
    String endpoint;
    String verkey;
    List<String> routingKeys;

    public String getEndpoint() {
        return endpoint;
    }

    public String getVerkey() {
        return verkey;
    }

    public List<String> getRoutingKeys() {
        return routingKeys;
    }



    public TheirEndpoint(String endpoint, String verkey, List<String> routingKeys) {
        this.endpoint = endpoint;
        this.verkey = verkey;
        this.routingKeys = routingKeys;
        if(routingKeys == null){
            this.routingKeys = new ArrayList<>();
        }
    }
    public TheirEndpoint(String endpoint, String verkey) {
        this(endpoint,verkey,null);
    }

/*
    @property
    def netloc(self) -> Optional[str]:
            if self.endpoint:
            return urlparse(self.endpoint).netloc
        else:
                return None

    @netloc.setter
    def netloc(self, value: str):
            if self.endpoint:
    components = list(urlparse(self.endpoint))
    components[1] = value
    self.endpoint = urlunparse(components)
*/


 /*   public String netloc(){

    }

    @property
    def netloc(self) -> Optional[str]:
            if self.endpoint:
            return urlparse(self.endpoint).netloc
        else:
                return None

    @netloc.setter
    def netloc(self, value: str):
            if self.endpoint:
    components = list(urlparse(self.endpoint))
    components[1] = value
    self.endpoint = urlunparse(components)*/
}
