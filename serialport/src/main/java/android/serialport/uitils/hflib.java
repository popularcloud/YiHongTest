package android.serialport.uitils;

import android.content.Context;
import android.util.Log;

import com.rfid.reader.ReaderBase;


public class hflib {
	public ReaderBase reader = new ReaderBase();
	private static final String TAG = null;
	private byte addr=(byte)255;
	
	private	byte TVersionInfo[]={-1,-1};
	private	byte ReaderType[]={-1};
	private	byte TrType[]={-1,-1};
	private	byte ScanTime[]={-1}; 
	private byte state[]=new byte[1];
	private int Cardnum[]=new int[1];
	private byte pOUcharUIDList[]=new byte[50000];
	private byte Data_15693[]=new byte[256];
	
	private byte Transparent_len[]=new byte[1];
	private byte Transparent_data[]=new byte[256];
	private byte Errorcode[]=new byte[1];
	private int uhf_speed;
	private byte uhf_addr;
	private Context mContext;
	private String Serial;
	private int logswitch;

	public hflib(int tty_speed,String serial, int log_swith,Context mCt)
	{	
		uhf_speed = tty_speed;
		uhf_addr = addr;
		mContext = mCt;
		Serial = serial;
		logswitch = log_swith;
	}
	public int open_reader()
	{
		try
		{
			int reply1=1;
			reply1=reader.ConnectReader(Serial,uhf_speed,logswitch);
			reply1=0;
			return reply1;
		}
		catch(Exception e)
		{
			return -1;
		}
	 }
	
	public int ReGetInfo()
	{
		try
		{
			byte RFU[]=new byte[2];
			int result=reader.GetReaderInfo(TVersionInfo, RFU,ReaderType, TrType, ScanTime);
			if  (result==0)
			return 0;
			return -1;
		}
		catch(Exception e)
		{
			return -1;
		}
	}

   public int close_reader()
   {
	   reader.DisconnectReader();
	   return 0;
   }
     
   public byte[] Get_TVersionInfo()
   {
	   return TVersionInfo;
   }
    
   public byte[] Get_ReaderType()
   {
	   return ReaderType;
   }
     
   public  byte[] Get_TrType()
   {
	   return TrType;
   }
      
   public byte[] Get_ScanTime()
   {
	   return ScanTime;
   } 
   
   public int Close_Rf()
   {
	   return reader.CloseRf(); 
   }
   
   public int Open_Rf()
   {
	   return reader.OpenRf();
   }
  
   private void ClearArray(byte[] source)
   {
	   for(int i=0;i<source.length;i++) {
		   source[i]=0;
	   }
   }
   
   public int Inventory(byte[]Target_Ant)
   {
	   try
	   {
		   Cardnum[0]=0;
		   ClearArray(pOUcharUIDList);
		   int[] num1 = new int[1];
		   num1[0]=0;
		   byte[] uidlist= new byte[50000];
		   byte Option=1;
		   int result = reader.Inventory_Collection(Option,Target_Ant,uidlist,num1);
		   Log.d("����:",String.valueOf(num1[0]));
		   if(num1[0]>0)
		   {
			   pOUcharUIDList = uidlist.clone();
			   Cardnum[0] = num1[0];
		   }
		   
		   return result;
	   }
	   catch(Exception e)
	   {
		   return -1;
	   }
   }
   
   public byte[] Inventory_15693()
   {
	   return pOUcharUIDList;
   } 
   
   public int Inventory_CardNum()
   {
	   return Cardnum[0];
   } 
      
     
   public byte[] Readdata_15693()
   {
	   return Data_15693;
   } 
     
   public byte Tran_Len()
   {
	   return Transparent_len[0];
   }
  
    public byte[] Tran_Data()
    {
	   return Transparent_data;
    }
}

