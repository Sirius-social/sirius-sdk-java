// Automatically generated by flapigen
package org.iota.client;


public final class GetAddressesBuilder {

    private GetAddressesBuilder() {}
    /**
     * Construct a new addressbuilder with a seed. Invalid seeds throw an error
     */
    public static GetAddressesBuilder from(String seed) {
        long ret = do_from(seed);
        GetAddressesBuilder convRet = new GetAddressesBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_from(String seed);
    /**
     * DEBUG DO NOT USE
     */
    public static GetAddressesBuilder fromOld(String seed) {
        long ret = do_fromOld(seed);
        GetAddressesBuilder convRet = new GetAddressesBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_fromOld(String seed);
    /**
     * Set the account index
     */
    public final GetAddressesBuilder withAccountIndex(long account_index) {
        long ret = do_withAccountIndex(mNativeObj, account_index);
        GetAddressesBuilder convRet = new GetAddressesBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withAccountIndex(long self, long account_index);
    /**
     * Set range to the builder
     */
    public final GetAddressesBuilder withRange(long start, long end) {
        long ret = do_withRange(mNativeObj, start, end);
        GetAddressesBuilder convRet = new GetAddressesBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withRange(long self, long start, long end);
    /**
     * Set bech32 human readable part (hrp)
     */
    public final GetAddressesBuilder withBech32Hrp(String bech32_hrp) {
        long ret = do_withBech32Hrp(mNativeObj, bech32_hrp);
        GetAddressesBuilder convRet = new GetAddressesBuilder(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_withBech32Hrp(long self, String bech32_hrp);
    /**
     * Set client to the builder
     */
    public final GetAddressesBuilder withClient(Client client) {
        long a0 = client.mNativeObj;
        long ret = do_withClient(mNativeObj, a0);
        GetAddressesBuilder convRet = new GetAddressesBuilder(InternalPointerMarker.RAW_PTR, ret);
        java.lang.ref.Reference.reachabilityFence(client);

        return convRet;
    }
    private static native long do_withClient(long self, long client);
    /**
     * Consume the builder and get a vector of public addresses bech32 encoded
     */
    public final java.lang.String [] finish() {
        java.lang.String [] ret = do_finish(mNativeObj);

        return ret;
    }
    private static native java.lang.String [] do_finish(long self);

    public final AddressStringPublicWrapper [] getAll() {
        AddressStringPublicWrapper [] ret = do_getAll(mNativeObj);

        return ret;
    }
    private static native AddressStringPublicWrapper [] do_getAll(long self);
    /**
     * Consume the builder and get the vector of public and internal addresses
     */
    public final AddressPublicWrapper [] getAllRaw() {
        AddressPublicWrapper [] ret = do_getAllRaw(mNativeObj);

        return ret;
    }
    private static native AddressPublicWrapper [] do_getAllRaw(long self);

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
    /*package*/ GetAddressesBuilder(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}