package server.handlers.events;

import android.os.Build;
import android.os.Handler;


import messages.MessageDefine;
import utils.PhoneDisplay;
import utils.PhoneTelephony;

import java.util.Timer;
import java.util.TimerTask;

import server.Session;
import server.handlers.base.DeviceEventHandler;
/**
 * Created by ponytail on 16/3/17.
 * Session的连接，断开
 * Heartbeat处理
 */
public class SessionControlHandler extends DeviceEventHandler {

    public class HeartbeatTimer {
        class Pump extends TimerTask {
            @Override
            public void run() {
                MessageDefine.DeviceHeartbeatMessage data = MessageDefine.DeviceHeartbeatMessage.newBuilder()
                        .setSerial(PhoneTelephony.getPhoneIMEI())
                        .build();

                MessageDefine.NetworkEnvelope envelope = MessageDefine.NetworkEnvelope.newBuilder()
                        .setType(MessageDefine.MessageTypes.Name.DeviceHeartbeatMessage)
                        .setMessage(data.toByteString())
                        .build();

                sendBytes(envelope.toByteArray());
            }
        }

        Timer _timer;
        int _internal;
        public HeartbeatTimer(int internal){
            _internal = internal;
        }

        public void start(){
            if(_timer == null){
                _timer = new Timer();
            }
            _timer.schedule(new Pump(), 0, _internal);
        }

        public void stop(){
            boolean ret = false;
            if(_timer != null){
                _timer.cancel();
                _timer = null;
            }
        }
    }

    HeartbeatTimer _heartbeat_timer = new HeartbeatTimer(5000);
    public SessionControlHandler(Session session, Handler log_handler) {
        super("SessionControlHandler", session, log_handler);
    }

    @Override
    public void onSessionStarted() {

        //主动发送DeviceReady消息
        MessageDefine.DeviceReadyMessage data = MessageDefine.DeviceReadyMessage.newBuilder()
                .setSerial(PhoneTelephony.getPhoneIMEI())
                .build();

        MessageDefine.NetworkEnvelope new_envelope = MessageDefine.NetworkEnvelope.newBuilder()
                .setType(MessageDefine.MessageTypes.Name.DeviceReadyMessage)
                .setMessage(data.toByteString())
                .build();

        sendBytes(new_envelope.toByteArray());

        _heartbeat_timer.start();
    }

    @Override
    public void onSessionEnded() {

        _heartbeat_timer.stop();

    }

    @Override
    protected void onDeviceProbeMessage(MessageDefine.DeviceProbeMessage deviceProbeMessage) {
        super.onDeviceProbeMessage(deviceProbeMessage);

        MessageDefine.DevicePhoneMessage devicePhoneMessage = MessageDefine.DevicePhoneMessage.newBuilder()
                .setImei("")
                .setPhoneNumber("")
                .setIccid("")
                .setNetwork("")
                .build();

        PhoneDisplay phoneDisplay = new PhoneDisplay();

        MessageDefine.DeviceDisplayMessage deviceDisplayMessage = MessageDefine.DeviceDisplayMessage.newBuilder()
                .setId(1)
                .setWidth(phoneDisplay._screen_size.widthPixels())
                .setHeight(phoneDisplay._screen_size.heightPixels())
                .setRotation(1)
                .setXdpi(1)
                .setYdpi(1)
                .setFps(1)
                .setDensity(1)
                .setSecure(true)
                .setUrl("")
                .setSize(1)
                .build();

        MessageDefine.DeviceIdentityMessage deviceIdentityMessage = MessageDefine.DeviceIdentityMessage.newBuilder()
                .setSerial(PhoneTelephony.getPhoneIMEI()/*android.os.Build.SERIAL*/)
                //.setSerial(android.os.Build.SERIAL)
                .setPlatform(Build.VERSION.CODENAME)
                .setManufacturer(android.os.Build.MANUFACTURER)
                //.setOperator("")
                .setModel(android.os.Build.MODEL)
                .setVersion(Build.VERSION.RELEASE)
                .setAbi(android.os.Build.CPU_ABI)
                .setSdk(Integer.toString(Build.VERSION.SDK_INT))
                .setDisplay(deviceDisplayMessage)
                .setPhone(devicePhoneMessage)
                .setProduct(android.os.Build.PRODUCT)
                .build();

        MessageDefine.NetworkEnvelope new_envelope = MessageDefine.NetworkEnvelope.newBuilder()
                .setType(MessageDefine.MessageTypes.Name.DeviceIdentityMessage)
                .setMessage(deviceIdentityMessage.toByteString())
                .build();

        sendBytes(new_envelope.toByteArray());
        sendLog("接收到设备探测请求，正在收集并发送设备基本信息...\r\n");
    }

    @Override
    protected void onDeviceInstanceErrorMessage(MessageDefine.DeviceInstanceError deviceInstanceError) {
        super.onDeviceInstanceErrorMessage(deviceInstanceError);
        //如何从容的通知主线程执行Session销毁？
        _heartbeat_timer.stop();
        sendLog(String.format("连接到错误的进程实例[%s], 尝试断开到服务器的连接，请重新\r\n", deviceInstanceError.getSerial()));

    }
}
