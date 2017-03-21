package com.example.ponytail.testjeromq;


import android.os.Handler;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import messages.MessageDefine;
import com.google.protobuf.InvalidProtocolBufferException;

import server.Session;
import server.handlers.DeviceSessionController;
import server.handlers.base.TransactionCommandHandler;
import utils.MinicapDeamon;
import utils.MinitouchDeamon;

public class MainActivity extends AppCompatActivity {

    public class LogHandler extends Handler{
        public LogHandler(){

        }
        public void sendLog(String str){
            sendMessage(obtainMessage(0,str));
        }
        @Override
        public void handleMessage(Message msg) {
            String log =(String) msg.obj;
            ((TextView)findViewById(R.id.textView)).append(log);
        }
    };

    class OnlineHandler extends TransactionCommandHandler {
        public OnlineHandler(){
            super("OnlineHandler", _register_session);
        }

        @Override
        protected MessageDefine.NetworkEnvelope onExecute(String transactionId) {

            MessageDefine.DeviceOnlineMessage data = MessageDefine.DeviceOnlineMessage.newBuilder()
                    .setKey("sshkey-gen")
                    .build();

            MessageDefine.NetworkEnvelope envelope = MessageDefine.NetworkEnvelope.newBuilder()
                    .setType(MessageDefine.MessageTypes.Name.DeviceOnlineMessage)
                    .setSessionId(_session_id)
                    .setMessage(data.toByteString())
                    .build();

            return envelope;
        }

        @Override
        protected void onExecuteTimeout() {

            ((TextView)findViewById(R.id.textView)).append("登录超时");
        }

        @Override
        protected void onExecuteSuccess(MessageDefine.TransactionDoneMessage msg) throws InvalidProtocolBufferException {
            String uri = "";
            switch(msg.getType()){
                case DeviceInstanceCreated:
                    MessageDefine.DeviceInstanceCreated deviceInstanceCreated = MessageDefine.DeviceInstanceCreated.parseFrom(msg.getMessage().toByteArray());
                    uri = deviceInstanceCreated.getUri();
                    break;
                case DeviceInstanceExisted:
                    MessageDefine.DeviceInstanceExisted deviceInstanceExisted = MessageDefine.DeviceInstanceExisted.parseFrom(msg.getMessage().toByteArray());
                    uri = deviceInstanceExisted.getUri();
                    break;

                default:
                    break;
            }

            if(uri.length() > 0){
                _device_controller.connect(uri);
                ((TextView)findViewById(R.id.textView)).append("登录成功");
            }

        }

        @Override
        protected void onExecuteFail(MessageDefine.TransactionDoneMessage msg) throws InvalidProtocolBufferException {
            ((TextView)findViewById(R.id.textView)).append("登录失败");
        }

        @Override
        protected void onExceptions(Exception e) {
            e.printStackTrace();

        }
    }


    class OfflineHandler extends TransactionCommandHandler {
        public OfflineHandler(){
            super("OfflineHandler", _register_session);
        }

        @Override
        protected MessageDefine.NetworkEnvelope onExecute(String transactionId) {

            MessageDefine.DeviceOfflineMessage data = MessageDefine.DeviceOfflineMessage.newBuilder()
                    .setKey("sshkey-gen")
                    .build();

            MessageDefine.NetworkEnvelope envelope = MessageDefine.NetworkEnvelope.newBuilder()
                    .setType(MessageDefine.MessageTypes.Name.DeviceOfflineMessage)
                    .setSessionId(_session_id)
                    .setMessage(data.toByteString())
                    .build();

            return envelope;
        }

        @Override
        protected void onExecuteTimeout() {
            ((TextView)findViewById(R.id.textView)).append("登出超时");
        }

        @Override
        protected void onExecuteSuccess(MessageDefine.TransactionDoneMessage msg) throws InvalidProtocolBufferException {
            MessageDefine.DeviceInstanceDestoried deviceInstanceDestoried = MessageDefine.DeviceInstanceDestoried.parseFrom(msg.getMessage().toByteArray());
            deviceInstanceDestoried.getUri();
            _device_controller.disconnect();
            ((TextView)findViewById(R.id.textView)).append("登出成功");
        }

        @Override
        protected void onExecuteFail(MessageDefine.TransactionDoneMessage msg) throws InvalidProtocolBufferException {
            ((TextView)findViewById(R.id.textView)).append("登出失败");
        }

        @Override
        protected void onExceptions(Exception e) {
            e.printStackTrace();
        }
    }

    Session _register_session = new Session("tcp://127.0.0.1:10000");
    LogHandler _logHandler = new LogHandler();
    OnlineHandler _online_handler = new OnlineHandler();
    DeviceSessionController _device_controller = new DeviceSessionController(_logHandler);

    MinicapDeamon _minicap = new MinicapDeamon();
    MinitouchDeamon _minitouch = new MinitouchDeamon();

    @Override
    protected void onDestroy() {
        super.onDestroy();

        _register_session.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _register_session.start(GlobalConfig.uri_reigster);

        TextView tv = (TextView)findViewById(R.id.textView);
        tv.setSelected(true);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv.append("\r\n");

        //login
        final Button button = (Button) findViewById(R.id.button);
        if(button != null){
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    _online_handler.execute();
                }
            });
        }


        ///logoff
        final Button button2 = (Button) findViewById(R.id.button2);
        if(button2 != null){
            button2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    _device_controller.disconnect();
                }
            });
        }


        //
        final Button button3 = (Button) findViewById(R.id.button3);
        if(button3 != null){
            button3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    _minicap.start();
                    ((TextView)findViewById(R.id.textView)).append(_minicap._process_output);
                }
            });
        }


        //
        final Button button4 = (Button)findViewById(R.id.button4);
        if(button4 != null){
            button4.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    _minicap.stop();
                }

            });
        }



        final Button button5 = (Button) findViewById(R.id.button5);
        if(button5 != null){
            button5.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    _minitouch.start();
                    ((TextView)findViewById(R.id.textView)).append(_minitouch._process_output);
                }
            });
        }


        //
        final Button button6 = (Button) findViewById(R.id.button6);
        if(button6 != null){
            button6.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    _minitouch.stop();

                }
            });
        }
        final Button button7 = (Button) findViewById(R.id.button7);
        if(button7 != null){
            button7.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ((TextView)findViewById(R.id.textView)).setText("");

                }
            });
        }

    }



}
