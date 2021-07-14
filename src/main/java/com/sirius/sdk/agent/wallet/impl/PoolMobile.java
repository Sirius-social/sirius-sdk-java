package com.sirius.sdk.agent.wallet.impl;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PoolMobile {
   /* POOL_REGISTRY = {}
    POOL_HANDLES = {}
    PROTOCOL_VERSION = 2*/


    public void  registerPool(String name,String genesis_file_path,boolean  ignore_errors){
        /*if name in POOL_REGISTRY:
        if not ignore_errors:
        raise RuntimeError('Pool "%s" already registered' % name)
        POOL_REGISTRY[name] = dict(
                genesis_file_path=genesis_file_path
        )*/
    }



    public void unregisterPool(String name,boolean ignore_errors){
       /* if name not in POOL_REGISTRY:
        if not ignore_errors:
        raise RuntimeError('Pool "%s" not registered' % name)
        if name in POOL_REGISTRY:
        del POOL_REGISTRY[name]*/
    }



    public Pool getPoolHandle(String name){
        try {
            Pool pool = Pool.openPoolLedger(name, null).get();
            return pool;
        } catch (Exception e) {
            e.printStackTrace();
        }
       /* pool_descriptor = POOL_HANDLES.get(name, None)
        if pool_descriptor:
        return pool_descriptor['handle']
            else:
        genesis_path = POOL_REGISTRY.get(name, {}).get('genesis_file_path', None)
        if not genesis_path:
        raise RuntimeError('Pool "%s" was not registered' % name)
        await indy.pool.set_protocol_version(PROTOCOL_VERSION)
        pool_config = json.dumps({'genesis_txn': genesis_path})
        try:
        await indy.pool.create_pool_ledger_config(config_name=name, config=pool_config)
        except PoolLedgerConfigAlreadyExistsError:
        pass
                pool_handle = await indy.pool.open_pool_ledger(config_name=name, config=None)
        pool_descriptor = dict(
                handle=pool_handle,
                created=datetime.datetime.now()
        )
        POOL_HANDLES[name] = pool_descriptor
        return pool_handle*/
        return null;
    }



    public void   closePool(String name){
     /*   pool_descriptor = POOL_HANDLES.get(name, None)
        if pool_descriptor:
        pool_handle = pool_descriptor['handle']
        await indy.pool.close_pool_ledger(pool_handle)
        await indy.pool.delete_pool_ledger_config(name)
        del POOL_HANDLES[name]*/
    }



    public void  refreshPool(String name, boolean ignore_errors){
        /*pool_descriptor = POOL_HANDLES.get(name, None)
        if not pool_descriptor:
        if not ignore_errors:
        raise RuntimeError('Pool "%s" was not opened' % name)
        if pool_descriptor:
        await indy.pool.refresh_pool_ledger(pool_descriptor['handle'])*/
    }


}
