package com.qiyangtech.kandibluetooth;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.qiyangtech.kandibluetooth.KdBtDevice.BtSettingChangedListener;

import java.util.Vector;


public class MainActivity extends Activity  implements View.OnClickListener, BtSettingChangedListener {

    private KdBtDevice kdBtDevice;
    private static final String   TAG =   "MainActivity";
    private Button      mCloseEchoBtn;
    private Button      mInquiryBtn;
    private TextView    mAddrsDispTextView;
    private Button      mPairBtn;
    private EditText    mPairEditText;
    private TextView    mPairSuccessTextView;
    private Button      mPinCodeBtn;
    private EditText    mPinCodeText;
    private TextView    mPinCodeSuccessTextView;

    private Button      mAudInitBtn;
    private TextView    mAudInitTextView;

    private Button      mHfInitBtn;
    private TextView    mHfInitTextView;

    private Button      mConAudAndHfBtn;
    private TextView    mConAudAndHfTextView;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initResource();


    }

    protected void initResource(){

        mCloseEchoBtn = (Button) findViewById(R.id.close_echo_button);
        mCloseEchoBtn.setOnClickListener(this);

        mInquiryBtn =   (Button) findViewById(R.id.inquiry_button);
        mInquiryBtn.setOnClickListener(this);
        mAddrsDispTextView  =   (TextView)findViewById(R.id.addrs_textView);

        mPairBtn    =   (Button) findViewById(R.id.pair_button);
        mPairBtn.setOnClickListener(this);
        mPairEditText   =   (EditText)findViewById(R.id.pairaddr_editText);
        mPairSuccessTextView  =  (TextView)findViewById(R.id.pairSuccess_textView);

        mPinCodeBtn    =   (Button) findViewById(R.id.pincode_button);
        mPinCodeBtn.setOnClickListener(this);
        mPinCodeText   =   (EditText)findViewById(R.id.pincode_editText);
        mPinCodeSuccessTextView  =  (TextView)findViewById(R.id.pincodeSuccess_textView);



        mAudInitBtn    =   (Button) findViewById(R.id.audinit_button);
        mAudInitBtn.setOnClickListener(this);
        mAudInitTextView    =   (TextView)findViewById(R.id.audinit_textView);


        mHfInitBtn      =   (Button) findViewById(R.id.handsfreeinit_button);
        mHfInitBtn.setOnClickListener(this);
        mHfInitTextView    =   (TextView)findViewById(R.id.handsfreeinit_textView);

        mConAudAndHfBtn = (Button) findViewById(R.id.conaudhf_button);
        mConAudAndHfBtn.setOnClickListener(this);
        mConAudAndHfTextView    =   (TextView)findViewById(R.id.conaudhf_textView);

        kdBtDevice  =   new KdBtDevice();
        kdBtDevice.setOnBtSettingStatusChangedListener(this);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_echo_button:
                btCloseEcho();
                break;
            case R.id.inquiry_button:
                btInquiry();
                break;
/*            case R.id.pair_button:
                btPair();
                break;
            case R.id.pincode_button:
                btPinCode();
                break;
            case R.id.audinit_button:
                btAudInit();
                break;
            case R.id.handsfreeinit_button:
                btHfInit();
                break;
            case R.id.conaudhf_button:
                btConAudAndHf();
                break;*/
            default:
                Log.d(TAG, "Nothing to do !!!");
                break;
        }
    }

    protected void btCloseEcho(){
        kdBtDevice.closeEcho();
        return ;
    }

    public void onCloseEchoChanged(boolean isTaskSuccess){


        if(isTaskSuccess) {
                        Log.d(TAG, "getCloseEchoState is "+ isTaskSuccess);
                    }else{
                        Log.d(TAG, "getCloseEchoState is "+ isTaskSuccess);
                    }
    }

    protected void btInquiry() {

        mAddrsDispTextView.setText("获取到的地址");

        kdBtDevice.inquiryBtDevice();

        return ;
    }
    public void onInquiryGetAddrs(boolean isTaskSuccess){
        Log.d(TAG, "onInquiryGetAddrs called ");
        if(isTaskSuccess){
            Log.d(TAG, "onInquiryGetAddrs isTaskSuccess "+isTaskSuccess);
            Vector<String> addrs = kdBtDevice.getBtAddrs();
            if(addrs != null){
                Log.d(TAG, "onInquiryGetAddrs addrs is not null ");
                for(int k = 0; k < addrs.size(); k++){
                    Log.d(TAG, "mBtAddrs["+k+"] = " + addrs.elementAt(k));
                    mAddrsDispTextView.append(addrs.elementAt(k));
                }
            }else{
                Log.d(TAG, "onInquiryGetAddrs addrs is  null ");
            }
        }else{
            Log.d(TAG, "onInquiryGetAddrs isTaskSuccess " + isTaskSuccess);
        }

    }
//    protected void btPair() {
//        new TaskThread(TOKEN_PAIR).start();
//        return ;
//    }
//
//    protected void btPinCode() {
//        new TaskThread(TOKEN_PINCODE).start();
//        return ;
//    }
//
//    protected void btAudInit() {
//        new TaskThread(TOKEN_AUDINIT).start();
//        return ;
//    }
//
//    protected void btHfInit() {
//        new TaskThread(TOKEN_HFINIT).start();
//        return ;
//    }
//    protected void btConAudAndHf() {
//        new TaskThread(TOKEN_CONAUDANDHF).start();
//        return ;
//    }
//
//    public class TaskThread extends Thread {
//        private int mToken  =   0;
//        TaskThread(int token) {
//            mToken  =   token;
//        }
//        @Override
//        public void run() {
//            super.run();
//            boolean isSuccess = false;
//            switch (mToken) {
//                case TOKEN_CLOSE_ECHO:
//                    if(kdBtDevice.closeEcho()) {
//                        isSuccess   =   true;
//                    }else{
//                        isSuccess   =   false;
//                    }
//                    break;
//                case TOKEN_INQUIRY:
//                    if(kdBtDevice.inquiryBtDevice()) {
//                        isSuccess   =   true;
//                    }else {
//                        isSuccess   =   false;
//                    }
//                    break;
//                case TOKEN_PAIR:
//                    String  addr    =   mPairEditText.getText().toString();
//                    Log.d(TAG, "==--TOKEN_PAIR addr " + addr+" --==" );
//                    if(kdBtDevice.pairBtDevice(addr)) {
//                        isSuccess   =   true;
//                    }else {
//                        isSuccess   =   false;
//                    }
//                    break;
//                case TOKEN_PINCODE:
//                    String  pincode    =   mPinCodeText.getText().toString();
//                    Log.d(TAG, "==--TOKEN_PINCODE pincode " + pincode+" --==" );
//                    if(kdBtDevice.pincodeBtDevice(pincode)) {
//                        isSuccess   =   true;
//                    }else {
//                        isSuccess   =   false;
//                    }
//                    break;
//                case TOKEN_AUDINIT:
//                    if(kdBtDevice.btAudInit()) {
//                        isSuccess   =   true;
//                    }else {
//                        isSuccess   =   false;
//                    }
//                    break;
//
//                case TOKEN_HFINIT:
//                    if(kdBtDevice.btHfInit()) {
//                        isSuccess   =   true;
//                    }else {
//                        isSuccess   =   false;
//                    }
//                    break;
//                case TOKEN_CONAUDANDHF:
//                    if(kdBtDevice.btConAudAndHf()){
//                        isSuccess   =   true;
//                    }else {
//                        isSuccess   =   false;
//                    }
//                    break;
//            }
//            Message message = new Message();
//            message.what = mToken;
//            Bundle date =new Bundle();// 存放数据
//            date.putBoolean("isSuccess", isSuccess);
//            message.setData(date);
//
//            MainActivity.this.myHandler.sendMessage(message);
//
//            return;
//        }
//    }
//
//
//    protected void onTaskCompleted(int token, boolean isSuccess){
//        switch (token){
//            case TOKEN_CLOSE_ECHO:
//                if(isSuccess){
//                    Log.d(TAG, "TOKEN_CLOSE_ECHO success");
//                }else{
//                    Log.d(TAG, "TOKEN_CLOSE_ECHO failed");
//                }
//                break;
//            case TOKEN_INQUIRY:
//                if(isSuccess){
//                    Log.d(TAG, "TOKEN_INQUIRY success");
//                    Vector<String> temp_addrs = kdBtDevice.getBtAddrs();
//
//                    String  temp_str = mAddrsDispTextView.getText().toString() +"\n" ;
//                    mAddrsDispTextView.setText(temp_str);
//                    Log.d(TAG, "mAddrsDispTextView + " +    mAddrsDispTextView.getText());
//                    for(int k = 0; k < temp_addrs.size(); k++){
//                        Log.d(TAG, "temp_addrs["+k+"] = " + temp_addrs.elementAt(k));
//                        String  tmp =   mAddrsDispTextView.getText().toString()+temp_addrs.elementAt(k) + "\n";
//                        mAddrsDispTextView.setText(tmp);
//                    }
//                    mPairEditText.setText(temp_addrs.elementAt(0));
//                }else{
//                    Log.d(TAG, "TOKEN_INQUIRY failed");
//                }
//                break;
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
//        }
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
