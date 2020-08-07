/*
 * Copyright (c) Terl Tech Ltd • 14/06/19 17:54 • goterl.com
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.goterl.lazycode.lazysodium.utils;

import com.sun.jna.NativeLong;

public class BaseChecker {

    public static boolean isBetween(long num, long min, long max) {
        return min <= num && num <= max;
    }

    public static boolean isBetween(NativeLong num, NativeLong min, NativeLong max) {
        long number = num.longValue();
        return min.longValue() <= number && number <= max.longValue();
    }

    public static boolean correctLen(long num, long len) {
        return num == len;
    }

}
