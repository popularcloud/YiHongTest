package cn.dlc.yihongtest.ui;

import android.os.Bundle;
import android.serialport.core.CmdPack;
import android.serialport.model.Commands;
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
import cn.dlc.yihongtest.util.LogPlus;
import cn.dlc.yihongtest.util.WaitingDailogUtil;

public class MainActivity extends AppCompatActivity {

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
    byte[] Target_Ant = new byte[4];
    public SerialPortManager mSerialPortManager;
    private String lockDevicePath;
    private String rfidDevicePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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
                LogPlus.e("获取的View的值"+((TextView)view).getText());
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

        if(TextUtils.isEmpty(lockDevicePath) || TextUtils.isEmpty(rfidDevicePath)){
            return;
        }
        WaitingDailogUtil.showWaitingDailog(this,"设备初始化中");
        try{
            //打开锁串口
            mSerialPortManager = SerialPortManager.instance();
            mSerialPortManager.open(lockDevicePath, Commands.BAUDRATESTR);

            //打开读头串口
            int result= HfData.HfGetData.OpenHf(rfidDevicePath,19200, 1, null);
            if(result == 0){
                LogPlus.e("打开读写器成功");
                sanRfidData();
            }else{
                LogPlus.e("打开读写器失败"+result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogPlus.e("打开读写器失败"+e.getMessage());
        }finally {
            WaitingDailogUtil.closeDialog();
        }
    }

    @OnClick({R.id.btn_openDoor,R.id.btn_reset,R.id.btn_startInventory})
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.btn_openDoor:

                break;
            case R.id.btn_reset:

                break;
            case R.id.btn_startInventory:

                break;
        }
    }
    /**
     * 扫描rfid
     */
    private void sanRfidData(){
        byte[] readdata_15693 = HfData.HfGetData.getReaddata_15693();

        LogPlus.e("读写器返回状态:"+ ByteUtil.bytes2BinStr(readdata_15693));

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
        String[] strings = HfData.HfGetData.Scan15693(Target_Ant, fcmdret);
        LogPlus.e("扫描获取的数据数量:"+ strings);
        for (String s:strings){
            LogPlus.e("扫描获取的数据:"+ s);
        }
    }

    /**
     * 开锁
     */
    private void openDoor(){
        CmdPack openCmd = new CmdPack("3BB30004A41000320A");
        LogPlus.e("MainActivity", "开门命令..." + openCmd.getPackHexStr());
        mSerialPortManager.sendCommand(openCmd);
    }
}
