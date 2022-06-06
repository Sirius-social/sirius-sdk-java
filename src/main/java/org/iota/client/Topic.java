// Automatically generated by flapigen
package org.iota.client;


public final class Topic {
    @Override
    public String toString() {{
        return this.to_string();
    }}


    private Topic() {}
    /**
     * Creates a new topic and checks if it's valid.
     */
    public static Topic from(String topic) {
        long ret = do_from(topic);
        Topic convRet = new Topic(InternalPointerMarker.RAW_PTR, ret);

        return convRet;
    }
    private static native long do_from(String topic);

    private final String to_string() {
        String ret = do_to_string(mNativeObj);

        return ret;
    }
    private static native String do_to_string(long self);

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
    /*package*/ Topic(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}