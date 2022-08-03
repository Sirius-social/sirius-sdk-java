// Automatically generated by flapigen
package org.iota.client;

/**
 * Struct containing network and PoW related information
 */
public final class NetworkInfo {
    @Override
    public String toString() {{
        return this.to_string();
    }}

    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj instanceof NetworkInfo)
        equal = ((NetworkInfo)obj).rustEq(this);
        return equal;
    }

    public int hashCode() {
        return (int)mNativeObj;
    }


    private NetworkInfo() {}

    private final String to_string() {
        String ret = do_to_string(mNativeObj);

        return ret;
    }
    private static native String do_to_string(long self);

    private final boolean rustEq(NetworkInfo o) {
        long a0 = o.mNativeObj;
        boolean ret = do_rustEq(mNativeObj, a0);
        java.lang.ref.Reference.reachabilityFence(o);

        return ret;
    }
    private static native boolean do_rustEq(long self, long o);

    public final java.util.Optional<String> network() {
        String ret = do_network(mNativeObj);
        java.util.Optional<String> convRet = java.util.Optional.ofNullable(ret);

        return convRet;
    }
    private static native String do_network(long self);

    public final long networkId() {
        long ret = do_networkId(mNativeObj);

        return ret;
    }
    private static native long do_networkId(long self);

    public final String bech32Hrp() {
        String ret = do_bech32Hrp(mNativeObj);

        return ret;
    }
    private static native String do_bech32Hrp(long self);

    public final double minPowScore() {
        double ret = do_minPowScore(mNativeObj);

        return ret;
    }
    private static native double do_minPowScore(long self);

    public final boolean localPow() {
        boolean ret = do_localPow(mNativeObj);

        return ret;
    }
    private static native boolean do_localPow(long self);

    public final boolean fallbackToLocalPow() {
        boolean ret = do_fallbackToLocalPow(mNativeObj);

        return ret;
    }
    private static native boolean do_fallbackToLocalPow(long self);

    public final long tipsInterval() {
        long ret = do_tipsInterval(mNativeObj);

        return ret;
    }
    private static native long do_tipsInterval(long self);

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
    /*package*/ NetworkInfo(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}