// Automatically generated by flapigen
package org.iota.client;


public final class Output {
    @Override
    public String toString() {{
        return this.to_string();
    }}


    private Output() {}

    private final String to_string() {
        String ret = do_to_string(mNativeObj);

        return ret;
    }
    private static native String do_to_string(long self);

    public final OutputKind kind() {
        int ret = do_kind(mNativeObj);
        OutputKind convRet = OutputKind.fromInt(ret);

        return convRet;
    }
    private static native int do_kind(long self);

    public final SignatureLockedSingleOutput asSignatureLockedSingleOutput() {
        long ret = do_asSignatureLockedSingleOutput(mNativeObj);
        SignatureLockedSingleOutput convRet = new SignatureLockedSingleOutput(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_asSignatureLockedSingleOutput(long self);

    public final SignatureLockedDustAllowanceOutput asSignatureLockedDustAllowanceOutput() {
        long ret = do_asSignatureLockedDustAllowanceOutput(mNativeObj);
        SignatureLockedDustAllowanceOutput convRet = new SignatureLockedDustAllowanceOutput(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_asSignatureLockedDustAllowanceOutput(long self);

    public final TreasuryOutput asTreasuryOutput() {
        long ret = do_asTreasuryOutput(mNativeObj);
        TreasuryOutput convRet = new TreasuryOutput(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_asTreasuryOutput(long self);

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
    /*package*/ Output(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}