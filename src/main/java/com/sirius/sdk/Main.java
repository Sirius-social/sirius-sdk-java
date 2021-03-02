package com.sirius.sdk;


import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.Hub;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

import java.nio.charset.StandardCharsets;

public class Main {

    public static Pair<String, String> qrCode(Context context) {
        String namespace = "samples";
        String storeId = "qr";
        RetrieveRecordOptions opts = new RetrieveRecordOptions();
        // Сохраняем инфо... о QR в самом Wallet чтобы не генерировать ключ при каждом запуске samples
        context.getNonSecrets().getWalletRecord(namespace, storeId, opts);

        return null;
    }

    public static void main(String[] args) {
        Hub.Config config = new Hub.Config();
        config.serverUri = "https://demo.socialsirius.com";
        config.credentials = "ez8ucxfrTiV1hPX99MHt/JZL1h63sUO9saQCgn2BsaC2EndwDSYpOo6eFpn8xP8ZDoj5B5KN4aaLiyzTqkrbDxrbAe/+2uObPTl6xZdXMBs=".getBytes(StandardCharsets.UTF_8);
        config.p2p = new P2PConnection("B1n1Hwj1USs7z6FAttHCJcqhg7ARe7xtcyfHJCdXoMnC",
                "y7fwmKxfatm6SLN6sqy6LFFjKufgzSsmqA2D4WZz55Y8W7JFeA3LvmicC36E8rdHoAiFhZgSf4fuKmimk9QyBec",
                "5NUzoX1YNm5VXsgzudvVikN7VQpRf5rhaTnPxyu12eZC");

        Context context = new Context();
        context.init(config);

        Message.MessageBuilder builder = new Message.MessageBuilder("","");
        builder.build();
    }
}
