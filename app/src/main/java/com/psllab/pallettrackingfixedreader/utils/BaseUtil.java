package com.psllab.pallettrackingfixedreader.utils;

public class BaseUtil {

    public static void Hex2Str(byte[] buf, int len, char[] out) {
        char[] hexes = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        for(int i = 0; i < len; ++i) {
            out[i * 2] = hexes[(buf[i] & 255) / 16];
            if (i * 2 + 1 < out.length) {
                out[i * 2 + 1] = hexes[(buf[i] & 255) % 16];
            }
        }
    }
}
