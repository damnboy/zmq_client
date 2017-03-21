package server.handlers.events;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;

import messages.MessageDefine;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;

import server.Session;
import server.handlers.base.DeviceEventHandler;

import utils.MinicapDeamon;
/**
 * Created by ponytail on 15/3/17.
 */
public class MinicapHandler extends DeviceEventHandler implements Runnable{

    public class Banner  {

        public int version = -1;
        public int banner_size = -1;
        public int pid = 0;
        public int width_pixels = 0;
        public int height_pixels = 0;
        public int v_width_pixels = 0;
        public int v_height_pixels = 0;
        public int orientation = 0;
        public int quirk = 0;

        public Banner(){
            reset();
        }

        public void reset(){
            version = -1;
            banner_size = -1;
            pid = 0;
            width_pixels = 0;
            height_pixels = 0;
            v_width_pixels = 0;
            v_height_pixels = 0;
            orientation = 0;
            quirk = 0;
        }

        public int parseBanner(byte[] data){
            reset();
            version = data[0];
            banner_size = data[1];
            for(int i=2; i< banner_size; i++){
                switch(i){
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        pid += (data[i] & 0xff) << ((i-2) *8);
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        width_pixels += (data[i] & 0xff) << ((i-6) *8);
                        break;
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                        height_pixels += (data[i] & 0xff) << ((i-10) *8);
                        break;
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                        v_width_pixels += (data[i] & 0xff) << ((i-14) *8);
                        break;
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                        v_height_pixels += Math.abs(data[i] << ((i-18) *8));
                        break;
                    case 22:
                        orientation = data[i];
                        break;
                    case 23:
                        quirk = data[i];
                        break;
                }
            }
            return pid;
        }
    }

    MinicapDeamon _minicap = new MinicapDeamon();
    Banner _banner = new Banner();
    LocalSocket _socket;
    Thread _thread;
    public MinicapHandler(Session session, Handler log_handler) {
        super("MinicapHandler", session, log_handler);
    }


    @Override
    public void onSessionStarted() {
        //minicap 启动检查

    }

    @Override
    public void onSessionEnded() {
        //minicap 关闭销毁

    }
    /*
    public Banner get() throws RuntimeException{

        reset();

        LocalSocket banner_socket = new LocalSocket();
        try{
            banner_socket.connect(new LocalSocketAddress("minicap"));
            InputStream input_stream = banner_socket.getInputStream();
            byte [] data = new byte[24];
            input_stream.read(data);
            this.parseBanner(data);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage() + "from [MINICAP]");
        }finally {
            try {
                banner_socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }
    */

    @Override
    public void run() {
        if (_socket == null) {
            return;
        }
        sendLog("minicap数据抓取线程启动...\r\n");
        try {
            InputStream input_stream = _socket.getInputStream();
            while (true) {
                byte[] buffer_recv = new byte[65535];
                int bytes = input_stream.read(buffer_recv);
                if (bytes <= 0) {
                    break;
                }
                ByteString byte_string = ByteString.copyFrom(buffer_recv, 0, bytes);
                MessageDefine.ScreenFrameMessage data = MessageDefine.ScreenFrameMessage.newBuilder()
                        .setFrame(byte_string)
                        .build();

                MessageDefine.NetworkEnvelope envelope = MessageDefine.NetworkEnvelope.newBuilder()
                        .setType(MessageDefine.MessageTypes.Name.ScreenFrameMessage)
                        .setMessage(data.toByteString())
                        .build();
                sendBytes(envelope.toByteArray());
            }

        }
        catch (Exception e) {
            sendLog(String.format("minicap数据抓取线程异常:%s\r\n", e.getMessage()));
            e.printStackTrace();
        }
    }

    private void start(){
        _minicap.start();
        sendLog("收到控制请求，正在尝试连接到minicap进程...\r\n");
        if(_socket == null) {
            _socket = new LocalSocket();
        }

        try {
            _socket.connect(new LocalSocketAddress("minicap"));
            InputStream input_stream = _socket.getInputStream();
            byte [] data = new byte[24];
            input_stream.read(data);
            if(_banner == null){
                _banner = new Banner();
            }
            _banner.parseBanner(data);
            String s = String.format("获取到minitouch的指纹信息:\r\npid: %d, height_pixels:%d, width_pixels:%d\r\n",
                    _banner.pid,
                    _banner.height_pixels,
                    _banner.width_pixels);
            sendLog(s);
        } catch (IOException e) {
            sendLog(String.format("连接到minitcap进程失败:%s\r\n", e.getMessage()));
            e.printStackTrace();

        }


        if(_socket.isConnected()){
            if(_thread == null){
                _thread = new Thread(this);
                _thread.start();
            }
        }
    }
    private void stop(){
        _minicap.stop();
        sendLog("收到控制请求，正在尝试断开与minicap进程的连接...\r\n");
        if(_socket != null && _socket.isConnected()){
            try {
                _socket.close();
                _socket = null;
                _banner = null;
            } catch (IOException e) {
                sendLog(String.format("尝试断开到minicap的连接失败:%s\r\n", e.getMessage()));
                e.printStackTrace();
            }
        }

        if(_thread != null /*&& _thread.isAlive()*/){
            try {
                _thread.join();
                _thread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onScreenStreamMessage(MessageDefine.ScreenStreamMessage screenStreamMessage) {
        super.onScreenStreamMessage(screenStreamMessage);
        if(!_minicap.isBinaryExisted()){
            sendLog(String.format("没有找到minicap的可执行文件\r\n"));
            return;
        }
        if(screenStreamMessage.getEnable()){
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
}
