package android.serialport.serial;

import android.serialport.serial.event.FeedbackEvent;
import com.licheedev.myutils.LogPlus;
import org.greenrobot.eventbus.EventBus;

/**
 * 页面:刘华乾  on  2018/1/16.
 * 对接口:
 * 作用:分发收到的消息
 */

public class DataDispatcher {

    public void dispatch(String command, String appdata, String allData, String devicePath) {
        /*LogPlus.e("command="
            + command
            + "..."
            + "收到appdata: "
            + appdata
            + "....alldata"
            + "..."
            + allData
            + "..."
            + devicePath);*/
        EventBus.getDefault().post(new FeedbackEvent(command,appdata,allData,devicePath));
        switch (command) {
            //开门返回
            case "A5":

                break;

            case "A6":
                /**
                 * 控制板控制灯应答命令
                 */
                // openTheDoorResults(appdata, devicePath);

                break;
            case "A7":
                /**
                 * 控制板语音应答命令
                 */
                // recvDeviceSendHeartbeat(command, appdata, allData, devicePath);
                break;
            case "5B":
                /**
                 * 安卓板获得控制板状态命令
                 * */
                // record(allData, devicePath);
                break;
        }
    }
}
