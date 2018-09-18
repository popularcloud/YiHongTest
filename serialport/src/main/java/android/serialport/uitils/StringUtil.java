package android.serialport.uitils;

import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * 页面:刘华乾  on  2018/1/16.
 * 对接口:
 * 作用:
 */

public class StringUtil {
    /**
     * 获取指定位数的String字符串
     *
     * @param msg 原字符串
     * @param require 要求的位数
     * @param characer 不够位数的替代字符
     * @return
     */
    public static String getStringFitBits(String msg, int require, Object characer) {
        StringBuilder stringBuilder = new StringBuilder(msg);
        while (stringBuilder.length() < require) {
            stringBuilder.insert(0, characer);
        }
        return stringBuilder.toString();
    }

    /**
     * 拼接字符串
     *
     * @param arg
     * @param args
     * @return 拼接完成的字符串
     */
    public static String join(String arg, String... args) {
        StringBuffer buffer = new StringBuffer(arg);
        for (String s : args) {
            buffer.append(s);
        }
        return buffer.toString();
    }

    public static String join(Object arg, Object... args) {
        StringBuffer buffer = new StringBuffer(arg.toString());
        for (Object s : args) {
            buffer.append(s.toString());
        }
        return buffer.toString();
    }

    /**
     * 截取目标数组指定长度的的数据
     *
     * @param data 目标数组
     * @param i 截取起始位置
     * @param length 截取长度
     * @return 截取到的数据
     */
    public static byte[] arrayCopy(byte[] data, int i, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(data, i, temp, 0, length);
        return temp;
    }

    public static byte[] arrayCopy(byte[] data, byte[] data2) {
        byte[] temp = new byte[data.length + data2.length];
        System.arraycopy(data, 0, temp, 0, data.length);
        System.arraycopy(data2, data.length, temp, 0, data2.length);
        return temp;
    }

    public static byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static String timenFormat(long l){
        SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
       return format.format(l);
    }

}

