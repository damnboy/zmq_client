package server.handlers.base;

import android.os.Message;


import messages.InternalMessages;
import messages.MessageDefine;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Timer;
import java.util.TimerTask;

import server.Session;

/**
 * Created by ponytail on 15/3/17.
 */
public abstract class TransactionCommandHandler extends NetworkEnvelopHandler /*implements Command*/{

    protected Timer _timer;
    protected String _session_id;
    private Session _session;
    public TransactionCommandHandler(String cmd, Session session){
        super(cmd);

        _session_id = java.util.UUID.randomUUID().toString();
        _session = session;
    }

    @Override
    public void handleMessage(Message msg) {
        if(msg.what == InternalMessages.Type.THREAD && msg.arg1 == 1 && msg.arg2 == 1){
            this.onExecuteTimeout();
            _timer = null;
        }
        super.handleMessage(msg);
    }

    @Override
    protected void onNetworkMessage(MessageDefine.NetworkEnvelope envelope) throws InvalidProtocolBufferException{

        _session.unregisterHandler(this);

        if(_timer != null){
            _timer.cancel();
            _timer = null;
        }

        MessageDefine.TransactionDoneMessage transaction_done = MessageDefine.TransactionDoneMessage.parseFrom(envelope.getMessage().toByteArray());
        boolean is_success = transaction_done.getSuccess();
        if(is_success){
            this.onExecuteSuccess(transaction_done);
        }
        else{
            this.onExecuteFail(transaction_done);
        }
    }

    //TODO
    //TimerTask的执行环境不在主线程中
    class TimeoutHandler extends TimerTask {
        TransactionCommandHandler _cmd;
        public TimeoutHandler(TransactionCommandHandler cmd){
            _cmd = cmd;
        }
        @Override
        public void run() {
            //卸载sub上注册的customhandler
            _session.unregisterHandler(_cmd);
            _cmd.sendMessage(_cmd.obtainMessage(InternalMessages.Type.THREAD, 1, 1));
        }
    }
    //@Override
    public void execute() {
        try{
            _session.registerHandler(this);
            MessageDefine.NetworkEnvelope envelope = this.onExecute(_session_id);
            if(_timer == null){
                //启动定时器，由定时器执行onExecuteTimeout
                //Timer与Thread类似，关闭之后必须重新构造对象
                //java.lang.IllegalStateException: Timer was canceled
                _timer = new Timer();
                _timer.schedule(new TimeoutHandler(this), 5000);
            }
            Message msg = _session._push.obtainMessage(0, envelope.toByteArray());
            _session._push.sendMessage(msg);
        }
        catch(Exception e){
            if(_timer != null) {
                _timer.cancel();
                _timer = null;
            }
            this.onExceptions(e);
        }
    }

    abstract protected void onExceptions(Exception e);

    abstract protected MessageDefine.NetworkEnvelope onExecute(String transactionId);

    abstract protected void onExecuteSuccess(MessageDefine.TransactionDoneMessage msg) throws InvalidProtocolBufferException;

    abstract protected void onExecuteFail(MessageDefine.TransactionDoneMessage msg) throws InvalidProtocolBufferException;

    abstract protected void onExecuteTimeout();
}
