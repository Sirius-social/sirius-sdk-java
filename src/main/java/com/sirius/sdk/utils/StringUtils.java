package com.sirius.sdk.utils;

import org.apache.commons.lang.CharUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringUtils {

    public static final Charset encodeCharset = StandardCharsets.US_ASCII;

    public static byte[] stringToBytes(String string) {
        return string.getBytes(encodeCharset);
    }

    public static String bytesToString(byte[] bytes) {
        return new String(bytes, encodeCharset);
    }

    public static String stringToBase58String(String string) {
        byte[] bytes = stringToBytes(string);
       return bytesToBase58String(bytes);
    }
    public static String bytesToBase58String(byte[] bytes) {
        return Base58.encode(bytes);
    }

    public static String escapeStringLikePython(String string){
        char[] chars =  string.toCharArray();
        StringBuilder escapedString = new StringBuilder();
        for(char charOne: chars){
            if(CharUtils.isAscii(charOne)){
                escapedString.append(charOne);
            }else{
                String escapedStr =   CharUtils.unicodeEscaped(charOne);
                escapedString.append(escapedStr);
            }
        }
        return escapedString.toString();
    }
}
