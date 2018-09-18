package cn.dlc.yihongtest;

import android.serialport.uitils.ByteUtil;
import android.serialport.uitils.HfData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.dlc.yihongtest.util.LogPlus;

public class MainActivity extends AppCompatActivity {

    byte[] Target_Ant = new byte[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       try{
        int result= HfData.HfGetData.OpenHf("/dev/ttyS2",19200, 1, null);
        if(result == 0){
            LogPlus.e("打开读写器成功");

            byte[] readdata_15693 = HfData.HfGetData.getReaddata_15693();

            LogPlus.e("读写器返回状态:"+ ByteUtil.bytes2BinStr(readdata_15693));

            /*    Target_Ant[0]= 0x00;
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
            String[] strings = HfData.HfGetData.Scan15693(Target_Ant, fcmdret);
            LogPlus.e("扫描获取的数据:"+ strings);
        }else{
            LogPlus.e("打开读写器失败"+result);
        }
    } catch (Exception e) {
        e.printStackTrace();
        LogPlus.e("打开读写器失败"+e.getMessage());
    }
    }
}
