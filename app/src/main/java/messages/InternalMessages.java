package messages;

/**
 * Created by ponytail on 30/1/17.
 * TODO
 * 对内置的Message对象进行包装
 *
 * Thread Message
 *  Message {
 *      what : InternalMessages.Type.THREAD
 *      arg1 : MessageDefine.MessageTypes.Name.XXXXX_VALUE || ...
 *      arg2 :
 *      obj :   bytes
 *  }
 *
 * Process Message
 *  Message  {
 *      what : InternalMessages.Type.PROCESS
 *
 *  }
 *
 * Network Message
 *  Message {
 *      what :   InternalMessages.Type.NETWORK
 *      arg1 :
 *      arg2 :
 *      obj :   bytes(NetworkEnvelop)
 *  }
 */
public class InternalMessages {

    public enum Type {
        RESERVE;

        public static final int THREAD  = 0; //message between threads
        public static final int PROCESS = 1;    //messages between processes
        public static final int NETWORK = 2;    //message between networks

    }

    public enum NETWORK{
        RESERVE;

        public static final int INCOMING = 0;
        public static final int OUTGOING = 1;
    }

    public enum ThreadMessage {
        RESERVE;

        public static final int MINICAP_BANNER = 0;
        public static final int MINICAP_CONNECTION_LOST = 1;
    }
}


