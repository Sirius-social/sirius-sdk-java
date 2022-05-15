package examples.iota;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;

import java.util.List;

public class IotaPrivateDidDoc extends IotaPublicDidDoc {

    List<String> seedPhrase;

    public IotaPrivateDidDoc(AbstractCrypto crypto) {
        super(crypto);
    }

    public static IotaPrivateDidDoc load(List<String> seedPhrase) {
        return null;
    }
}
