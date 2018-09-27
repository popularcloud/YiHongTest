package cn.dlc.yihongtest.ui;

import android.os.Bundle;
import android.serialport.core.CmdPack;
import android.serialport.model.Commands;
import android.serialport.serial.event.FeedbackEvent;
import android.serialport.uitils.ByteUtil;
import android.serialport.uitils.HfData;
import android.serialport.uitils.SerialPortManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.dlc.yihongtest.R;
import cn.dlc.yihongtest.base.App;
import cn.dlc.yihongtest.bean.HeartBean;
import cn.dlc.yihongtest.util.GsonUtil;
import cn.dlc.yihongtest.util.LogUtil;
import cn.dlc.yihongtest.util.MqttManager;
import cn.dlc.yihongtest.util.RxTimerUtil;
import cn.dlc.yihongtest.util.WaitingDailogUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity implements MqttCallback{

    @BindView(R.id.sp_lock_devicePath)
    Spinner sp_lock_devicePath;
    @BindView(R.id.sp_rfid_devicePath)
    Spinner sp_rfid_devicePath;
    @BindView(R.id.btn_reset)
    Button btn_reset;
    @BindView(R.id.btn_openDoor)
    Button btn_openDoor;
    @BindView(R.id.btn_startInventory)
    Button btn_startInventory;
    @BindView(R.id.tv_result)
    TextView tv_result;
    @BindView(R.id.tv_rfid_status)
    TextView tv_rfid_status;
    byte[] Target_Ant = new byte[4];
    public SerialPortManager mSerialPortManager;
    private String lockDevicePath;
    private String rfidDevicePath;
    private String[] strings;
    private byte[] readdata_15693;
    private boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        LogUtil.e("设备号为:"+ App.getInstances().imei);

        //注册eventBus 处理硬件反馈信息
        EventBus.getDefault().register(this);
        //初始化mqtt
        if(MqttManager.getInstance(MainActivity.this).creatConnect(App.getInstances().imei)){
            sendMQTTHeart();
        }else{
            repairConnect();
        }

        initView();
        initData();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        sp_lock_devicePath.setSelection(0);
        sp_rfid_devicePath.setSelection(0);
        sp_lock_devicePath.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.e("获取的View的值"+((TextView)view).getText());
                String value = ((TextView)view).getText().toString();
                if("请选择门锁的连接串口".equals(value)){
                    lockDevicePath = "";
                }else{
                    lockDevicePath = value;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        sp_rfid_devicePath.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = ((TextView)view).getText().toString();
                if("请选择读写器的连接串口".equals(value)){
                    rfidDevicePath = "";
                }else{
                    rfidDevicePath = value;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }

    /**
     * 初始化数据
     */
    private void initData(){

     /*   if(TextUtils.isEmpty(lockDevicePath) || TextUtils.isEmpty(rfidDevicePath)){
            btn_openDoor.setEnabled(false);
            btn_startInventory.setEnabled(false);
            return;
        }*/
        WaitingDailogUtil.showWaitingDailog(this,"设备初始化中");
        try{
            //打开锁串口
            mSerialPortManager = SerialPortManager.instance();
            ///dev/ttyS1
            mSerialPortManager.open("/dev/ttyS1", Commands.BAUDRATESTR);
            btn_openDoor.setEnabled(true);

            ///dev/ttyS2
            //打开读头串口
            int result= HfData.HfGetData.OpenHf("/dev/ttyS2",19200, 1, null);
            if(result == 0){
                LogUtil.e("打开读写器成功");
                btn_startInventory.setEnabled(true);
                sanRfidData();
            }else{
                LogUtil.e("打开读写器失败"+result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("打开读写器失败"+e.getMessage());
        }finally {
            WaitingDailogUtil.closeDialog();
        }
    }

    @OnClick({R.id.btn_openDoor,R.id.btn_reset,R.id.btn_startInventory})
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.btn_openDoor:
                openDoor();
                break;
            case R.id.btn_reset:
                initData();
                break;
            case R.id.btn_startInventory:
                sanRfidData();
                break;
        }
    }
    /**
     * 扫描rfid
     */
    private void sanRfidData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                readdata_15693 = HfData.HfGetData.getReaddata_15693();
                LogUtil.e("读写器返回状态:"+ ByteUtil.bytes2BinStr(readdata_15693));
        

        /*  Target_Ant[0]= 0x00;
            Target_Ant[1]= 0x00;
            Target_Ant[2]= 0x03;
            Target_Ant[3]= (byte) 0xff;*/
                Target_Ant[0]=Target_Ant[1]=Target_Ant[2]=Target_Ant[3]=0;
                Target_Ant[3]|=0x01;
                Target_Ant[3]|=0x02;
                Target_Ant[3]|=0x04;
                Target_Ant[3]|=0x08;
                Target_Ant[3]|=0x10;
                Target_Ant[3]|=0x20;
                Target_Ant[3]|=0x40;
                Target_Ant[3]|=0x80;
                Target_Ant[2]|=0x01;
                Target_Ant[2]|=0x02;
                int[] fcmdret=new int[1];
                strings = HfData.HfGetData.Scan15693(Target_Ant, fcmdret);
                LogUtil.e("扫描获取的数据数量:"+ strings);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_rfid_status.setText("读写器状态:"+ByteUtil.bytes2BinStr(readdata_15693));
                        tv_result.setText("扫描到的标签数:"+strings.length);
                    }
                });
            }
        }).start();

}
    /**
     * 开锁
     */
    private void openDoor(){
        CmdPack openCmd = new CmdPack("3BB30004A41000320A");
        LogUtil.e("MainActivity", "开门命令..." + openCmd.getPackHexStr());
        mSerialPortManager.sendCommand(openCmd);
    }

    /**
     * 开锁反馈
     * @param timeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
    public void getCabinetGautam(FeedbackEvent timeEvent) {
        switch (timeEvent.command) {
            case "A4"://开门成功通知后台

                String doorNumber = timeEvent.appdata.substring(0,2);
                if("00".equals(timeEvent.appdata.substring(2,4))){
                    LogUtil.e("门编号:"+doorNumber + "开门成功");
                }else{
                    LogUtil.e("门编号:"+doorNumber + "开门失败");
                }

                break;
            case "5C":
                LogUtil.e("收到控制板主动上传的状态"+timeEvent.appdata);
                if(isOpen){

                }
                break;
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        LogUtil.e("mqtt失去连接");
        //停止心跳发送
        realeseTask();

        repairConnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LogUtil.e("topic"+topic + "message"+message);

        /**
         * 1.openDoor_common 购买开门
         * 2.openDoor_addGoods 补货开门
         * 3.openDoor_clearAll 清空
         */
        String myAction = message.toString();
        switch (myAction){
            case "openDoor_common": //购物

                openDoor();

                onOpenDoor("openDoor_common");
                break;
            case "openDoor_addGoods":
                onOpenDoor("openDoor_addGoods");
                break;
            case "openDoor_clearAll":
                onOpenDoor("openDoor_clearAll");
                break;
        }

    }

    public void onOpenDoor(final String myAction){
        Map<String,String> map = new HashMap();
        map.put("macno",App.getInstances().imei);
        map.put("apiname",myAction);
        map.put("status","1");
        switch (myAction){
            case "openDoor_common":
                map.put("openType","2");
                break;
            case "openDoor_addGoods":
                map.put("openType","1");
                break;
            case "openDoor_clearAll":
                map.put("openType","3");
                break;
        }
        String data = GsonUtil.GsonString(map);

        sendToMQTT("yihongshg/apk/index/doorOpen",data);

        /**
         * 5 秒后关门 上传rfid
         */
        RxTimerUtil.timer(5000, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(long number) {

                if("openDoor_common".equals(myAction)){
                    Map<String,String> params = new HashMap<>();
                    params.put("api_name","userDoor");
                    params.put("macno",App.getInstances().imei);
                    params.put("rfid","bb12345678,bb12345679,bb12345671");
                    String data = GsonUtil.GsonString(params);
                    sendToMQTT("yihongshg/apk/Device/api",data);
                }else{
                    Map<String,String> params = new HashMap<>();
                    params.put("api_name","closeDoor");
                    params.put("macno",App.getInstances().imei);
                    params.put("rfid","bb12345678,bb12345679,bb12345671");
                    switch (myAction){
                        case "openDoor_common":

                            break;
                        case "openDoor_addGoods":
                            params.put("openType","1");
                            break;
                        case "openDoor_clearAll":
                            params.put("closeType","2");
                            break;
                    }
                    String data = GsonUtil.GsonString(params);
                    sendToMQTT("yihongshg/apk/Device/api",data);
                }
            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        LogUtil.e("发布订阅完成"+token);
    }

    private void sendToMQTT(final String topic, final String jsonData){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtil.e("发布订阅：!"+jsonData);
                MqttManager.getInstance(MainActivity.this).publish(topic,2,jsonData.getBytes());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 断开mqtt
         */
        MqttManager.release();
    }

    private Timer mTimer;
    private TimerTask mTask;

    private final java.util.Timer timer = new java.util.Timer(true);

    //定义重连的任务
    private TimerTask connectTask;

    /**
     * 修复连接
     */
    private void repairConnect(){
        LogUtil.e("mqtt连接失败  5秒后重新连接");

        connectTask = null;

        if(connectTask == null){
            connectTask = new TimerTask() {
                @Override
                public void run() {
                    LogUtil.e("mqtt准备重新连接");
                    /**
                     * 断开重连
                     */
                    if(MqttManager.getInstance(MainActivity.this).creatConnect(App.getInstances().imei)){
                        sendMQTTHeart();
                    }else{
                        repairConnect();
                    }
                }
            };
        }
        if(timer != null){
            timer.schedule(connectTask,5000);
        }
    }

    /**
     * 发送心跳
     */
    private void sendMQTTHeart(){
        mTimer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if(TextUtils.isEmpty(App.getInstances().imei)){
                        return;
                    }
                    HeartBean heartBean = new HeartBean(App.getInstances().imei);
                    heartBean.setUpdate("true");
                    String heartStr = GsonUtil.GsonString(heartBean);
                    LogUtil.d("发布心跳：!"+heartStr);

                    MqttManager.getInstance(MainActivity.this).publish("yihongshg/apk/index/deviceStatus",2,heartStr.getBytes());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mTimer.schedule(mTask, 0, 30000);
    }

    /**
     * 释放心跳任务
     */
    public void realeseTask(){
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        if (mTimer != null) {
            mTimer.purge();
            mTimer.cancel();
            mTimer = null;
        }
    }
}
