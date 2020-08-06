/*
 * Copyright (c) Terl Tech Ltd • 14/06/19 17:54 • goterl.com
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.goterl.lazycode.lazysodium.utils;

import com.goterl.lazycode.lazysodium.LazySodium;


public class DetachedEncrypt extends Detached {

    byte[] cipher;

    public DetachedEncrypt(byte[] cipher, byte[] mac) {
        super(mac);
        this.cipher = cipher;
    }

    public byte[] getCipher() {
        return cipher;
    }

    public String getCipherString() {
        return LazySodium.toHex(getCipher());
    }


}
