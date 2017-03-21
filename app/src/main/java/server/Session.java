package server;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.example.ponytail.testjeromq.JeroMQApp;
import messages.InternalMessages;
import utils.PhoneTelephony;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ponytail on 15/3/17.
 */
public class Session
        implements Runnable
{

    public class Push extends Handler{
        private ZMQ.Socket _push;
        public Push(Looper looper){
            super(looper);
        }
        public void eof(){
            this.sendMessage(this.obtainMessage(0, null));
        }

        public void sendBytes(byte[] bytes){
            this.sendMessage(this.obtainMessage(0, bytes));
        }
        @Override
        public void handleMessage(Message msg) {
            //内建push对象

            //push上的send方法是阻塞操作，因此Session对象需要绑定一个HandlerThread
            super.handleMessage(msg);
            if(_push == null){
                _push = JeroMQApp.getZMQContent().socket(ZMQ.PUSH);
                _push.connect(_uri_pull);
            }

            if(msg.obj == null) {
                _push.send("");
            }
            //TODO
            //对obj的类型进行校验
            _push.send((byte[])msg.obj);
        }
    }

    private String _uri_remote;
    private String _uri_pull;
    private Thread _thread;
    public Push _push;
    HandlerThread _handlerThread;
    private List<Handler> _handlers = new ArrayList<Handler>();

    public Session(String uri_binding){
        _uri_pull = uri_binding;
    }

    //TODO
    //线程安全？？？
    public void registerHandler(Handler handler){
        synchronized(_handlers){
            _handlers.add(handler);
        }

    }

    public void unregisterHandler(Handler handler){
        synchronized(_handlers){
            _handlers.remove(handler);
        }
    }

    /*
        启动连接到指定uri的线程
     */
    public void start(String uri){

        if(_thread == null){
            _thread = new Thread(this);
            _uri_remote = uri;
            _thread.start();
        }

        if(_handlerThread == null){
            _handlerThread = new HandlerThread("Push", Process.THREAD_PRIORITY_BACKGROUND);
            _handlerThread.start();

            if(_push == null){
                _push = new Push(_handlerThread.getLooper());
            }
        }

        return;
    }
    /*
        关闭连接到指定uri的线程
     */
    public void stop(){
        try{
            if(_thread != null && _thread.isAlive()){
                _push.eof();
                _thread.join();
                synchronized (_handlers){
                    _handlers.clear();
                }
                _thread = null;
            }

            if(_handlerThread != null && _handlerThread.isAlive()){
                _handlerThread.quit();
                _handlerThread = null;
                _push = null;
            }
        }
        catch (InterruptedException e){

        }
    }
    /*
        重启连接到指定uri的线程

    public void restart(String uri){
        this.stop();
        this.start(uri);
    }
    */

    @Override
    public void run() {

        Log.i("[Session]", "Session thread started");
        ZMQ.Socket pull = JeroMQApp.getZMQContent().socket(ZMQ.PULL);
        ZMQ.Socket remote = JeroMQApp.getZMQContent().socket(ZMQ.DEALER);

        try{
            pull.bind(this._uri_pull);
            remote.setIdentity(PhoneTelephony.getPhoneIMEI().getBytes());
            remote.connect(this._uri_remote);

            ZMQ.Poller poller = new ZMQ.Poller(2);
            int INDEX_POLLER_PULL = poller.register(pull, ZMQ.Poller.POLLIN);
            int INDEX_POLLER_REMOTE = poller.register(remote, ZMQ.Poller.POLLIN);
            Log.i("[Session]", "Session thread ready for incoming messages");
            while(true) {
                int ret_poll = poller.poll(-1);
                if (ret_poll > 0) {

                    //pull上的消息，均转发到remote中
                    if (poller.pollin(INDEX_POLLER_PULL)) {
                        ZMsg msg_request = ZMsg.recvMsg(pull);
                        int frame_len = msg_request.size();
                        if (frame_len == 1 ) {
                            int data_len = msg_request.getFirst().size();
                            if(data_len == 0) {//pull收到长度为0的消息，视为EOF
                                break;
                            }
                        }
                        msg_request.send(remote, true);
                    }

                    //remote上的消息，转发到sessionhandler中
                    if (poller.pollin(INDEX_POLLER_REMOTE)) {
                        ZMsg msg_reply = ZMsg.recvMsg(remote);
                        int size_buffer = 0;
                        for (ZFrame f : msg_reply) {
                            size_buffer += f.size();
                        }
                        ByteBuffer buffer = ByteBuffer.allocate(size_buffer);
                        for (ZFrame f : msg_reply) {
                            buffer.put(f.getData());
                        }
                        buffer.flip();

                        synchronized(_handlers){
                            for(int i=0; i< _handlers.size(); i++){
                                Handler hander =  _handlers.get(i);
                                Message network_message = hander.obtainMessage(InternalMessages.Type.NETWORK, buffer);
                                hander.sendMessage(network_message);
                            }
                        }

                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            /*
                默认情况下zeromq的LINGER值被设置为-1，就是说如果网络读写没有进行完是不能退出的。
                例如你用一个zeromq的socket发送数据，但是数据没有发送出去。
                那么你调用zmq_term()终止zeromq上下文将阻塞，直到指定的数据正常发送。
                你可以使用zmq_setsockopt来设置一个LINGER值。
                如果LINGER被设置为0,那么zmq_term时将离开返回，并丢弃一切未完成的网络操作。
                如果LINGER被设置的大于0，那么zmq_term将等待LINGER毫秒用来完成未完成的网络读写，在指定的时间里完成或者超时都会立即返回。
             */

            pull.setLinger(0);
            remote.setLinger(0);
            pull.close();
            remote.close();
        }
        Log.i("[Session]", "Session thread exited");
    }
}
