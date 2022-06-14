package com.sirius.sdk.agent.n_wise.messages;

import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.messaging.Message;

public class InitialMessage extends BaseNWiseMessage {

    static {
        Message.registerMessageClass(InitialMessage.class, BaseNWiseMessage.PROTOCOL, "initial-message");
    }

    public String chatName;
    public String creatorNickname;
    public String creatorDid;
    public Endpoint creatorEndpoint;

    public String getChatName() {
        return getMessageObj().optString("chatName");
    }

    public void setChatName(String chatName) {
        this.getMessageObj().put("chatName", chatName);
    }

    public String getCreatorNickname() {
        return creatorNickname;
    }

    public void setCreatorNickname(String creatorNickname) {
        this.creatorNickname = creatorNickname;
    }

    public String getCreatorDid() {
        return creatorDid;
    }

    public void setCreatorDid(String creatorDid) {
        this.creatorDid = creatorDid;
    }

    public Endpoint getCreatorEndpoint() {
        return creatorEndpoint;
    }

    public void setCreatorEndpoint(Endpoint creatorEndpoint) {
        this.creatorEndpoint = creatorEndpoint;
    }
}
