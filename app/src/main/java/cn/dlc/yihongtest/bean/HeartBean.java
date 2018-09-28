package cn.dlc.yihongtest.bean;

/**
 * @author YoungeTao
 * @Email youngetao@gmail.com
 * @date 2018/9/3 15:54
 * @describe 心跳包对象
 */
public class HeartBean {
    String macno;
    String version;
    String update;
    int doorStatus;

    public HeartBean(String macno){
        setMacno(macno);
    }

    public String getMacno() {
        return macno;
    }

    public void setMacno(String macno) {
        this.macno = macno;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public int getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(int doorStatus) {
        this.doorStatus = doorStatus;
    }
}
