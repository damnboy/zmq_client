package server.handlers.base;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import messages.InternalMessages;

/**
 * Created by ponytail on 15/3/17.
 *
 * what:
 *  ThreadMessage   0
 *  ProcessMessage  1
 *  NetworkMessage  2
 *
 * arg1:
 *
 * arg2:
 *
 * object:
 *  anything~~~
 *
 *
 */
public abstract class SessionHandler extends Handler {

    private String _tag_name;
    public SessionHandler(String tag_name){
        _tag_name = tag_name;
    }


    /*
        子类实现该方法，实现对ProtocolBuffer消息的处理

        switch(message.getType()){
            case ScreenControlMessage:
                onScreenControlMessage(MessageDefine.ScreenControlMessage.parseFrom(message.getMessage());
                break;

            default:
                break;
        }
    */
    protected abstract void onNetworkMessageImpl(Message msg);

    /*
    子类实现该方法，实现对来自其他线程的消息的处理
     */
    protected abstract void onThreadMessageImpl(Message msg);

    /*
    子类实现该方法，实现对来自其他进程的消息的处理
     */
    protected abstract void onProcessMessageImpl(Message msg);


    @Override
    public void handleMessage(Message msg) {
        Log.i(_tag_name, "GOT MESSAGE!!!");
        try {
            switch (msg.what) {
                case InternalMessages.Type.THREAD:
                    onThreadMessageImpl(msg);
                    break;

                case InternalMessages.Type.PROCESS:
                    onProcessMessageImpl(msg);
                    break;

                case InternalMessages.Type.NETWORK:
                    onNetworkMessageImpl(msg);
                    break;
            }
        }
        catch (Exception e){
            //一个万能的try－catch来应付所有在消息处理过程中尚未捕获的异常
            e.printStackTrace();
        }
    }

}
