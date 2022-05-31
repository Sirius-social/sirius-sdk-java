package com.github.wenweihu86.raft.storage;

import com.github.wenweihu86.raft.models.LogMetaData;
import com.github.wenweihu86.raft.models.SnapshotMetaData;

import java.util.HashMap;
import java.util.Map;

public class MemoryStorage {
    String serverId;

    private MemoryStorage(String serverId) {
        this.serverId = serverId;
    }

    private static Map<String, MemoryStorage> instanseMap = new HashMap<String, MemoryStorage>();

    public static MemoryStorage getInstance(String serverId) {
        if (!instanseMap.containsKey(serverId)) {
            MemoryStorage instanse = new MemoryStorage(serverId);
            instanseMap.put(serverId, instanse);
        }
        return instanseMap.get(serverId);
    }

    LogMetaData logMetaData;
    SnapshotMetaData snapshotMetaData;

    public LogMetaData readLogMetaData() {
        if (logMetaData == null) {
            logMetaData = new LogMetaData();
        }
        return logMetaData;
    }

    public SnapshotMetaData readSnapshotMetaData() {
        if (snapshotMetaData == null) {
            snapshotMetaData = new SnapshotMetaData();
        }
        return snapshotMetaData;
    }
}
