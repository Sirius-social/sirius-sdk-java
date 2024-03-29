// Automatically generated by flapigen
package org.iota.client;


public final class Address {
    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj instanceof Address)
        equal = ((Address)obj).rustEq(this);
        return equal;
    }

    public int hashCode() {
        return (int)mNativeObj;
    }

    @Override
    public String toString() {{
        return this.to_string();
    }}


    private Address() {}

    private final String to_string() {
        String ret = do_to_string(mNativeObj);

        return ret;
    }
    private static native String do_to_string(long self);

    private final boolean rustEq(Address o) {
        long a0 = o.mNativeObj;
        boolean ret = do_rustEq(mNativeObj, a0);
        java.lang.ref.Reference.reachabilityFence(o);

        return ret;
    }
    private static native boolean do_rustEq(long self, long o);
    /**
     * Tries to create an `Address` from a Bech32 encoded string.
     */
    public static Address tryFromBech32(String addr) {
        long ret = do_tryFromBech32(addr);
        Address convRet = new Address(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_tryFromBech32(String addr);

    public final String toBech32(String hrp) {
        String ret = do_toBech32(mNativeObj, hrp);

        return ret;
    }
    private static native String do_toBech32(long self, String hrp);

    public final void verify(byte [] msg, SignatureUnlock signature) {
        long a1 = signature.mNativeObj;
        signature.mNativeObj = 0;

        do_verify(mNativeObj, msg, a1);
        java.lang.ref.Reference.reachabilityFence(signature);
    }
    private static native void do_verify(long self, byte [] msg, long signature);

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
    /*package*/ Address(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}