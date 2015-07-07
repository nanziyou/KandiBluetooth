/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

public class SerialPortPackage {

    private static final String TAG =   "SerialPortPackage";
	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	int tempBuffLen = 0;
	byte tempBuff[] = new byte[1024];
	double purseBalanceData ;
	String cardIDInformation;
    Queue<String>   btSerialInfoQueue  = new LinkedList<String>();
    StringBuffer    btSerialInfoBuf =   new StringBuffer();

	public class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[1024];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
                    Log.d(TAG, "mInputStream.read " + size);
                    if(size != -1) {

                        btSerialInfoBuf.append(byte2char(buffer, size), 0, size);
                        if (size > 0) {
                            parseBtSerialInfo();
                        }

                    }

				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
    private void parseBtSerialInfo(){
        int index   =   0;

        if(-1 != (index = btSerialInfoBuf.indexOf("\r\n",0))) {
            String btInfo   = btSerialInfoBuf.substring(0, index);
            btInfo = btInfo.trim();
            Log.d(TAG, "one btInfo is " + btInfo);
            btSerialInfoBuf     =   new StringBuffer(btSerialInfoBuf.substring(index+2));
            btSerialInfoQueue.offer(btInfo);
            Log.d(TAG, "left btInfo is" + btSerialInfoBuf.toString()+"||||");
        }



    }

    public String getBtSerialInfo(){
        String btInfo = null;
        if(!btSerialInfoQueue.isEmpty()){
            if((btInfo = btSerialInfoQueue.poll()) != null)
                return btInfo;
        }
        return null;
    }



	public SerialPortPackage() {
			try {
				System.out.println("-----------SerialPortActivity open");
				mSerialPort = new SerialPort(new File("/dev/ttymxc4"), 115200, 0);
				mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();

				/* Create a receiving thread */
				mReadThread = new ReadThread();
                mReadThread.start();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public boolean SerialWrite(byte[] buffer) {
		System.out.println("---------SerialWrite----------");
		try {
			mOutputStream.write(buffer);
            mOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}




    public String SerialRead(String flag) {

        StringBuffer temp = new StringBuffer();
        byte[] buf = new byte[1024];
        if (mInputStream == null) return null;
        int has_read = 0;
        int sum = 0;
        boolean is_read_flg = false;
        boolean is_read_has_read = false;
        int index = 0;
        try {
            while ((has_read = mInputStream.read(buf)) != 0) {
                if(has_read == -1) {
                    Log.d(TAG, "SerialRead has_read " + has_read);
                    return null;
                }


                temp.append(byte2char(buf, has_read), 0, has_read);
                if(-1 != (index = temp.indexOf(flag,0))) {
                    is_read_flg = true;
                }
                if(-1 != (index = temp.indexOf("+ERR",0))) {
                    Log.d(TAG, "+ERR");
                    return null;
                }
                if(is_read_flg && -1 !=temp.indexOf("\r\n",index)) {
                    return temp.toString();
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public String SerialTrytimeRead() {

        StringBuffer temp = new StringBuffer();
        byte[] buf = new byte[1024];
        if (mInputStream == null) return null;
        int has_read = 0;
        int sum = 0;
        boolean is_read_flg = false;
        boolean is_read_has_read = false;
        int index = 0;
//        long currentTime = 0;
//        long lastTime = 0;
//        int ignore = 0;
        Log.d(TAG, "SerialTrytimeRead start " + has_read);
        try {
            has_read = mInputStream.read(buf);

            //Log.d(TAG, "first read :" + byte2char(buf, has_read));
            while (has_read != 0 && has_read !=-1) {
                if(has_read != 0 && has_read != -1) {
                    //Log.d(TAG, "has_read :" + has_read + temp);
                    temp.append(byte2char(buf, has_read), 0, has_read);
                }

                has_read = mInputStream.read(buf);
                Log.d(TAG, "------has_read :" + has_read + temp);
            }
            return temp.toString();
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public char[] byte2char(byte[] t, int size)
    {
        char [] return_value = new char[size];
        Log.d(TAG, "size is " + size);
        for(int x=0; x<size; x++)
        {
            return_value[x] = (char)t[x];
        }
        return return_value;
    }
	public void onDataReceived( byte[] buffer, final int size) {
        System.out.println(new String(buffer));

		//��Ҫ���·�װЭ�������
		System.out.println("==============onDataReceived=================size:"+ size);

        for (int x = 0; x < size; x++) {
            System.out.print(""+(char)buffer[x]);
        }

/*		if (size > 1024)
			return;

	   for (int x = 0; x < size; x++) {
            tempBuff[tempBuffLen++] = buffer[x];
        }
		if (tempBuffLen >= 127) {
			byte[] cardIDInfo = new byte[8];
			byte[] purseBalanceInfo = new byte[8];

			System.out
					.println("----------------------start---------------------"
							+ tempBuffLen);
			System.out.println(bytesToHexString(tempBuff, tempBuffLen));
			cardIDInfo[0] = tempBuff[7];
			cardIDInfo[1] = tempBuff[8];
			cardIDInfo[2] = tempBuff[9];
			cardIDInfo[3] = tempBuff[10];
			this.cardIDInformation = bytesToHexString(cardIDInfo, 4);
			int t1 = tempBuff[57] & 0xFF;
			int t2 = 0xff & tempBuff[56] & 0xFF;
			this.purseBalanceData = (t1 * 256 + t2) / 100;
			System.out.println("purseBalanceData:" + purseBalanceData);
			purseBalanceInfo[0] = tempBuff[54];
			purseBalanceInfo[1] = tempBuff[55];
			purseBalanceInfo[2] = tempBuff[56];
			purseBalanceInfo[3] = tempBuff[57];
			System.out.println("Ǯ����" + purseBalanceData + "===:"
					+ bytesToHexString(purseBalanceInfo, 4));
			System.out
					.println("-----------------------end--------------------");
			tempBuffLen = 0;
			CardInfoComing(this.cardIDInformation, this.purseBalanceData+"");
		}*/
	}

	
	public static String bytesToHexString(byte[] src, int len) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < len; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
	public void SerialClose() 
	{
		if (mReadThread != null)
			mReadThread.interrupt();
		mSerialPort.close();
		mSerialPort = null;
	}
}
