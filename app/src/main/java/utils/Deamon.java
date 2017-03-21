package utils;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ponytail on 8/3/17.
 */
public class Deamon {
    String _binary_path;
    String[] _binary_file;

    Process _process;
    String _process_name;
    String[] _process_cmds;

    public Deamon(String binary_path, String[] binary_file,
                 String process_name, String[] process_cmds ){
        _binary_path = binary_path;
        _binary_file = binary_file;
        _process_name = process_name;
        _process_cmds = process_cmds;
    }

    public boolean isBinaryExisted(){
        int existed = 0;
        for(int i=0; i<_binary_file.length; i++){
            File file = new File(_binary_path + _binary_file[0]);
            existed += file.exists() ? 1 : 0;
        }
        return existed == _binary_file.length;
    }


    public int pid(){
        int pid = -1;
        ShellUtils.CommandResult result = ShellUtils.execCommand(String.format("ps | grep %s", _process_name), true, true);
        if(result.result == 0 && result.successMsg.length() != 0){
            String [] r = result.successMsg.split("\\s+");
            pid = Integer.parseInt(r[1]);
        }
        return pid;
    }

    public String start(){
        int pid = this.pid();
        if( pid > 0){
            return "";
        }

        if(_process != null){
            _process.destroy();
            _process = null;
        }

        /*
            分别读取来自目标进程stdout与stderr的输出，因为有些奇葩程序喜欢往stderr里写
            同时为了避免InputStream的read方法阻塞，建议在read之前，使用available检查是否存在可读的字节
            available并不是一个可靠的方法，
            使用stdout与stderr进行交互，需要目标进程在代码级别上的配合
         */
        StringBuffer result = new StringBuffer();
        try {
            ProcessBuilder builder = new ProcessBuilder(_process_cmds);
            builder.directory(new File(_binary_path));
            _process = builder.start();

            //su授权需要时间
            Thread.sleep(2000);

            InputStream error = _process.getErrorStream();
            while(error.available() > 0){
                byte[] ree = new byte[1024];
                error.read(ree);
                String e = new String(ree);
                result = result.append(e);
            }

            if(error != null){
                error.close();
            }

            InputStream in = _process.getInputStream();
            while(in.available() > 0) {
                byte[] re = new byte[1024];
                in.read(re);
                String i = new String(re);
                result = result.append(i);
            }
            if (in != null) {
                in.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        catch (IllegalThreadStateException e){
            //Process has not yet terminated: 23411
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result.toString();
        //return this.pid();
    }


    public boolean stop(){
        int pid = this.pid();
        if( pid < 0){
            return true;
        }

        ShellUtils.CommandResult result =
                ShellUtils.execCommand(String.format("kill %d", pid), true, true);

            if(_process != null){

                try {
                    _process.destroy();
                    _process.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    _process = null;
                }
            }

        return this.pid() <=0 ;
    }
}
