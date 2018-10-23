package cn.dlc.yihongtest.event;

/**
 * @author YoungeTao
 * @Email youngetao@gmail.com
 * @date 2018/10/13 10:34
 * @describe
 */
public class ShowLogEvent {
    private String message;
    public ShowLogEvent(String message){
        this.message=message;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
