package helpers;

import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.CloudHub;

public class CloudNWiseClient extends AbstractNWiseClient {

    public CloudNWiseClient(CloudHub.Config config, String nickname) {
        this.nickname = nickname;
        context = new CloudContext(config);
        routine();
    }
}
