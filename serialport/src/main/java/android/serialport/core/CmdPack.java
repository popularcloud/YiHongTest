package android.serialport.core;

import android.serialport.uitils.ByteUtil;
import android.serialport.uitils.CRC16Utils;
import android.serialport.uitils.StringUtil;
import android.text.TextUtils;

/**
 * 页面:刘华乾  on  2018/1/16.
 * 对接口:
 * 作用:封装的发送命令数据包
 */

public class CmdPack {

    private String mCmd;

    public CmdPack(String cmd) {
        mCmd = cmd;
    }

    public String getPackHexStr() {
        return mCmd;
    }

    /**
     * 把命令打包成字节数组
     *
     * @reurn
     */
    public byte[] pack() {
        return ByteUtil.hexStr2bytes(mCmd);
    }
}
