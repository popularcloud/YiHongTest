package android.serialport.uitils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HfData {
	String[] UID = new String[9];
	public static Map<String, Integer> scanResult15693 = new HashMap<String, Integer>();
	static Map<String, byte[]> UIDBytes = new HashMap<String, byte[]>();
	private static int scaned_num;

	private String addr;
	private String num;
	private static boolean isDeviceOpen = false;
	static SoundPool soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100);;
	static ExecutorService soundThread = Executors.newSingleThreadExecutor();
	static int soundid = soundpool.load("/etc/Scan_new.ogg", 1);
	
	private static String fh_id;

	public static String getfh_id() {
		return fh_id;
	}

	public static void setfh_id(String fh_id) {
		HfData.fh_id = fh_id;
	}
    
	public static boolean isDeviceOpen() {
		return isDeviceOpen;
	}

	public static void setDeviceOpen(boolean b) {
		isDeviceOpen = b;
	}
	
	public static int getScanedNum(){
		return scaned_num;
	}
	
	public static void Inventory_15693(byte[]Target_Ant,int[] fCmdRet){
		try
		{
			scanResult15693 = new HashMap<String, Integer>();
			String[] lable = HfGetData.Scan15693(Target_Ant,fCmdRet);
			if(lable == null){ 
				scaned_num = 0;
				return ;
			}
			scaned_num = lable.length;
			for (int i = 0; i < scaned_num; i++) {
				String key = lable[i];
				if(key == null || key.equals("")) return;
				int num = scanResult15693.get(key) == null ? 0 : scanResult15693.get(key);
				scanResult15693.put(key, num + 1);
			}
		}
		catch(Exception e)
		{
			e.toString();
		}
	}
	
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	
	public static class HfGetData
	{	
		private static byte Readdata_15693[]=new byte[256];
		private static byte hfTime[]={-1};
		private static int ScanNum_15693=-1;
		private static byte ScanData_15693[]=new byte[5000];

		static hflib hf = null;
		
		public static byte[] getReaddata_15693() {
			return Readdata_15693;
		}
		public static byte[] gethfVersion() {
			
			return hf.Get_TVersionInfo();
		}
		public static byte gethfTime() {
			return hfTime[0];
		}
		public static int getScanNum_15693() {
			return ScanNum_15693;
		}
		public static byte[] getScanUID_15693() {
			return ScanData_15693;
		}

		public static hflib getUhf() {
			return hf;
		}
		
		public static int OpenHf(String serial,int tty_speed,int log_swith,Context mCt )
		{	
			try
			{
				File device = new File(serial);
				if (!device.canRead() || !device.canWrite()) {
					try {
						/* Missing read/write permission, trying to chmod the file */
						Process su;
						su = Runtime.getRuntime().exec("/system/bin/su");
						String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
								+ "exit\n";
						su.getOutputStream().write(cmd.getBytes());
						if ((su.waitFor() != 0) || !device.canRead()
								|| !device.canWrite()) {
							throw new SecurityException();
						}
					} catch (Exception e) {
					}
				}
				
				 hf =new hflib(tty_speed, serial, log_swith,mCt);
				 return hf.open_reader();
			}
			catch(Exception e)
			{
				return -1;
			}
		}
		
		public static int CloseHf()
		{
			try
			{
				if(hf==null)return 0;
				hf.close_reader();
				HfData.setDeviceOpen(false);
				return 0;
			}
			catch(Exception e)
			{
				return -1;
			}
		}
	
		public static int GetHfInfo()
		{
			try
			{
				return hf.ReGetInfo();
			}
			catch(Exception e)
			{
				return -1;
			}
			
		}
			
		public static String byteToString(byte[] b){
			StringBuffer sb = new StringBuffer("");
			for(int i = 0; i < b.length; i++){
				sb.append(Integer.toHexString(b[i] & 0xff));
			}
			return sb.toString();
		}
		
		public static byte[] stringToByte(String str){
			byte[] b = new byte[str.length()];
			for(int i = 0; i < str.length(); i++){
				b[i] = Byte.valueOf(str.substring(i, i+1));
			}
			return b;
		}
		
		public static String byteToString(byte[] b, int len){
			StringBuffer sb = new StringBuffer("");
			for(int i = 0; i < len; i++){
				sb.append(Integer.toHexString(b[i] & 0xff));
			}
			return sb.toString();
		}
		
		private static Runnable soundRun = new Runnable(){
			public void run(){
				soundpool.play(soundid, 1, 1, 0, 0, 1f);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		
		public static String[] Scan15693(byte[]Target_Ant,int[] fCmdRet)
		{	
			try
			{
				ScanNum_15693=0;
				int result=hf.Inventory(Target_Ant);
				fCmdRet[0] = result;
				if(result!=0x30){
					ScanNum_15693=hf.Inventory_CardNum();
					if(ScanNum_15693==0) return null;
					soundThread.execute(soundRun);
				    ScanData_15693=hf.Inventory_15693();
				    String[] lable = new String[ScanNum_15693];
				    StringBuffer bf;
				    int j = 0, k;
				    String str;
				    byte[] UID;
				    for(int i = 0; i < ScanNum_15693; i++){
				    	bf = new StringBuffer("");
				    	UID = new byte[8];
				    	for(k = 0; k < 8; k++){
				    		str = Integer.toHexString(ScanData_15693[j+k+1] & 0xff);
				    		if(str.length() == 1){
				    			bf.append("0");//������λǰ���Ȳ�0
				    		}
				    		bf.append(str);
				    		UID[k] = ScanData_15693[j+k+1];
				    	}
				    	lable[i] = bf.toString().toUpperCase();
				    	UIDBytes.put(lable[i], UID);
				    	j = j+k+3;
				    }
				    return lable;
				}
				return null;
			}
			catch(Exception e)
			{
				return null;
			}
		}
		
		   /**
	     * Convert byte[] to hex
	     * string
	     * 
	     * @param src byte[] data
	     * @return hex string
	     */
	    public static String bytesToHexString(byte[] src) {
	        StringBuilder stringBuilder = new StringBuilder("");
	        if (src == null || src.length <= 0) {
	            return null;
	        }
	        for (int i = 0; i < src.length; i++) {
	            int v = src[i] & 0xFF;
	            String hv = Integer.toHexString(v);
				if (hv.length() == 1){
					hv = '0' + hv;
				}
	            stringBuilder.append(hv);
	        }
	        return stringBuilder.toString();
	    }

	    public static String bytesToHexString(byte[] src, int offset, int length) {
	        StringBuilder stringBuilder = new StringBuilder("");
	        if (src == null || src.length <= 0) {
	            return null;
	        }
	        for (int i = offset; i < length; i++) {
	            int v = src[i] & 0xFF;
	            String hv = Integer.toHexString(v);
	            if (hv.length() == 1) {
	                stringBuilder.append(0);
	            }
	            stringBuilder.append(hv);
	        }
	        return stringBuilder.toString();
	    }

	    public static byte[] hexStringToBytes(String hexString) {  
	        if (hexString == null || hexString.equals("")) {  
	            return null;  
	        }  
	        hexString = hexString.toUpperCase();  
	        int length = hexString.length() / 2;  
	        char[] hexChars = hexString.toCharArray();  
	        byte[] d = new byte[length];  
	        for (int i = 0; i < length; i++) {  
	            int pos = i * 2;  
	            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
	        }  
	        return d;  
	    }   
	    private static byte charToByte(char c) {  
	        return (byte) "0123456789ABCDEF".indexOf(c);  
	    } 
	}
}
