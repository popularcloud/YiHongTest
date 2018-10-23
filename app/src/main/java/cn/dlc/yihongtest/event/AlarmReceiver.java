package cn.dlc.yihongtest.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.dlc.yihongtest.util.LogUtil;

public class AlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.e("您设置的闹钟时间到了");
    }
}