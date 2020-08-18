package models;

import com.sirius.sdk.encryption.P2PConnection;
import org.json.JSONObject;

public class AgentParams {
    String serverAddress;

    public AgentParams(String serverAddress, String credentials, P2PConnection connection, JSONObject entitiesObject) {
        this.serverAddress = serverAddress;
        this.credentials = credentials;
        this.connection = connection;
        this.entitiesObject = entitiesObject;
    }

    String credentials;
    P2PConnection connection;
    JSONObject entitiesObject;

    public String getServerAddress() {
        return serverAddress;
    }

    public String getCredentials() {
        return credentials;
    }

    public P2PConnection getConnection() {
        return connection;
    }

    public JSONObject getEntitiesObject() {
        return entitiesObject;
    }
}
