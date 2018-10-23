package cn.dlc.yihongtest.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.tencent.bugly.crashreport.CrashReport;

import cn.dlc.yihongtest.util.LogUtil;

/**
 * @author YoungeTao
 * @Email youngetao@gmail.com
 * @date 2018/5/16 17:46
 * @describe
 */
public class App extends Application {

    private static App instances;
    public String imei;

    public static App getInstances(){
        return instances;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instances = this;
        imei = getIMEI();

        /**
         * 初始化日志打印
         */
        LogUtil.init("yihongtest",true,LogUtil.ERROR);

        /**
         * 初始化bugly
         */
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        PackageInfo packageInfo = null;
        try {
            packageInfo = getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String localVersion = packageInfo.versionName;
        strategy.setAppChannel("test");   //App的渠道
        strategy.setAppVersion(localVersion);      //App的版本
        strategy.setAppPackageName(getPackageName());  //App的包名

        CrashReport.initCrashReport(getApplicationContext(), "6be38a3b1c", true,strategy);

        /**
         * 初始化语音
         */
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=5bc3ef49");
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public String getIMEI(){
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        LogUtil.e("设备号为:"+ telephonyManager.getDeviceId());
        return telephonyManager.getDeviceId();
    }

    public void setEnableWifi(boolean isWifiEnable){
        //默认打开wifi
        WifiManager wifiManager =(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(isWifiEnable);
    }
}
