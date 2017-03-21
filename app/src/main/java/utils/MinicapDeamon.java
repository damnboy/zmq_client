package utils;

/**
 * Created by ponytail on 8/3/17.
 */
public class MinicapDeamon {

    Deamon _deamon;
    public String _process_output;
    public MinicapDeamon(){
        PhoneDisplay phoneDisplay = new PhoneDisplay();
        _deamon  = new Deamon("/data/local/tmp/minicap-devel/",
                new String[] {"minicap.so", "minicap"},
                "minicap",
                new String[]{"su", "-c",
                        "LD_LIBRARY_PATH=/data/local/tmp/minicap-devel/ &&" + String.format("/data/local/tmp/minicap-devel/minicap -x 0.5 -z 5 -Q 20 -P %s", phoneDisplay._screen_size.toString() )
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
