package android.serialport.uitils;

import android.serialport.core.CmdPack;
import android.serialport.model.Device;
import android.serialport.serial.SerialReadThread;
import android.util.Log;
import com.licheedev.myutils.LogPlus;
import com.rfid.serialport.SerialPort;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.OutputStream;

/**
 * 页面:刘华乾  on  2018/1/16.
 * 对接口:
 * 作用:
 */

public class SerialPortManager {
    private static final String TAG = "SerialPortManager";

    private SerialReadThread mReadThread;
    private OutputStream mOutputStream;

    private static SerialPortManager sManager;

    public static SerialPortManager instance() {
        if (sManager == null) sManager = new SerialPortManager();
        return sManager;
    }

    private SerialPort mSerialPort;

    private SerialPortManager() {


    }

    /**
     * 打开串口
     *
     * @param device
     * @return
     */
    public SerialPort open(Device device) {
        return open(device.getPath(), device.getBaudrate());
    }

    /**
     * 打开串口
     *
     * @param devicePath
     * @param baudrateString
     * @return
     */
    public SerialPort open(String devicePath, String baudrateString) {
        if (mSerialPort != null) {
            close();
        }

        try {
            File device = new File(devicePath);
            int baurate = Integer.parseInt(baudrateString);
            mSerialPort = new SerialPort(device, baurate, 0);

            mReadThread = new SerialReadThread(mSerialPort.getInputStream(), devicePath);
            mReadThread.start();

            mOutputStream = mSerialPort.getOutputStream();

            return mSerialPort;
        } catch (Throwable tr) {
            Log.e(TAG, "打开串口失败", tr);
            return null;
        }
    }

    /**
     * 关闭串口
     */
    public void close() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }

        if (mReadThread != null) {
            mReadThread.close();
        }
    }

    /**
     * 发送数据
     *
     * @param datas
     * @return
     */
    private void sendData(byte[] datas) throws Exception {
        mOutputStream.write(datas);
    }

    /**
     * (rx包裹)发送数据
     *
     * @param datas
     * @return
     */
    private Observable<Void> rxSendData(final byte[] datas) {

        return Observable.unsafeCreate(new ObservableSource<Void>() {
            @Override
            public void subscribe(Observer<? super Void> observer) {
                try {
                    sendData(datas);
                } catch (Exception e) {
                    Log.e("发送：" + ByteUtil.bytes2HexStr(datas) + " 失败", e.toString());
                    observer.onError(e);
                }
              //  observer.onNext(null);
                //subscriber.onCompleted();
                observer.onComplete();
            }
        });
    }
    /**
     * 发送命令包
     *
     * @param cmdPack
     */
    public void sendCommand(CmdPack cmdPack) {
        String packHexStr = cmdPack.getPackHexStr();
        LogPlus.w("发送命令ABCD：" + packHexStr);


        Observable.just(cmdPack)
            .subscribeOn(Schedulers.io())
            .flatMap(new Function<CmdPack, ObservableSource<?>>() {
                @Override
                public ObservableSource<?> apply(CmdPack cmdPack) throws Exception {
                    return rxSendData(cmdPack.pack());
                }
            })
            .subscribe(new Observer<Object>() {
                @Override
                public void onSubscribe(Disposable disposable) {

                }

                @Override
                public void onNext(Object o) {

                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e("发送失败", throwable.toString());
                }

                @Override
                public void onComplete() {

                }
            });
    }

    //public ExecutorService getThreadPool() {
    //    return mThreadPool;
    //}
}
