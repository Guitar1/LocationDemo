package xxq.com.locationdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/1/4 0004.
 */

public class LocationUtils {
    private static final String TAG = "LocationUtils";
    private static String WARNING = "00000000";// 报警标识
    private static String STATE = "00000002";// 状态
    private static String LATITUDE;// 纬度
    private static String LONGITUDE;// 经度
    private static String ALTITUDE;// 高度
    private static String SPEED;// 速度
    private static String BEARING; // 方向
    private static String TIME;   // 时间


    /**
     * 打开Gps设置界面
     */
    public static void openGpsSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    /**
     * 获取校验码\
     * @return  返回校验码字符串
     */
    public  String getCheckCode(String data){
        if (data==null){
            return null;
        }
        String s=data;
        byte code = 0;
        String sub;
        while (s!=null&&s.length()>0){
            sub = s.substring(0,2);
            System.out.println("输入1:"+String.format("%02X",code)+"===输入2:"+sub+"====news:"+s);
            code = (byte)(code^Short.parseShort(sub,16));
            System.out.println("输出"+String.format("%02X",code));
            s = s.substring(2,s.length());
        }
        s = String.format("%02X",code);
        System.out.println("code"+String.format("%02X",code));
        return s;
    }
    /**
     *获取消息体
     * @return
     */
    public static String getMessage(Location location){
        if (location==null){
            return null;
        }
        LONGITUDE=String.valueOf(Integer.toHexString((int)Math.round(location.getLongitude()*10E5)));
        LATITUDE=String.valueOf(Integer.toHexString((int)Math.round(location.getLatitude()*10E5)));
        ALTITUDE= Integer.toHexString((int)(Math.round(location.getAltitude())));
        SPEED= Integer.toHexString((int)Math.round(location.getSpeed()* 3.6));
        BEARING= Integer.toHexString(Math.round(location.getBearing()));
//        ALTITUDE= String.format("%04d", Math.round(location.getAltitude()));
//        SPEED= String.format("%04d", Math.round(location.getSpeed()* 3.6));
//        BEARING= String.format("%04d", Math.round(location.getBearing()));
        Date date = new Date();
//        Date date = new Date(location.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMDDhhmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        TIME = sdf.format(date);
//        LONGITUDE=String.valueOf(Integer.toHexString((int)Math.round(113.939716666*10E5)));
//        LATITUDE=String.valueOf(Integer.toHexString((int)Math.round(22.5632550000*10E5)));
//        ALTITUDE= Integer.toHexString((int)(Math.round(47.3)));
//        SPEED= Integer.toHexString((int)Math.round(4.6* 3.6));
//        BEARING= Integer.toHexString((int)Math.round(90.12));
//        TIME = "180111111449";
        LATITUDE = addZeroForNum(LATITUDE,8);
        LONGITUDE = addZeroForNum(LONGITUDE,8);
        ALTITUDE = addZeroForNum(ALTITUDE,4);
        SPEED = addZeroForNum(SPEED,4);
        BEARING = addZeroForNum(BEARING,4);
        Log.d(TAG, "getMessage: "+(int)Math.round(location.getLongitude()*10E5)+"     "+(int)Math.round(location.getLatitude()*10E5));
        Log.d(TAG, "getMessage: "+"\n"+LATITUDE+"\n"+LONGITUDE+"\n"+ALTITUDE+"\n"+SPEED+"\n"+BEARING+"\n");
        return WARNING + STATE + LATITUDE + LONGITUDE + ALTITUDE + SPEED + BEARING + TIME;
    }
    public static String addZeroForNum(String str,int strLength) {
        int strLen =str.length();
        if (strLen <strLength) {
            while (strLen< strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);//左补0
                str= sb.toString();
                strLen= str.length();
            }
        }
        return str;
    }
}
