// Automatically generated by flapigen
package org.iota.client;

/**
 * The options builder for a client connected to multiple nodes.
 */
public final class ClientBuilder {
    /**
     * Create a new instance of the Client
     */
    public ClientBuilder() {
        mNativeObj = init();
    }
    private static native long init();
    /**
     * Adds an IOTA node by its URL.
     * @param node The node URL
     */
    public final ClientBuilder withNode(String node) {
        long ret = do_withNode(mNativeObj, node);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withNode(long self, String node);
    /**
     * Adds a list of IOTA nodes by their URLs.
     * @param nodes The list of node URLs
     */
    public final ClientBuilder withNodes(java.lang.String [] nodes) {
        long ret = do_withNodes(mNativeObj, nodes);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withNodes(long self, java.lang.String [] nodes);
    /**
     * Adds an IOTA node by its URL with optional jwt and or basic authentication
     * @param node The node URL
     * @param jwt The JWT, can be `null`
     * @param username The username, can be `null`
     * @param password The password, can be `null`. Only checked if username is not `null`
     */
    public final ClientBuilder withNodeAuth(String node, String jwt, String username, String password) {
        long ret = do_withNodeAuth(mNativeObj, node, jwt, username, password);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withNodeAuth(long self, String node, String jwt, String username, String password);
    /**
     * Adds an IOTA node by its URL to be used as primary node, with optional jwt and or basic authentication
     * @param node The node URL
     * @param jwt The JWT, can be `null`
     * @param username The username, can be `null`
     * @param password The password, can be `null`. Only checked if username is not `null`
     */
    public final ClientBuilder withPrimaryNode(String node, String jwt, String username, String password) {
        long ret = do_withPrimaryNode(mNativeObj, node, jwt, username, password);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withPrimaryNode(long self, String node, String jwt, String username, String password);
    /**
     * Adds an IOTA node by its URL to be used as primary PoW node (for remote PoW), with optional jwt and or basic
     * authentication
     * @param node The node URL
     * @param jwt The JWT, can be `null`
     * @param username The username, can be `null`
     * @param password The password, can be `null`. Only checked if username is not `null`
     */
    public final ClientBuilder withPrimaryPowNode(String node, String jwt, String username, String password) {
        long ret = do_withPrimaryPowNode(mNativeObj, node, jwt, username, password);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withPrimaryPowNode(long self, String node, String jwt, String username, String password);
    /**
     * Adds an IOTA permanode by its URL, with optional jwt and or basic authentication
     * @param node The node URL
     * @param jwt The JWT, can be `null`
     * @param username The username, can be `null`
     * @param password The password, can be `null`. Only checked if username is not `null`
     */
    public final ClientBuilder withPermanode(String node, String jwt, String username, String password) {
        long ret = do_withPermanode(mNativeObj, node, jwt, username, password);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withPermanode(long self, String node, String jwt, String username, String password);
    /**
     * Get node list from the node_pool_urls
     * @param nodes node_pool_urls list of node URLs for the node pool
     */
    public final ClientBuilder withNodePoolUrls(java.lang.String [] node_pool_urls) {
        long ret = do_withNodePoolUrls(mNativeObj, node_pool_urls);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withNodePoolUrls(long self, java.lang.String [] node_pool_urls);
    /**
     * Allows creating the client without nodes for offline address generation or signing
     */
    public final ClientBuilder withOfflineMode() {
        long ret = do_withOfflineMode(mNativeObj);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withOfflineMode(long self);
    /**
     * Client connected to the default Network node pool unless other nodes are provided.
     * 
     * ```
     * import org.iota.client.ClientBuilder;
     * Client clientOptions = new ClientBuilder().with_network("devnet").build();
     * ```
     * @param network The network we connect to
     */
    public final ClientBuilder withNetwork(String network) {
        long ret = do_withNetwork(mNativeObj, network);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withNetwork(long self, String network);
    /**
     * Set the node sync interval
     * @param node_sync_interval The interval in seconds
     */
    public final ClientBuilder withNodeSyncInterval(float node_sync_interval) {
        long ret = do_withNodeSyncInterval(mNativeObj, node_sync_interval);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withNodeSyncInterval(long self, float node_sync_interval);
    /**
     * Disables the node syncing process.
     * Every node will be considered healthy and ready to use.
     */
    public final ClientBuilder withNodeSyncDisabled() {
        long ret = do_withNodeSyncDisabled(mNativeObj);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withNodeSyncDisabled(long self);
    /**
     * Set if quorum should be used or not
     * @param quorum `true` if we use a quorum
     */
    public final ClientBuilder withQuorum(boolean quorum) {
        long ret = do_withQuorum(mNativeObj, quorum);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withQuorum(long self, boolean quorum);
    /**
     * Set amount of nodes which should be used for quorum
     * @param quorum_size The amount of nodes
     */
    public final ClientBuilder withQuorumSize(long quorum_size) {
        long ret = do_withQuorumSize(mNativeObj, quorum_size);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withQuorumSize(long self, long quorum_size);
    /**
     * Set quorum threshold
     * @param threshold The percentage of nodes that need to agree (0-100)
     */
    public final ClientBuilder withQuorumThreshold(long threshold) {
        long ret = do_withQuorumThreshold(mNativeObj, threshold);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withQuorumThreshold(long self, long threshold);
    /**
     * Sets the MQTT broker options.
     * @param options the MQTT options
     */
    public final ClientBuilder withMqttBrokerOptions(BrokerOptions options) {
        long a0 = options.mNativeObj;
        options.mNativeObj = 0;

        long ret = do_withMqttBrokerOptions(mNativeObj, a0);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);
        java.lang.ref.Reference.reachabilityFence(options);

        return convRet;
    }
    private static native long do_withMqttBrokerOptions(long self, long options);
    /**
     * Sets whether the PoW should be done locally or remotely.
     * @param local Enables or disables local PoW
     */
    public final ClientBuilder withLocalPow(boolean local) {
        long ret = do_withLocalPow(mNativeObj, local);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withLocalPow(long self, boolean local);
    /**
     * Sets after how many seconds new tips will be requested during PoW
     * @param tips delay in seconds
     */
    public final ClientBuilder withTipsInterval(long tips) {
        long ret = do_withTipsInterval(mNativeObj, tips);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withTipsInterval(long self, long tips);
    /**
     * Sets the default request timeout in seconds.
     * @param timeout The request timeout in seconds
     */
    public final ClientBuilder withRequestTimeout(float timeout) {
        long ret = do_withRequestTimeout(mNativeObj, timeout);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withRequestTimeout(long self, float timeout);
    /**
     * Sets the request timeout in seconds for a specific API usage.
     * @param api The API we set the timeout for
     * @param timeout The request timeout in seconds
     */
    public final ClientBuilder withApiTimeout(Api api, float timeout) {
        int a0 = api.getValue();
        long ret = do_withApiTimeout(mNativeObj, a0, timeout);
        ClientBuilder convRet = new ClientBuilder(InternalPointerMarker.RAW_PTR, ret);
        java.lang.ref.Reference.reachabilityFence(api);

        return convRet;
    }
    private static native long do_withApiTimeout(long self, int api, float timeout);
    /**
     * Build the Client instance.
     */
    public final Client finish() {
        long ret = do_finish(mNativeObj);
        Client convRet = new Client(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_finish(long self);

    public synchronized void delete() {
        if (mNativeObj != 0) {
            do_delete(mNativeObj);
            mNativeObj = 0;
       }
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            delete();
        }
        finally {
             super.finalize();
        }
    }
    private static native void do_delete(long me);
    /*package*/ ClientBuilder(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}