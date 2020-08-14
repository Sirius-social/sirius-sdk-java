package com.sirius.sdk.agent.wallet.abstract_wallet;

import com.goterl.lazycode.lazysodium.models.Pair;
import com.goterl.lazycode.lazysodium.models.Triple;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.NYMRole;

import java.util.List;

public abstract class AbstractLedger {
    /**
     *    Builds a GET_NYM request. Request to get information about a DID (NYM).
     * @param poolName Ledger pool.
     * @param submitterDid (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param targetDid Target DID as base58-encoded string for 16 or 32 bit DID value.
     * @return  result as json.
     */
    public abstract Pair<Boolean,String> readNym(String poolName, String submitterDid, String targetDid);

    /**
     * Builds a GET_ATTRIB request. Request to get information about an Attribute for the specified DID.
     * @param poolName  Ledger
     * @param submitterDid  (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param targetDid  Target DID as base58-encoded string for 16 or 32 bit DID value.
     * @param name attribute name.
     * @return Request result as json.
     */
    public abstract Pair<Boolean,String> readAttribute(String poolName,String submitterDid,String targetDid,String name);

    /**
     *  Builds a NYM request.
     * @param poolName  Ledger pool.
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param targetDid  Target DID as base58-encoded string for 16 or 32 bit DID value.
     * @param verKey Target identity verification key as base58-encoded string.
     * @param alias NYM's alias.
     * @param role Role of a user NYM record:
     *                      null (common USER)
     *                      TRUSTEE
     *                      STEWARD
     *                      TRUST_ANCHOR
     *                      ENDORSER - equal to TRUST_ANCHOR that will be removed soon
     *                      NETWORK_MONITOR
     *                      empty string to reset role
     * @return  success, result as json.
     */
    public abstract Pair<Boolean,String> writeNum(String poolName,String submitterDid, String targetDid,
                                                  String verKey,String alias, NYMRole role);

    /**
     *      Builds a SCHEMA request. Request to add Credential's schema.
     * @param poolName Ledger
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param data Schema data
     *             {
     *                 id: identifier of schema
     *                 attrNames: array of attribute name strings
     *                 name: schema's name string
     *                 version: schema's version string,
     *                 ver: version of the Schema json
     *             }
     * @return  success, Request result as json.
     */
    public abstract Pair<Boolean,String> registerSchema(String poolName,String submitterDid,String data);

    /**
     *      Builds an CRED_DEF request. Request to add a credential definition (in particular, public key),
     *         that Issuer creates for a particular Credential Schema.
     * @param poolName  Ledger
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param data credential definition json
     *                      {
     *                          id: string - identifier of credential definition
     *                          schemaId: string - identifier of stored in ledger schema
     *                          type: string - type of the credential definition. CL is the only supported type now.
     *                          tag: string - allows to distinct between credential definitions for the same issuer and schema
     *                          value: Dictionary with Credential Definition's data: {
     *                              primary: primary credential public key,
     *                              Optional<revocation>: revocation credential public key
     *                          },
     *                          ver: Version of the CredDef json
     *                      }
     * @return success, Request result as json.
     */
    public abstract Pair<Boolean,String> registerCredDef( String poolName,String submitterDid,String data);

    /**
     * Builds an ATTRIB request. Request to add attribute to a NYM record.
     * @param poolName  Ledger
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param targetDid Target DID as base58-encoded string for 16 or 32 bit DID value.
     * @param name attribute name
     * @param value  attribute value
     * @return Request result as json.
     */
    public abstract Pair<Boolean,String> writeAttribute(String poolName,String submitterDid,String targetDid,String name,Object value);

    /**
     *   Signs and submits request message to validator pool.
     *    Adds submitter information to passed request json, signs it with submitter
     *         sign key (see wallet_sign), and sends signed request message
     *         to validator pool (see write_request).
     * @param poolName Ledger pool.
     * @param submitterDid  Id of Identity stored in secured Wallet.
     * @param request  Request data json.
     * @return  Request result as json.
     */
    public abstract String signAndSubmit(String poolName,String submitterDid,String request);

    /**
     * Publishes request message to validator pool (no signing, unlike sign_and_submit_request).
     *         The request is sent to the validator pool as is. It's assumed that it's already prepared.
     * @param poolName  Ledger pool.
     * @param request Request data json.
     * @return Request result
     */
    public abstract String submitRequest(String  poolName,String request);

    /**
     *  Send action to particular nodes of validator pool.
     *   The list of requests can be send:
     *             POOL_RESTART
     *             GET_VALIDATOR_INFO
     *
     *         The request is sent to the nodes as is. It's assumed that it's already prepared.
     * @param poolName Ledger pool.
     * @param request  Request data json.
     * @param nodes : (Optional) List of node names to send the request.
     *                ["Node1", "Node2",...."NodeN"]
     * @param timeout (Optional) Time to wait respond from nodes (override the default timeout) (in sec).
     * @return Request result as json.
     */
    public abstract String submitAction(String poolName, String request, List<String> nodes, Integer timeout);

    /**
     *  Signs request message.
     *
     *         Adds submitter information to passed request json, signs it with submitter
     *         sign key (see wallet_sign).
     * @param submitterDid  Id of Identity stored in secured Wallet.
     * @param request  Request data json.
     * @return Signed request json.
     */
    public abstract String signRequest(String submitterDid,String request);

    /**
     *     Multi signs request message.
     *
     *         Adds submitter information to passed request json, signs it with submitter
     *         sign key (see wallet_sign).
     * @param submitterDid  Id of Identity stored in secured Wallet.
     * @param request Request data json.
     * @return Signed request json.
     */
    public abstract String multiSignRequest(String submitterDid,String request);

    /**
     * Builds a request to get a DDO.
     * @param submitterDid (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param targetDid target_did: Id of Identity stored in secured Wallet.
     * @return Request result as json.
     */
    public abstract String buildGetDddoRequest(String submitterDid,String targetDid);

    /**
     * Builds a NYM request.
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param targetDid  Target DID as base58-encoded string for 16 or 32 bit DID value.
     * @param verKey Target identity verification key as base58-encoded string.
     * @param alias  NYM's alias.
     * @param role  Role of a user NYM record:
     *                                  null (common USER)
     *                                  TRUSTEE
     *                                  STEWARD
     *                                  TRUST_ANCHOR
     *                                  ENDORSER - equal to TRUST_ANCHOR that will be removed soon
     *                                  NETWORK_MONITOR
     *                                  empty string to reset role
     * @return  Request result as json.
     */
    public abstract String buildNymRequest( String submitterDid,String targetDid,String verKey,
                                            String alias,NYMRole role);

    /**
     *      Builds an ATTRIB request. Request to add attribute to a NYM record.
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param targetDid Target DID as base58-encoded string for 16 or 32 bit DID value.
     * @param xhash (Optional) Hash of attribute data.
     * @param raw (Optional) Json, where key is attribute name and value is attribute value.
     * @param enc (Optional) Encrypted value attribute data.
     * @return Request result as json.
     */
    public abstract String buildAttribRequest(String submitterDid,String targetDid,String xhash,String raw,String enc);

    /**
     * Builds a GET_ATTRIB request. Request to get information about an Attribute for the specified DID.
     * @param submitterDid (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param targetDid Target DID as base58-encoded string for 16 or 32 bit DID value.
     * @param raw (Optional) Requested attribute name.
     * @param xhash (Optional) Requested attribute hash.
     * @param enc (Optional) Requested attribute encrypted value.
     * @return Request result as json.
     */
    public abstract String buildGetAttribRequest(String  submitterDid,String targetDid,String raw,String xhash,String enc);

    /**
     * Builds a GET_NYM request. Request to get information about a DID (NYM).
     * @param submitterDid (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param targetDid Target DID as base58-encoded string for 16 or 32 bit DID value.
     * @return Request result as json.
     */
    public abstract String buildGetNymRequest(String submitterDid,String targetDid);

    /**
     * Parse a GET_NYM response to get NYM data.
     * @param response response on GET_NYM request.
     * @return NYM data
     *         {
     *             did: DID as base58-encoded string for 16 or 32 bit DID value.
     *             verkey: verification key as base58-encoded string.
     *             role: Role associated number
     *                                     null (common USER)
     *                                     0 - TRUSTEE
     *                                     2 - STEWARD
     *                                     101 - TRUST_ANCHOR
     *                                     101 - ENDORSER - equal to TRUST_ANCHOR that will be removed soon
     *                                     201 - NETWORK_MONITOR
     *         }
     */
    public abstract String parseGetNymResponse(Object response);

    /**
     * Builds a SCHEMA request. Request to add Credential's schema.
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param data Credential schema.
     *                      {
     *                          id: identifier of schema
     *                          attrNames: array of attribute name strings (the number of attributes should be less or equal than 125)
     *                          name: Schema's name string
     *                          version: Schema's version string,
     *                          ver: Version of the Schema json
     *                      }
     * @return Request result as json.
     */
    public abstract String buildSchemaRequest(String submitterDid,String  data);

    /**
     * Builds a GET_SCHEMA request. Request to get Credential's Schema.
     * @param submitterDid (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param id  Schema Id in ledger
     * @return Request result as json.
     */
    public abstract String buildGetSchemaRequest(String  submitterDid,String id);

    /**
     *  Parse a GET_SCHEMA response to get Schema in the format compatible with Anoncreds API
     * @param getSchemaResponse  response of GET_SCHEMA request.
     * @return Schema Id and Schema json.
     *          {
     *              id: identifier of schema
     *              attrNames: array of attribute name strings
     *              name: Schema's name string
     *              version: Schema's version string
     *              ver: Version of the Schema json
     *          }
     */
    public abstract Pair<String,String> parseGetSchemaResponse(String getSchemaResponse);

    /**
     *  Builds an CRED_DEF request. Request to add a credential definition (in particular, public key),
     *         that Issuer creates for a particular Credential Schema.
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param data credential definition json
     *                      {
     *                          id: string - identifier of credential definition
     *                          schemaId: string - identifier of stored in ledger schema
     *                          type: string - type of the credential definition. CL is the only supported type now.
     *                          tag: string - allows to distinct between credential definitions for the same issuer and schema
     *                          value: Dictionary with Credential Definition's data: {
     *                              primary: primary credential public key,
     *                              Optional<revocation>: revocation credential public key
     *                          },
     *                          ver: Version of the CredDef json
     *                      }
     * @return  Request result as json.
     */
    public abstract String buildCredDef(String submitterDid,String  data);

    /**
     *  Builds a GET_CRED_DEF request. Request to get a credential definition (in particular, public key),
     *         that Issuer creates for a particular Credential Schema.
     * @param submitterDid (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param id  Credential Definition Id in ledger.
     * @return Request result as json.
     */
    public abstract String buildGetCredDefRequest(String submitterDid,String id);

    /**
     *  Parse a GET_CRED_DEF response to get Credential Definition in the format compatible with Anoncreds API.
     * @param getCredDefResponse  response of GET_CRED_DEF request.
     * @return Credential Definition Id and Credential Definition json.
     *           {
     *               id: string - identifier of credential definition
     *               schemaId: string - identifier of stored in ledger schema
     *               type: string - type of the credential definition. CL is the only supported type now.
     *               tag: string - allows to distinct between credential definitions for the same issuer and schema
     *               value: Dictionary with Credential Definition's data: {
     *                   primary: primary credential public key,
     *                   Optional<revocation>: revocation credential public key
     *               },
     *               ver: Version of the Credential Definition json
     *           }
     */
    public abstract Pair<String,String> parseGetCredDefResponse(String getCredDefResponse);

    /**
     *    Builds a NODE request. Request to add a new node to the pool, or updates existing in the pool.
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param targetDid  Target Node's DID.  It differs from submitter_did field.
     * @param data Data associated with the Node:
     *           {
     *               alias: string - Node's alias
     *               blskey: string - (Optional) BLS multi-signature key as base58-encoded string.
     *               blskey_pop: string - (Optional) BLS key proof of possession as base58-encoded string.
     *               client_ip: string - (Optional) Node's client listener IP address.
     *               client_port: string - (Optional) Node's client listener port.
     *               node_ip: string - (Optional) The IP address other Nodes use to communicate with this Node.
     *               node_port: string - (Optional) The port other Nodes use to communicate with this Node.
     *               services: array<string> - (Optional) The service of the Node. VALIDATOR is the only supported one now.
     *           }
     * @return Request result as json.
     */
    public abstract String buildNodeRequest(String submitterDid,String targetDid,String data);

    /**
     * Builds a GET_VALIDATOR_INFO request.
     * @param submitterDid Id of Identity stored in secured Wallet.
     * @return Request result as json.
     */
    public abstract String buildGetValidatorInfoRequest(String submitterDid);

    /**
     *  Builds a GET_TXN request. Request to get any transaction by its seq_no.
     * @param submitterDid (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param ledgerType (Optional) type of the ledger the requested transaction belongs to:
     *                    DOMAIN - used default,
     *             POOL,
     *             CONFIG
     *             any number
     * @param seq_no requested transaction sequence number as it's stored on Ledger.
     * @return Request result as json.
     */
    public abstract String buildGetTxnRequest(String submitterDid,String ledgerType,int seq_no);

    /**
     *  Builds a POOL_CONFIG request. Request to change Pool's configuration.
     * @param submitterDid  Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param writes Whether any write requests can be processed by the pool
     *                        (if false, then pool goes to read-only state). True by default.
     * @param force  Whether we should apply transaction (for example, move pool to read-only state)
     *                       without waiting for consensus of this transaction
     * @return Request result as json.
     */
    public abstract String buildPoolConfigRequest(String submitterDid ,boolean writes,boolean force);

    /**
     *   Builds a POOL_RESTART request
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param action  Action that pool has to do after received transaction.
     *                               Can be "start" or "cancel"
     * @param datetime Time when pool must be restarted.
     * @return
     */
    public abstract String buildPoolRestart(String submitterDid,String action,String datetime);

    /**
     * Builds a POOL_UPGRADE request. Request to upgrade the Pool (sent by Trustee).
     *         It upgrades the specified Nodes (either all nodes in the Pool, or some specific ones).
     * @param submitter_did Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param name  Human-readable name for the upgrade.
     * @param version The version of indy-node package we perform upgrade to.
     *                         Must be greater than existing one (or equal if reinstall flag is True).
     * @param action Either start or cancel.
     * @param sha256  sha256 hash of the package.
     * @param timeout  (Optional) Limits upgrade time on each Node.
     * @param schedule  (Optional) Schedule of when to perform upgrade on each node. Map Node DIDs to upgrade time.
     * @param justification  (Optional) justification string for this particular Upgrade.
     * @param reinstall Whether it's allowed to re-install the same version. False by default.
     * @param force Whether we should apply transaction (schedule Upgrade) without waiting
     *                       for consensus of this transaction.
     * @param packageString  (Optional) Package to be upgraded.
     * @return Request result as json.
     */
    public abstract String buildPoolUpgradeRequest( String submitter_did,String name,String version,String action,String sha256,Integer timeout,
                                                    String schedule,String justification,boolean reinstall,boolean force,String packageString);

    /**
     * Builds a REVOC_REG_DEF request. Request to add the definition of revocation registry
     *         to an exists credential definition.
     * @param submitter_did Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param data  Revocation Registry data:
     *           {
     *               "id": string - ID of the Revocation Registry,
     *               "revocDefType": string - Revocation Registry type (only CL_ACCUM is supported for now),
     *               "tag": string - Unique descriptive ID of the Registry,
     *               "credDefId": string - ID of the corresponding CredentialDefinition,
     *               "value": Registry-specific data {
     *                   "issuanceType": string - Type of Issuance(ISSUANCE_BY_DEFAULT or ISSUANCE_ON_DEMAND),
     *                   "maxCredNum": number - Maximum number of credentials the Registry can serve.
     *                   "tailsHash": string - Hash of tails.
     *                   "tailsLocation": string - Location of tails file.
     *                   "publicKeys": <public_keys> - Registry's public key.
     *               },
     *               "ver": string - version of revocation registry definition json.
     *           }
     * @return Request result as json.
     */
    public abstract String buildRevocRegDefRequest(String submitter_did,String data);

    /**
     *         Builds a GET_REVOC_REG_DEF request. Request to get a revocation registry definition,
     *         that Issuer creates for a particular Credential Definition.
     * @param submitter_did (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param revRegDefId  ID of Revocation Registry Definition in ledger.
     * @return Request result as json.
     */
    public abstract String buildGetRevocRegDefRequest( String submitter_did,String revRegDefId);

    /**
     *    Parse a GET_REVOC_REG_DEF response to get Revocation Registry Definition in the format compatible with Anoncreds API.
     * @param getRevocRefDefResponse  response of GET_REVOC_REG_DEF request.
     * @return Revocation Registry Definition Id and Revocation Registry Definition json.
     *           {
     *               "id": string - ID of the Revocation Registry,
     *               "revocDefType": string - Revocation Registry type (only CL_ACCUM is supported for now),
     *               "tag": string - Unique descriptive ID of the Registry,
     *               "credDefId": string - ID of the corresponding CredentialDefinition,
     *               "value": Registry-specific data {
     *                   "issuanceType": string - Type of Issuance(ISSUANCE_BY_DEFAULT or ISSUANCE_ON_DEMAND),
     *                   "maxCredNum": number - Maximum number of credentials the Registry can serve.
     *                   "tailsHash": string - Hash of tails.
     *                   "tailsLocation": string - Location of tails file.
     *                   "publicKeys": <public_keys> - Registry's public key.
     *               },
     *               "ver": string - version of revocation registry definition json.
     *           }
     */
    public abstract Pair<String,String> parseGetRevocRegDefResponse(String getRevocRefDefResponse);

    /**
     * Builds a REVOC_REG_ENTRY request.  Request to add the RevocReg entry containing
     *         the new accumulator value and issued/revoked indices.
     *         This is just a delta of indices, not the whole list. So, it can be sent each time a new credential is issued/revoked.
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param revocRegDefId  ID of the corresponding RevocRegDef.
     * @param revDefType Revocation Registry type (only CL_ACCUM is supported for now).
     * @param value  Registry-specific data:
     *            {
     *                value: {
     *                    prevAccum: string - previous accumulator value.
     *                    accum: string - current accumulator value.
     *                    issued: array<number> - an array of issued indices.
     *                    revoked: array<number> an array of revoked indices.
     *                },
     *                ver: string - version revocation registry entry json
     *
     *            }
     * @return Request result as json.
     */
    public abstract String buildRevocRegEntryRequest(String  submitterDid, String revocRegDefId,String revDefType,String value);

    /**
     * Builds a GET_REVOC_REG request. Request to get the accumulated state of the Revocation Registry
     *         by ID. The state is defined by the given timestamp.
     * @param submitterDid  (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param revocRegDefId ID of the corresponding Revocation Registry Definition in ledger.
     * @param timestamp Requested time represented as a total number of seconds from Unix Epoch
     * @return  Request result as json.
     */
    public abstract String buildGetREvocRegRequest(String submitterDid,String revocRegDefId,int timestamp);

    /**
     *  Parse a GET_REVOC_REG response to get Revocation Registry in the format compatible with Anoncreds API.
     * @param getRevocRegResponse response of GET_REVOC_REG request.
     * @return Revocation Registry Definition Id, Revocation Registry json and Timestamp.
     *           {
     *               "value": Registry-specific data {
     *                   "accum": string - current accumulator value.
     *               },
     *               "ver": string - version revocation registry json
     *           }
     */
    public abstract Triple<String, String,Integer> parseGetRevocRegResponse(String getRevocRegResponse);

    /**
     * Builds a GET_REVOC_REG_DELTA request. Request to get the delta of the accumulated state of the Revocation Registry.
     *         The Delta is defined by from and to timestamp fields.
     *         If from is not specified, then the whole state till to will be returned.
     * @param submitterDid (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param revocRegDefId  ID of the corresponding Revocation Registry Definition in ledger.
     * @param from Requested time represented as a total number of seconds from Unix Epoch
     * @param to Requested time represented as a total number of seconds from Unix Epoch
     * @return Request result as json.
     */
    public abstract String buildGetRevocRegDeltaRequest(String submitterDid,String revocRegDefId,Integer from,int to);

    /**
     *       Parse a GET_REVOC_REG_DELTA response to get Revocation Registry Delta in the format compatible with Anoncreds API.
     * @param getRevocRegDeltaResponse response of GET_REVOC_REG_DELTA request.
     * @return Revocation Registry Definition Id, Revocation Registry Delta json and Timestamp.
     *           {
     *               "value": Registry-specific data {
     *                   prevAccum: string - previous accumulator value.
     *                   accum: string - current accumulator value.
     *                   issued: array<number> - an array of issued indices.
     *                   revoked: array<number> an array of revoked indices.
     *               },
     *               "ver": string
     *           }
     */
    public abstract Triple<String,String,Integer> parseGetRevocRegDeltaResponse(String getRevocRegDeltaResponse);

    /**
     *  Parse transaction response to fetch metadata.
     *          The important use case for this method is validation of Node's response freshens.
     *
     *          Distributed Ledgers can reply with outdated information for consequence read request after write.
     *          To reduce pool load libindy sends read requests to one random node in the pool.
     *          Consensus validation is performed based on validation of nodes multi signature for current ledger Merkle Trie root.
     *          This multi signature contains information about the latest ldeger's transaction ordering time and sequence number that this method returns.
     *
     *          If node that returned response for some reason is out of consensus and has outdated ledger
     *          it can be caught by analysis of the returned latest ledger's transaction ordering time and sequence number.
     *
     *          There are two ways to filter outdated responses:
     *              1) based on "seqNo" - sender knows the sequence number of transaction that he consider as a fresh enough.
     *              2) based on "txnTime" - sender knows the timestamp that he consider as a fresh enough.
     *
     *          Note: response of GET_VALIDATOR_INFO request isn't supported
     * @param response response of write or get request.
     * @return Response Metadata.
     *         {
     *             "seqNo": Option<u64> - transaction sequence number,
     *             "txnTime": Option<u64> - transaction ordering time,
     *             "lastSeqNo": Option<u64> - the latest transaction seqNo for particular Node,
     *             "lastTxnTime": Option<u64> - the latest transaction ordering time for particular Node
     *         }
     */
    public abstract String responseMetadata(String response);

    /**
     *  Builds a AUTH_RULE request. Request to change authentication rules for a ledger transaction.
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param txnType ledger transaction alias or associated value.
     * @param action  type of an action.
     *                Can be either "ADD" (to add a new rule) or "EDIT" (to edit an existing one).
     * @param field transaction field.
     * @param old_value   (Optional) old value of a field, which can be changed to a new_value (mandatory for EDIT action).
     * @param new_value (Optional) new value that can be used to fill the field.
     * @param constraint  set of constraints required for execution of an action in the following format:
     *             {
     *                 constraint_id - <string> type of a constraint.
     *                     Can be either "ROLE" to specify final constraint or  "AND"/"OR" to combine constraints.
     *                 role - <string> (optional) role of a user which satisfy to constrain.
     *                 sig_count - <u32> the number of signatures required to execution action.
     *                 need_to_be_owner - <bool> (optional) if user must be an owner of transaction (false by default).
     *                 off_ledger_signature - <bool> (optional) allow signature of unknow for ledger did (false by default).
     *                 metadata - <object> (optional) additional parameters of the constraint.
     *             }
     *           can be combined by
     *             {
     *                 'constraint_id': <"AND" or "OR">
     *                 'auth_constraints': [<constraint_1>, <constraint_2>]
     *             }
     *
     *         Default ledger auth rules: https://github.com/hyperledger/indy-node/blob/master/docs/source/auth_rules.md
     *
     *         More about AUTH_RULE request: https://github.com/hyperledger/indy-node/blob/master/docs/source/requests.md#auth_rule
     * @return Request result as json.
     */
    public abstract String buildAuthRuleRequest( String  submitterDid,String txnType,String action,String field,String old_value,
                                                   String new_value,String constraint);

    /**
     *  Builds a AUTH_RULES request. Request to change multiple authentication rules for a ledger transaction.
     * @param submitterDid dentifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param data  a list of auth rules: [
     *             {
     *                 "auth_type": ledger transaction alias or associated value,
     *                 "auth_action": type of an action,
     *                 "field": transaction field,
     *                 "old_value": (Optional) old value of a field, which can be changed to a new_value (mandatory for EDIT action),
     *                 "new_value": (Optional) new value that can be used to fill the field,
     *                 "constraint": set of constraints required for execution of an action in the format described above for `build_auth_rule_request` function.
     *             }
     *         ]
     *
     *         Default ledger auth rules: https://github.com/hyperledger/indy-node/blob/master/docs/source/auth_rules.md
     *
     *         More about AUTH_RULE request: https://github.com/hyperledger/indy-node/blob/master/docs/source/requests.md#auth_rules
     * @return Request result as json.
     */
    public abstract String buildAuthRulesRequest( String submitterDid,String data);

    /**
     *    Builds a GET_AUTH_RULE request. Request to get authentication rules for a ledger transaction.
     *            NOTE: Either none or all transaction related parameters must be specified (`old_value` can be skipped for `ADD` action).
     *             * none - to get all authentication rules for all ledger transactions
     *             * all - to get authentication rules for specific action (`old_value` can be skipped for `ADD` action)
     * @param submitterDid  (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param txnType target ledger transaction alias or associated value.
     * @param action target action type. Can be either "ADD" or "EDIT".
     * @param field  target transaction field.
     * @param old_value (Optional) old value of field, which can be changed to a new_value (must be specified for EDIT action).
     * @param new_value  (Optional) new value that can be used to fill the field
     * @return Request result as json.
     */
    public abstract String buildGetAuthRuleRequest(String submitterDid,String  txnType,String action,String field,
                                                   String old_value,String new_value);

    /**
     *   Builds a TXN_AUTHR_AGRMT request. Request to add a new version of Transaction Author Agreement to the ledger.
     *
     *         EXPERIMENTAL
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param text (Optional) a content of the TTA.
     *                               Mandatory in case of adding a new TAA. An existing TAA text can not be changed.
     *                               for Indy Node version <= 1.12.0:
     *                                   Use empty string to reset TAA on the ledger
     *                               for Indy Node version > 1.12.0
     *                                   Should be omitted in case of updating an existing TAA (setting `retirement_ts`)
     * @param version  a version of the TTA (unique UTF-8 string).
     * @param ratification_ts  Optional) the date (timestamp) of TAA ratification by network government.
     *                               for Indy Node version <= 1.12.0:
     *                                  Must be omitted
     *                               for Indy Node version > 1.12.0:
     *                                  Must be specified in case of adding a new TAA
     *                                  Can be omitted in case of updating an existing TAA
     * @param retirement_ts (Optional) the date (timestamp) of TAA retirement.
     *                               for Indy Node version <= 1.12.0:
     *                                   Must be omitted
     *                               for Indy Node version > 1.12.0:
     *                                   Must be omitted in case of adding a new (latest) TAA.
     *                                   Should be used for updating (deactivating) non-latest TAA on the ledger.
     *                        Note: Use `build_disable_all_txn_author_agreements_request` to disable all TAA's on the ledger.
     * @return Request result as json.
     */
    public abstract String buildTxnAuthorAgreementRequest( String submitterDid,String text,String version,
                                                           Integer ratification_ts,Integer retirement_ts);

    /**
     *     Builds a DISABLE_ALL_TXN_AUTHR_AGRMTS request. Request to disable all Transaction Author Agreement on the ledger.
     *
     *         EXPERIMENTAL
     * @param submitter Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @return Request result as json.
     */
    public abstract String buildDisableAllTxnAuthorAgreementsRequest(String submitter);

    /**
     *   Builds a GET_TXN_AUTHR_AGRMT request. Request to get a specific Transaction Author Agreement from the ledger.
     *
     *         EXPERIMENTAL
     * @param submitterDid (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param data (Optional) specifies a condition for getting specific TAA.
     *              Contains 3 mutually exclusive optional fields:
     *          {
     *              hash: Optional<str> - hash of requested TAA,
     *              version: Optional<str> - version of requested TAA.
     *              timestamp: Optional<i64> - ledger will return TAA valid at requested timestamp.
     *          }
     *          Null data or empty JSON are acceptable here. In this case, ledger will return the latest version of TAA.
     * @return  Request result as json.
     */
    public abstract String buildGetTxnAuthorAgreementRequest(String submitterDid,String data);

    /**
     *  Builds a SET_TXN_AUTHR_AGRMT_AML request. Request to add a new list of acceptance mechanisms for transaction author agreement.
     *         Acceptance Mechanism is a description of the ways how the user may accept a transaction author agreement.
     *
     *         EXPERIMENTAL
     * @param submitterDid Identifier (DID) of the transaction author as base58-encoded string.
     *                               Actual request sender may differ if Endorser is used (look at `append_request_endorser`)
     * @param aml a set of new acceptance mechanisms:
     *         {
     *             “<acceptance mechanism label 1>”: { acceptance mechanism description 1},
     *             “<acceptance mechanism label 2>”: { acceptance mechanism description 2},
     *             ...
     *         }
     * @param version  a version of new acceptance mechanisms. (Note: unique on the Ledger)
     * @param amlContext (Optional) common context information about acceptance mechanisms (may be a URL to external resource).
     * @return  Request result as json.
     */
    public abstract String buildAcceptanceMechanismsRequest(String submitterDid,String aml,String version,String amlContext);

    /**
     *    Builds a GET_TXN_AUTHR_AGRMT_AML request. Request to get a list of  acceptance mechanisms from the ledger
     *         valid for specified time or the latest one.
     *
     *         EXPERIMENTAL
     * @param submitterDid (Optional) DID of the read request sender (if not provided then default Libindy DID will be used).
     * @param timestamp (Optional) time to get an active acceptance mechanisms. The latest one will be returned for the empty timestamp.
     * @param version (Optional) version of acceptance mechanisms.
     *                NOTE: timestamp and version cannot be specified together.
     * @return  Request result as json.
     */
    public abstract String buildGetAcceptanceMechanismsRequest(String  submitterDid, Integer timestamp,String version);

    /**
     * Append transaction author agreement acceptance data to a request.
     *         This function should be called before signing and sending a request
     *         if there is any transaction author agreement set on the Ledger.
     *
     *         EXPERIMENTAL
     *
     *         This function may calculate hash by itself or consume it as a parameter.
     *         If all text, version and taa_digest parameters are specified, a check integrity of them will be done.
     * @param request original request data json.
     * @param text  (Optional) raw data about TAA from ledger.
     *                    These parameters should be passed together.
     *
     * @param version  These parameters are required if taa_digest parameter is omitted.
     * @param taa_digest(Optional) digest on text and version.
     *                Digest is sha256 hash calculated on concatenated strings: version || text.
     *                  This parameter is required if text and version parameters are omitted.
     * @param mechanism   mechanism how user has accepted the TAA
     * @param time  UTC timestamp when user has accepted the TAA. Note that the time portion will be discarded to avoid a privacy risk.
     * @return  Updated request result as json.
     */
    public abstract String appendTxnAuthorAgreementAcceptanceToRequest(String request,String text, String version,
                                                                       String taa_digest,String mechanism,int time);

    /**
     *      Append Endorser to an existing request.
     *
     *         An author of request still is a `DID` used as a `submitter_did` parameter for the building of the request.
     *         But it is expecting that the transaction will be sent by the specified Endorser.
     *
     *         Note: Both Transaction Author and Endorser must sign output request after that.
     *
     *         More about Transaction Endorser: https://github.com/hyperledger/indy-node/blob/master/design/transaction_endorser.md
     *                                          https://github.com/hyperledger/indy-sdk/blob/master/docs/configuration.md
     * @param request original request data json.
     * @param endorserDid DID of the Endorser that will submit the transaction.
     *                              The Endorser's DID must be present on the ledger.
     * @return Updated request result as json.
     */
    public abstract String appendRequestEndorser(String request,String endorserDid);
}
