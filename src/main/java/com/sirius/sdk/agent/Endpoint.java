package com.sirius.sdk.agent;

import java.util.List;

/**
 * Active Agent endpoints
 *     https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0094-cross-domain-messaging
 */
public class Endpoint {
    String address;
    List<String> routingKeys;
    boolean isDefault;

    public Endpoint(String address, List<String> routingKeys, boolean isDefault) {
        this.address = address;
        this.routingKeys = routingKeys;
        this.isDefault = isDefault;
    }

    public Endpoint(String address, List<String> routingKeys) {
        this.address = address;
        this.routingKeys = routingKeys;
    }



    public String getAddress() {
        return address;
    }

    public List<String> getRoutingKeys() {
        return routingKeys;
    }

    public boolean isDefault() {
        return isDefault;
    }
}

