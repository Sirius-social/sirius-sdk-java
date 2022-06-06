// Automatically generated by flapigen
package org.iota.client;

/**
 * Response of GET /api/v1/info.
 * Returns general information about the node.
 */
public final class InfoResponse {
    @Override
    public String toString() {{
        return this.to_string();
    }}

    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj instanceof InfoResponse)
        equal = ((InfoResponse)obj).rustEq(this);
        return equal;
    }

    public int hashCode() {
        return (int)mNativeObj;
    }


    private InfoResponse() {}

    private final String to_string() {
        String ret = do_to_string(mNativeObj);

        return ret;
    }
    private static native String do_to_string(long self);

    private final boolean rustEq(InfoResponse o) {
        long a0 = o.mNativeObj;
        boolean ret = do_rustEq(mNativeObj, a0);

        JNIReachabilityFence.reachabilityFence1(o);

        return ret;
    }
    private static native boolean do_rustEq(long self, long o);
    /**
     * The name of the node
     */
    public final String name() {
        String ret = do_name(mNativeObj);

        return ret;
    }
    private static native String do_name(long self);
    /**
     * The node software version
     */
    public final String version() {
        String ret = do_version(mNativeObj);

        return ret;
    }
    private static native String do_version(long self);
    /**
     * The network id the node is connected to
     */
    public final String networkId() {
        String ret = do_networkId(mNativeObj);

        return ret;
    }
    private static native String do_networkId(long self);
    /**
     * The bech32 HRP which is accepted by this node
     */
    public final String bech32Hrp() {
        String ret = do_bech32Hrp(mNativeObj);

        return ret;
    }
    private static native String do_bech32Hrp(long self);
    /**
     * The messages per second this node is receiving
     */
    public final double messagesPerSecond() {
        double ret = do_messagesPerSecond(mNativeObj);

        return ret;
    }
    private static native double do_messagesPerSecond(long self);
    /**
     * The referenced messages per second this node is receiving
     */
    public final double referencedMessagesPerSecond() {
        double ret = do_referencedMessagesPerSecond(mNativeObj);

        return ret;
    }
    private static native double do_referencedMessagesPerSecond(long self);
    /**
     * The reference rate
     */
    public final double referencedRate() {
        double ret = do_referencedRate(mNativeObj);

        return ret;
    }
    private static native double do_referencedRate(long self);
    /**
     * The timestamp of the latest received milestone
     */
    public final long latestMilestoneTimestamp() {
        long ret = do_latestMilestoneTimestamp(mNativeObj);

        return ret;
    }
    private static native long do_latestMilestoneTimestamp(long self);
    /**
     * The minimum required PoW for a message to be accepted
     */
    public final double minPowScore() {
        double ret = do_minPowScore(mNativeObj);

        return ret;
    }
    private static native double do_minPowScore(long self);
    /**
     * The index of the latest seen milestone
     */
    public final long latestMilestoneIndex() {
        long ret = do_latestMilestoneIndex(mNativeObj);

        return ret;
    }
    private static native long do_latestMilestoneIndex(long self);
    /**
     * The index of the latest confirmed milestone
     */
    public final long confirmedMilestoneIndex() {
        long ret = do_confirmedMilestoneIndex(mNativeObj);

        return ret;
    }
    private static native long do_confirmedMilestoneIndex(long self);
    /**
     * The milestone index this node is pruning from
     */
    public final long pruningIndex() {
        long ret = do_pruningIndex(mNativeObj);

        return ret;
    }
    private static native long do_pruningIndex(long self);
    /**
     * List of features running on this node
     */
    public final java.lang.String [] features() {
        java.lang.String [] ret = do_features(mNativeObj);

        return ret;
    }
    private static native java.lang.String [] do_features(long self);

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
    /*package*/ InfoResponse(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}