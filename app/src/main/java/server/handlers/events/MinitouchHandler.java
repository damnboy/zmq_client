package server.handlers.events;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;

import messages.MessageDefine;
import utils.MinitouchDeamon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import server.Session;
import server.handlers.base.DeviceEventHandler;

/**
 * Created by ponytail on 15/3/17.
 */
public class MinitouchHandler extends DeviceEventHandler {

    public class Banner{
        public int version;
        public int pid;
        public int max_contact;
        public int max_x;
        public int max_y;
        public int max_pressure;

        public Banner(){
            reset();
        }

        public void reset(){
            version = 0;
            pid = 0;
            max_contact = 0;
            max_x = 0;
            max_y = 0;
            max_pressure = 0;
        }
        /*
            v <version>
            ^ <max-contacts> <max-x> <max-y> <max-pressure>
            $ <pid>

            example:
                v 1
                ^ 10 1079 1919 255
                $ 18705
         */
        public int parseBanner(byte[] data){
            if(data != null){
                String banner_data = new String(data);
                String[] banners = banner_data.split("\n");
                if(banners.length > 3){
                    String [] version_banner = banners[0].split(" ");
                    if(version_banner.length == 2){
                        version = Integer.parseInt(version_banner[1]);
                    }
                    String [] device_banner = banners[1].split(" ");
                    if(device_banner.length == 5){
                        max_contact = Integer.parseInt(device_banner[1]);
                        max_x = Integer.parseInt(device_banner[2]);
                        max_y = Integer.parseInt(device_banner[3]);
                        max_pressure = Integer.parseInt(device_banner[4]);

                    }
                    String [] pid_banner = banners[2].split(" ");
                    if(pid_banner.length == 2){
                        pid = Integer.parseInt(pid_banner[1]);
                    }
                }
            }
            return pid;
        }
    }

    MinitouchDeamon _minitouch = new MinitouchDeamon();
    LocalSocket _socket;
    Banner _banner = new Banner();
    public MinitouchHandler(Session session, Handler log_handler) {
        super("MinitouchHandler", session, log_handler);
    }

    @Override
    public void onSessionStarted() {
        //minitouch 启动检查

    }

    @Override
    public void onSessionEnded() {
        //minitouch 关闭销毁

    }

    public void start(){
        sendLog("收到控制请求，正在尝试开启并连接到minitouch进程...\r\n");
        _minitouch.start();
        if(_socket == null) {
            _socket = new LocalSocket();
        }

        try {
            _socket.connect(new LocalSocketAddress("minitouch"));
            InputStream input_stream = _socket.getInputStream();
            byte [] data = new byte[64];
            input_stream.read(data);
            _banner = new Banner();
            _banner.parseBanner(data);

            //Got Banner, log to MainActivity
            sendLog(String.format("获取到minitouch的指纹信息:\r\npid: %d, max_x:%d, max_y:%d\r\n",
                    _banner.pid,
                    _banner.max_x,
                    _banner.max_y));

        } catch (IOException e) {
            sendLog(String.format("连接到minitouch进程失败:%s\r\n", e.getMessage()));
            e.printStackTrace();
        }

    }

    public void stop(){
        sendLog("收到控制请求，正在尝试断开与minitouch进程的连接...\r\n");
        //_minitouch.stop();
        if(_socket != null && _socket.isConnected()){
            try {
                _socket.close();
                _socket = null;
                //_banner = null;
            } catch (IOException e) {
                sendLog(String.format("尝试断开与minitouch的连接失败:%s\r\n", e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onScreenControlMessage(MessageDefine.ScreenControlMessage screenControlMessage) {
        super.onScreenControlMessage(screenControlMessage);
        if(!_minitouch.isBinaryExisted()){
            sendLog(String.format("没有找到minitouch的可执行文件\r\n"));
            return;
        }
        if(screenControlMessage.getEnable()){
            start();
        }
        else{
            stop();
        }
    }

    @Override
    protected void onDeviceInstanceErrorMessage(MessageDefine.DeviceInstanceError deviceInstanceError) {
        super.onDeviceInstanceErrorMessage(deviceInstanceError);

        sendLog(String.format("连接到错误的进程实例[%s]\r\n", deviceInstanceError.getSerial()));
        stop();
    }

    @Override
    protected void onTouchCommitMessage(MessageDefine.TouchCommitMessage touchCommitMessage) {
        super.onTouchCommitMessage(touchCommitMessage);
        this.sendTouchCommand(String.format("c\n"));
    }

    @Override
    protected void onTouchDownMessage(MessageDefine.TouchDownMessage touchDownMessage) {
        super.onTouchDownMessage(touchDownMessage);
        float x = ((float)_banner.max_x) * touchDownMessage.getX();
        float y = ((float)_banner.max_y) * touchDownMessage.getY();
        this.sendTouchCommand(String.format("d 0 %d %d 50\n", (int)x, (int)y));
    }

    @Override
    protected void onTouchUpMessage(MessageDefine.TouchUpMessage touchUpMessage) {
        super.onTouchUpMessage(touchUpMessage);
        this.sendTouchCommand(String.format("u %d\n", touchUpMessage.getContact()));
    }

    @Override
    protected void onTouchMoveMessage(MessageDefine.TouchMoveMessage touchMoveMessage) {
        super.onTouchMoveMessage(touchMoveMessage);
        float x = ((float)_banner.max_x) * touchMoveMessage.getX();
        float y = ((float)_banner.max_y) * touchMoveMessage.getY();
        this.sendTouchCommand(String.format("m 0 %d %d 50\n", (int)x, (int)y));
    }

    @Override
    protected void onTouchResetMessage(MessageDefine.TouchResetMessage touchResetMessage) {
        super.onTouchResetMessage(touchResetMessage);
    }


    private void sendTouchCommand(String command){
        try{
            if(_socket != null && _socket.isConnected()){
                OutputStream output = _socket.getOutputStream();
                output.write(command.getBytes());
            }
        }catch (IOException e){
            sendLog(String.format("尝试发送控制请求到minitouch失败:%s\r\n", e.getMessage()));

        }
    }
}