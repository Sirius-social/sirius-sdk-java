package com.sirius.sdk.agent.wallet.abstract_wallet;

import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;

import java.util.List;

public abstract  class AbstractNonSecrets {
    /**
     *  Create a new non-secret record in the wallet
     * @param type allows to separate different record types collections
     * @param id the id of record
     * @param value  the value of record
     * @param tags the record tags used for search and storing meta information as json:
     *              {
     *              "tagName1": <str>, // string tag (will be stored encrypted)
     *              "tagName2": <str>, // string tag (will be stored encrypted)
     *              "~tagName3": <str>, // string tag (will be stored un-encrypted)
     *              "~tagName4": <str>, // string tag (will be stored un-encrypted)
     *            }
     */
    public abstract void addWalletRecord(String type,String id,String value,String tags);

    /**
     *   Update a non-secret wallet record value
     * @param type allows to separate different record types collections
     * @param id  the id of record
     * @param value the value of record
     */
    public abstract void updateWalletRecordValue(String type,String id,String value);

    /**
     * Update a non-secret wallet record value
     * @param type allows to separate different record types collections
     * @param id  the id of record
     * @param tags tags_json: the record tags used for search and storing meta information as json:
     *              {
     *              "tagName1": <str>, // string tag (will be stored encrypted)
     *              "tagName2": <str>, // string tag (will be stored encrypted)
     *              "~tagName3": <str>, // string tag (will be stored un-encrypted)
     *              "~tagName4": <str>, // string tag (will be stored un-encrypted)
     *            }
     */
    public abstract void updateWalletRecordTags(String type,String id,String tags);

    /**
     *         Add new tags to the wallet record
     * @param type  allows to separate different record types collections
     * @param id  the id of record
     * @param tags  tags_json: the record tags used for search and storing meta information as json:
     *            {
     *              "tagName1": <str>, // string tag (will be stored encrypted)
     *              "tagName2": <str>, // string tag (will be stored encrypted)
     *              "~tagName3": <str>, // string tag (will be stored un-encrypted)
     *              "~tagName4": <str>, // string tag (will be stored un-encrypted)
     *            }
     */
    public abstract void addWalletRecordTags(String type,String id,String tags);

    /**
     *    Delete tags from the wallet record
     * @param type allows to separate different record types collections
     * @param id the id of record
     * @param tagNames the list of tag names to remove from the record as json array: ["tagName1", "tagName2", ...]
     */
    public abstract void deleteWalletRecord(String type, String id, List<String> tagNames);

    /**
     *   Delete an existing wallet record in the wallet
     * @param type allows to separate different record types collections
     * @param id the id of record
     */
    public abstract void deleteWalletRecord(String type,String id);

    /**
     *     Get an wallet record by id
     * @param type  allows to separate different record types collections
     * @param id  the id of record
     * @param options  {
     *             retrieveType: (optional, false by default) Retrieve record type,
     *             retrieveValue: (optional, true by default) Retrieve record value,
     *             retrieveTags: (optional, true by default) Retrieve record tags
     *           }
     * @return wallet record json:
     *          {
     *            id: "Some id",
     *            type: "Some type", // present only if retrieveType set to true
     *            value: "Some value", // present only if retrieveValue set to true
     *            tags: <tags json>, // present only if retrieveTags set to true
     *          }
     */
    public abstract String getWalletRecord(String type, String id, RetrieveRecordOptions options);

    /**
     *  Search for wallet records
     * @param type allows to separate different record types collections
     * @param query MongoDB style query to wallet record tags:
     *                {
     *             "tagName": "tagValue",
     *             $or: {
     *               "tagName2": { $regex: 'pattern' },
     *               "tagName3": { $gte: '123' },
     *             },
     *           }
     * @param options {
     *             retrieveRecords: (optional, true by default) If false only "counts" will be calculated,
     *             retrieveTotalCount: (optional, false by default) Calculate total count,
     *             retrieveType: (optional, false by default) Retrieve record type,
     *             retrieveValue: (optional, true by default) Retrieve record value,
     *             retrieveTags: (optional, true by default) Retrieve record tags,
     *           }
     * @param limit max record count to retrieve
     * @return wallet records json:
     *          {
     *            totalCount: <str>, // present only if retrieveTotalCount set to true
     *            records: [{ // present only if retrieveRecords set to true
     *                id: "Some id",
     *                type: "Some type", // present only if retrieveType set to true
     *                value: "Some value", // present only if retrieveValue set to true
     *                tags: <tags json>, // present only if retrieveTags set to true
     *            }],
     *          }
     */
    public abstract Pair<List<String>,Integer> walletSearch(String type,String query,RetrieveRecordOptions options, int limit);
}
