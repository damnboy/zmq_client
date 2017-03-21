package server.handlers.base;

import android.os.Handler;

import messages.MessageDefine;
import com.google.protobuf.InvalidProtocolBufferException;

import server.Session;

/**
 * Created by ponytail on 16/3/17.
 */
public abstract class DeviceEventHandler extends NetworkEnvelopHandler {

    Session _session;
    Handler _log_handler;
    public DeviceEventHandler(String tag_name, Session session, Handler log_handler){
        super("DeviceEventHandler[ " + tag_name + "]");
        _session = session;
        _log_handler = log_handler;
    }

    public abstract void onSessionStarted();
    public abstract void onSessionEnded();

    protected void sendBytes(byte[] bytes){
        _session._push.sendBytes(bytes);
    }

    protected void sendLog(String str){
        _log_handler.sendMessage(_log_handler.obtainMessage(0, str));
    }

    protected void onDeviceProbeMessage(MessageDefine.DeviceProbeMessage deviceProbeMessage){

    }

    protected void onDeviceInstanceErrorMessage(MessageDefine.DeviceInstanceError deviceInstanceError){

    }

    protected void onScreenStreamMessage(MessageDefine.ScreenStreamMessage screenStreamMessage){

    }

    protected void onScreenControlMessage(MessageDefine.ScreenControlMessage screenControlMessage){

    }

    protected void onScreenCaptureMessage(MessageDefine.ScreenCaptureMessage screenCaptureMessage){

    }

    protected void onTouchCommitMessage(MessageDefine.TouchCommitMessage touchCommitMessage){

    }

    protected void onTouchDownMessage(MessageDefine.TouchDownMessage touchDownMessage){

    }

    protected void onTouchUpMessage(MessageDefine.TouchUpMessage touchUpMessage){

    }


    protected void onTouchMoveMessage(MessageDefine.TouchMoveMessage touchMoveMessage){

    }


    protected void onTouchResetMessage(MessageDefine.TouchResetMessage touchResetMessage){

    }

    @Override
    protected void onNetworkMessage(MessageDefine.NetworkEnvelope envelope) throws InvalidProtocolBufferException {
        switch(envelope.getType()){
            case DeviceProbeMessage:
                this.onDeviceProbeMessage(MessageDefine.DeviceProbeMessage.parseFrom(envelope.getMessage()));
                break;

            //DeviceSession内部尚未关闭的dealer类型的socket会不断尝试重连服务器
            //而服务器重启之后对应的端口，可能是为其他设备所准备的。
            //因此，得到该错误之后，应主动关闭所有DeviceSession上的连接。
            case DeviceInstanceError:
                this.onDeviceInstanceErrorMessage(MessageDefine.DeviceInstanceError.parseFrom(envelope.getMessage()));
                break;

            case ScreenStreamMessage:
                this.onScreenStreamMessage(MessageDefine.ScreenStreamMessage.parseFrom(envelope.getMessage()));
                break;

            case ScreenControlMessage:
                this.onScreenControlMessage(MessageDefine.ScreenControlMessage.parseFrom(envelope.getMessage()));

            case ScreenCaptureMessage:
                this.onScreenCaptureMessage(MessageDefine.ScreenCaptureMessage.parseFrom(envelope.getMessage()));
                break;


            case TouchCommitMessage:
                this.onTouchCommitMessage(MessageDefine.TouchCommitMessage.parseFrom(envelope.getMessage()));
                break;

            case TouchDownMessage:
                this.onTouchDownMessage(MessageDefine.TouchDownMessage.parseFrom(envelope.getMessage()));
                break;

            case TouchUpMessage:
                this.onTouchUpMessage(MessageDefine.TouchUpMessage.parseFrom(envelope.getMessage()));
                break;

            case TouchMoveMessage:
                this.onTouchMoveMessage(MessageDefine.TouchMoveMessage.parseFrom(envelope.getMessage()));
                break;

            case TouchResetMessage:
                this.onTouchResetMessage(MessageDefine.TouchResetMessage.parseFrom(envelope.getMessage()));
                break;

            default:
                break;
        }
    }
}
