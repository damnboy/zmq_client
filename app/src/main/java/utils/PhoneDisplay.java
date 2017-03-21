package utils;

import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.example.ponytail.testjeromq.JeroMQApp;

/**
 * Created by ponytail on 3/2/17.
 */
public class PhoneDisplay {

    public class ScreenSize{

        Point _screen_size = new Point();
        int _rotation;

        public ScreenSize(Display display){
            display.getRealSize(_screen_size);
            _rotation = display.getRotation();
        }
        // 屏幕宽度（像素）
        public int widthPixels(){
            return _screen_size.x;
        }

        // 屏幕高度（像素）
        public int heightPixels(){
            return _screen_size.y;
        }

        public int rotation(){
            return _rotation;
        }
        @Override
        public String toString() {
            return String.format("%dx%d@%dx%d/%d",
                    _screen_size.x,_screen_size.y,
                    _screen_size.x,_screen_size.y,
                    _rotation);
        }
    }

    public ScreenSize _screen_size;
    public PhoneDisplay(){
        WindowManager wnd_manager = (WindowManager)JeroMQApp.getContext().getApplicationContext().getSystemService(JeroMQApp.WINDOW_SERVICE);
        if(wnd_manager != null) {
            Display display = wnd_manager.getDefaultDisplay();
            _screen_size = new ScreenSize(display);
        }
    }


}
