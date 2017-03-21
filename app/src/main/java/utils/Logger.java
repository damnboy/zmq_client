package utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Created by ponytail on 3/2/17.
 */
public class Logger {
    public String _tag;
    public int _level;

    public Logger(String name, int level){
        _tag = name;
        _level = level;
    }

    public void info(String log){
        checkAndLog(log, Log.INFO);
    }

    public void verbose(String log){
        checkAndLog(log, Log.VERBOSE);
    }

    public void error(String log){
        checkAndLog(log, Log.ERROR);
    }

    public void checkAndLog(String log, int level){
        if(_level <= level){
            Handler handler = new Handler((Looper.getMainLooper()));
            handler.sendMessage(handler.obtainMessage(0,String.format("[%s] says: %s", _tag, log)));
            Log.println(level, _tag, log);
        }
    }
}
