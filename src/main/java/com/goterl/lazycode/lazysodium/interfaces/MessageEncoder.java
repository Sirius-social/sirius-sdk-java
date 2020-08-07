/*
 * Copyright (c) Terl Tech Ltd • 03/04/2020, 23:19 • goterl.com
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.goterl.lazycode.lazysodium.interfaces;

public interface MessageEncoder {
    String encode(byte[] cipher);
    byte[] decode(String cipherText);
}
