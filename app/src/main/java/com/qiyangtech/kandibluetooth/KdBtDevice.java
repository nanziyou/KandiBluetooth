package com.qiyangtech.kandibluetooth;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.os.Handler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;


import android_serialport_api.SerialPortPackage;

/**
 * Created by zhn on 2015/6/18.
 */
public class KdBtDevice {
    private static final String TAG =   "KdBtDevice";
    private static final String AT_CLOSE_ECHO   =   "AT+ECHO=0\r\n";
    private static final String AT_INQUIRY      =  "AT+INQUIRY\r\n";
    private static final String AT_PAIR      =  "AT+PAIR=";
    private static final String AT_PINCODE      =  "AT+PINCODE=";
    private static final String AT_AUDINIT        = "AT+AUDINIT\r\n";
    private static final String AT_HANDSFREEINIT        = "AT+HANDSFREE\r\n";
    private static final String AT_CONAUD           = "AT+CONAUD=";
    private static final String KEY_TASKTOKEN       =   "TaskToken";
    private static final String KEY_FLAGSUCCESS       =  "isTaskSuccess";
    private Vector<String> mBtAddrs =   new Vector<String>() ;
    private String  curBtTryingPairAddr  =   null;
    private String  curBtPairedAddr      = null;

    private int btaddr_num  =   0;
    private BtSettingChangedListener   mBtSettingStatusListener;

    private enum BtTaskToken{
        TOKEN_CLOSE_ECHO,
        TOKEN_INQUIRY,
        TOKEN_PAIR,
        TOKEN_PINCODE,
        TOKEN_AUDINIT,
        TOKEN_HFINIT,
        TOKEN_CONAUDANDHF
    }
    protected class BtTask {
        BtTask(BtTaskToken token, Message message){
            mToken    =   token;
            mMsg        =  message;
            isComplete  =  false;
        }
        BtTaskToken mToken;
        Message mMsg;
        boolean isComplete;
    }

    private  BtTask curTask = null;
    private  boolean mIsTaskSuccess = false;
    private Queue<BtTask>   taskQueue = new LinkedList<BtTask>();


    android.os.Handler mTaskCompletedHandler = null;
    SerialPortPackage   mSerialPortPackage;
    BtInfoHandler  mBtInfoHandler;
    BtTaskProducer mBtTaskProducer;



    public KdBtDevice(){

        //主动任务完成时，调用变化的接口实现（Ui变更）
        mTaskCompletedHandler = new android.os.Handler() {
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                BtTaskToken token = BtTaskToken.values()[bundle.getInt(KEY_TASKTOKEN)];
                boolean isSuccess = bundle.getBoolean(KEY_FLAGSUCCESS);
                onTaskCompleted(token,isSuccess);
                super.handleMessage(msg);
            }
        };
        //接收串口信息、解析（以\r\n为分隔符进行解析）
        mSerialPortPackage = new SerialPortPackage();
        //对接收到的信息进行处理，实现主动任务完成之后的接口调用（如Ui变化），做相应的处理。
        mBtInfoHandler  =   new BtInfoHandler();
        //按顺序执行任务，每次只执行一次主动任务
        mBtTaskProducer     = new BtTaskProducer();

    }







    public class BtInfoHandler {
        BtInfoHandlerThread mBtInfoHandlerThread = null;
        BtInfoHandler(){
            mBtInfoHandlerThread    =   new BtInfoHandlerThread();
            mBtInfoHandlerThread.start();
        }

        public class BtInfoHandlerThread extends Thread {
            @Override
            public void run() {
                super.run();
                String btInfo = null;
                boolean isTaskSuccess =  false;
                Log.d(TAG, "BtInfoHandlerThread start");
                while (!isInterrupted()) {
                    if ((btInfo = mSerialPortPackage.getBtSerialInfo()) != null) {
                        Log.d(TAG, "btInfo is" + btInfo + "xxxx");
                        //先检查是否是被动任务，被连接，被呼叫

                        //再检查是否是主动任务，主动查询，主动拨号
                        synchronized (curTask) {
                            if (curTask != null) {
                                Log.d(TAG, "curTask.mToken is " + curTask.mToken);
                                Log.d(TAG, "curTask.isComplete is " + curTask.isComplete);

                                switch (curTask.mToken) {
                                    //可以用于检测蓝牙设备是否存在
                                    case TOKEN_CLOSE_ECHO:
                                        if(btInfo.equals("+OK") || btInfo.equals("+ERR")){
                                            if (btInfo.equals("+OK")) {
                                                isTaskSuccess = true;
                                                Log.d(TAG, "mEchoState is " + isTaskSuccess);
                                            } else if (btInfo.equals("+ERR")) {
                                                isTaskSuccess = false;
                                                Log.d(TAG, "mEchoState is " + isTaskSuccess);
                                            }
                                            curTask.isComplete = true;
                                            Bundle bundle = new Bundle();
                                            bundle.putInt(KEY_TASKTOKEN,curTask.mToken.ordinal());
                                            bundle.putBoolean(KEY_FLAGSUCCESS, isTaskSuccess);
                                            mTaskCompletedHandler.sendMessage(bundle);
                                            mBtSettingStatusListener.onCloseEchoChanged(isTaskSuccess);

                                        }

                                        break;
                                    case TOKEN_INQUIRY:
                                        if (btInfo.equals("+OK")) {
                                            isTaskSuccess = true;
                                            Log.d(TAG, "mInquiryState is " + isTaskSuccess);
                                        } else if (btInfo.equals("+ERR")) {
                                            isTaskSuccess = false;
                                            Log.d(TAG, "mInquiryState is " + isTaskSuccess);
                                        }else if(btInfo.indexOf("Num_Dev") != -1){
                                            String num = btInfo.substring(btInfo.indexOf(":")+1,btInfo.length());
                                            btaddr_num = Integer.parseInt(num);
                                            Log.d(TAG, "--------NUM is " + num + "btaddr_num"+btaddr_num+"!!!");
                                        }else if(btInfo.matches("^[0-9].*")){
                                            Log.d(TAG,"btInfo matched");
                                            if(btaddr_num != 0) {
                                                //地址范例1,0x14fadbadfdf57
                                                mBtAddrs.addElement(btInfo.substring(btInfo.indexOf(",")+1,btInfo.length()));
                                                Log.d(TAG,"addr is"+btInfo.substring(btInfo.indexOf(",")+1,btInfo.length())+"||");
                                                btaddr_num--;
                                                if(btaddr_num   ==  0) {
                                                    curTask.isComplete = true;
                                                    Log.d(TAG,"TOKEN_INQUIRY completed");
                                                    mBtSettingStatusListener.onInquiryGetAddrs(isTaskSuccess);
                                                }
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public class BtTaskProducer {
        BtTaskProducerThread mBtTaskProducerThread = null;
        BtTaskProducer(){
            mBtTaskProducerThread    =   new BtTaskProducerThread();
            mBtTaskProducerThread.start();
        }

        public class BtTaskProducerThread extends Thread {
            protected void ExecuteTask(BtTask task){
                Bundle  bundle = task.mMsg.getData();
                String at_cmd = bundle.getString("at_cmd");
                byte[] temp = at_cmd.getBytes();
                mSerialPortPackage.SerialWrite(temp);
            }

            @Override
            public void run() {
                super.run();
                BtTask task =   null;
                Log.d(TAG, "BtTaskProducerThread start");
                while (!isInterrupted()) {
                    synchronized (taskQueue){
                        if((task = taskQueue.peek()) != null){
                            if(curTask == null){
                                ExecuteTask(task);
                            }
                            if(curTask != null){
                                synchronized (curTask){
                                    if(curTask.isComplete == true){
                                        ExecuteTask(task);
                                    }else{
                                        continue;
                                    }
                                }
                            }
                            curTask =   task;
                            taskQueue.poll();//赋值给当前任务，出队
                        }
                    }
                }
            }
        }
    }



    public void setOnBtSettingStatusChangedListener(BtSettingChangedListener l) {
        mBtSettingStatusListener = l;
    }

    public interface BtSettingChangedListener {
        /**
         * Called when the background color of current note has just changed
         */
        void onCloseEchoChanged(boolean isSuccess);

        /**
         * Called when user set clock
         */
        void onInquiryGetAddrs(boolean isSuccess);
//
//        /**
//         * Call when user create note from widget
//         */
//        void onWidgetChanged();
//
//        /**
//         * Call when switch between check list mode and normal mode
//         * @param oldMode is previous mode before change
//         * @param newMode is new mode
//         */
//        void onCheckListModeChanged(int oldMode, int newMode);
    }

    public interface BtTaskCompleteListener {

        void onBtTaskSuccess();
    }





/*
 * close the echo function via serial port
 * return true if Success
 */
    public boolean closeEcho(){

        String at_cmd = AT_CLOSE_ECHO;
        //byte[] temp = at_cmd.getBytes();
        //mSerialPortPackage.SerialWrite(temp);

        Message message = new Message();
        message.what = BtTaskToken.TOKEN_CLOSE_ECHO.ordinal();
        Bundle date =new Bundle();// 存放数据
        date.putString("at_cmd", at_cmd);
        message.setData(date);


        BtTask  task    =   new BtTask(BtTaskToken.TOKEN_CLOSE_ECHO, message);
        synchronized (taskQueue){
            taskQueue.add(task);
            Log.d(TAG, "taskQueue add task  " + task);
        }

        return true;
    }


    /*
     *  Inquiry the ambient bluetooth devices addresses
     *  Function getBtAddrs() get the bluetooth addresses
     */

    public boolean inquiryBtDevice(){
        String at_cmd = AT_INQUIRY;
        Message message = new Message();
        message.what = BtTaskToken.TOKEN_INQUIRY.ordinal();
        Bundle date =new Bundle();// 存放数据
        date.putString("at_cmd", at_cmd);
        message.setData(date);

        BtTask  task    =   new BtTask(BtTaskToken.TOKEN_INQUIRY, message);
        synchronized (taskQueue){
            taskQueue.add(task);
            Log.d(TAG, "taskQueue add task  " + task);
        }
        return true;
    }

    protected void parseBtAddrs(String strAddrs){
        String[]  temp_str  =   strAddrs.split("\r\n");
/*        for(int i = 0; i < temp_str.length; i++){
            Log.d(TAG, "tem_str["+i+"] = " + temp_str[i] + "temp_str len" + temp_str.length);
        }*/

/*        for(int j = 2; j < temp_str.length; j++){
            String[] index_addrs = temp_str[j].split(",");
            mBtAddrs.addElement(index_addrs[1]);
        }*/
        for(int j = 0; j < temp_str.length; j++){
            String[] index_addrs = temp_str[j].split(",");
            //mBtAddrs.addElement(index_addrs[1]);
            for(int k = 0; k < index_addrs.length; k++) {
                Log.d(TAG, "index_addrs["+k+"] is " + index_addrs[k]);
                if( k == 1 ) {
                    if(index_addrs[1].indexOf("0x") ==  0)
                        mBtAddrs.addElement(index_addrs[1]);
                }

            }

        }
/*        for(int k = 0; k < mBtAddrs.size(); k++){
            Log.d(TAG, "mBtAddrs["+k+"] = " + mBtAddrs.elementAt(k));
        }*/
    }
    public Vector<String> getBtAddrs(){
        return mBtAddrs;
    }



    public boolean  pairBtDevice(String addr) {
        String at_cmd = AT_PAIR+addr+"\r\n";
        byte[] temp = at_cmd.getBytes();
        mSerialPortPackage.SerialWrite(temp);
        if(addr ==  curBtTryingPairAddr) {
            Log.d(TAG, "Trying to pair but can not pair the same address twice");
            return false;
        }


        String return_value;

        if(null !=(return_value = mSerialPortPackage.SerialRead("+IND=Please Enter PIN Code"))){
            Log.d(TAG, "return_value" + return_value);
            curBtTryingPairAddr = addr;
            return true;
        }

        return false;
    }

    public boolean  pincodeBtDevice(String pincode) {
        String at_cmd = AT_PINCODE+pincode+"\r\n";
        byte[] temp = at_cmd.getBytes();
        mSerialPortPackage.SerialWrite(temp);

        String return_value;

        if(null !=(return_value = mSerialPortPackage.SerialRead("+OK"))){
            Log.d(TAG, "return_value" + return_value);
            curBtPairedAddr = curBtTryingPairAddr;
            return true;
        }

        return false;
    }
    public boolean  btAudInit() {
        String at_cmd = AT_AUDINIT;
        byte[] temp = at_cmd.getBytes();
        mSerialPortPackage.SerialWrite(temp);

        String return_value;

        if(null !=(return_value = mSerialPortPackage.SerialRead("+OK"))){
            Log.d(TAG, "return_value" + return_value);
            return true;
        }

        return false;
    }
    public boolean  btHfInit() {
        String at_cmd = AT_HANDSFREEINIT;
        byte[] temp = at_cmd.getBytes();
        mSerialPortPackage.SerialWrite(temp);

        String return_value;

        if(null !=(return_value = mSerialPortPackage.SerialRead("+OK"))){
            Log.d(TAG, "return_value" + return_value);
            return true;
        }

        return false;
    }

    public boolean  btConAudAndHf(){
        String at_cmd   =   AT_CONAUD +curBtPairedAddr + "\r\n";
        Log.d(TAG, at_cmd);
        byte[] temp = at_cmd.getBytes();
        mSerialPortPackage.SerialWrite(temp);

        String return_value;

        if(null !=(return_value = mSerialPortPackage.SerialRead("+OK"))){
            Log.d(TAG, "return_value" + return_value);
            return true;
        }

        return false;
    }

        protected void onTaskCompleted(BtTaskToken token, boolean isSuccess) {
            switch (token) {
                case TOKEN_CLOSE_ECHO:
                    if (isSuccess) {
                        Log.d(TAG, "TOKEN_CLOSE_ECHO success");

                    } else {
                        Log.d(TAG, "TOKEN_CLOSE_ECHO failed");
                    }
                    break;
                case TOKEN_INQUIRY:
                    if (isSuccess) {

                    } else {
                        Log.d(TAG, "TOKEN_INQUIRY failed");
                    }
                    break;
//            case TOKEN_PAIR:
//                if(isSuccess){
//                    Log.d(TAG, "TOKEN_PAIR success");
//                    mPairSuccessTextView.setText("尝试配对成功");
//                }else{
//                    Log.d(TAG, "TOKEN_PAIR failed");
//                    mPairSuccessTextView.setText("尝试配对失败");
//                }
//                break;
//            case TOKEN_PINCODE:
//                if(isSuccess){
//                    Log.d(TAG, "TOKEN_PINCODE success");
//                    mPinCodeSuccessTextView.setText("配对成功");
//                }else{
//                    Log.d(TAG, "TOKEN_PINCODE failed");
//                    mPinCodeSuccessTextView.setText("配对失败");
//                }
//                break;
//            case TOKEN_AUDINIT:
//                if(isSuccess){
//                    Log.d(TAG, "TOKEN_AUDINIT success");
//                    mAudInitTextView.setText("音频初始化成功");
//                }else{
//                    Log.d(TAG, "TOKEN_AUDINIT failed");
//                    mAudInitTextView.setText("音频初始化失败");
//                }
//                break;
//            case TOKEN_HFINIT:
//                if(isSuccess){
//                    Log.d(TAG, "TOKEN_HFINIT success");
//                    mHfInitTextView.setText("免提初始化成功");
//                }else{
//                    Log.d(TAG, "TOKEN_HFINIT failed");
//                    mHfInitTextView.setText("免提初始化失败");
//                }
//                break;
//            case TOKEN_CONAUDANDHF:
//                if(isSuccess){
//                    Log.d(TAG, "TOKEN_CONAUDANDHF success");
//                    mConAudAndHfTextView.setText("连接手机蓝牙和音频蓝牙成功");
//                }else{
//                    Log.d(TAG, "TOKEN_CONAUDANDHF failed");
//                    mConAudAndHfTextView.setText("连接手机蓝牙和音频蓝牙失败");
//                }
//                break;
            }
        }




}
