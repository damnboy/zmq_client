package utils;

import android.telephony.TelephonyManager;

import com.example.ponytail.testjeromq.JeroMQApp;

/**
 * Created by ponytail on 3/2/17.
 */
public class PhoneTelephony {
    /*
 IMEI(international mobile Equipment identity)
    手机唯一标识码；
 */
    static public class PhoneIdentity{
        public String IMEI = "";
        public String IMEISoftwareVersion = "";
        public PhoneIdentity(TelephonyManager tele_mgr){
            this.IMEI = tele_mgr.getDeviceId();
            this.IMEISoftwareVersion = tele_mgr.getDeviceSoftwareVersion();
        }
    }

    /*
     IMSI(international mobiles subscriber identity)
        国际移动用户号码标识，这个一般大家是不知道，GSM必须写在卡内相关文件中；
        IMSI由MCC、MNC、MSIN组成，
        MCC（移动国家号码）
            3位数字组成
            唯一地识别移动客户所属的国家，我国为460；
        MNC（网络id）
            2位数字组成
            用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；
        MSIN（移动客户识别码），
            采用等长11位数字构成
            唯一地识别国内GSM移动通信网中移动客户。

     MSISDN(mobile subscriber ISDN)
        用户号码，这个是我们说的139，136那个号码；

     ICCID(ICC identity)
        集成电路卡标识，这个是唯一标识一张卡片物理号码的；
     */
    static public class SimcardIdentity{
        public String IMSI = "";
        public String IMSI_MCC = "";
        public String IMSI_MNC = "";
        public String IMSI_MSIN = "";
        public String network_name = "";
        public String ICCID = "";
        public String MSISDN = "";

        public SimcardIdentity(TelephonyManager tele_mgr){
            if( TelephonyManager.SIM_STATE_READY == tele_mgr.getSimState()) {
                this.MSISDN = tele_mgr.getLine1Number();     //取出MSISDN，很可能为空
                this.ICCID = tele_mgr.getSimSerialNumber();  //取出ICCID
                this.IMSI = tele_mgr.getSubscriberId();     //取出IMSI
                if(this.IMSI.length() != 0){
                    this.IMSI_MCC = this.IMSI.substring(0,3);
                    this.IMSI_MNC = this.IMSI.substring(3,5);
                    this.IMSI_MSIN = this.IMSI.substring(5, this.IMSI.length() - 1);
                }

                this.network_name = tele_mgr.getNetworkOperatorName();
            }

        }

    }



    static public String getPhoneIMEI(){
        TelephonyManager tele_mgr = (TelephonyManager) JeroMQApp.getContext().getSystemService(JeroMQApp.TELEPHONY_SERVICE);
        PhoneIdentity phone_identity = new PhoneIdentity(tele_mgr);
        return phone_identity.IMEI;
    }
}
