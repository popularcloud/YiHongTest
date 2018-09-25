package cn.dlc.yihongtest.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import cn.dlc.yihongtest.base.App;

/**
 * @author YoungeTao
 * @Email youngetao@gmail.com
 * @date 2018/9/20 15:03
 * @describe
 */
public class LogUtil {

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    private static String tag = "default";
    private static boolean isSaveToFile = false;
    private static int saveLevel = 0;
    private static String PATH_LOGCAT;

    public static void init(String defaultTag){
        if(!TextUtils.isEmpty(defaultTag)){
            tag = defaultTag;
        }
    }

    public static void init(String defaultTag, boolean saveToFile, int mySaveLevel){
        if(!TextUtils.isEmpty(defaultTag)){
            tag = defaultTag;
        }
        isSaveToFile = saveToFile;
        saveLevel = mySaveLevel;

    }

    public static void v(String tag, String msg){
        printer(VERBOSE,msg);
        if(isSaveToFile){
         saveToFile(tag,msg,VERBOSE);
        }
    }

    public static void v(String msg){
        printer(VERBOSE,msg);
        if(isSaveToFile){
            saveToFile(tag,msg,VERBOSE);
        }
    }

    public static void d(String tag, String msg){
        printer(DEBUG,msg);
        if(isSaveToFile){
            saveToFile(tag,msg,DEBUG);
        }
    }

    public static void d(String msg){
        printer(DEBUG,msg);
        if(isSaveToFile){
            saveToFile(tag,msg,DEBUG);
        }
    }

    public static void i(String tag, String msg){
        printer(INFO,msg);
        if(isSaveToFile){
            saveToFile(tag,msg,INFO);
        }
    }

    public static void i(String msg){
        printer(INFO,msg);
        if(isSaveToFile){
            saveToFile(tag,msg,INFO);
        }
    }

    public static void w(String tag, String msg){
        printer(WARN,msg);
        if(isSaveToFile){
            saveToFile(tag,msg,WARN);
        }
    }

    public static void w(String msg){
        printer(WARN,msg);
        if(isSaveToFile){
            saveToFile(tag,msg,WARN);
        }
    }

    public static void e(String tag, String msg){
        printer(ERROR,msg);
        if(isSaveToFile){
            saveToFile(tag,msg,ERROR);
        }
    }

    public static void e(String msg){
        printer(ERROR,msg);
        if(isSaveToFile){
            saveToFile(tag,msg,ERROR);
        }
    }

    /**
     * 打印
     * @param tagLevel
     * @param msg
     */
    private static void printer(int tagLevel,String msg){
        StackTraceElement e = Thread.currentThread().getStackTrace()[4];
        String fileName = e.getFileName();
        int lineNum = e.getLineNumber();
        String methodName = e.getMethodName();

        StringBuilder sb = new StringBuilder();

        sb.append(methodName)
                .append('(')
                .append(fileName)
                .append(':')
                .append(lineNum)
                .append(')')
                .append(msg);

        msg = sb.toString();

        switch (tagLevel){
            case VERBOSE:
                Log.v(tag, msg);
                break;
            case DEBUG:
                Log.d(tag, msg);
                break;
            case INFO:
                Log.i(tag, msg);
                break;
            case WARN:
                Log.w(tag, msg);
                break;
            case ERROR:
                Log.e(tag, msg);
                break;
        }
    }

    /**
     * 保存文件到本地
     */
    public static void saveToFile(String tag, String msg, int level){
        if(level < saveLevel){
            return;
        }
        String dateTime = DateTimeUtil.formatDateTime(System.currentTimeMillis());
        String log = DateTimeUtil.formatDateTimeMill(System.currentTimeMillis()) + "***" + tag + "***" + msg + "\n";
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "qblog";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = App.getInstances().getFilesDir().getAbsolutePath()
                    + File.separator + "qplog";
        }

        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            File files = new File(PATH_LOGCAT);
            if (!files.exists()) {
                files.mkdirs();
            }

            String path = PATH_LOGCAT + File.separator + "log" + dateTime + "_" + App.getInstances().imei + ".txt";
            File file = new File(path);
            if(!file.exists()){
                file.createNewFile();
            }

            fos = new FileOutputStream(path, true);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(log);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();//关闭缓冲流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取需要上传的文件
     * @param fileName
     * @return
     */
    public static File updataToService(String fileName){
        String dateTime = DateTimeUtil.formatDateTime(System.currentTimeMillis());
        if(!TextUtils.isEmpty(fileName)){
            dateTime = fileName;
        }
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "qblog";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = App.getInstances().getFilesDir().getAbsolutePath()+ File.separator + "qplog";
        }
        String path = PATH_LOGCAT + File.separator + "log"+dateTime+"_"+App.getInstances().imei+".txt";
        File file = new File(path);
        if(file.exists()){
            return file;
        }
        return null;
    }

    /**
     * 检查文件数
     */
    public void checkedFileNum(){

    }
}
