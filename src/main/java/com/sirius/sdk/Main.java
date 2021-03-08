package com.sirius.sdk;

import com.sirius.sdk.agent.Event;
import com.sirius.sdk.agent.Listener;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.StateMachineInviter;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.model.Endpoint;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.Hub;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {

    static Context context;

    public static Pair<String, String> qrCode() {
        String namespace = "samples";
        String storeId = "qr";
        RetrieveRecordOptions opts = new RetrieveRecordOptions();
        // Сохраняем инфо... о QR в самом Wallet чтобы не генерировать ключ при каждом запуске samples
        String retStr = null;
        try {
            retStr = context.getNonSecrets().getWalletRecord(namespace, storeId, opts);
        } catch (Exception ignored) {

        }
        if (retStr != null) {
            JSONObject ret = new JSONObject(retStr);
            JSONArray vals = new JSONArray(ret.optString("value"));
            String connectionKey = vals.getString(0);
            String qrContent = vals.getString(1);
            String qrUrl = vals.getString(2);
            return new Pair<>(connectionKey, qrUrl);
        } else { // WalletItemNotFound
            // Ключ установки соединения. Аналог Bob Pre-key
            //см. [2.4. Keys] https://signal.org/docs/specifications/x3dh/
            String connectionKey = context.getCrypto().createKey();
            // Теперь сформируем приглашение для других через 0160
            // шаг 1 - определимся какой endpoint мы возьмем, для простоты возьмем endpoint без доп шифрования
            List<Endpoint> endpoints = context.getEndpoints();
            Endpoint myEndpoint = null;
            for (Endpoint e : endpoints) {
                if (e.getRoutingKeys().isEmpty()) {
                    myEndpoint = e;
                    break;
                }
            }
            if (myEndpoint == null)
                return null;
            // шаг 2 - создаем приглашение
            Invitation invitation = Invitation.builder().
                    setLabel("0160 Sample J").
                    setRecipientKeys(Collections.singletonList(connectionKey)).
                    setEndpoint(myEndpoint.getAddress()).
                    build();

            // шаг 3 - согласно Aries-0160 генерируем URL
            String qrContent = invitation.invitationUrl();

            // шаг 4 - создаем QR
            String qrUrl = context.generateQrCode(qrContent);
            if (qrUrl == null)
                return null;
            // Кладем в Wallet для повторного использования
            JSONArray dump = new JSONArray();
            dump.put(connectionKey).put(qrContent).put(qrUrl);
            context.getNonSecrets().addWalletRecord(namespace, storeId, dump.toString());
            return new Pair<>(connectionKey, qrUrl);
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Hub.Config config = new Hub.Config();
        config.serverUri = "https://demo.socialsirius.com";
        config.credentials = "ez8ucxfrTiV1hPX99MHt/JZL1h63sUO9saQCgn2BsaC2EndwDSYpOo6eFpn8xP8ZDoj5B5KN4aaLiyzTqkrbDxrbAe/+2uObPTl6xZdXMBs=".getBytes(StandardCharsets.UTF_8);
        config.p2p = new P2PConnection("B1n1Hwj1USs7z6FAttHCJcqhg7ARe7xtcyfHJCdXoMnC",
                "y7fwmKxfatm6SLN6sqy6LFFjKufgzSsmqA2D4WZz55Y8W7JFeA3LvmicC36E8rdHoAiFhZgSf4fuKmimk9QyBec",
                "5NUzoX1YNm5VXsgzudvVikN7VQpRf5rhaTnPxyu12eZC");

        context = new Context(config);
        Pair<String, String> qrCodeRes = qrCode();
        String connectionKey = qrCodeRes.first;
        String qrUrl = qrCodeRes.second;
        System.out.println("Открой QR код и просканируй в Sirius App: " + qrUrl);
        // Формируем DID - свой идентификатор в контексте relationship и VERKEY - открытый ключ
        Pair<String, String> didVerkey = context.getDid().createAndStoreMyDid(null, "000000000000000000000000000MISHA");
        String myDid = didVerkey.first;
        String myVerkey = didVerkey.second;
        System.out.println("DID: " + myDid);
        System.out.println("Verkey: " + myVerkey);
        // определимся какой endpoint мы возьмем, для простоты возьмем endpoint без доп шифрования
        List<Endpoint> endpoints = context.getEndpoints();
        Endpoint myEndpoint = null;
        for (Endpoint e : endpoints) {
            if (e.getRoutingKeys().isEmpty()) {
                myEndpoint = e;
                break;
            }
        }
        if (myEndpoint == null)
            return;
        // Слушаем запросы
        System.out.println("Слушаем запросы");
        Listener listener = context.subscribe();
        Event event = listener.getOne().get();
        System.out.println("Получено событие");
        // В рамках Samples интересны только запросы 0160 на установку соединения для connection_key нашего QR
        if (event.getRecipientVerkey().equals(connectionKey) && event.message() instanceof ConnRequest) {
            ConnRequest request = (ConnRequest) event.message();
            // Establish connection with Sirius Communicator via standard Aries protocol
            // https://github.com/hyperledger/aries-rfcs/blob/master/features/0160-connection-protocol/README.md#states
            StateMachineInviter sm = new StateMachineInviter(context, new Pairwise.Me(myDid, myVerkey), connectionKey, myEndpoint);
            Pairwise p2p = sm.createConnection(request);
            if (p2p != null) {
                // Ensure pairwise is stored
                context.getPairwiseList().ensureExists(p2p);
                Message hello = Message.builder().
                        setContext("Привет в новый МИР!!!" + (new Date()).toString()).
                        setLocale("ru").
                        build();
                System.out.println("Sending hello");
                context.sendTo(hello, p2p);
                System.out.println("sended");
            }
        }
    }
}
