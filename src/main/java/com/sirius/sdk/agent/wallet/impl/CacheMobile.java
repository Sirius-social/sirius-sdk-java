package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCache;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.CacheOptions;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.PurgeOptions;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.cache.Cache;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CacheMobile extends AbstractCache {

    Wallet wallet;
    int timeoutSec = 60;
    PoolMobile poolMobile ;

    public CacheMobile(Wallet wallet) {
        this.wallet = wallet;
        poolMobile = new PoolMobile();
    }

    @Override
    public String getSchema(String poolName, String submitter_did, String id, CacheOptions options) {
        try {
            Pool pool = poolMobile.getPoolHandle(poolName);
            String optionsStr =  options.serialize();
            return Cache.getSchema(pool,wallet,submitter_did,id,optionsStr).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getCredDef(String poolName, String submitter_did, String id, CacheOptions options) {
        try {
            Pool pool = poolMobile.getPoolHandle(poolName);
            String optionsStr =  options.serialize();
            return Cache.getCredDef(pool, wallet,submitter_did,id,optionsStr).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void purgeSchemaCache(PurgeOptions options) {
        try {
            Cache.purgeSchemaCache(wallet,options.serialize()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void purgeCredDefCache(PurgeOptions options) {
        try {
            Cache.purgeCredDefCache(wallet,options.serialize()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
