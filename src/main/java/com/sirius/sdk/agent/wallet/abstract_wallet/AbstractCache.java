package com.sirius.sdk.agent.wallet.abstract_wallet;

import com.sirius.sdk.agent.wallet.abstract_wallet.model.CacheOptions;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.PurgeOptions;

public abstract class AbstractCache {
    /**
     *     Gets schema json data for specified schema id.
     *         If data is present inside of cache, cached data is returned.
     *         Otherwise data is fetched from the ledger and stored inside of cache for future use.
     *
     *         EXPERIMENTAL
     * @param poolName Ledger.
     * @param submitter_did  DID of the submitter stored in secured Wallet.
     * @param id  identifier of schema.
     * @param options {
     *             noCache: (bool, optional, false by default) Skip usage of cache,
     *             noUpdate: (bool, optional, false by default) Use only cached data, do not try to update.
     *             noStore: (bool, optional, false by default) Skip storing fresh data if updated,
     *             minFresh: (int, optional, -1 by default) Return cached data if not older than this many seconds. -1 means do not check age.
     *         }
     * @return Schema json.
     *         {
     *             id: identifier of schema
     *             attrNames: array of attribute name strings
     *             name: Schema's name string
     *             version: Schema's version string
     *             ver: Version of the Schema json
     *         }
     */
    public abstract String getSchema(String poolName, String submitter_did, String id, CacheOptions options );

    /**
     *  Gets credential definition json data for specified credential definition id.
     *         If data is present inside of cache, cached data is returned.
     *         Otherwise data is fetched from the ledger and stored inside of cache for future use.
     *
     *         EXPERIMENTAL
     * @param poolName  Ledger.
     * @param submitter_did DID of the submitter stored in secured Wallet.
     * @param id identifier of credential definition.
     * @param options   {
     *             noCache: (bool, optional, false by default) Skip usage of cache,
     *             noUpdate: (bool, optional, false by default) Use only cached data, do not try to update.
     *             noStore: (bool, optional, false by default) Skip storing fresh data if updated,
     *             minFresh: (int, optional, -1 by default) Return cached data if not older than this many seconds. -1 means do not check age.
     *         }
     * @return Credential Definition json.
     *         {
     *             id: string - identifier of credential definition
     *             schemaId: string - identifier of stored in ledger schema
     *             type: string - type of the credential definition. CL is the only supported type now.
     *             tag: string - allows to distinct between credential definitions for the same issuer and schema
     *             value: Dictionary with Credential Definition's data: {
     *                 primary: primary credential public key,
     *                 Optional<revocation>: revocation credential public key
     *             },
     *             ver: Version of the Credential Definition json
     *         }
     */
    public abstract String getCredDef(String poolName, String submitter_did, String id, CacheOptions options);

    /**
     *  Purge schema cache.
     *
     *         EXPERIMENTAL
     * @param options   {
     *             maxAge: (int, optional, -1 by default) Purge cached data if older than this many seconds. -1 means purge all.
     *         }
     */
    public abstract void purgeSchemaCache(PurgeOptions options);

    /**
     *  Purge credential definition cache.
     *
     *         EXPERIMENTAL
     * @param options  {
     *             maxAge: (int, optional, -1 by default) Purge cached data if older than this many seconds. -1 means purge all.
     *         }
     */
    public abstract void purgeCredDefCache(PurgeOptions options);
}
