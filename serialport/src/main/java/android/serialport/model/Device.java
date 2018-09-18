package android.serialport.model;

/**
 * 页面:刘华乾  on  2018/1/16.
 * 对接口:
 * 作用:
 */

public class Device {
    private String path;
    private String baudrate;

    public Device() {
    }

    public Device(String path, String baudrate) {
        this.path = path;
        this.baudrate = baudrate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(String baudrate) {
        this.baudrate = baudrate;
    }

    @Override
    public String toString() {
        return "Device{" + "path='" + path + '\'' + ", baudrate='" + baudrate + '\'' + '}';
    }
}
