// Automatically generated by flapigen
package org.iota.client;


public final class GetMessageBuilder {

    private GetMessageBuilder() {}
    /**
     * GET /api/v1/messages?index={Index} endpoint
     * Consume the builder and search for messages matching the index
     * @param index the index string
     */
    public final MessageId [] indexString(String index) {
        MessageId [] ret = do_indexString(mNativeObj, index);

        return ret;
    }
    private static native MessageId [] do_indexString(long self, String index);
    /**
     * GET /api/v1/messages?index={Index} endpoint
     * Consume the builder and search for messages matching the index
     * @param index the index in bytes
     */
    public final MessageId [] indexVec(byte [] index) {
        MessageId [] ret = do_indexVec(mNativeObj, index);

        return ret;
    }
    private static native MessageId [] do_indexVec(long self, byte [] index);
    /**
     * GET /api/v1/messages/{messageID} endpoint
     * Consume the builder and find a message by its identifer. This method returns the given message object.
     * @param message_id The id of the message to find
     */
    public final Message data(MessageId message_id) {
        long a0 = message_id.mNativeObj;
        message_id.mNativeObj = 0;

        long ret = do_data(mNativeObj, a0);
        Message convRet = new Message(InternalPointerMarker.RAW_PTR, ret);
        java.lang.ref.Reference.reachabilityFence(message_id);

        return convRet;
    }
    private static native long do_data(long self, long message_id);
    /**
     * GET /api/v1/messages/{messageID}/metadata endpoint
     * Consume the builder and find a message by its identifer. This method returns the given message metadata.
     * @param message_id The id of the message to find
     */
    public final MessageMetadata metadata(MessageId message_id) {
        long a0 = message_id.mNativeObj;
        message_id.mNativeObj = 0;

        long ret = do_metadata(mNativeObj, a0);
        MessageMetadata convRet = new MessageMetadata(InternalPointerMarker.RAW_PTR, ret);
        java.lang.ref.Reference.reachabilityFence(message_id);

        return convRet;
    }
    private static native long do_metadata(long self, long message_id);
    /**
     * GET /api/v1/messages/{messageID}/raw endpoint
     * Consume the builder and find a message by its identifer. This method returns the given message raw data.
     * @param message_id The id of the message to find
     */
    public final String raw(MessageId message_id) {
        long a0 = message_id.mNativeObj;
        message_id.mNativeObj = 0;

        String ret = do_raw(mNativeObj, a0);
        java.lang.ref.Reference.reachabilityFence(message_id);

        return ret;
    }
    private static native String do_raw(long self, long message_id);
    /**
     * GET /api/v1/messages/{messageID}/children endpoint
     * Consume the builder and returns the list of message IDs that reference a message by its identifier.
     * @param message_id The id of the message to find
     */
    public final MessageId [] children(MessageId message_id) {
        long a0 = message_id.mNativeObj;
        message_id.mNativeObj = 0;

        MessageId [] ret = do_children(mNativeObj, a0);
        java.lang.ref.Reference.reachabilityFence(message_id);

        return ret;
    }
    private static native MessageId [] do_children(long self, long message_id);

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
    /*package*/ GetMessageBuilder(InternalPointerMarker marker, long ptr) {
        assert marker == InternalPointerMarker.RAW_PTR;
        this.mNativeObj = ptr;
    }
    /*package*/ long mNativeObj;
}