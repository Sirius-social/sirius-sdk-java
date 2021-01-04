package com.sirius.sdk.agent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sirius.sdk.agent.model.ledger.CredentialDefinition;
import com.sirius.sdk.agent.model.ledger.Schema;
import com.sirius.sdk.agent.model.ledger.SchemaFilters;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractAnonCreds;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCache;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractLedger;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.CacheOptions;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;
import com.sirius.sdk.utils.GsonUtils;
import com.sirius.sdk.utils.Pair;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Ledger {
    public String getName() {
        return name;
    }

    String name;
    AbstractLedger api;
    AbstractAnonCreds issuer;
    AbstractCache cache;
    AbstractImmutableCollection storage;
    String db;

    public Ledger(String name, AbstractLedger api, AbstractAnonCreds issuer,
                  AbstractCache cache, AbstractImmutableCollection storage) {
        this.name = name;
        this.api = api;
        this.issuer = issuer;
        this.cache = cache;
        this.storage = storage;
        db = String.format("ledger_storage_%s", name);
    }

    public Schema loadSchema(String id, String submitterDid) {
        String schemaString = cache.getSchema(name, submitterDid, id, new CacheOptions());
        return new Schema(schemaString);
    }


    public CredentialDefinition loadCredDef(String id, String submitterDid) {
        String credDef = cache.getCredDef(name, submitterDid, id, new CacheOptions());
        CredentialDefinition credentialDefinition = new CredentialDefinition().deserialize(credDef);
        String tag = credentialDefinition.getTag();
        return null;
    }

    /*   async def load_cred_def(self, id_: str, submitter_did: str) -> CredentialDefinition:
       cred_def_body = await self._cache.get_cred_def(
       pool_name=self.name,
       submitter_did=submitter_did,
       id_=id_,
       options=CacheOptions()
           )
       tag = cred_def_body.get('tag')
       schema_seq_no = int(cred_def_body['schemaId'])
       cred_def_seq_no = int(cred_def_body['id'].split(':')[3]) + 1
       txn_request = await self._api.build_get_txn_request(
       submitter_did=submitter_did,
       ledger_type=None,
       seq_no=schema_seq_no
           )
       resp = await self._api.sign_and_submit_request(
       pool_name=self.name,
       submitter_did=submitter_did,
       request=txn_request
           )
                   if resp['op'] == 'REPLY':
       txn_data = resp['result']['data']
       schema_body = {
           'name': txn_data['txn']['data']['data']['name'],
                   'version': txn_data['txn']['data']['data']['version'],
                   'attrNames': txn_data['txn']['data']['data']['attr_names'],
                   'id': txn_data['txnMetadata']['txnId'],
                   'seqNo': txn_data['txnMetadata']['seqNo']
       }
       schema_body['ver'] = schema_body['id'].split(':')[-1]
       schema = Schema(**schema_body)
       cred_def = CredentialDefinition(
               tag=tag, schema=schema, body=cred_def_body, seq_no=cred_def_seq_no
       )
               return cred_def
           else:
       raise SiriusInvalidPayloadStructure()

   */
    public Pair<Boolean, Schema> registerSchema(AnonCredSchema schema, String submitterDid) {
        Pair<Boolean, String> successTxnResponse = api.registerSchema(name, submitterDid, schema);
        JsonObject txnResponse = GsonUtils.toJsonObject(successTxnResponse.second);
        if (successTxnResponse.first && "REPLY".equals(txnResponse.get("op").getAsString())) {
            String body = schema.serialize();
            int seqNo = txnResponse.getAsJsonObject("result").getAsJsonObject("txnMetadata").get("seqNo").getAsInt();
            Schema schemaInLedger = new Schema(body);
            schemaInLedger.setSeqNo(seqNo);
            ensureExistInStorage(schemaInLedger,submitterDid);
            return new Pair<>(true, schemaInLedger);
        } else {
            JsonElement reason = txnResponse.get("reason");
            if (reason != null) {
                if (!reason.isJsonNull()) {
                    String reasonStr = reason.getAsString();
                    Logger.getGlobal().log(Level.WARNING, reasonStr);
                }
            }
        }
        return new Pair<>(false,null);
    }

    public void ensureExistInStorage(Schema entity, String submitter_did) {
     //   await self._storage.select_db(self.__db)
       storage.selectDb(db);
        JsonObject tagObject = new JsonObject();
        tagObject.addProperty("id", entity.getId());
        tagObject.addProperty("category", "schema");

        Pair<List<Object>,Integer> count = storage.fetch(tagObject.toString());
        if(count.second==0){
            JsonObject tagUpdate = new JsonObject();
            tagUpdate.addProperty("id", entity.getId());
            tagUpdate.addProperty("name", entity.getName());
            tagUpdate.addProperty("version", entity.getVersion());
            tagUpdate.addProperty("submitter_did", submitter_did);
            GsonUtils.updateJsonObject(tagObject, tagUpdate);
            storage.add(entity.serializeToJsonObject().toString(),tagObject.toString());
        }
    }



    public void ensureExistInStorage(CredentialDefinition entity, JsonObject searchTags) {
        storage.selectDb(db);
        JsonObject tagObject = new JsonObject();
        tagObject.addProperty("id", entity.getId());
        tagObject.addProperty("seq_no", String.valueOf(entity.getSeqNo()));
        tagObject.addProperty("category", "cred_def");

        Pair<List<Object>, Integer> searchCount = storage.fetch(tagObject.toString());
        if (searchCount.second == 0) {
            JsonObject tagUpdate = new JsonObject();
            tagUpdate.addProperty("id", entity.getId());
            tagUpdate.addProperty("tag", entity.getTag());
            tagUpdate.addProperty("schema_id", entity.getSchema().getId());
            tagUpdate.addProperty("submitter_did", entity.getSubmitterDid());
            GsonUtils.updateJsonObject(tagObject, tagUpdate);
            if (searchTags != null) {
                GsonUtils.updateJsonObject(tagObject, searchTags);
            }
            storage.add(entity.serialize(), tagObject.toString());

        }
    }

    public void ensureExistInStorage(CredentialDefinition entity) {
        ensureExistInStorage(entity, null);
    }



    public Schema ensureSchemaExists(AnonCredSchema schema,String submitterDid){
        try{
            String schemaString = cache.getSchema(name,submitterDid,schema.getId(), new CacheOptions());
            Schema ledgerSchema = new Schema(schemaString);
            ensureExistInStorage(ledgerSchema,submitterDid);
            return ledgerSchema;
        }catch (Exception e){
            e.printStackTrace();
           Pair<Boolean,Schema> okSchema =  registerSchema(schema,submitterDid);
           if(okSchema.first){
               return okSchema.second;
           }

        }
        return null;

    }

    public List<Schema> fetchSchemas(String id, String name,String version,String submitterDid ){
        SchemaFilters filters = new SchemaFilters();
        filters.setId(id);
        filters.setName(name);
        filters.setVersion(version);
        filters.setSubmitterDid(submitterDid);

        Pair<List<Object>,Integer> results = storage.fetch(filters.getTags().serialize());
        List<Schema> schemaList = new ArrayList<>();
        if(results!=null){
           List<Object> objects =  results.first;
           for(int i=0;i<objects.size();i++){
              Object ob = objects.get(i);
              if(ob instanceof String){
                  Schema schema = new Schema((String) ob);
                  schemaList.add(schema);
              }
           }
        }
        return schemaList;
    }
    public List<Schema> fetchSchemas(String id, String name,String version ){
        return fetchSchemas(id,name,version,null);
    }
    public List<Schema> fetchSchemas(String id, String name ){
        return  fetchSchemas(id,name,null,null);
    }
    public List<Schema> fetchSchemas(String id ){
        return fetchSchemas(id,null,null,null);
    }
    public List<Schema> fetchSchemas( ){
        return fetchSchemas(null,null,null,null);
    }



    public Pair<Boolean,CredentialDefinition> registerCredDef(CredentialDefinition credDef,String submitterDid, JsonObject tags ){

    /*   Pair<String,String> credDefidBody =  issuer.issuerCreateAndStoreCredentialDef(submitterDid,
                credDef.getSchema().serializeToJsonObject(),credDef.getTag(),credDef.getConfig().serialize());
*/
        return new Pair<>(true, credDef);
    }
   /* async def register_cred_def(
            self, cred_def: CredentialDefinition, submitter_did: str, tags: dict = None
    ) -> (bool, CredentialDefinition):
    cred_def_id, body = await self._issuer.issuer_create_and_store_credential_def(
    issuer_did=submitter_did,
    schema=cred_def.schema.body,
    tag=cred_def.tag,
    config=cred_def.config.serialize()
            )
    build_request = await self._api.build_cred_def_request(
    submitter_did=submitter_did,
    data=body
        )
    signed_request = await self._api.sign_request(
    submitter_did=submitter_did,
    request=build_request
        )
    resp = await self._api.submit_request(self.name, signed_request)
    success = resp.get('op', None) == 'REPLY'
            if success:
    txn_response = resp
        else:
                return False, None
        if success:
    ledger_cred_def = CredentialDefinition(
            tag=cred_def.tag,
            schema=cred_def.schema,
            config=cred_def.config,
            body=body,
            seq_no=txn_response['result']['txnMetadata']['seqNo']
    )
    await self.__ensure_exists_in_storage(ledger_cred_def, submitter_did, tags)
            return True, ledger_cred_def
*/
}
