package cn.dlc.yihongtest.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import cn.dlc.yihongtest.base.App;

/**
 * @author YoungeTao
 * @Email youngetao@gmail.com
 * @date 2018/10/18 14:24
 * @describe
 */
public class NetworkConnectChangedReceiver extends BroadcastReceiver{

    private static final String TAG = "NetWork";
    private boolean isChecked = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager
        // .WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
        // 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，
        // 当然刚打开wifi肯定还没有连接到有效的无线
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态
                LogUtil.e(TAG, "isConnected" + isConnected);
                if (isConnected) {
                   // App.getInstances().setEnableWifi(true);
                    LogUtil.e("#################wifi可用");
                } else {
                  //  App.getInstances().setEnableWifi(false);

                    if(isChecked){
                        return;
                    }
                    isChecked = true;

                    RxTimerUtil rxTimerUtil = new RxTimerUtil();
                    rxTimerUtil.timer(30000, new RxTimerUtil.IRxNext() {
                        @Override
                        public void doNext(long number) {
                            if(NetWorkUtil.ping()){
                                isChecked = false;
                            }else{
                                LogUtil.e("#################wifi不可用");
                                isChecked = false;
                                App.getInstances().setEnableWifi(false);
                            }
                        }
                    });
                }
            }
        }
    }
}
