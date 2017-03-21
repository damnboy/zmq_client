package com.example.ponytail.testjeromq;

import android.app.Application;
import android.content.Context;

import org.zeromq.ZMQ;

/**
 * Created by ponytail on 7/12/16.
 */
public class JeroMQApp extends Application {
    private static Context context;
    private static ZMQ.Context _zmq_context;
    @Override
    public void onCreate() {
        super.onCreate(); //得到一个应用程序级别的Context
        context = getApplicationContext();

        _zmq_context = ZMQ.context(1);


   }

    public static Context getContext() {
        return context;
    }

    public static ZMQ.Context getZMQContent(){
        return _zmq_context;
    }
}
