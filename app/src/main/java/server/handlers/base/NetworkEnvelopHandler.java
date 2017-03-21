package server.handlers.base;

import android.os.Message;

import messages.MessageDefine;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;

/**
 * Created by ponytail on 15/3/17.
 */
public abstract class NetworkEnvelopHandler extends SessionHandler {

    public NetworkEnvelopHandler(String tag_name){
        super(tag_name);
    }
    /*
        子类必须实现该方法，以处理无效的ProtocolBuffer消息
     */
    protected void onInvalidNetworkEnvelop(InvalidProtocolBufferException e){
        e.printStackTrace();
    }

    protected abstract void onNetworkMessage(MessageDefine.NetworkEnvelope envelope) throws InvalidProtocolBufferException;

    protected void onNetworkMessageImpl(Message msg){
        if(msg.obj == null){
            return;
        }
        try{
            ByteBuffer buffer = (ByteBuffer)msg.obj;
            MessageDefine.NetworkEnvelope envelope = MessageDefine.NetworkEnvelope.parseFrom(buffer.array());
            this.onNetworkMessage(envelope);
        }
        catch (InvalidProtocolBufferException e){
            this.onInvalidNetworkEnvelop(e);
        }
    }

    /*
    TODO
     */
    protected void onThreadMessageImpl(Message msg){
        return;

    }

    /*
    TODO
     */
    protected void onProcessMessageImpl(Message msg){
        return;

    }
}
