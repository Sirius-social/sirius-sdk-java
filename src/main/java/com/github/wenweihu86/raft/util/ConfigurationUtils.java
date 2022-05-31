package com.github.wenweihu86.raft.util;

import com.github.wenweihu86.raft.models.Server;
import com.github.wenweihu86.raft.models.Configuration;


import java.util.List;

/**
 * Created by wenweihu86 on 2017/5/22.
 */
public class ConfigurationUtils {

    // configuration
    public static boolean containsServer(Configuration configuration, String serverId) {
        for (Server server : configuration.getServersList()) {
            if (server.getServerId().equals(serverId)) {
                return true;
            }
        }
        return false;
    }

    public static Configuration removeServers(
            Configuration configuration, List<Server> servers) {
        Configuration confBuilder = new Configuration();
        for (Server server : configuration.getServersList()) {
            boolean toBeRemoved = false;
            for (Server server1 : servers) {
                if (server.getServerId() == server1.getServerId()) {
                    toBeRemoved = true;
                    break;
                }
            }
            if (!toBeRemoved) {
                confBuilder.addServers(server);
            }
        }
        return confBuilder;
    }

    public static Server getServer(Configuration configuration, String serverId) {
        for (Server server : configuration.getServersList()) {
            if (server.getServerId() == serverId) {
                return server;
            }
        }
        return null;
    }

}
