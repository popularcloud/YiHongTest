//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.serialport.uitils;

import android.os.SystemClock;
import android.serialport.SerialPort;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class ReaderBase {
    private SerialPort mSerialPort = null;
    private InputStream mInStream = null;
    private OutputStream mOutStream = null;
    private byte[] recvBuff = new byte[20000];
    private int[] recvLength = new int[1];
    private byte ComAddr = -1;
    private int RecvTimeOut = 0;

    public ReaderBase() {
    }

    private void getCRC(byte[] data, int Len) {
        try {
            int current_crc_value = '\uffff';

            int i;
            for(i = 0; i < Len; ++i) {
                current_crc_value ^= data[i] & 255;

                for(int j = 0; j < 8; ++j) {
                    if((current_crc_value & 1) != 0) {
                        current_crc_value = current_crc_value >> 1 ^ '萈';
                    } else {
                        current_crc_value >>= 1;
                    }
                }
            }

            data[i++] = (byte)(current_crc_value & 255);
            data[i] = (byte)(current_crc_value >> 8 & 255);
        } catch (Exception var6) {
            ;
        }

    }

    private boolean CheckCRC(byte[] data, int len) {
        try {
            byte[] daw = new byte[256];
            System.arraycopy(data, 0, daw, 0, len);
            this.getCRC(daw, len);
            return daw[len + 1] == 0 && daw[len] == 0;
        } catch (Exception var4) {
            return false;
        }
    }

    private int SendCMD(byte[] CMD) {
        this.recvLength[0] = 0;

        try {
            byte[] buffer = new byte[4096];
            this.mInStream.read(buffer);
            this.mOutStream.write(CMD);
            return 0;
        } catch (IOException var3) {
            var3.printStackTrace();
            return 48;
        }
    }

    private int GetCMDData(byte[] data, int[] Nlen, long endTime) {
        byte[] buffer = new byte[2560];
        int Count = 0;
        byte[] btArray = new byte[20000];
        int btLength = 0;
        long beginTime = System.currentTimeMillis();

        try {
            while(System.currentTimeMillis() - beginTime < endTime) {
                try {
                    //int Count = false;
                    SystemClock.sleep(5L);
                    Count = this.mInStream.read(buffer);
                } catch (IOException var12) {
                    var12.printStackTrace();
                }

                if(Count > 0) {
                    System.arraycopy(buffer, 0, btArray, btLength, Count);
                    btLength += Count;
                    if((btArray[0] & 255) + 1 == btLength && (btArray[0] & 255) > 3 && this.CheckCRC(btArray, btLength)) {
                        System.arraycopy(btArray, 0, data, 0, btLength);
                        Nlen[0] = btLength;
                        return 0;
                    }
                }
            }
        } catch (Exception var13) {
            var13.toString();
        }

        return 48;
    }

    public int ConnectReader(String serialpath, int speed, int logswitch) {
        try {
            this.mSerialPort = new SerialPort(new File(serialpath), speed, logswitch);
        } catch (SecurityException var5) {
            ;
        } catch (IOException var6) {
            ;
        } catch (InvalidParameterException var7) {
            ;
        }

        if(this.mSerialPort != null) {
            this.mInStream = this.mSerialPort.getInputStream();
            this.mOutStream = this.mSerialPort.getOutputStream();
            SystemClock.sleep(150L);
            return 0;
        } else {
            return 48;
        }
    }

    public int DisconnectReader() {
        if(this.mInStream != null) {
            try {
                this.mInStream.close();
                this.mOutStream.close();
                this.mSerialPort.close();
                this.mSerialPort = null;
                this.mInStream = null;
                this.mOutStream = null;
                return 0;
            } catch (IOException var2) {
                var2.printStackTrace();
                return 48;
            }
        } else {
            return 48;
        }
    }

    public int GetReaderInfo(byte[] TVersionInfo, byte[] RFU, byte[] ReaderType, byte[] TrType, byte[] ScanTime) {
        byte[] buffer = new byte[]{5, this.ComAddr, 0, -16, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        if(result == 0) {
            this.ComAddr = this.recvBuff[1];
            if(this.recvBuff[2] == 0) {
                TVersionInfo[0] = this.recvBuff[3];
                TVersionInfo[1] = this.recvBuff[4];
                RFU[0] = this.recvBuff[5];
                RFU[1] = this.recvBuff[6];
                ReaderType[0] = this.recvBuff[7];
                TrType[0] = this.recvBuff[8];
                TrType[1] = this.recvBuff[9];
                ScanTime[0] = this.recvBuff[10];
                this.RecvTimeOut = (ScanTime[0] & 255) * 100;
            }

            return this.recvBuff[2] & 255;
        } else {
            return 48;
        }
    }

    public int OpenRf() {
        byte[] buffer = new byte[]{5, this.ComAddr, 2, -16, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        return result == 0?this.recvBuff[2] & 255:48;
    }

    public int CloseRf() {
        byte[] buffer = new byte[]{5, this.ComAddr, 1, -16, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        return result == 0?this.recvBuff[2] & 255:48;
    }

    public int SetActiveANT(byte Antenna) {
        byte[] buffer = new byte[]{6, this.ComAddr, 8, -16, Antenna, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        return result == 0?this.recvBuff[2] & 255:48;
    }

    public int GetActiveANT(byte[] Antenna) {
        byte[] buffer = new byte[]{5, this.ComAddr, 9, -16, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        if(result == 0) {
            if(this.recvBuff[2] == 0) {
                Antenna[0] = this.recvBuff[3];
            }

            return this.recvBuff[2] & 255;
        } else {
            return 48;
        }
    }

    public int GetPower(byte[] Power) {
        byte[] buffer = new byte[]{5, this.ComAddr, 34, -16, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        if(result == 0) {
            if(this.recvBuff[2] == 0) {
                Power[0] = (byte)(this.recvBuff[3] & 127);
            }

            return this.recvBuff[2] & 255;
        } else {
            return 48;
        }
    }

    public int SetPower(byte Power) {
        byte[] buffer = new byte[]{6, this.ComAddr, 32, -16, Power, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        return result == 0?this.recvBuff[2] & 255:48;
    }

    public int GetReaderRunStatus(byte[] PAvolt, byte[] PAcur, byte[] pwrForward, byte[] pwrReverse, byte[] Temperature) {
        byte[] buffer = new byte[]{5, this.ComAddr, 64, -16, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        if(result == 0) {
            if(this.recvBuff[2] == 0) {
                PAvolt[0] = this.recvBuff[3];
                PAcur[0] = this.recvBuff[4];
                pwrForward[0] = this.recvBuff[5];
                pwrReverse[0] = this.recvBuff[6];
                Temperature[0] = this.recvBuff[7];
            }

            return this.recvBuff[2] & 255;
        } else {
            return 48;
        }
    }

    public int GetAntennaSWR(byte[] SWR_int, byte[] SWR_dec) {
        byte[] buffer = new byte[]{5, this.ComAddr, 65, -16, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        if(result == 0) {
            if(this.recvBuff[2] == 0) {
                SWR_int[0] = this.recvBuff[3];
                SWR_dec[0] = this.recvBuff[4];
            }

            return this.recvBuff[2] & 255;
        } else {
            return 48;
        }
    }

    public int SetSafeThreshold(byte Temp, byte SWR_int, byte SWR_dec) {
        byte[] buffer = new byte[]{8, this.ComAddr, 66, -16, Temp, SWR_int, SWR_dec, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        return result == 0?this.recvBuff[2] & 255:48;
    }

    public int GetSafeThreshold(byte[] Temp, byte[] SWR_int, byte[] SWR_dec) {
        byte[] buffer = new byte[]{5, this.ComAddr, 67, -16, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        if(result == 0) {
            if(this.recvBuff[2] == 0) {
                Temp[0] = this.recvBuff[3];
                SWR_int[0] = this.recvBuff[4];
                SWR_dec[0] = this.recvBuff[5];
            }

            return this.recvBuff[2] & 255;
        } else {
            return 48;
        }
    }

    public int OperatingVoltage(byte[] Voltage) {
        byte[] buffer = new byte[]{6, this.ComAddr, 41, -16, Voltage[0], 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        if(result == 0) {
            if(this.recvBuff[2] == 0) {
                Voltage[0] = this.recvBuff[3];
            }

            return this.recvBuff[2] & 255;
        } else {
            return 48;
        }
    }

    public int GetNoiseMeasurement(byte[] _Noise) {
        byte[] buffer = new byte[]{5, this.ComAddr, 18, -16, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        if(result == 0) {
            if(this.recvBuff[2] == 0) {
                _Noise[0] = this.recvBuff[3];
            }

            return this.recvBuff[2] & 255;
        } else {
            return 48;
        }
    }

    public int ResetToReady(byte state, byte[] uid, byte[] Errorcode) {
        byte[] buffer = new byte[14];
        if(state == 0) {
            buffer[0] = 13;
            buffer[1] = this.ComAddr;
            buffer[2] = 38;
            buffer[3] = state;
            System.arraycopy(uid, 0, buffer, 4, 8);
        } else {
            buffer[0] = 5;
            buffer[1] = this.ComAddr;
            buffer[2] = 38;
            buffer[3] = state;
        }

        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        if(result == 0) {
            if(this.recvBuff[2] == 0) {
                Errorcode[0] = this.recvBuff[0];
            } else {
                Errorcode[0] = this.recvBuff[3];
            }

            return this.recvBuff[2] & 255;
        } else {
            return 48;
        }
    }

    public int GetTagSNR(byte[] uid, byte[] SNR, byte[] _Noise) {
        byte[] buffer = new byte[14];
        buffer[0] = 13;
        buffer[1] = this.ComAddr;
        buffer[2] = 42;
        buffer[3] = -16;
        System.arraycopy(uid, 0, buffer, 4, 8);
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 1000L);
        if(result == 0) {
            if(this.recvBuff[2] == 0) {
                SNR[0] = this.recvBuff[11];
                _Noise[0] = this.recvBuff[12];
            }

            return this.recvBuff[2] & 255;
        } else {
            return 48;
        }
    }

    private int GetInventoryData_Collection1(byte[] pdata, int[] size) {
        size[0] = 0;
        byte[] buffer = new byte[2560];
        int Count = 0;
        byte[] btArray = new byte['썐'];
        int btLength = 0;
        long time1 = System.currentTimeMillis();
        long time2 = 0L;

        try {
            do {
                try {
                    Count = false;
                    SystemClock.sleep(1L);
                    Count = this.mInStream.read(buffer);
                } catch (IOException var16) {
                    var16.printStackTrace();
                }

                if(Count > 0) {
                    byte[] daw = new byte[Count + btLength];
                    System.arraycopy(btArray, 0, daw, 0, btLength);
                    System.arraycopy(buffer, 0, daw, btLength, Count);
                    int index = 0;

                    while(daw.length - index > 4 && (daw.length - index >= 5 && daw[index] == 4 || daw.length - index >= 16 && daw[index] == 15)) {
                        int len = daw[index] & 255;
                        if(daw.length < index + len + 1) {
                            break;
                        }

                        byte[] uidArr = new byte[len + 1];
                        System.arraycopy(daw, index, uidArr, 0, uidArr.length);
                        if(this.CheckCRC(uidArr, uidArr.length)) {
                            int nLen = (uidArr[0] & 255) + 1;
                            index += nLen;
                            if(nLen == 16) {
                                System.arraycopy(uidArr, 0, pdata, size[0], 16);
                                size[0] += 16;
                                time1 = System.currentTimeMillis();
                            } else if(nLen == 5 && uidArr[2] == 14) {
                                System.arraycopy(uidArr, 0, pdata, size[0], 5);
                                size[0] += 5;
                                return 0;
                            }
                        } else {
                            ++index;
                            Log.d("UID", "CRC错误");
                        }
                    }

                    if(daw.length > index) {
                        btLength = daw.length - index;
                        System.arraycopy(daw, index, btArray, 0, btLength);
                    } else {
                        btLength = 0;
                    }
                }
            } while(time2 - time1 <= (long)(this.RecvTimeOut * 3 + 8000));
        } catch (Exception var17) {
            var17.toString();
        }

        return 48;
    }

    private int GetInventoryData_Collection2(byte[] pdata, int[] size) {
        size[0] = 0;
        byte[] buffer = new byte[2560];
        int Count = 0;
        byte[] btArray = new byte['썐'];
        int btLength = 0;
        long time1 = System.currentTimeMillis();
        long time2 = 0L;

        try {
            do {
                try {
                    Count = false;
                    SystemClock.sleep(1L);
                    Count = this.mInStream.read(buffer);
                } catch (IOException var16) {
                    var16.printStackTrace();
                }

                if(Count > 0) {
                    byte[] daw = new byte[Count + btLength];
                    System.arraycopy(btArray, 0, daw, 0, btLength);
                    System.arraycopy(buffer, 0, daw, btLength, Count);
                    int index = 0;

                    while(daw.length - index > 4) {
                        if((daw.length - index < 5 || (daw[index] & 255) != 4) && (daw.length - index < 17 || (daw[index] & 255) != 16)) {
                            ++index;
                        } else {
                            int len = daw[index] & 255;
                            if(daw.length < index + len + 1) {
                                break;
                            }

                            byte[] uidArr = new byte[len + 1];
                            System.arraycopy(daw, index, uidArr, 0, uidArr.length);
                            if(this.CheckCRC(uidArr, uidArr.length)) {
                                int nLen = (uidArr[0] & 255) + 1;
                                index += nLen;
                                if(nLen == 16) {
                                    System.arraycopy(uidArr, 0, pdata, size[0], 17);
                                    size[0] += 17;
                                    time1 = System.currentTimeMillis();
                                } else if(nLen == 5 && uidArr[2] == 14) {
                                    System.arraycopy(uidArr, 0, pdata, size[0], 5);
                                    size[0] += 5;
                                    return 0;
                                }
                            } else {
                                ++index;
                            }
                        }
                    }

                    if(daw.length > index) {
                        btLength = daw.length - index;
                        System.arraycopy(daw, index, btArray, 0, btLength);
                    } else {
                        btLength = 0;
                    }
                }
            } while(time2 - time1 <= (long)(this.RecvTimeOut * 3 + 8000));
        } catch (Exception var17) {
            var17.toString();
        }

        return 48;
    }

    public int Inventory_Collection(byte Option, byte[] Target_Ant, byte[] Data, int[] cardnum) {
        byte[] buffer = new byte[11];
        byte[] UIDBuff = new byte[20000];
        buffer[0] = 10;
        buffer[1] = this.ComAddr;
        buffer[2] = 38;
        buffer[3] = -16;
        buffer[4] = Option;
        System.arraycopy(Target_Ant, 0, buffer, 5, 4);
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int turnCount = 0;
        int nLen;
        int CardIndex;
        byte[] Buff;
        int result = true;
        if((Option & 4 & 255) == 4) {
            result = this.GetInventoryData_Collection2(this.recvBuff, this.recvLength);
            nLen = this.recvLength[0];
            CardIndex = 0;
            if(result == 0) {
                if(nLen == 5) {
                    return this.recvBuff[2];
                } else {
                    do {
                        Buff = new byte[20];
                        System.arraycopy(this.recvBuff, 17 * CardIndex, Buff, 0, 17);
                        if(this.CheckCRC(Buff, 17)) {
                            System.arraycopy(Buff, 3, UIDBuff, 12 * turnCount, 12);
                            ++turnCount;
                            ++CardIndex;
                        } else {
                            ++CardIndex;
                        }

                        nLen -= 17;
                    } while(nLen - 17 >= 5);

                    cardnum[0] = CardIndex;
                    System.arraycopy(UIDBuff, 0, Data, 0, CardIndex * 12);
                    return this.recvBuff[this.recvLength[0] - 3];
                }
            } else {
                return result;
            }
        } else {
            result = this.GetInventoryData_Collection1(this.recvBuff, this.recvLength);
            nLen = this.recvLength[0];
            CardIndex = 0;
            if(result == 0) {
                if(nLen == 5) {
                    return this.recvBuff[2];
                } else {
                    do {
                        Buff = new byte[20];
                        System.arraycopy(this.recvBuff, 16 * CardIndex, Buff, 0, 16);
                        if(this.CheckCRC(Buff, 16)) {
                            System.arraycopy(Buff, 3, UIDBuff, 11 * turnCount, 11);
                            ++turnCount;
                            ++CardIndex;
                        } else {
                            ++CardIndex;
                        }

                        nLen -= 16;
                    } while(nLen - 16 >= 5);

                    cardnum[0] = CardIndex;
                    System.arraycopy(UIDBuff, 0, Data, 0, CardIndex * 11);
                    return this.recvBuff[this.recvLength[0] - 3];
                }
            } else {
                return result;
            }
        }
    }
}
