package helpers;

import com.sirius.sdk.hub.MobileContext;
import com.sirius.sdk.hub.MobileHub;

public class MobileNWiseClient extends AbstractNWiseClient {

    public MobileNWiseClient(MobileHub.Config config, String nickname) {
        this.nickname = nickname;
        context = new MobileContext(config);
        routine();
    }
}
