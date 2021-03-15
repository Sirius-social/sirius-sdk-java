package com.sirius.sdk.agent.wallet.abstract_wallet;

import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.errors.indy_exceptions.DuplicateMasterSecretNameException;
import com.sirius.sdk.errors.indy_exceptions.WalletItemNotFoundException;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractAnonCreds {
    /**
     * Create credential schema entity that describes credential attributes list and allows credentials
     * interoperability.
     * <p>
     * Schema is public and intended to be shared with all anoncreds workflow actors usually by publishing SCHEMA transaction
     * to Indy distributed ledger.
     * <p>
     * It is IMPORTANT for current version POST Schema in Ledger and after that GET it from Ledger
     * with correct seq_no to save compatibility with Ledger.
     * After that can call indy_issuer_create_and_store_credential_def to build corresponding Credential Definition.
     *
     * @param issuerDid DID of schema issuer
     * @param name      a name the schema
     * @param version   a version of the schema
     * @param attrs     a list of schema attributes descriptions (the number of attributes should be less or equal than 125)
     *                  `["attr1", "attr2"]`
     * @return schema_id: identifier of created schema
     * schema_json: schema as json
     * {
     * id: identifier of schema
     * attrNames: array of attribute name strings
     * name: schema's name string
     * version: schema's version string,
     * ver: version of the Schema json
     * }
     */
    public abstract Pair<String, AnonCredSchema> issuerCreateSchema(String issuerDid, String name, String version, List<String> attrs);

    public Pair<String, AnonCredSchema> issuerCreateSchema(String issuerDid, String name, String version, String... attrs) {
        return issuerCreateSchema(issuerDid, name, version, Arrays.asList(attrs));
    }


    /**
     * Create credential definition entity that encapsulates credentials issuer DID, credential schema, secrets used for
     * signing credentials and secrets used for credentials revocation.
     * <p>
     * Credential definition entity contains private and public parts. Private part will be stored in the wallet.
     * Public part will be returned as json intended to be shared with all anoncreds workflow actors usually by
     * publishing CRED_DEF transaction to Indy distributed ledger.
     * <p>
     * It is IMPORTANT for current version GET Schema from Ledger with correct seq_no to save compatibility with Ledger.
     * <p>
     * Note: Use combination of `issuer_rotate_credential_def_start` and `issuer_rotate_credential_def_apply` functions
     * to generate new keys for an existing credential definition.
     *
     * @param issuerDid     a DID of the issuer signing cred_def transaction to the Ledger
     * @param schema        credential schema as a json
     *                      {
     *                      id: identifier of schema
     *                      attrNames: array of attribute name strings
     *                      name: schema's name string
     *                      version: schema's version string,
     *                      seqNo: (Optional) schema's sequence number on the ledger,
     *                      ver: version of the Schema json
     *                      }
     * @param tag           allows to distinct between credential definitions for the same issuer and schema
     * @param signatureType credential definition type (optional, 'CL' by default) that defines credentials signature and revocation math.
     *                      Supported types are:
     *                      - 'CL': Camenisch-Lysyanskaya credential signature type that is implemented according to the algorithm in this paper:
     *                      https://github.com/hyperledger/ursa/blob/master/libursa/docs/AnonCred.pdf
     *                      And is documented in this HIPE:
     *                      https://github.com/hyperledger/indy-hipe/blob/c761c583b1e01c1e9d3ceda2b03b35336fdc8cc1/text/anoncreds-protocol/README.md
     * @param config        (optional) type-specific configuration of credential definition as json:
     *                      *             - 'CL':
     *                      *                 {
     *                      *                     "support_revocation" - bool (optional, default false) whether to request non-revocation credential
     *                      *                 }
     * @return Pair first - cred_def_id: identifier of created credential definition
     * Pair second -  cred_def_json: public part of created credential definition
     * {
     * id: string - identifier of credential definition
     * schemaId: string - identifier of stored in ledger schema
     * type: string - type of the credential definition. CL is the only supported type now.
     * tag: string - allows to distinct between credential definitions for the same issuer and schema
     * value: Dictionary with Credential Definition's data is depended on the signature type: {
     * primary: primary credential public key,
     * Optional<revocation>: revocation credential public key
     * },
     * ver: Version of the CredDef json
     * }
     */
    public abstract Pair<String, String> issuerCreateAndStoreCredentialDef(String issuerDid, Object schema, String tag, String signatureType, Object config);

    /**
     * Overload method {@link #issuerCreateAndStoreCredentialDef(String issuerDid, Object schema, String tag, String signatureType, Object config)}
     */

    public Pair<String, String> issuerCreateAndStoreCredentialDef(String issuerDid, Object schema, String tag) {
        return issuerCreateAndStoreCredentialDef(issuerDid, schema, tag, null, null);
    }

    /**
     * Generate temporary credential definitional keys for an existing one (owned by the caller of the library).
     * <p>
     * Use `issuer_rotate_credential_def_apply` function to set generated temporary keys as the main.
     * <p>
     * WARNING: Rotating the credential definitional keys will result in making all credentials issued under the previous keys unverifiable.
     *
     * @param credDefId an identifier of created credential definition stored in the wallet
     * @param config    (optional) type-specific configuration of credential definition as json:
     *                  - 'CL':
     *                  {
     *                  "support_revocation" - bool (optional, default false) whether to request non-revocation credential
     *                  }
     * @return cred_def_json: public part of temporary created credential definition
     */
    public abstract String issuerRotateCredentialDefStart(String credDefId, String config);

    /**
     * Overload method {@link #issuerRotateCredentialDefStart(String credDefId, String  config)}
     */
    public String issuerRotateCredentialDefStart(String credDefId) {
        return issuerRotateCredentialDefStart(credDefId, null);
    }

    /**
     * Apply temporary keys as main for an existing Credential Definition (owned by the caller of the library).
     * <p>
     * WARNING: Rotating the credential definitional keys will result in making all credentials issued under the previous keys unverifiable.
     *
     * @param credDefId an identifier of created credential definition stored in the wallet
     */
    public abstract void issuerRotateCredentialDefApply(String credDefId);

    /**
     * Create a new revocation registry for the given credential definition as tuple of entities:
     * - Revocation registry definition that encapsulates credentials definition reference, revocation type specific configuration and
     * secrets used for credentials revocation
     * - Revocation registry state that stores the information about revoked entities in a non-disclosing way. The state can be
     * represented as ordered list of revocation registry entries were each entry represents the list of revocation or issuance operations.
     * <p>
     * Revocation registry definition entity contains private and public parts. Private part will be stored in the wallet. Public part
     * will be returned as json intended to be shared with all anoncreds workflow actors usually by publishing REVOC_REG_DEF transaction
     * to Indy distributed ledger.
     * <p>
     * Revocation registry state is stored on the wallet and also intended to be shared as the ordered list of REVOC_REG_ENTRY transactions.
     * This call initializes the state in the wallet and returns the initial entry.
     * <p>
     * Some revocation registry types (for example, 'CL_ACCUM') can require generation of binary blob called tails used to hide information about revoked credentials in public
     * revocation registry and intended to be distributed out of leger (REVOC_REG_DEF transaction will still contain uri and hash of tails).
     * This call requires access to pre-configured blob storage writer instance handle that will allow to write generated tails.
     *
     * @param issuerDid         a DID of the issuer signing transaction to the Ledger
     * @param revocDefType      revocation registry type (optional, default value depends on credential definition type). Supported types are:
     *                          *                     - 'CL_ACCUM': Type-3 pairing based accumulator implemented according to the algorithm in this paper:
     *                          *                                       https://github.com/hyperledger/ursa/blob/master/libursa/docs/AnonCred.pdf
     *                          *                                   This type is default for 'CL' credential definition type.
     * @param tag               allows to distinct between revocation registries for the same issuer and credential definition
     * @param credDefId         id of stored in ledger credential definition
     * @param config            type-specific configuration of revocation registry as json:
     *                          - 'CL_ACCUM':
     *                          "issuance_type": (optional) type of issuance. Currently supported:
     *                          1) ISSUANCE_BY_DEFAULT: all indices are assumed to be issued and initial accumulator is calculated over all indices;
     *                          Revocation Registry is updated only during revocation.
     *                          2) ISSUANCE_ON_DEMAND: nothing is issued initially accumulator is 1 (used by default);
     *                          "max_cred_num": maximum number of credentials the new registry can process (optional, default 100000)
     *                          }
     * @param tailsWriterHandle handle of blob storage to store tails
     *                          NOTE:
     *                          Recursive creation of folder for Default Tails Writer (correspondent to `tails_writer_handle`)
     *                          in the system-wide temporary directory may fail in some setup due to permissions: `IO error: Permission denied`.
     *                          In this case use `TMPDIR` environment variable to define temporary directory specific for an application.
     * @return revoc_reg_id: identifier of created revocation registry definition
     * revoc_reg_def_json: public part of revocation registry definition
     * {
     * "id": string - ID of the Revocation Registry,
     * "revocDefType": string - Revocation Registry type (only CL_ACCUM is supported for now),
     * "tag": string - Unique descriptive ID of the Registry,
     * "credDefId": string - ID of the corresponding CredentialDefinition,
     * "value": Registry-specific data {
     * "issuanceType": string - Type of Issuance(ISSUANCE_BY_DEFAULT or ISSUANCE_ON_DEMAND),
     * "maxCredNum": number - Maximum number of credentials the Registry can serve.
     * "tailsHash": string - Hash of tails.
     * "tailsLocation": string - Location of tails file.
     * "publicKeys": <public_keys> - Registry's public key (opaque type that contains data structures internal to Ursa.
     * It should not be parsed and are likely to change in future versions).
     * },
     * "ver": string - version of revocation registry definition json.
     * }
     * revoc_reg_entry_json: revocation registry entry that defines initial state of revocation registry
     * {
     * value: {
     * prevAccum: string - previous accumulator value.
     * accum: string - current accumulator value.
     * issued: array<number> - an array of issued indices.
     * revoked: array<number> an array of revoked indices.
     * },
     * ver: string - version revocation registry entry json
     * }
     */
    public abstract Triple<String, String, String> issuerCreateAndStoreRevocReg(String issuerDid, String revocDefType, String tag, String credDefId, String config, int tailsWriterHandle);

    /**
     * Create credential offer that will be used by Prover for
     * credential request creation. Offer includes nonce and key correctness proof
     * for authentication between protocol steps and integrity checking.
     *
     * @param credDefId id of credential definition stored in the wallet
     * @return credential offer json:
     * {
     * "schema_id": string, - identifier of schema
     * "cred_def_id": string, - identifier of credential definition
     * // Fields below can depend on Cred Def type
     * "nonce": string,
     * "key_correctness_proof" : key correctness proof for credential definition correspondent to cred_def_id
     * (opaque type that contains data structures internal to Ursa.
     * It should not be parsed and are likely to change in future versions).
     * }
     */
    public abstract JSONObject issuerCreateCredentialOffer(String credDefId);

    /**
     * Check Cred Request for the given Cred Offer and issue Credential for the given Cred Request.
     * <p>
     * Cred Request must match Cred Offer. The credential definition and revocation registry definition
     * referenced in Cred Offer and Cred Request must be already created and stored into the wallet.
     * <p>
     * Information for this credential revocation will be store in the wallet as part of revocation registry under
     * generated cred_revoc_id local for this wallet.
     * <p>
     * This call returns revoc registry delta as json file intended to be shared as REVOC_REG_ENTRY transaction.
     * Note that it is possible to accumulate deltas to reduce ledger load.
     *
     * @param credOffer               a cred offer created by issuer_create_credential_offer
     * @param credReq                 a credential request created by prover_create_credential_req
     * @param credValues              a credential containing attribute values for each of requested attribute names.
     *                                Example:
     *                                {
     *                                "attr1" : {"raw": "value1", "encoded": "value1_as_int" },
     *                                "attr2" : {"raw": "value1", "encoded": "value1_as_int" }
     *                                }
     *                                If you want to use empty value for some credential field, you should set "raw" to "" and "encoded" should not be empty
     * @param revRegId                (Optional) id of revocation registry definition stored in the wallet
     * @param blobStorageReaderHandle pre-configured blob storage reader instance handle that
     *                                *         will allow to read revocation tails
     * @return cred_json: Credential json containing signed credential values
     * {
     * "schema_id": string,
     * "cred_def_id": string,
     * "rev_reg_def_id", Optional<string>,
     * "values": <see cred_values_json above>,
     * // Fields below can depend on Cred Def type
     * "signature": <credential signature>,
     * (opaque type that contains data structures internal to Ursa.
     * It should not be parsed and are likely to change in future versions).
     * "signature_correctness_proof": credential signature correctness proof
     * (opaque type that contains data structures internal to Ursa.
     * It should not be parsed and are likely to change in future versions).
     * "rev_reg" - (Optional) revocation registry accumulator value on the issuing moment.
     * (opaque type that contains data structures internal to Ursa.
     * It should not be parsed and are likely to change in future versions).
     * "witness" - (Optional) revocation related data
     * (opaque type that contains data structures internal to Ursa.
     * It should not be parsed and are likely to change in future versions).
     * }
     * cred_revoc_id: local id for revocation info (Can be used for revocation of this cred)
     * revoc_reg_delta_json: Revocation registry delta json with a newly issued credential
     */
    public abstract Triple<JSONObject, String, JSONObject> issuerCreateCredential(JSONObject credOffer, JSONObject credReq, JSONObject credValues, String revRegId,
                                                                                  Integer blobStorageReaderHandle);

    /**
     * Overload method {@link #issuerCreateCredential(JSONObject credOffer, JSONObject credReq, JSONObject credValues, String revRegId,
     * Integer blobStorageReaderHandle)}
     */
    public Triple<JSONObject, String, JSONObject> issuerCreateCredential(JSONObject credOffer, JSONObject credReq, JSONObject credValues) {
        return issuerCreateCredential(credOffer, credReq, credValues, null, null);
    }

    /**
     * Overload method {@link #issuerCreateCredential(JSONObject credOffer, JSONObject credReq, JSONObject credValues, String revRegId,
     * Integer blobStorageReaderHandle)}
     */
    public Triple<JSONObject, String, JSONObject> issuerCreateCredential(JSONObject credOffer, JSONObject credReq, JSONObject credValues, String revRegId) {
        return issuerCreateCredential(credOffer, credReq, credValues, revRegId, null);
    }

    /**
     * Revoke a credential identified by a cred_revoc_id (returned by issuer_create_credential).
     * <p>
     * The corresponding credential definition and revocation registry must be already
     * created an stored into the wallet.
     * <p>
     * This call returns revoc registry delta as json file intended to be shared as REVOC_REG_ENTRY transaction.
     * Note that it is possible to accumulate deltas to reduce ledger load.
     *
     * @param blobStorageReaderHandle pre-configured blob storage reader instance handle that will allow
     *                                to read revocation tails
     * @param revRegId                id of revocation registry stored in wallet
     * @param credRevocId             local id for revocation info
     * @return Revocation registry delta json with a revoked credential.
     */
    public abstract String issuerRevokeCredential(Integer blobStorageReaderHandle, String revRegId, String credRevocId);

    /**
     * Merge two revocation registry deltas (returned by issuer_create_credential or issuer_revoke_credential) to accumulate common delta.
     * Send common delta to ledger to reduce the load.
     *
     * @param revRegDelta      revocation registry delta json
     * @param otherRevRegDelta : revocation registry delta for which PrevAccum value  is equal to current accum value of rev_reg_delta_json.
     * @return Merged revocation registry delta
     */
    public abstract String issuerMergeRevocationRegistryDeltas(String revRegDelta, String otherRevRegDelta);

    /**
     * Creates a master secret with a given name and stores it in the wallet.
     * The name must be unique.
     *
     * @param masterSecretName (optional, if not present random one will be generated) new master id
     * @return id of generated master secret.
     */
    public abstract String proverCreateMasterSecret(String masterSecretName) throws DuplicateMasterSecretNameException;

    /**
     * Overload method {@link #proverCreateMasterSecret(String masterSecretName)}
     */

    public String proverCreateMasterSecret() throws DuplicateMasterSecretNameException {
        return proverCreateMasterSecret(null);
    }

    /**
     * Creates a credential request for the given credential offer.
     * The method creates a blinded master secret for a master secret identified by a provided name.
     * The master secret identified by the name must be already stored in the secure wallet (see prover_create_master_secret)
     * The blinded master secret is a part of the credential request.
     *
     * @param proverDid      a DID of the prover
     * @param credOffer      credential offer as a json containing information about the issuer and a credential
     *                       {
     *                       "schema_id": string, - identifier of schema
     *                       "cred_def_id": string, - identifier of credential definition
     *                       ...
     *                       Other fields that contains data structures internal to Ursa.
     *                       These fields should not be parsed and are likely to change in future versions.
     *                       }
     * @param credDef        credential definition json related to <cred_def_id> in <cred_offer_json>
     * @param masterSecretId the id of the master secret stored in the wallet
     * @return cred_req_json: Credential request json for creation of credential by Issuer
     * {
     * "prover_did" : string,
     * "cred_def_id" : string,
     * // Fields below can depend on Cred Def type
     * "blinded_ms" : <blinded_master_secret>,
     * (opaque type that contains data structures internal to Ursa.
     * It should not be parsed and are likely to change in future versions).
     * "blinded_ms_correctness_proof" : <blinded_ms_correctness_proof>,
     * (opaque type that contains data structures internal to Ursa.
     * It should not be parsed and are likely to change in future versions).
     * "nonce": string
     * }
     * cred_req_metadata_json:  Credential request metadata json for further processing of received form Issuer credential.
     * Credential request metadata contains data structures internal to Ursa.
     * Credential request metadata mustn't be shared with Issuer.
     */
    public abstract Pair<JSONObject, JSONObject> proverCreateCredentialReq(String proverDid, JSONObject credOffer, JSONObject credDef, String masterSecretId);

    /**
     * Set credential attribute tag policy for input credential definition id.
     * Specify None to clear policy, resetting to default (tag all attributes).
     * Set retroactive to force all existing credentials in wallet on input credential definition id into compliance,
     * rewriting their tags accordingly.
     *
     * @param credDefId   credential definition identifier.
     * @param taAttrs     JSON array of attribute names to tag - empty array for None, null for all.
     * @param retroactive whether to rewrite tags on existing credentials to comply with specified policy.
     */
    public abstract void proverSetCredentialAttrTagPolicy(String credDefId, String taAttrs, boolean retroactive);

    /**
     * Get current attribute tag policy for input credential definition id, as a JSON list
     * of attribute names (null for default policy tagging all attributes).
     *
     * @param credDefId credential definition identifier.
     * @return credential attr tag policy as JSON list with canonical names of attributes to tag (JSON null for all).
     */
    public abstract String proverGetCredentialAttrTagPolicy(String credDefId);

    /**
     * Check credential provided by Issuer for the given credential request,
     * updates the credential by a master secret and stores in a secure wallet.
     * <p>
     * To support efficient search the following tags will be created for stored credential:
     * {
     * "schema_id": <credential schema id>,
     * "schema_issuer_did": <credential schema issuer did>,
     * "schema_name": <credential schema name>,
     * "schema_version": <credential schema version>,
     * "issuer_did": <credential issuer did>,
     * "cred_def_id": <credential definition id>,
     * "rev_reg_id": <credential revocation registry id>, # "None" as string if not present
     * // for every attribute in <credential values> that credential attribute tagging policy marks taggable
     * "attr::<attribute name>::marker": "1",
     * "attr::<attribute name>::value": <attribute raw value>,
     * }
     *
     * @param credId          (optional, default is a random one) identifier by which credential will be stored in the wallet
     * @param credReqMetadata a credential request metadata created by prover_create_credential_req
     * @param cred            credential json received from issuer
     * @param credDef         credential definition json related to <cred_def_id> in <cred_json>
     * @param revReqDef       revocation registry definition json related to <rev_reg_def_id> in <cred_json>
     * @return cred_id: identifier by which credential is stored in the wallet
     */
    public abstract String proverStoreCredential(String credId, JSONObject credReqMetadata, JSONObject cred, JSONObject credDef, String revReqDef);

    /**
     * Overload method {@link #proverCreateMasterSecret(String masterSecretName)}
     */
    public String proverStoreCredential(String credId, JSONObject credReqMetadata, JSONObject cred, JSONObject credDef) {
        return proverStoreCredential(credId, credReqMetadata, cred, credDef, null);
    }

    /**
     * Gets human readable credential by the given id.
     *
     * @param credDefId Identifier by which requested credential is stored in the wallet
     * @return credential json
     * {
     * "referent": string, - id of credential in the wallet
     * "attrs": {"key1":"raw_value1", "key2":"raw_value2"}, - credential attributes
     * "schema_id": string, - identifier of schema
     * "cred_def_id": string, - identifier of credential definition
     * "rev_reg_id": Optional<string>, - identifier of revocation registry definition
     * "cred_rev_id": Optional<string> - identifier of credential in the revocation registry definition
     * }
     */
    public abstract String proverGetCredential(String credDefId) throws WalletItemNotFoundException;

    /**
     * Delete identified credential from wallet.
     *
     * @param credId identifier by which wallet stores credential to delete
     */
    public abstract void proverDeleteCredential(String credId);

    /**
     * Gets human readable credentials according to the filter.
     * If filter is NULL, then all credentials are returned.
     * Credentials can be filtered by tags created during saving of credential.
     * <p>
     * NOTE: This method is deprecated because immediately returns all fetched credentials.
     * Use <prover_search_credentials> to fetch records by small batches.
     *
     * @param filters filter for credentials
     *                {
     *                "schema_id": string, (Optional)
     *                "schema_issuer_did": string, (Optional)
     *                "schema_name": string, (Optional)
     *                "schema_version": string, (Optional)
     *                "issuer_did": string, (Optional)
     *                "cred_def_id": string, (Optional)
     *                }
     * @return credentials json
     * [{
     * "referent": string, - id of credential in the wallet
     * "attrs": {"key1":"raw_value1", "key2":"raw_value2"}, - credential attributes
     * "schema_id": string, - identifier of schema
     * "cred_def_id": string, - identifier of credential definition
     * "rev_reg_id": Optional<string>, - identifier of revocation registry definition
     * "cred_rev_id": Optional<string> - identifier of credential in the revocation registry definition
     * }]
     */
    public abstract List<String> proverGetCredentials(String filters);

    /**
     * Search for credentials stored in wallet.
     * Credentials can be filtered by tags created during saving of credential.
     * <p>
     * Instead of immediately returning of fetched credentials this call returns search_handle that can be used later
     * to fetch records by small batches (with prover_credentials_search_fetch_records).
     *
     * @param query wql style filter for credentials searching based on tags.
     *              where wql query: indy-sdk/docs/design/011-wallet-query-language/README.md
     * @return [{
     * "referent": string, - id of credential in the wallet
     * "attrs": {"key1":"raw_value1", "key2":"raw_value2"}, - credential attributes
     * "schema_id": string, - identifier of schema
     * "cred_def_id": string, - identifier of credential definition
     * "rev_reg_id": Optional<string>, - identifier of revocation registry definition
     * "cred_rev_id": Optional<string> - identifier of credential in the revocation registry definition
     * }]
     */
    public abstract List<String> proverSearchCredential(String query);

    /**
     * Gets human readable credentials matching the given proof request.
     * <p>
     * NOTE: This method is deprecated because immediately returns all fetched credentials.
     * Use <prover_search_credentials_for_proof_req> to fetch records by small batches.
     *
     * @param proofRequest proof request json
     *                     {
     *                     "name": string,
     *                     "version": string,
     *                     "nonce": string, - a decimal number represented as a string (use `indy_generate_nonce` function to generate 80-bit number)
     *                     "requested_attributes": { // set of requested attributes
     *                     "<attr_referent>": <attr_info>, // see below
     *                     ...,
     *                     },
     *                     "requested_predicates": { // set of requested predicates
     *                     "<predicate_referent>": <predicate_info>, // see below
     *                     ...,
     *                     },
     *                     "non_revoked": Optional<<non_revoc_interval>>, // see below,
     *                     // If specified prover must proof non-revocation
     *                     // for date in this interval for each attribute
     *                     // (applies to every attribute and predicate but can be overridden on attribute level)
     *                     "ver": Optional<str>  - proof request version:
     *                     - omit to use unqualified identifiers for restrictions
     *                     - "1.0" to use unqualified identifiers for restrictions
     *                     - "2.0" to use fully qualified identifiers for restrictions
     *                     }
     *                     where
     *                     attr_referent: Proof-request local identifier of requested attribute
     *                     attr_info: Describes requested attribute
     *                     {
     *                     "name": Optional<string>, // attribute name, (case insensitive and ignore spaces)
     *                     "names": Optional<[string, string]>, // attribute names, (case insensitive and ignore spaces)
     *                     // NOTE: should either be "name" or "names", not both and not none of them.
     *                     // Use "names" to specify several attributes that have to match a single credential.
     *                     "restrictions": Optional<filter_json>, // see below
     *                     "non_revoked": Optional<<non_revoc_interval>>, // see below,
     *                     // If specified prover must proof non-revocation
     *                     // for date in this interval this attribute
     *                     // (overrides proof level interval)
     *                     }
     *                     predicate_referent: Proof-request local identifier of requested attribute predicate
     *                     predicate_info: Describes requested attribute predicate
     *                     {
     *                     "name": attribute name, (case insensitive and ignore spaces)
     *                     "p_type": predicate type (">=", ">", "<=", "<")
     *                     "p_value": int predicate value
     *                     "restrictions": Optional<filter_json>, // see below
     *                     "non_revoked": Optional<<non_revoc_interval>>, // see below,
     *                     // If specified prover must proof non-revocation
     *                     // for date in this interval this attribute
     *                     // (overrides proof level interval)
     *                     }
     *                     non_revoc_interval: Defines non-revocation interval
     *                     {
     *                     "from": Optional<int>, // timestamp of interval beginning
     *                     "to": Optional<int>, // timestamp of interval ending
     *                     }
     *                     filter_json:
     *                     {
     *                     "schema_id": string, (Optional)
     *                     "schema_issuer_did": string, (Optional)
     *                     "schema_name": string, (Optional)
     *                     "schema_version": string, (Optional)
     *                     "issuer_did": string, (Optional)
     *                     "cred_def_id": string, (Optional)
     *                     }
     * @return json with credentials for the given proof request.
     * {
     * "attrs": {
     * "<attr_referent>": [{ cred_info: <credential_info>, interval: Optional<non_revoc_interval> }],
     * ...,
     * },
     * "predicates": {
     * "requested_predicates": [{ cred_info: <credential_info>, timestamp: Optional<integer> }, { cred_info: <credential_2_info>, timestamp: Optional<integer> }],
     * "requested_predicate_2_referent": [{ cred_info: <credential_2_info>, timestamp: Optional<integer> }]
     * }
     * }, where <credential_info> is
     * {
     * "referent": string, - id of credential in the wallet
     * "attrs": {"key1":"raw_value1", "key2":"raw_value2"}, - credential attributes
     * "schema_id": string, - identifier of schema
     * "cred_def_id": string, - identifier of credential definition
     * "rev_reg_id": Optional<string>, - identifier of revocation registry definition
     * "cred_rev_id": Optional<string> - identifier of credential in the revocation registry definition
     * }
     */
    public abstract String proverGetCredentialsForProofReq(String proofRequest);


    /**
     * Search for credentials matching the given proof request.
     * <p>
     * Instead of immediately returning of fetched credentials this call returns search_handle that can be used later
     * to fetch records by small batches (with prover_fetch_credentials_for_proof_req).
     *
     * @param proofRequest   proof request json
     *                       {
     *                       "name": string,
     *                       "version": string,
     *                       "nonce": string, - a decimal number represented as a string (use `indy_generate_nonce` function to generate 80-bit number)
     *                       "requested_attributes": { // set of requested attributes
     *                       "<attr_referent>": <attr_info>, // see below
     *                       ...,
     *                       },
     *                       "requested_predicates": { // set of requested predicates
     *                       "<predicate_referent>": <predicate_info>, // see below
     *                       ...,
     *                       },
     *                       "non_revoked": Optional<<non_revoc_interval>>, // see below,
     *                       // If specified prover must proof non-revocation
     *                       // for date in this interval for each attribute
     *                       // (applies to every attribute and predicate but can be overridden on attribute level)
     *                       // (can be overridden on attribute level)
     *                       "ver": Optional<str>  - proof request version:
     *                       - omit to use unqualified identifiers for restrictions
     *                       - "1.0" to use unqualified identifiers for restrictions
     *                       - "2.0" to use fully qualified identifiers for restrictions
     *                       }
     * @param extraQuery     (Optional) List of extra queries that will be applied to correspondent attribute/predicate:
     *                       {
     *                       "<attr_referent>": <wql query>,
     *                       "<predicate_referent>": <wql query>,
     *                       }
     * @param limitReferents max number of referent search
     *                       where
     *                       attr_info: Describes requested attribute
     *                       {
     *                       "name": Optional<string>, // attribute name, (case insensitive and ignore spaces)
     *                       "names": Optional<[string, string]>, // attribute names, (case insensitive and ignore spaces)
     *                       // NOTE: should either be "name" or "names", not both and not none of them.
     *                       // Use "names" to specify several attributes that have to match a single credential.
     *                       "restrictions": Optional<filter_json>, // see below
     *                       "non_revoked": Optional<<non_revoc_interval>>, // see below,
     *                       // If specified prover must proof non-revocation
     *                       // for date in this interval this attribute
     *                       // (overrides proof level interval)
     *                       }
     *                       predicate_referent: Proof-request local identifier of requested attribute predicate
     *                       predicate_info: Describes requested attribute predicate
     *                       {
     *                       "name": attribute name, (case insensitive and ignore spaces)
     *                       "p_type": predicate type (">=", ">", "<=", "<")
     *                       "p_value": predicate value
     *                       "restrictions": Optional<wql query>, // see below
     *                       "non_revoked": Optional<<non_revoc_interval>>, // see below,
     *                       // If specified prover must proof non-revocation
     *                       // for date in this interval this attribute
     *                       // (overrides proof level interval)
     *                       }
     *                       non_revoc_interval: Defines non-revocation interval
     *                       {
     *                       "from": Optional<int>, // timestamp of interval beginning
     *                       "to": Optional<int>, // timestamp of interval ending
     *                       }
     *                       extra_query_json:(Optional) List of extra queries that will be applied to correspondent attribute/predicate:
     *                       {
     *                       "<attr_referent>": <wql query>,
     *                       "<predicate_referent>": <wql query>,
     *                       }
     *                       where wql query: indy-sdk/docs/design/011-wallet-query-language/README.md
     *                       The list of allowed fields:
     *                       "schema_id": <credential schema id>,
     *                       "schema_issuer_did": <credential schema issuer did>,
     *                       "schema_name": <credential schema name>,
     *                       "schema_version": <credential schema version>,
     *                       "issuer_did": <credential issuer did>,
     *                       "cred_def_id": <credential definition id>,
     *                       "rev_reg_id": <credential revocation registry id>, // "None" as string if not present
     * @return [{
     * cred_info: <credential_info>,
     * interval: Optional<non_revoc_interval>
     * }]
     * where credential_info is
     * {
     * "referent": string, - id of credential in the wallet
     * "attrs": {"key1":"raw_value1", "key2":"raw_value2"}, - credential attributes
     * "schema_id": string, - identifier of schema
     * "cred_def_id": string, - identifier of credential definition
     * "rev_reg_id": Optional<string>, - identifier of revocation registry definition
     * "cred_rev_id": Optional<string> - identifier of credential in the revocation registry definition
     * }
     * NOTE: The list of length less than the requested count means that search iterator correspondent to the requested <item_referent> is completed.
     */
    public abstract JSONObject proverSearchCredentialsForProofReq(JSONObject proofRequest, String extraQuery, int limitReferents);

    /**
     * Overload method {@link #proverSearchCredentialsForProofReq(JSONObject proofRequest, String extraQuery, int limitReferents)}
     */
    public JSONObject proverSearchCredentialsForProofReq(JSONObject proofRequest, String extraQuery) {
        return proverSearchCredentialsForProofReq(proofRequest, extraQuery, 1);
    }

    /**
     * Overload method {@link #proverSearchCredentialsForProofReq(JSONObject proofRequest, String extraQuery, int limitReferents)}
     */
    public JSONObject proverSearchCredentialsForProofReq(JSONObject proofRequest) {
        return proverSearchCredentialsForProofReq(proofRequest, null);
    }

    /**
     * Overload method {@link #proverSearchCredentialsForProofReq(JSONObject proofRequest, String extraQuery, int limitReferents)}
     */
    public JSONObject proverSearchCredentialsForProofReq(JSONObject proofRequest, int limitReferents) {
        return proverSearchCredentialsForProofReq(proofRequest, null, limitReferents);
    }

    /**
     * Creates a proof according to the given proof request
     * Either a corresponding credential with optionally revealed attributes or self-attested attribute must be provided
     * for each requested attribute (see indy_prover_get_credentials_for_pool_req).
     * A proof request may request multiple credentials from different schemas and different issuers.
     * All required schemas, public keys and revocation registries must be provided.
     * The proof request also contains nonce.
     * The proof contains either proof or self-attested attribute value for each requested attribute.
     *
     * @param proofReq             proof request json
     *                             {
     *                             "name": string,
     *                             "version": string,
     *                             "nonce": string, - a decimal number represented as a string (use `generate_nonce` function to generate 80-bit number)
     *                             "requested_attributes": { // set of requested attributes
     *                             "<attr_referent>": <attr_info>, // see below
     *                             ...,
     *                             },
     *                             "requested_predicates": { // set of requested predicates
     *                             "<predicate_referent>": <predicate_info>, // see below
     *                             ...,
     *                             },
     *                             "non_revoked": Optional<<non_revoc_interval>>, // see below,
     *                             // If specified prover must proof non-revocation
     *                             // for date in this interval for each attribute
     *                             // (applies to every attribute and predicate but can be overridden on attribute level)
     *                             // (can be overridden on attribute level)
     *                             "ver": Optional<str>  - proof request version:
     *                             - omit to use unqualified identifiers for restrictions
     *                             - "1.0" to use unqualified identifiers for restrictions
     *                             - "2.0" to use fully qualified identifiers for restrictions
     *                             }
     * @param requestedCredentials either a credential or self-attested attribute for each requested attribute
     *                             {
     *                             "self_attested_attributes": {
     *                             "self_attested_attribute_referent": string
     *                             },
     *                             "requested_attributes": {
     *                             "requested_attribute_referent_1": {"cred_id": string, "timestamp": Optional<number>, revealed: <bool> }},
     *                             "requested_attribute_referent_2": {"cred_id": string, "timestamp": Optional<number>, revealed: <bool> }}
     *                             },
     *                             "requested_predicates": {
     *                             "requested_predicates_referent_1": {"cred_id": string, "timestamp": Optional<number> }},
     *                             }
     *                             }
     * @param masterSecretName     the id of the master secret stored in the wallet
     * @param schemas              all schemas json participating in the proof request
     *                             {
     *                             <schema1_id>: <schema1>,
     *                             <schema2_id>: <schema2>,
     *                             <schema3_id>: <schema3>,
     *                             }
     * @param credentialDefs       :param credential_defs: all credential definitions json participating in the proof request
     *                             {
     *                             "cred_def1_id": <credential_def1>,
     *                             "cred_def2_id": <credential_def2>,
     *                             "cred_def3_id": <credential_def3>,
     *                             }
     * @param revStates            all revocation states json participating in the proof request
     *                             {
     *                             "rev_reg_def1_id or credential_1_id": {
     *                             "timestamp1": <rev_state1>,
     *                             "timestamp2": <rev_state2>,
     *                             },
     *                             "rev_reg_def2_id or credential_2_id": {
     *                             "timestamp3": <rev_state3>
     *                             },
     *                             "rev_reg_def3_id or credential_3_id": {
     *                             "timestamp4": <rev_state4>
     *                             },
     *                             } - Note: use credential_id instead rev_reg_id in case proving several credentials from the same revocation registry.
     *                             <p>
     *                             where
     *                             attr_referent: Proof-request local identifier of requested attribute
     *                             attr_info: Describes requested attribute
     *                             {
     *                             "name": Optional<string>, // attribute name, (case insensitive and ignore spaces)
     *                             "names": Optional<[string, string]>, // attribute names, (case insensitive and ignore spaces)
     *                             // NOTE: should either be "name" or "names", not both and not none of them.
     *                             // Use "names" to specify several attributes that have to match a single credential.
     *                             "restrictions": Optional<filter_json>, // see below
     *                             "non_revoked": Optional<<non_revoc_interval>>, // see below,
     *                             // If specified prover must proof non-revocation
     *                             // for date in this interval this attribute
     *                             // (overrides proof level interval)
     *                             }
     *                             predicate_referent: Proof-request local identifier of requested attribute predicate
     *                             predicate_info: Describes requested attribute predicate
     *                             {
     *                             "name": attribute name, (case insensitive and ignore spaces)
     *                             "p_type": predicate type (">=", ">", "<=", "<")
     *                             "p_value": predicate value
     *                             "restrictions": Optional<wql query>, // see below
     *                             "non_revoked": Optional<<non_revoc_interval>>, // see below,
     *                             // If specified prover must proof non-revocation
     *                             // for date in this interval this attribute
     *                             // (overrides proof level interval)
     *                             }
     *                             non_revoc_interval: Defines non-revocation interval
     *                             {
     *                             "from": Optional<int>, // timestamp of interval beginning
     *                             "to": Optional<int>, // timestamp of interval ending
     *                             }
     *                             where wql query: indy-sdk/docs/design/011-wallet-query-language/README.md
     *                             The list of allowed fields:
     *                             "schema_id": <credential schema id>,
     *                             "schema_issuer_did": <credential schema issuer did>,
     *                             "schema_name": <credential schema name>,
     *                             "schema_version": <credential schema version>,
     *                             "issuer_did": <credential issuer did>,
     *                             "cred_def_id": <credential definition id>,
     *                             "rev_reg_id": <credential revocation registry id>, // "None" as string if not present
     * @return Proof json
     * For each requested attribute either a proof (with optionally revealed attribute value) or
     * self-attested attribute value is provided.
     * Each proof is associated with a credential and corresponding schema_id, cred_def_id, rev_reg_id and timestamp.
     * There is also aggregated proof part common for all credential proofs.
     * {
     * "requested_proof": {
     * "revealed_attrs": {
     * "requested_attr1_id": {sub_proof_index: number, raw: string, encoded: string},
     * "requested_attr4_id": {sub_proof_index: number: string, encoded: string},
     * },
     * "revealed_attr_groups": {
     * "requested_attr5_id": {
     * "sub_proof_index": number,
     * "values": {
     * "attribute_name": {
     * "raw": string,
     * "encoded": string
     * }
     * },
     * }
     * },
     * "unrevealed_attrs": {
     * "requested_attr3_id": {sub_proof_index: number}
     * },
     * "self_attested_attrs": {
     * "requested_attr2_id": self_attested_value,
     * },
     * "predicates": {
     * "requested_predicate_1_referent": {sub_proof_index: int},
     * "requested_predicate_2_referent": {sub_proof_index: int},
     * }
     * }
     * "proof": {
     * "proofs": [ <credential_proof>, <credential_proof>, <credential_proof> ],
     * "aggregated_proof": <aggregated_proof>
     * } (opaque type that contains data structures internal to Ursa.
     * It should not be parsed and are likely to change in future versions).
     * "identifiers": [{schema_id, cred_def_id, Optional<rev_reg_id>, Optional<timestamp>}]
     * }
     */
    public abstract JSONObject proverCreateProof(JSONObject proofReq, JSONObject requestedCredentials, String masterSecretName,
                                             JSONObject schemas, JSONObject credentialDefs, JSONObject revStates);

    /**
     * Verifies a proof (of multiple credential).
     * All required schemas, public keys and revocation registries must be provided.
     * <p>
     * IMPORTANT: You must use *_id's (`schema_id`, `cred_def_id`, `rev_reg_id`) listed in `proof[identifiers]`
     * as the keys for corresponding `schemas_json`, `credential_defs_json`, `rev_reg_defs_json`, `rev_regs_json` objects.
     *
     * @param proofRequest   {
     *                       "name": string,
     *                       "version": string,
     *                       "nonce": string, - a decimal number represented as a string (use `generate_nonce` function to generate 80-bit number)
     *                       "requested_attributes": { // set of requested attributes
     *                       "<attr_referent>": <attr_info>, // see below
     *                       ...,
     *                       },
     *                       "requested_predicates": { // set of requested predicates
     *                       "<predicate_referent>": <predicate_info>, // see below
     *                       ...,
     *                       },
     *                       "non_revoked": Optional<<non_revoc_interval>>, // see below,
     *                       // If specified prover must proof non-revocation
     *                       // for date in this interval for each attribute
     *                       // (can be overridden on attribute level)
     *                       "ver": Optional<str>  - proof request version:
     *                       - omit to use unqualified identifiers for restrictions
     *                       - "1.0" to use unqualified identifiers for restrictions
     *                       - "2.0" to use fully qualified identifiers for restrictions
     *                       }
     * @param proof          created for request proof json
     *                       {
     *                       "requested_proof": {
     *                       "revealed_attrs": {
     *                       "requested_attr1_id": {sub_proof_index: number, raw: string, encoded: string}, // NOTE: check that `encoded` value match to `raw` value on application level
     *                       "requested_attr4_id": {sub_proof_index: number: string, encoded: string}, // NOTE: check that `encoded` value match to `raw` value on application level
     *                       },
     *                       "revealed_attr_groups": {
     *                       "requested_attr5_id": {
     *                       "sub_proof_index": number,
     *                       "values": {
     *                       "attribute_name": {
     *                       "raw": string,
     *                       "encoded": string
     *                       }
     *                       }, // NOTE: check that `encoded` value match to `raw` value on application level
     *                       }
     *                       },
     *                       "unrevealed_attrs": {
     *                       "requested_attr3_id": {sub_proof_index: number}
     *                       },
     *                       "self_attested_attrs": {
     *                       "requested_attr2_id": self_attested_value,
     *                       },
     *                       "requested_predicates": {
     *                       "requested_predicate_1_referent": {sub_proof_index: int},
     *                       "requested_predicate_2_referent": {sub_proof_index: int},
     *                       }
     *                       }
     *                       "proof": {
     *                       "proofs": [ <credential_proof>, <credential_proof>, <credential_proof> ],
     *                       "aggregated_proof": <aggregated_proof>
     *                       }
     *                       "identifiers": [{schema_id, cred_def_id, Optional<rev_reg_id>, Optional<timestamp>}]
     *                       }
     * @param schemas        all schema jsons participating in the proof
     *                       {
     *                       <schema1_id>: <schema1>,
     *                       <schema2_id>: <schema2>,
     *                       <schema3_id>: <schema3>,
     *                       }
     * @param credentialDefs all credential definitions json participating in the proof
     *                       {
     *                       "cred_def1_id": <credential_def1>,
     *                       "cred_def2_id": <credential_def2>,
     *                       "cred_def3_id": <credential_def3>,
     *                       }
     * @param revRegDefs     all revocation registry definitions json participating in the proof
     *                       {
     *                       "rev_reg_def1_id": <rev_reg_def1>,
     *                       "rev_reg_def2_id": <rev_reg_def2>,
     *                       "rev_reg_def3_id": <rev_reg_def3>,
     *                       }
     * @param revRegs        all revocation registries json participating in the proof
     *                       {
     *                       "rev_reg_def1_id": {
     *                       "timestamp1": <rev_reg1>,
     *                       "timestamp2": <rev_reg2>,
     *                       },
     *                       "rev_reg_def2_id": {
     *                       "timestamp3": <rev_reg3>
     *                       },
     *                       "rev_reg_def3_id": {
     *                       "timestamp4": <rev_reg4>
     *                       },
     *                       }
     * @return valid: true - if signature is valid, false - otherwise
     */
    public abstract boolean verifierVerifyProof(JSONObject proofRequest, JSONObject proof, JSONObject schemas, JSONObject credentialDefs, JSONObject revRegDefs, JSONObject revRegs);

    /**
     * Create revocation state for a credential that corresponds to a particular time.
     * <p>
     * Note that revocation delta must cover the whole registry existence time.
     * You can use `from`: `0` and `to`: `needed_time` as parameters for building request to get correct revocation delta.
     * <p>
     * The resulting revocation state and provided timestamp can be saved and reused later with applying a new
     * revocation delta with `update_revocation_state` function.
     * This new delta should be received with parameters: `from`: `timestamp` and `to`: `needed_time`.
     *
     * @param blobStorageReaderHandle configuration of blob storage reader handle that will allow to read revocation tails
     * @param revRegDef               revocation registry definition json
     * @param revRegDelta             revocation registry definition delta which covers the whole registry existence time
     * @param timestamp               time represented as a total number of seconds from Unix Epoch
     * @param credRevId               user credential revocation id in revocation registry
     * @return revocation state json {
     * "rev_reg": <revocation registry>,
     * "witness": <witness>,
     * "timestamp" : integer
     * }
     */
    public abstract String createRevocation(int blobStorageReaderHandle, String revRegDef,
                                            String revRegDelta, int timestamp, String credRevId);

    /**
     * Create a new revocation state for a credential based on a revocation state created before.
     * Note that provided revocation delta must cover the registry gap from based state creation until the specified time
     * (this new delta should be received with parameters: `from`: `state_timestamp` and `to`: `needed_time`).
     * <p>
     * This function reduces the calculation time.
     * <p>
     * The resulting revocation state and provided timestamp can be saved and reused later by applying a new revocation delta again.
     *
     * @param blobStorageReaderHandle configuration of blob storage reader handle that will allow to read revocation tails
     * @param revState                revocation registry state json
     * @param revRegDef               revocation registry definition json
     * @param revRegDelta             revocation registry definition delta which covers the gap form original `rev_state_json` creation till the requested timestamp
     * @param timestamp               time represented as a total number of seconds from Unix Epoch
     * @param credRevId               user credential revocation id in revocation registry
     * @return revocation state json {
     * "rev_reg": <revocation registry>,
     * "witness": <witness>,
     * "timestamp" : integer
     * }
     */
    public abstract String updateRevocationState(int blobStorageReaderHandle, String revState, String revRegDef,
                                                 String revRegDelta, int timestamp, String credRevId);

    /**
     * Generates 80-bit numbers that can be used as a nonce for proof request.
     *
     * @return nonce: generated number as a string
     */
    public abstract String generateNonce();

    /**
     * Get unqualified form (short form without method) of a fully qualified entity like DID.
     * <p>
     * This function should be used to the proper casting of fully qualified entity to unqualified form in the following cases:
     * Issuer, which works with fully qualified identifiers, creates a Credential Offer for Prover, which doesn't support fully qualified identifiers.
     * Verifier prepares a Proof Request based on fully qualified identifiers or Prover, which doesn't support fully qualified identifiers.
     * another case when casting to unqualified form needed
     *
     * @param entity target entity to disqualify. Can be one of:
     *               Did
     *               SchemaId
     *               CredentialDefinitionId
     *               RevocationRegistryId
     *               Schema
     *               CredentialDefinition
     *               RevocationRegistryDefinition
     *               CredentialOffer
     *               CredentialRequest
     *               ProofRequest
     * @return entity either in unqualified form or original if casting isn't possible
     */
    public abstract String toUnqualified(String entity);
}

