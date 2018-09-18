package android.serialport.serial.event;

import android.serialport.uitils.ByteUtil;

public class FeedbackEvent {
    public String command;
    public String appdata;
    public String allData;
    public String devicePath;

    public FeedbackEvent(String command, String appdata, String allData, String devicePath) {
        this.command = command;
        this.appdata = appdata;
        this.allData = allData;
        this.devicePath = devicePath;
    }

    @Override
    public String toString() {
        return "FeedbackEvent{"
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

    public String getTemperature() {
        if ("5B".equalsIgnoreCase(command)) {
            String tt = appdata.substring(2, appdata.length());
            byte[] Tarr = ByteUtil.hexStr2bytes(tt);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Tarr.length; i++) {
                sb.append((char) Tarr[i]);
            }
            return sb.toString();
        }
        return "";
    }
}

