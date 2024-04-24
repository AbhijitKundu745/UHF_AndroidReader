package com.psllab.pallettrackingfixedreader.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BaseUtils {
    private static final ByteBuffer buffer = ByteBuffer.allocate(8);

    public static void Hex2Str(byte[] buf, int len, char[] out) {
        char[] hexes = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        for (int i = 0; i < len; ++i) {
            out[i * 2] = hexes[(buf[i] & 255) / 16];
            if (i * 2 + 1 < out.length) {
                out[i * 2 + 1] = hexes[(buf[i] & 255) % 16];
            }
        }
    }

    /**
     * hex字符串转byte数组
     *
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte数组结果
     */
    public static byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            //奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            //偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }


    /**
     * Hex字符串转byte
     *
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte
     */
    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }


    /**
     * 字节数组转16进制
     *
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * @param x long值
     * @return byte数组
     */
    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    /**
     * @param bytes byte数组
     * @return long
     */
    public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        return buffer.getLong();
    }

    /**
     * @param antennaEnable 天线是否启用的long值
     * @return 天线数组
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static int[] longToAntInt(long antennaEnable) {
        byte[] bytes = longToBytes(antennaEnable);
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == (byte) 1) {
                arrayList.add(i + 1);
            }
        }
        return arrayList.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * 1为启用，0为不启用，int从右到左的八位对应天线从一到八
     * 如111表示启用第1,2,3根天线
     *
     * @param antennaEnable 天线是否启用的byte值
     * @return 天线数组
     */
    public static int[] byteToAntInt(int antennaEnable) {
        int[] array = new int[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (antennaEnable & 1);
            antennaEnable = (byte) (antennaEnable >> 1);
        }
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 1) {
                arrayList.add(8 - i);
            }
        }
        return arrayList.stream().mapToInt(Integer::intValue).toArray();
    }
}
