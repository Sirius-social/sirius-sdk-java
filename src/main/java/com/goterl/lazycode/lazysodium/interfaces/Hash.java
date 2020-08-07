/*
 * Copyright (c) Terl Tech Ltd • 14/06/19 17:54 • goterl.com
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.goterl.lazycode.lazysodium.interfaces;


import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public interface Hash {

    int SHA256_BYTES = 32,
        SHA512_BYTES = 64,
        BYTES = SHA512_BYTES;

    interface Native {

        boolean cryptoHashSha256(byte[] out, byte[] in, long inLen);

        boolean cryptoHashSha512(byte[] out, byte[] in, long inLen);


        boolean cryptoHashSha256Init(State256 state);

        boolean cryptoHashSha256Update(State256 state,
                                       byte[] in,
                                       long inLen);

        boolean cryptoHashSha256Final(State256 state, byte[] out);


        boolean cryptoHashSha512Init(State512 state);

        boolean cryptoHashSha512Update(State512 state,
                                       byte[] in,
                                       long inLen);

        boolean cryptoHashSha512Final(State512 state, byte[] out);

    }

    interface Lazy {

        String cryptoHashSha256(String message) throws SodiumException;

        String cryptoHashSha512(String message) throws SodiumException;

        boolean cryptoHashSha256Init(State256 state);

        boolean cryptoHashSha256Update(State256 state, String messagePart);

        String cryptoHashSha256Final(State256 state) throws SodiumException;

        boolean cryptoHashSha512Init(State512 state);

        boolean cryptoHashSha512Update(State512 state, String messagePart);

        String cryptoHashSha512Final(State512 state) throws SodiumException;

    }


    class State256 extends Structure {

        public static class ByReference extends State256 implements Structure.ByReference { }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("state", "count", "buf");
        }

        public long[] state = new long[8];
        public long count;
        public byte[] buf = new byte[64];

    }

    class State512 extends Structure {

        public static class ByReference extends State512 implements Structure.ByReference { }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("state", "count", "buf");
        }

        public long[] state = new long[8];
        public long[] count = new long[2];
        public byte[] buf = new byte[128];

    }

}
