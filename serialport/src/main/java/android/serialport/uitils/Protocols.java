package android.serialport.uitils;

/**
 * 页面:刘华乾  on  2018/1/16.
 * 对接口:
 * 作用:
 */

public class Protocols {
    public static final byte[] FRAME_HEAD = { 0x3B, (byte) 0xB3, 0x01 };

    public static final byte FRAME_HEAD_0 = FRAME_HEAD[0];
    public static final byte FRAME_HEAD_1 = FRAME_HEAD[1];
    public static final byte FRAME_HEAD_2 = FRAME_HEAD[2];
    //帧头2	数据帧类型1  数据长度(1字节，包括命令码、协议版本和数据域的长度)    命令码1     协议版本1	数据(0-512)	校验和1 {一一对应数据长度0}
    public static final int MIN_SIZE = 2 + 1 + 1 + 1 + 1 + 1;
    public static final int RF_MIN_SIZE = 2 + 1 + 1 + 1 + 1 + 1 + 1;
    public static final byte RF_HEAD_0 = 0x43;
    public static final byte RF_HEAD_1 = 0x4D;
}
