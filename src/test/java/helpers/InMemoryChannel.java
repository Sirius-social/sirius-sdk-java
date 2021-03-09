package helpers;

import com.sirius.sdk.base.ReadOnlyChannel;
import com.sirius.sdk.base.WriteOnlyChannel;

import java.util.concurrent.CompletableFuture;

public class InMemoryChannel implements ReadOnlyChannel, WriteOnlyChannel {

    public InMemoryChannel() {
    }

    CompletableFuture<byte[]> cf = new CompletableFuture<>();

    @Override
    public CompletableFuture<byte[]> read() {
        return cf;
    }

    @Override
    public boolean write(byte[] data) {
        if (cf.isDone())
            cf = new CompletableFuture<>();
        cf.complete(data);
        return true;
    }
}


