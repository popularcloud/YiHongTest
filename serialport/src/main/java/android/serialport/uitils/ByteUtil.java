package android.serialport.uitils;

import android.os.SystemClock;
import android.serialport.model.Commands;
import android.support.annotation.Nullable;
import com.licheedev.myutils.LogPlus;
import java.io.UnsupportedEncodingException;

/**
 * 页面:刘华乾  on  2018/1/16.
 * 对接口:
 * 作用:
 */

public class ByteUtil {
    private static String hexStr = "0123456789ABCDEF";
    private static String[] binaryArray = {
        "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010",
        "1011", "1100", "1101", "1110", "1111"
    };

    /**
     * 字节数组转换成对应的16进制表示的字符串
     *
     * @param src
     * @return
     */
    public static String bytes2HexStr(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return "";
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            builder.append(buffer);
        }
        return builder.toString().toUpperCase();
    }

    /**
     * 十六进制字节数组转字符串
     *
     * @param src 目标数组
     * @param dec 起始位置8
     * @param length 长度2   SLEN
     * @return
     */
    public static String bytes2HexStr(byte[] src, int dec, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(src, dec, temp, 0, length);
        return bytes2HexStr(temp);
    }

    /**
     * 数组拷贝
     *
     * @param src 目标数组
     * @param dec 起始位置8
     * @param length 长度2   SLEN
     * @return
     */
    public static byte[] bytesToArr(byte[] src, int dec, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(src, dec, temp, 0, length);
        return temp;
    }

    /**
     * 16进制字符串转数字
     *
     * @param hex
     * @return
     */
    public static long hexStr2decimal(String hex) {
        return Long.parseLong(hex, 16);
    }

    /**
     * 16进制字符串转10进制字符串,并补全空位
     *
     * @param hex
     * @return
     */
    public static String hexStr2decimalString(String hex, int strLength) {
        String decimalStr = String.valueOf(Long.parseLong(hex, 16));
        StringBuilder stringBuilder = new StringBuilder(decimalStr);
        while (stringBuilder.length() < strLength) {
            stringBuilder.insert(0, '0');
        }
        return stringBuilder.toString();
    }

    /**
     * 把十进制数字转换成足位的十六进制字符串,并补全空位
     *
     * @param num
     * @return
     */
    public static String decimal2fitHex(long num) {
        String hex = Long.toHexString(num).toUpperCase();
        if (hex.length() % 2 != 0) {
            return "0" + hex;
        }
        return hex.toUpperCase();
    }

    /**
     * 把十进制数字转换成足位的十六进制字符串,并补全空位
     *
     * @param num
     * @param strLength 字符串的长度
     * @return
     */
    public static String decimal2fitHex(long num, int strLength) {
        String hexStr = Long.toHexString(num).toUpperCase();
        StringBuilder stringBuilder = new StringBuilder(hexStr);
        while (stringBuilder.length() < strLength) {
            stringBuilder.insert(0, '0');
        }
        return stringBuilder.toString();
    }

    public static void timeTest() {
        long millis = SystemClock.uptimeMillis();
        LogPlus.i("timeTest" + "longTime: " + millis);
        String hex = ByteUtil.decimal2fitHex(millis, 8);
        System.out.println("timeTest: " + "hexTime: " + hex);
    }

    /**
     * \
     * 转16进制字符串
     *
     * @param str
     * @return
     */
    public static String hexString(String str, int length) {
        String ret = "";
        byte[] b;
        try {
            b = str.getBytes("utf-8");
            for (int i = b.length - 1; i >= 0; i--) {
                String hex = Integer.toHexString(b[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                ret += hex.toUpperCase();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        while (ret.length() < length) {
            ret = "0" + ret;
        }
        return ret;
    }

    public static String fitDecimalStr(int dicimal, int strLength) {
        StringBuilder builder = new StringBuilder(String.valueOf(dicimal));
        while (builder.length() < strLength) {
            builder.insert(0, "0");
        }
        return builder.toString();
    }

    /**
     * 字符串转十六进制字符串
     *
     * @param str
     * @return
     */
    public static String str2HexString(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder();
        byte[] bs = null;
        try {

            bs = str.getBytes("utf8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString();
    }

    /**
     * 把十六进制表示的字节数组字符串，转换成十六进制字节数组
     *
     * @param
     * @return byte[]
     */
    public static byte[] hexStr2bytes(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toUpperCase().toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (hexChar2byte(achar[pos]) << 4 | hexChar2byte(achar[pos + 1]));
        }
        return result;
    }

    /**
     * 把16进制字符[0123456789abcde]（含大小写）转成字节
     *
     * @param c
     * @return
     */
    private static int hexChar2byte(char c) {
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'a':
            case 'A':
                return 10;
            case 'b':
            case 'B':
                return 11;
            case 'c':
            case 'C':
                return 12;
            case 'd':
            case 'D':
                return 13;
            case 'e':
            case 'E':
                return 14;
            case 'f':
            case 'F':
                return 15;
            default:
                return -1;
        }
    }

    /**
     * @param hexString
     * @return 将十六进制转换为二进制字节数组   16-2
     */
    public static String hexStr2BinArr(String hexString) {
        //hexString的长度对2取整，作为bytes的长度  
        int len = hexString.length() / 2;
        byte[] bytes = new byte[len];
        byte high = 0;//字节高四位  
        byte low = 0;//字节低四位  
        for (int i = 0; i < len; i++) {
            //右移四位得到高位  
            high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
            low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
            bytes[i] = (byte) (high | low);//高地位做或运算  
        }
        return bytes2BinStr(bytes);
        //return new BigInteger(1, bytes).toString(2);// 这里的1代表正数
    }

    /**
     * @param
     * @return 二进制数组转换为二进制字符串   2-2
     */
    public static String bytes2BinStr(byte[] bArray) {

        String outStr = "";
        int pos = 0;
        for (byte b : bArray) {
            //高四位  
            pos = (b & 0xF0) >> 4;
            outStr += binaryArray[pos];
            //低四位  
            pos = b & 0x0F;
            outStr += binaryArray[pos];
        }
        return outStr;
    }

    /**
     * @param cmd 命令
     * @param data 数据
     * @return 除了数据头和校验位的 数据字符串
     */
    public static String cmdAndData(String cmd, @Nullable String data) {
        int length = 0;
        if (data == null) data = "";

        length += (cmd.length() / 2 + data.length() / 2 + 1);
        String s = Integer.toHexString(length);
        if (s.length() % 2 == 1) s = "0" + s;
        return s + cmd + "10" + data;
    }

    /**
     * 获取 命令   异或校验和
     *
     * @param cmdData
     * @return
     */
    public static String command(String cmdData) {
        String jy_str = Commands.FH_ANDROID + cmdData;
        LogPlus.w(jy_str.length() + "");
        if (jy_str.length() % 2 != 0) return "";
        int jy = 0;
        for (int i = 0; i < jy_str.length() / 2; i++) {
            String sub = jy_str.substring(i * 2, 2 * (i + 1));
            int anInt = Integer.parseInt(sub, 16);
            if (i == 0) {
                jy = anInt;
            } else {
                jy = jy ^ anInt;
            }
        }
        String s = Integer.toHexString(jy);
        s = s.toUpperCase();
        if (s.length() % 2 == 1) s = "0" + s;
        s = Commands.FH_ANDROID + cmdData + s;
        return s;
    }

    /**
     * 数组 异或校验
     *
     * @param arr
     * @return
     */
    public static String verify(byte[] arr) {
        byte verify = 0;
        for (int i = 0; i < arr.length; i++) {
            verify = (byte) (verify ^ arr[i]);
        }
        byte[] jy_arr = { verify };
        String s = bytes2HexStr(jy_arr);
        return s;
    }

    /**
     * @param cmd 命令码一个字节
     * @param data 数据
     * @return 完整命令字符串
     */
    public static String RFIDcommand(String cmd, @Nullable String data) {
        if (data == null) {
            data = "";
        } else if (data.length() % 2 != 0) {
            return "";
        }

        int length = data.length() / 2;

        String length_str = Integer.toHexString(length);
        switch (length_str.length() % 4) {
            case 1:
                length_str = "000" + length_str;
                break;
            case 2:
                length_str = "00" + length_str;
                break;
            case 3:
                length_str = "0" + length_str;
        }
        length_str = length_str.substring(2, 4) + length_str.substring(0, 2);

        int jy = 0;
        for (int i = 0; i < data.length() / 2; i++) {
            String sub = data.substring(i * 2, 2 * (i + 1));
            int anInt = Integer.parseInt(sub, 16);
            jy = jy ^ anInt;
        }
        String jy_str = Integer.toHexString(jy);
        jy_str = jy_str.toUpperCase();
        if (jy_str.length() % 2 == 1) jy_str = "0" + jy_str;
        return Commands.RFID_HEAD + cmd + "00" + length_str + data + jy_str;
    }
}

