package cn.dlc.yihongtest.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

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
        LogUtil.init("yihongtest",false,LogUtil.ERROR);

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
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public String getIMEI(){
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
