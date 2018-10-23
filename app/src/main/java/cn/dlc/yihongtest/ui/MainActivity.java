package cn.dlc.yihongtest.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.serialport.core.CmdPack;
import android.serialport.model.Commands;
import android.serialport.serial.event.FeedbackEvent;
import android.serialport.uitils.HfData;
import android.serialport.uitils.SerialPortManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.dlc.yihongtest.R;
import cn.dlc.yihongtest.base.App;
import cn.dlc.yihongtest.base.BaseActivity;
import cn.dlc.yihongtest.bean.HeartBean;
import cn.dlc.yihongtest.bean.OperateBean;
import cn.dlc.yihongtest.event.ShowLogEvent;
import cn.dlc.yihongtest.util.GsonUtil;
import cn.dlc.yihongtest.util.LogUtil;
import cn.dlc.yihongtest.util.MqttManager;
import cn.dlc.yihongtest.util.NetWorkUtil;
import cn.dlc.yihongtest.util.NetworkConnectChangedReceiver;
import cn.dlc.yihongtest.util.RxTimerUtil;
import cn.dlc.yihongtest.util.ToastUtil;
import cn.dlc.yihongtest.util.WaitingDailogUtil;

public class MainActivity extends BaseActivity implements MqttCallback{

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
    @BindView(R.id.btn_clear_log)
    Button btn_clear_log;
/*    @BindView(R.id.tv_rfid_status)
    TextView tv_rfid_status;*/
    byte[] Target_Ant = new byte[4];
    public SerialPortManager mSerialPortManager;
    private String lockDevicePath;
    private String rfidDevicePath;

    private boolean isOpen = false;
    private String openType = "first_inventory";

    String[] beforList = null;
    String[] presentList = null;

    private int invertoryType = 0;
    private Thread scanThead;
    private OperateBean operateBean;
    StringBuilder sb = new StringBuilder();

    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer = "xiaoyan";
    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue;
    private boolean isRepairIng = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //注册eventBus
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

        // 初始化合成语音对象
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED,"50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH,"50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME,"50");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        // 云端发音人名称列表
       // mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        //mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        registerReceiver(new NetworkConnectChangedReceiver(),filter);


        RxTimerUtil rxTimerUtil = new RxTimerUtil();
        rxTimerUtil.intervalM(30, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(long number) {
                LogUtil.e("已过30分钟 开始检测网络");

                if(NetWorkUtil.isWifi(MainActivity.this)){ //判读是否为wifi网络
                    if(NetWorkUtil.ping()){
                        LogUtil.e("网络可用！");
                    }else{
                        LogUtil.e("网络不可用！准备关闭wifi");
                        App.getInstances().setEnableWifi(false);
                    }
                }else{
                    App.getInstances().setEnableWifi(true);

                    RxTimerUtil rxTimerUtil2 = new RxTimerUtil();
                    rxTimerUtil2.timer(60000, new RxTimerUtil.IRxNext() {
                        @Override
                        public void doNext(long number) {
                            //开启wifi 30秒后检查wifi是否可用
                            if(NetWorkUtil.ping()){
                                LogUtil.e("网络可用！");
                            }else{
                                LogUtil.e("网络不可用！准备关闭wifi");
                                App.getInstances().setEnableWifi(false);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 初始化语音监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            LogUtil.e("InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                LogUtil.e("语音初始化失败,错误码："+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
                LogUtil.e("语音初始化成功！请使用");
            }
        }
    };

    private void SpeakingMsg(String message){
        if(mTts != null){
            mTts.startSpeaking(message,mTtsListener);
        }
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            LogUtil.e("语音开始播放");
        }

        @Override
        public void onSpeakPaused() {
            LogUtil.e("语音暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            LogUtil.e("语音继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                LogUtil.e("语音播放完成");
            } else if (error != null) {
                LogUtil.e((error.getPlainDescription(true)));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}

            if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
                byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
                LogUtil.e("MscSpeechLog", "buf is =" + buf);
            }
        }
    };

    /**
     * 初始化数据
     */
    private void initData(){
        WaitingDailogUtil.showWaitingDailog(this,"设备初始化中...");
        try{
            //打开锁串口
            mSerialPortManager = SerialPortManager.instance();
            ///dev/ttyS1
            if(mSerialPortManager.open("/dev/ttyS1", Commands.BAUDRATESTR) != null){
                btn_openDoor.setEnabled(true);
                ///dev/ttyS2
                //打开读头串口
                int result= HfData.HfGetData.OpenHf("/dev/ttyS2",19200, 1, null);
                if(result == 0){
                    LogUtil.e("打开读写器成功");
                    SpeakingMsg("设备初始化成功!请使用");
                    btn_startInventory.setEnabled(true);
                    sanRfidData();
                }else{
                    SpeakingMsg("初始化读写器失败！");
                    LogUtil.e("打开读写器失败"+result);
                }
            }else{
                SpeakingMsg("初始化锁失败！");
                LogUtil.e("初始化锁失败");
            }



        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("打开读写器失败"+e.getMessage());
        }finally {
            WaitingDailogUtil.closeDialog();
        }
    }

    @OnClick({R.id.btn_openDoor,R.id.btn_clear_log,R.id.btn_reset,R.id.btn_startInventory})
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.btn_openDoor:
                openDoor();
                break;
            case R.id.btn_reset:
                initData();
                LogUtil.e("重新初始化!");
                break;
            case R.id.btn_startInventory:
                sanRfidData();
                break;
            case R.id.btn_clear_log:
                sb.setLength(0);
                tv_result.setText(sb.toString());
                break;
        }
    }

    /**
     * 把日志显示到屏幕
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showLogToDisplay(ShowLogEvent ShowLogEvent){
        String logs = ShowLogEvent.getMessage();
        sb.append(logs+"\n");
        tv_result.setText(sb.toString());

    }

    /**
     * 扫描rfid
     */
    private void sanRfidData(){
        if(scanThead != null && scanThead.isAlive()){
            LogUtil.e("正在扫描*******************请稍后");
            return;
        }
        scanThead = new Thread(new Runnable() {
            @Override
            public void run() {
               final byte[]  readdata_15693 = HfData.HfGetData.getReaddata_15693();
               // LogUtil.e("读写器返回状态:"+ ByteUtil.bytes2BinStr(readdata_15693));
        

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
                final String[]  strings = HfData.HfGetData.Scan15693(Target_Ant, fcmdret);
                final String[] finalStrings = strings == null ? new String[0] : strings;
                LogUtil.e("扫描获取的数据数量:"+ finalStrings.length);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // tv_rfid_status.setText("读写器状态:"+ByteUtil.bytes2BinStr(readdata_15693));
                        tv_result.setText("扫描到的标签数:"+finalStrings.length);
                        switch (openType){
                            case "first_inventory":
                                beforList = finalStrings.clone();
                                LogUtil.e("第一次扫描处理成功!");
                            break;
                            case "openDoor_common":
                                presentList = finalStrings.clone();
                                LogUtil.e("普通扫描处理成功!");
                                sendToServiceData();
                                break;
                            case "openDoor_addGoods":
                                presentList = finalStrings.clone();
                                LogUtil.e("补货扫描处理成功!");
                                sendToServiceData();
                                break;
                            case "openDoor_clearAll":
                                presentList = finalStrings.clone();
                                LogUtil.e("清空处理成功！!");
                                sendToServiceData();
                                break;
                        }
                    }
                });
            }
        });
        scanThead.start();

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
                    SpeakingMsg("开门成功!请选购您的商品");
                    isOpen = true;
                    LogUtil.e("门编号:"+doorNumber + "开门成功");
                    onOpenDoor(openType);
                }else{
                    LogUtil.e("门编号:"+doorNumber + "开门失败");
                }
                break;
            case "5C":
                //LogUtil.e("收到控制板主动上传的状态"+timeEvent.appdata);
                String appData = timeEvent.appdata;
                String appStatus = appData.substring(appData.length()-8,appData.length());
                String doorOne = appStatus.substring(0,2);
                String lockOne = appStatus.substring(4,6);
               // LogUtil.e("doorOne="+doorOne +"lockOne="+lockOne);
                if("00".equals(doorOne) && "01".equals(lockOne) && !isOpen){
                    LogUtil.e("门被拉开！");
                    isOpen = true;
                }
                if("01".equals(doorOne) && "00".equals(lockOne) && isOpen){
                    LogUtil.e("门被关闭！");
                    SpeakingMsg("关门结算中，请稍后！");
                    isOpen = false;
                    sanRfidData();
                }
                break;
        }
    }

    /**
     * 比较数组
     * @param t1
     * @param t2
     * @return
     */
    public String compare(String[] t1, String[] t2) {
        if(t1 == null){
            return GsonUtil.GsonString(t2);
        }
        if(t2 == null){
            return GsonUtil.GsonString(t1);
        }
        if(t1.length <= t2.length){
            List<String> list1 = Arrays.asList(t1); //将t1数组转成list数组
            //List<String> list2 = new ArrayList<String>();//用来存放2个数组中不相同的元素
            StringBuilder sb = new StringBuilder();
            for (String t : t2) {
                if (!list1.contains(t)) {
                    sb.append(t+",");
                }
            }
            return sb.toString();
        }else{
            List<String> list1 = Arrays.asList(t2); //将t1数组转成list数组
            StringBuilder sb = new StringBuilder();//用来存放2个数组中不相同的元素
            for (String t : t1) {
                if (!list1.contains(t)) {
                    sb.append(t+",");
                }
            }
            return sb.toString();
        }
    }

    private void sendToServiceData(){
                    LogUtil.e("presentList.length:"+ presentList.length+"****beforList.length:"+beforList.length);
                    String rfid = compare(presentList,beforList);
                    if (rfid != null) {
                        rfid =  rfid.replace("[","").replace("]","").replace("\"","");
                    }
                    LogUtil.e("rfid","rfid:"+rfid);
                    Map<String,String> params = new HashMap<>();
                    params.put("api_name","closeDoor");
                    params.put("macno",App.getInstances().imei);
                    params.put("oid",operateBean.getOid());
                    params.put("rfid",rfid);
                    switch (openType){
                        case "openDoor_common":
                            params.put("openType","2");
                            break;
                        case "openDoor_addGoods":
                            params.put("openType","1");
                            break;
                        case "openDoor_clearAll":
                            params.put("closeType","3");
                            break;
                    }
                    if (presentList.length > beforList.length){
                        params.put("type","1");
                    }else if (presentList.length < beforList.length){
                        params.put("type","2");
                    }
                    String data = GsonUtil.GsonString(params);
                    sendToMQTT("yihongshg/apk/device/closeDoorNotify",data);

                    if(presentList != null){
                        beforList = presentList.clone();
                    }
    }

    private void closeDoorWithEmptyGoods(){
        Map<String,String> params = new HashMap<>();
        params.put("api_name","closeDoor");
        params.put("macno",App.getInstances().imei);
        params.put("oid",operateBean.getOid());
        switch (openType){
            case "openDoor_common":
                params.put("openType","2");
                break;
            case "openDoor_addGoods":
                params.put("openType","1");
                break;
            case "openDoor_clearAll":
                params.put("closeType","3");
                break;
        }
        String data = GsonUtil.GsonString(params);
        sendToMQTT("yihongshg/apk/device/closeDoorNotify",data);
    }

    @Override
    public void connectionLost(Throwable cause) {
        LogUtil.e("mqtt失去连接");

        if(isRepairIng){
            return;
        }

        isRepairIng = true;

        //停止心跳发送
        realeseTask();

        repairConnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LogUtil.e("topic"+topic + "message"+message);

        String myAction = message.toString();
        operateBean = GsonUtil.GsonToBean(myAction, OperateBean.class);
    /*    if(operateBean == null){
            return;
        }*/


        /**
         * 1.openDoor_common 购买开门
         * 2.openDoor_addGoods 补货开门
         * 3.openDoor_clearAll 清空
         */

        switch (operateBean.getOperateType()){
            case "openDoor_common": //购物
                openType = "openDoor_common";
                openDoor();
                break;
            case "openDoor_addGoods":
                openType = "openDoor_addGoods";
                openDoor();
                break;
            case "openDoor_clearAll":
                openType = "openDoor_clearAll";
                openDoor();
                break;
             case "confirm":
                 LogUtil.e("执行确认操作");
                if(presentList != null){
                    beforList = presentList.clone();
                }
                break;
        }

    }

    public void onOpenDoor(final String myAction){
        if(operateBean == null){
            LogUtil.e("tcp对象为空!");
            return;
        }
        Map<String,String> map = new HashMap();
        map.put("macno",App.getInstances().imei);
        map.put("apiname",myAction);
        map.put("oid",operateBean.getOid());
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

        sendToMQTT("yihongshg/apk/device/openDoorNotify",data);
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

        if( null != mTts ){
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
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
       // App.getInstances().setEnableWifi(false);
        try {
            MqttManager.getInstance(MainActivity.this).disConnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
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
                        isRepairIng = false;
                        sendMQTTHeart();
                    }else{
                        isRepairIng = false;
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
                    if(isOpen){
                        heartBean.setDoorStatus(1);
                    }else{
                        heartBean.setDoorStatus(0);
                    }

                    String heartStr = GsonUtil.GsonString(heartBean);
                    LogUtil.d("发布心跳：!"+heartStr);

                    MqttManager.getInstance(MainActivity.this).publish("yihongshg/apk/beat/deviceStatus",2,heartStr.getBytes());

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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        ToastUtil.show(this,"点击了退出!");
        return;
    }
}
