package server.handlers;

import android.os.Handler;


import java.util.ArrayList;
import java.util.List;

import server.Session;
import server.handlers.base.DeviceEventHandler;
import server.handlers.events.MinicapHandler;
import server.handlers.events.MinitouchHandler;
import server.handlers.events.SessionControlHandler;

/**
 * Created by ponytail on 15/3/17.
 */
public class DeviceSessionController {

    Session _device_session = new Session("tcp://127.0.0.1:10001");
    String _uri_remote;
    Handler _log_handler;
    List<DeviceEventHandler> _handlers = new ArrayList<DeviceEventHandler>();

    public DeviceSessionController(Handler log_handler){
        _log_handler = log_handler;

        _handlers.add(new SessionControlHandler(_device_session, _log_handler));
        _handlers.add(new MinicapHandler(_device_session, _log_handler));
        _handlers.add(new MinitouchHandler(_device_session, _log_handler));
    }

    public void connect(String uri){
        if(_uri_remote == null || _uri_remote.compareTo(uri) != 0){
            _device_session.stop();
            _device_session.start(uri);

            for(int i=0; i<_handlers.size(); i++){
                DeviceEventHandler device_event_handler = _handlers.get(i);
                device_event_handler.onSessionStarted();
                _device_session.registerHandler(device_event_handler);
            }
        }

        _uri_remote = uri;
    }

    public void disconnect(){

        for(int i=0; i<_handlers.size(); i++){
            DeviceEventHandler device_event_handler = _handlers.get(i);
            device_event_handler.onSessionEnded();
            _device_session.unregisterHandler(device_event_handler);
        }

        _device_session.stop();
        _uri_remote = null;
    }
}
