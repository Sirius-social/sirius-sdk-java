package com.sirius.sdk.agent.wallet.impl;

import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PoolMobile {
    public static Map<String, Pool> openedPoolRegistry = new ConcurrentHashMap<>();

    public static void registerPool(String name, String genesisFilePath) {
        try {
            PoolJSONParameters.CreatePoolLedgerConfigJSONParameter createPoolLedgerConfigJSONParameter
                    = new PoolJSONParameters.CreatePoolLedgerConfigJSONParameter(genesisFilePath);
            Pool.createPoolLedgerConfig(name, createPoolLedgerConfigJSONParameter.toJson()).get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (!e.getMessage().contains("PoolLedgerConfigExists"))
                e.printStackTrace();
        }
    }

    public Pool getPoolHandle(String name) {
        if (openedPoolRegistry.containsKey(name))
            return openedPoolRegistry.get(name);
        try {
            Pool pool = Pool.openPoolLedger(name, null).get();
            return pool;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
