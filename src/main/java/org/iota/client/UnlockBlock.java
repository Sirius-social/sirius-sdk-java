// Automatically generated by flapigen
package org.iota.client;


public final class UnlockBlock {
    @Override
    public String toString() {{
        return this.to_string();
    }}


    private UnlockBlock() {}

    private final String to_string() {
        String ret = do_to_string(mNativeObj);

        return ret;
    }
    private static native String do_to_string(long self);

    public final UnlockBlockKind kind() {
        int ret = do_kind(mNativeObj);
        UnlockBlockKind convRet = UnlockBlockKind.fromInt(ret);

        return convRet;
    }
    private static native int do_kind(long self);

    public final ReferenceUnlock asReference() {
        long ret = do_asReference(mNativeObj);
        ReferenceUnlock convRet = new ReferenceUnlock(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_asReference(long self);

    public final SignatureUnlock asSignature() {
        long ret = do_asSignature(mNativeObj);
        SignatureUnlock convRet = new SignatureUnlock(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_asSignature(long self);

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
    /*package*/ UnlockBlock(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}