
package utils;

import utils.Deamon;
/**
 * Created by ponytail on 8/3/17.
 */
public class MinitouchDeamon {

    Deamon _deamon;
    public String _process_output;
    public MinitouchDeamon(){
        _deamon  = new Deamon("/data/local/tmp/",
                new String[] {"minitouch"},
                "minitouch",
                new String[]{"su", "-c",
                        String.format("/data/local/tmp/minitouch")
                });
    }

    public boolean isBinaryExisted(){
        return _deamon.isBinaryExisted();
    }
    public int pid(){
        return _deamon.pid();
    }

    public int start(){
        _process_output = _deamon.start();
        return pid();
    }

    public boolean stop(){
        return _deamon.stop();
    }

}
