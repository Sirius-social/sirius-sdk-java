// Automatically generated by flapigen
package org.iota.client;


public final class OutputsOptions {
    @Override
    public String toString() {{
        return this.to_string();
    }}

    /**
     * Creates a new instance of output options with default values
     */
    public OutputsOptions() {
        mNativeObj = init();
    }
    private static native long init();

    private final String to_string() {
        String ret = do_to_string(mNativeObj);

        return ret;
    }
    private static native String do_to_string(long self);
    /**
     * Whether the query should include spent outputs or not.
     */
    public final void includeSpent(boolean include_spent) {
        do_includeSpent(mNativeObj, include_spent);
    }
    private static native void do_includeSpent(long self, boolean include_spent);
    /**
     * The output type filter.
     */
    public final void outputType(OutputKind output_type) {
        int a0 = (output_type != null) ? output_type.getValue() : -1;

        do_outputType(mNativeObj, a0);
        java.lang.ref.Reference.reachabilityFence(output_type);
    }
    private static native void do_outputType(long self, int output_type);

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
    /*package*/ OutputsOptions(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}