// Automatically generated by flapigen
package org.iota.client;

/**
 * Represents an input referencing an output.
 */
public final class UtxoInput {
    @Override
    public String toString() {{
        return this.to_string();
    }}

    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj instanceof UtxoInput)
        equal = ((UtxoInput)obj).rustEq(this);
        return equal;
    }

    public int hashCode() {
        return (int)mNativeObj;
    }


    private UtxoInput() {}

    private final String to_string() {
        String ret = do_to_string(mNativeObj);

        return ret;
    }
    private static native String do_to_string(long self);

    private final boolean rustEq(UtxoInput o) {
        long a0 = o.mNativeObj;
        boolean ret = do_rustEq(mNativeObj, a0);
        java.lang.ref.Reference.reachabilityFence(o);

        return ret;
    }
    private static native boolean do_rustEq(long self, long o);
    /**
     * Create a new `UtxoInput`
     * @param id The ouput Id
     * @param index The output Index
     */
    public static UtxoInput from(TransactionId id, int index) {
        long a0 = id.mNativeObj;
        id.mNativeObj = 0;

        long ret = do_from(a0, index);
        UtxoInput convRet = new UtxoInput(InternalPointerMarker.RAW_PTR, ret);
        java.lang.ref.Reference.reachabilityFence(id);

        return convRet;
    }
    private static native long do_from(long id, int index);
    /**
     * Returns the `TransactionId` of the Output.
     */
    public final TransactionId transactionId() {
        long ret = do_transactionId(mNativeObj);
        TransactionId convRet = new TransactionId(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_transactionId(long self);
    /**
     * Returns the index of the Output.
     */
    public final int index() {
        int ret = do_index(mNativeObj);

        return ret;
    }
    private static native int do_index(long self);

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
    /*package*/ UtxoInput(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}