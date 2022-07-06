// Automatically generated by flapigen
package org.iota.client;


public final class MigratedFundsEntryDto {
    @Override
    public String toString() {{
        return this.to_string();
    }}


    private MigratedFundsEntryDto() {}

    private final String to_string() {
        String ret = do_to_string(mNativeObj);

        return ret;
    }
    private static native String do_to_string(long self);
    /**
     * The tail transaction hash
     */
    public final String tailTransactionHash() {
        String ret = do_tailTransactionHash(mNativeObj);

        return ret;
    }
    private static native String do_tailTransactionHash(long self);
    /**
     * The address this was deposited to
     */
    public final AddressDto address() {
        long ret = do_address(mNativeObj);
        AddressDto convRet = new AddressDto(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_address(long self);
    /**
     * The amount that was deposited
     */
    public final long deposit() {
        long ret = do_deposit(mNativeObj);

        return ret;
    }
    private static native long do_deposit(long self);

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
    /*package*/ MigratedFundsEntryDto(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}