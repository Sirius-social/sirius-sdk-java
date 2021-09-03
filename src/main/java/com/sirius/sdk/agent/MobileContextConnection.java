package com.sirius.sdk.agent;

import java.util.List;

public class MobileContextConnection{
    public String getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    public List<String> getRecipientKeys() {
        return recipientKeys;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    String type;
        int priority;
        List<String> recipientKeys;
        String serviceEndpoint;

        public MobileContextConnection(String type, int priority, List<String> recipientKeys, String serviceEndpoint) {
            this.type = type;
            this.priority = priority;
            this.recipientKeys = recipientKeys;
            this.serviceEndpoint = serviceEndpoint;
        }
    }