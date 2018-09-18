package android.serialport.serial;

import android.os.SystemClock;
import android.serialport.uitils.ByteUtil;
import android.serialport.uitils.Protocols;
import com.licheedev.myutils.LogPlus;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 页面:刘华乾  on  2018/1/16.
 * 对接口:
 * 作用:
 */

public class SerialReadThread extends Thread {

    private BufferedInputStream mInputStream;

    /**
     * 收到数据的缓存
     */
    private byte[] buffer = new byte[8192];
    /**
     * 标记收到数据的总大小
     */
    private int currentSize = 0;
    //ExecutorService mThreadPool;
    private final DataDispatcher mDispatcher;
    private String devicePath;

    public SerialReadThread(InputStream is, String devicePath) {

        mInputStream = new BufferedInputStream(is);
        mDispatcher = new DataDispatcher();
        this.devicePath = devicePath;
        //this.mThreadPool = mThreadPool;
    }

    @Override
    public void run() {
        super.run();
        byte[] received = new byte[1024];
        int size;

        while (!isInterrupted()) {
            try {
                size = mInputStream.read(received);
                final String s = ByteUtil.bytes2HexStr(received, 0, received.length);
//                LogPlus.e("收到命令ABCD:"+s);
                if (size > 0) {
                    onDataReceive(received, size);
                }
            } catch (IOException e) {
                LogPlus.e("读取数据失败，线程结束", e);
                return;
            }
        }
        LogPlus.e("结束读进程");
    }

    /**
     * 处理获取到的数据，解决粘包
     *
     * @param received
     * @param size
     */
    private void onDataReceive(final byte[] received, final int size) {

        // SYN RES PTROL ADDR SLEN COMMAND APPDATA CRC16
        // 2   3   1     2    2    2       *       2     字节
        // 0   2   5     6    8    10      12      12+*  偏移

        final String s = ByteUtil.bytes2HexStr(received, 0, size);
       /* if (size==24&&s.startsWith("55AA1416")&&s.endsWith("FF")){
            processReceiveCardData(s.substring(10,18));
        }*/
        //LogPlus.e("收到：" + s);
        //if (s.contains("09CA")) {
        //    LogPlus.e("收到09CA：" + s);
        //}
        // 先缓存收到的数据
        try {
            System.arraycopy(received, 0, buffer, currentSize, size);
        } catch (Exception e) {
            currentSize = 0;
            return;
        }
        currentSize += size;
        int temp = 0; // 下标
        int frameStart = 0;// 开始位置下标
        int frameEnd = 0;

        while (temp < currentSize) {
            SystemClock.sleep(1);//确保是堵塞线程 让interrupt()起作用 可以退出线程
            // 如果长度不满足最小长度，则跳出循环，等下新数据
            if (currentSize - temp >= Protocols.MIN_SIZE) {
                // 第1个字节不为 3A，则直接下一轮
                if (buffer[temp++] != Protocols.FRAME_HEAD_0) {
                    continue;
                }
                // 第2个字节不为 A3，则直接下一轮
                if (buffer[temp++] != Protocols.FRAME_HEAD_1) {
                    continue;
                }
                //第三个字节不为01,则直接下一轮
                if (buffer[temp++] != Protocols.FRAME_HEAD_2) {
                    continue;
                }
                // 开始处理帧，先回溯3位，得到帧头位置

                frameStart = temp - 3;

                // 得到数据长度
                String slenStr = ByteUtil.bytes2HexStr(buffer, frameStart + 3, 1);
                int slen = (int) ByteUtil.hexStr2decimal(slenStr);//获取到数据长度

                // slen包括command和appdata，所以要把command的两字节减去
                //数据长度：1字节，包括命令码、协议版本和数据域的长度。 因为最小长度包括命令码、协议版所以要减2
                int total = Protocols.MIN_SIZE + slen - 2;
                // 得到帧
                frameEnd = frameStart + total;

                // 缓存长度不小于帧末索引，否则此帧无效，继续查找
                if (currentSize < frameEnd) {
                    break;
                }

                // 找到crc16校验位
                String crc16 = ByteUtil.bytes2HexStr(buffer, frameEnd - 1, 1);
                // 除开CRC16的数据
                byte[] other = new byte[total - 1];
                System.arraycopy(buffer, frameStart, other, 0, other.length);
                // 计算其他数据的校验
                String otherCrc16 = ByteUtil.verify(other);
                //LogPlus.e("收到otherCrc16：" + otherCrc16);

                if (crc16.equals(otherCrc16)) {
                    //命令码字符串
                    String command = ByteUtil.bytes2HexStr(buffer, frameStart + 4, 1);
                    //数据
                    String appdata = ByteUtil.bytes2HexStr(buffer, frameStart + 6, slen - 2);
                    // 所有数据
                    String allData = ByteUtil.bytes2HexStr(buffer, frameStart, total);

                    //LogPlus.e(devicePath+"收到==命令：" + ByteUtil.bytes2HexStr(buffer));
                    // 处理数据
                    processReceiveData(command, appdata, allData);

                    if (currentSize > frameEnd) {
                        // 未处理的数据
                        byte[] remain = new byte[currentSize - frameEnd];
                        System.arraycopy(buffer, frameEnd, remain, 0, remain.length);
                        //将未处理的数据remain替换从0开始替换到buffer
                        System.arraycopy(remain, 0, buffer, 0, remain.length);
                        currentSize = currentSize - frameEnd;
                        temp = 0;
                    } else if (currentSize == frameEnd) {
                        currentSize = 0;
                        break;
                    }
                } else {
                    // 不一致则继续，找到下一个3AA3
                    continue;
                }
            } else {
                break;
            }
        }
        // 如果下标跟缓存长度一样，则说明没有待处理数据，缓存清零
        if (temp == currentSize) {
            currentSize = 0;
        }
    }

    /**
     * 处理收到的数据
     *
     * @param command
     * @param appdata
     * @param allData
     */
    private void processReceiveData(final String command, final String appdata,
        final String allData) {
        Observable.unsafeCreate(new ObservableSource<Object>() {
            @Override
            public void subscribe(Observer<? super Object> observer) {
                // 交给分发器处理
                mDispatcher.dispatch(command, appdata, allData, devicePath);
                //LogPlus.e(devicePath+"分发器处理=：" + command + "=" + appdata + "=" + allData);
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 停止读线程
     */
    public void close() {
        interrupt();
        try {
            mInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}