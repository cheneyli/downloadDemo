package com.cheney.filedownexample.utils;

import java.security.MessageDigest;

/**
 * Created by cheney on 2017/5/20.
 */

public class Utils {
    private static final char[] HexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getMD5(String str) {
        String resultString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // md.digest() 该函数返回值为存放哈希值结果的byte数组
            resultString = byteArrayToHexString(md.digest(str
                    .getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString;
    }

    public static String charArrayToHexString(char[] chars) {
        if (chars == null) {
            return null;
        }

        byte[] bytes = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            byte high = (byte) ((c >> 8) & 0x00ff);
            byte low = (byte) (c & 0x00ff);
            bytes[i * 2] = high;
            bytes[i * 2 + 1] = low;
        }
        return byteArrayToHexString(bytes);
    }

    public static String byteArrayToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(HexDigits[(b >> 4) & 0x0f]);
            sb.append(HexDigits[b & 0x0f]);
        }
        return sb.toString();
    }
}
