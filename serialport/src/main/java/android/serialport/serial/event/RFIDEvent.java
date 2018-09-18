package android.serialport.serial.event;

/**
 * 创建日期：2018/3/5 on 18:12
 * 描述:
 * 作者:luojianping
 */

public class RFIDEvent {
    public String command;
    public String appdata;
    public String allData;
    public String devicePath;

    public RFIDEvent(String command, String appdata, String allData, String devicePath) {
        this.command = command;
        this.appdata = appdata;
        this.allData = allData;
        this.devicePath = devicePath;
    }

    @Override
    public String toString() {
        return "RFIDEvent{"
            + "command='"
            + command
            + '\''
            + ", appdata='"
            + appdata
            + '\''
            + ", allData='"
            + allData
            + '\''
            + ", devicePath='"
            + devicePath
            + '\''
            + '}';
    }
}
