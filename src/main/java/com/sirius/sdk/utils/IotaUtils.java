package com.sirius.sdk.utils;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.sirius.sdk.naclJava.LibSodium;
import org.bitcoinj.core.Base58;
import org.iota.client.Client;
import org.iota.client.Message;
import org.iota.client.MessageId;
import org.iota.client.MessageMetadata;
import org.scijava.nativelib.NativeLoader;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IotaUtils {

    static {
        try {
            NativeLoader.loadLibrary("iota_client");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final String MAINNET = "https://chrysalis-nodes.iota.cafe:443";
    public static final String TESTNET = "https://api.lb-0.h.chrysalis-devnet.iota.cafe";

    public static String iotaNetwork = MAINNET;

    public static Client node = null;

    public static Client node() {
        if (node == null)
            node = Client.Builder().withNode(iotaNetwork).finish();
        return node;
    }

    public static Comparator<Message> msgComparator = new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            MessageMetadata meta1 = node().getMessage().metadata(o1.id());
            MessageMetadata meta2 = node().getMessage().metadata(o2.id());
            if (meta1.milestoneIndex() < meta2.milestoneIndex())
                return -1;
            else if (meta1.milestoneIndex() > meta2.milestoneIndex())
                return 1;
            else
                return o1.id().toString().compareTo(o2.id().toString());
        }
    };

    public static String generateTag(byte[] key) {
        LazySodiumJava s = LibSodium.getInstance().getLazySodium();
        byte[] outputBytes = new byte[32];
        s.cryptoGenericHash(outputBytes, 32, key, key.length, null, 0);
        return Base58.encode(outputBytes);
    }

    public static Map<MessageId, List<Message>> messages = new HashMap<>();
    public static Message getMessage(MessageId id) {
        if (messages.containsKey(id))
            return messages.get(id).get(0);
        Message msg = IotaUtils.node().getMessage().data(id);
        //messages.put(id, msg);
        return msg;
    }
}
