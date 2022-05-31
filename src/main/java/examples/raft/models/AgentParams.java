package examples.raft.models;

import com.sirius.sdk.agent.model.Entity;
import com.sirius.sdk.encryption.P2PConnection;

import java.util.List;

public class AgentParams {
    String serverAddress;

    public AgentParams(String serverAddress, String credentials, P2PConnection connection, List<Entity> entitiesList) {
        this.serverAddress = serverAddress;
        this.credentials = credentials;
        this.connection = connection;
        this.entitiesList = entitiesList;
    }

    String credentials;
    P2PConnection connection;
    List<Entity> entitiesList;

    public String getServerAddress() {
        return serverAddress;
    }

    public String getCredentials() {
        return credentials;
    }

    public P2PConnection getConnection() {
        return connection;
    }


    public List<Entity> getEntitiesList() {
        return entitiesList;
    }


}
