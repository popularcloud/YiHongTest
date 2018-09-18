package android.serialport.model;

/**
 * 页面:刘华乾  on  2018/1/17.
 * 对接口:
 * 作用:
 */

public class Commands {
    /**
     * 设备登录请求0901
     */
    public static final int CMD_LOGIN = 0x0901;
    /**
     * 设备向上位机(安卓板)发送心跳包
     */
    public static final int CMD_DEVICE_SEND_HEARTBEAT = 0x0902;
    /**
     * 设备向上位机(安卓板)发送心跳包
     */
    public static final int CMD_OPEN_THE_DOOR = 0x0A06;
    /**
     * 设备向上位机(安卓板)发送纪录
     */
    public static final int CMD_OPEN_RECORD = 0x0903;

    /**
     * 检测comm口
     */
    public static final String BAUDRATESTR = "115200";
    public static final int BAUDRATESTR_19200 = 19200;
    public static final String devicePathzero = "/dev/ttyS0";
    public static final String devicePathOne = "/dev/ttyS1";
    public static final String devicePathTwo = "/dev/ttyS2";
    public static final String devicePathThree = "/dev/ttyS3";
    public static final String devicePathUSB0 = "/dev/ttyUSB0";
    public static final String devicePathUSB1 = "/dev/ttyUSB1";
    public static final String devicePathUSB2 = "/dev/ttyUSB2";
    public static final String devicePathUSB3 = "/dev/ttyUSB3";
    public static final String devicePathUSB4 = "/dev/ttyUSB4";
    public static String devicePathOneType = "0";
    public static String devicePathTwoType = "0";
    public static String devicePathThreeType = "0";

    //帧头：2字节，固定为0x3B、0xB3
    //数据帧类型：安卓板发送固定为0x00, 控制板发送固定为0x01
    //数据长度：1字节，包括命令码、协议版本和数据域的长度。
    //命令码：1字节
    //协议版本：1字节，表示当前的协议版本，起始版本为0x10
    //数据：0-N字节，N小于等于512
    //校验和：1字节, 异或校验，为本帧数据包括帧头，数据长度，命令码，协议版本，数据在内的全部数据做异或校验。
    /**
     * 开锁数据头
    * */
    public static final String FH_ANDROID = "3BB300";
    /**
     *rfid数据头
     * */
    public static final String RFID_HEAD = "434D";

    public static final byte[] CMD_OPEN = { 0x3B, (byte) 0xB3, 0x00, 0x02, (byte) 0xA5, 0x10, };
}
