package com.sirius.sdk.agent.wallet.abstract_wallet;

import com.sirius.sdk.utils.Pair;

import java.util.List;

public abstract  class AbstractPairwise {
    /**
     *     Check if pairwise is exists.
     * @param theirDid encoded Did.
     * @return true - if pairwise is exists, false - otherwise
     */
    public abstract boolean isPairwiseExist(String theirDid);

    /**
     *
     * @param theirDid encrypting DID
     * @param myDid encrypting DID
     * @param metadata (Optional) extra information for pairwise
     * @param tags: tags for searching operations
     * @return  Error code
     *
     */
    public abstract void createPairwise(String theirDid,String myDid,String metadata,String tags);



    /**
     * Overload method {@link #createPairwise(String theirDid,String myDid,String metadata,String tags)}
     */
    public  void createPairwise(String theirDid,String myDid,String metadata){
        createPairwise(theirDid,myDid,metadata,null);
    }

    /**
     * Overload method {@link #createPairwise(String theirDid,String myDid,String metadata,String tags)}
     */
    public  void createPairwise(String theirDid,String myDid){
        createPairwise(theirDid,myDid,null);
    }

    /**
     *    Get list of saved pairwise.
     * @return pairwise_list: list of saved pairwise
     */
    public abstract List<Object> listPairwise();

    /**
     *  Gets pairwise information for specific their_did.
     *  @param thierDid: encoded Did
     * @return
     */
    public abstract String getPairwise(String thierDid);

    /**
     *  Save some data in the Wallet for pairwise associated with Did.
     * @param theirDid  encoded DID
     * @param metadata some extra information for pairwise
     * @param tags tags for searching operation
     */
    public abstract void setPairwiseMetadata(String theirDid,String metadata,String tags);

    /**
     * Overload method {@link #setPairwiseMetadata(String theirDid,String metadata,String tags)}
     */
    public  void setPairwiseMetadata(String theirDid,String metadata){
        setPairwiseMetadata(theirDid,metadata,null);
    }
    /**
     * Overload method {@link #setPairwiseMetadata(String theirDid,String metadata,String tags)}
     */
    public  void setPairwiseMetadata(String theirDid){
        setPairwiseMetadata(theirDid,null);
    }


    /**
     * Search Pairwises
     * @param tags tags based query
     * @param limit limit: max items count
     * @return Results, TotalCount
     */
    public abstract Pair<List<String>,Integer> search(String tags, Integer limit);

    /**
     * Overload method {@link #search(String tags, Integer limit)}
     */
    public  Pair<List<String>,Integer> search(String tags){
        return search(tags,null);
    }
}


