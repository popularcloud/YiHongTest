package cn.dlc.yihongtest.util;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

/**
 * @author YoungeTao
 * @Email youngetao@gmail.com
 * @date 2018/5/24 20:02
 * @describe 管理mqtt的连接,发布,订阅,断开连接, 断开重连等操作
 */
public class MqttManager {

    /**
     * 基本的连接配置数据
     */
    public final String URL = "tcp://120.77.72.190:1883";
    private final String userName = "dlc";
    private final String passWord = "123456";

    /**
     * 实例对象
     */
    private static MqttManager mInstance = null;

    /**
     * mqtt回调处理接口
     */
    private MqttCallback mCallback;

    /**
     * mqtt client对象
     */
    private MqttClient client;
    private MqttConnectOptions conOpt;
    private boolean clean = true;

    private MqttManager(MqttCallback mCallback) {
         this.mCallback = mCallback; //操作activity中实现该回调
    }

    public static MqttManager getInstance(MqttCallback mCallback) {
        if (null == mInstance) {
            mInstance = new MqttManager(mCallback);
        }
        return mInstance;
    }

    /**
     * 释放单例, 及其所引用的资源
     */
    public static void release() {
        try {
            if (mInstance != null) {
                mInstance.disConnect();
                mInstance = null;
            }
        } catch (Exception e) {

        }
    }

    /**
     * 创建mqtt连接
     * @return
     */
    public boolean creatConnect(String imei) {
        boolean flag = false;
        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
        LogUtil.d("开始连接");
        try {
            conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(clean);
            if (passWord != null) {
                conOpt.setPassword(passWord.toCharArray());
            }
            if (userName != null) {
                conOpt.setUserName(userName);
            }

            //
            client = new MqttClient(URL, imei, dataStore);
            client.setCallback(mCallback);

            /**
             * 返回true表示连接成功
             */
            flag = doConnect();
            if(flag){
                LogUtil.d("连接成功，开始订阅");
                /**
                 * 订阅报文
                 */
                mInstance.subscribe(imei, 2);
            }
        } catch (MqttException e) {
            LogUtil.d("连接失败或订阅失败"+ e.getMessage());
        }
        return flag;
    }

    /**
     * 建立连接
     *
     * @return
     */
    public boolean doConnect() {
        boolean flag = false;
        if (client != null) {
            try {
                client.connect(conOpt);
                LogUtil.d("Connected to " + client.getServerURI() + " with client ID " + client.getClientId());
                flag = true;
            } catch (Exception e) {
            }
        }
        return flag;
    }

    /**
     * 发布订阅
     * @param topicName
     * @param qos 1.发送者最少发送一次消息，确保消息到达Broker，Broker需要返回确认消息PUBACK。在Qos1情况下，Broker可能接受到重复消息
     *            2.Qos2使用两阶段确认来保证消息的不丢失和不重复。在Qos2情况下，Broker肯定会收到消息，且只收到一次
     *            0.发送者只发送一次消息，不进行重试，Broker不会返回确认消息。在Qos0情况下，Broker可能没有接受到消息
     * @param payload
     * @return
     */
    public boolean publish(String topicName, int qos, byte[] payload) {

        boolean flag = false;

        if (client != null && client.isConnected()) {

            LogUtil.d("Publishing to topic \"" + topicName + "\" qos " + qos);

            // Create and configure a message
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);

            // Send the message to the server, control is not returned until
            // it has been delivered to the server meeting the specified
            // quality of service.
            try {
                client.publish(topicName, message);
                flag = true;
                LogUtil.e("发送成功!");
            } catch (MqttException e) {
                LogUtil.e("发送失败!");
                if(mCallback != null){
                    mCallback.connectionLost(new Throwable());
                }
            }
        }else{
            if(mCallback != null){
                mCallback.connectionLost(new Throwable());
            }

        }
        return flag;
    }

    /**
     * 订阅报文
     * @param topicName
     * @param qos
     * @return
     */
    public boolean subscribe(String topicName, int qos) {

        boolean flag = false;

        if (client != null && client.isConnected()) {

            LogUtil.d("Subscribing to topic \"" + topicName + "\" qos " + qos);
            try {
                client.subscribe(topicName, qos);
                flag = true;
            } catch (MqttException e) {

            }
        }
        return flag;
    }

    /**
     * 取消连接
     *
     * @throws MqttException
     */
    public void disConnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }
}
