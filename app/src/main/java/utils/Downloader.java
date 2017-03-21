package utils;

import android.os.Build;
import android.util.Log;

import java.io.File;

/**
 * Created by ponytail on 17/3/17.
 */
public class Downloader {

    private String _abi = android.os.Build.CPU_ABI;
    private String _sdk = Build.VERSION.SDK;

    public Downloader(){

    }

    public void download(){
        downloadMinitouch();
        downloadMiniCap();
        downloadMinicapLib();
    }

    public boolean downloadMinitouch(){
        String url = String.format("http://localhost/binary/minitouch/bin/%s/minitouch", _abi);
        return downloadFile(url);

    }

    public boolean downloadMiniCap(){
        String url = String.format("http://localhost/binary/minicap/bin/%s/minicap", _abi);
        return downloadFile(url);

    }

    public boolean downloadMinicapLib(){
        String url = String.format("http://localhost/binary/minicap/libs/android-%s/%s/minicap.so", _sdk, _abi);
        return downloadFile(url);

    }

    public boolean downloadFile(String url){
        Log.i("DOWNLOAD", url);
        boolean ret = false;
        return ret;
    }

}
