package helpers;

import com.sirius.sdk.base.ReadOnlyChannel;
import com.sirius.sdk.base.WriteOnlyChannel;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.util.concurrent.CompletableFuture;

public class InMemoryChannel implements ReadOnlyChannel, WriteOnlyChannel {

    public InMemoryChannel() {
    }

    PublishSubject<byte[]> publishSubject = PublishSubject.create();

    @Override
    public boolean write(byte[] data) {
        publishSubject.onNext(data);
        return true;
    }

    @Override
    public Observable<byte[]> listen() {
        return publishSubject;
    }
}


