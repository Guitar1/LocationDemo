package xxq.com.locationdemo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
/**
 * Created by Administrator on 2018/1/4 0004.
 */

public class GpsService extends Service {
    private static final String TAG = "GpsService";
    private Context mContext;
    private static final String Date="date";
    private static final String IP="IP";
    private static final String PORT="PORT";
    private static final String ID="ID";
    private static final String TIMES="TIMES";
//    public static final String GPS_IP = "171.217.92.216";
//    public static final String GPS_PORT = "32001";
    public static  String GPS_IP = "125.67.64.225";
    public static String GPS_PORT = "19000";
    private Socket mSocketLocation ;
    private int mPort;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private static LocationManager mLocationManager;
    private static LocationUtils locationUtils;
    private static Location mLocation;
    private Handler mHandler;
    private static TelephonyManager mTelephonyMgr;
    public static final String SIGN = "7E";//标识位
    private static String NEW_ID = "0200"; //消息ID
    private static String NEW_PROPERTY = "001C";//消息体属性
    private static String PHONE_NUMBER ="00000000000";// 手机号
    private static String NEW_SERIAL_NUMBER = "0000";// 消息流水号
    private static String HEADERS;//消息头
    private static String CHECK_CODE ="00"; //校验码
    private static String NEWS;//消息体
    private static int CALLBALK_TIME =30*1000;//回调时间
    private static MyReceiver myReceiver;
    public static final String ACTION_GPSSERVICE = "action_gpsservice";



    @Override
    public void onCreate() {
        mContext = getApplicationContext();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null&&intent.getAction() == null ){
            return START_NOT_STICKY;
        }
        locationUtils = new LocationUtils();

        mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //获取定位管理器
        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        // 判断GPS是否正常启动
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            locationUtils.openGpsSettings(mContext);
        }
        // 通过GPS获取定位的位置数据
        mLocation = mLocationManager.getLastKnownLocation(mLocationManager.GPS_PROVIDER);
        locationUtils.getMessage(mLocation);
        myReceiver = new MyReceiver();
//        getDate();
        IntentFilter filter = new IntentFilter(ACTION_GPSSERVICE);
        registerReceiver(myReceiver,filter);
        startSocket(GPS_IP, GPS_PORT);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,mLocationListener);
        ProvideCommand();
        return START_NOT_STICKY;
    }
    private void getdates(){
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Date, MODE_PRIVATE);
        String ip = mSharedPreferences.getString(IP, "");
        int port = mSharedPreferences.getInt(PORT, 19000);
        int callbalk_time = mSharedPreferences.getInt(TIMES, 30000);
        String id = mSharedPreferences.getString(ID, "00000000000");
        if (ip!=null&&!ip.equals("")){
            GPS_IP = ip;
        }
        if (port!=0){
            GPS_PORT = port+"";
        }
        if (id!=null&&!id.equals("")){
            PHONE_NUMBER = id;
        }
        if (callbalk_time!=0){
            CALLBALK_TIME = callbalk_time;
        }
    }
    private void ProvideCommand(){
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(this, CALLBALK_TIME);
                if (getDate()!=null){
                    sendCommand(ByteUtil.hexStringToBytes(getDate()));
                }
                Log.e(TAG,ByteUtil.bytes2hex(ByteUtil.hexStringToBytes(getDate())));
            }
        }, CALLBALK_TIME);
    }


    private void startSocket(final String host, final String port) {
        Log.e(TAG, "startSocket: " );
        new Thread(){
            @Override
            public void run() {
                super.run();
                mPort = Integer.parseInt(port);
                try {
                    SocketAddress service = new InetSocketAddress(host, mPort);
                    mSocketLocation = new Socket();
                    mSocketLocation.connect(service,15*1000);
                    mSocketLocation.setReuseAddress(true);
                    mSocketLocation.setKeepAlive(true);
                    mInputStream = mSocketLocation.getInputStream();
                    mOutputStream = mSocketLocation.getOutputStream();
                }catch (Exception e) {
                    e.printStackTrace();
                    reset();
                }
            }
        }.start();
    }

    private void sendCommand(byte[] cmd){
        if (cmd==null){
            return;
        }
        try {
            mOutputStream.write(cmd);
            mOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void reset() {
        try {
            if (mSocketLocation != null) {
                mSocketLocation.close();
            }
            if (mInputStream !=null){
                mInputStream.close();
            }
            if (mOutputStream !=null){
                mOutputStream.close();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取消息头
     */
    public static String getHeaders() {
        //获取手机号码
        if (mTelephonyMgr.getLine1Number()==null){
            PHONE_NUMBER="000000000000";
        }
        if (PHONE_NUMBER.length()<12){
            PHONE_NUMBER="0"+PHONE_NUMBER;
        }
        return NEW_ID + NEW_PROPERTY + PHONE_NUMBER + NEW_SERIAL_NUMBER;
    }

    /**
     * 获取数据
     */
    public static String getDate(){
        if (mLocation != null) {
            NEWS = locationUtils.getMessage(mLocation).toLowerCase();
            HEADERS = getHeaders();
            CHECK_CODE = locationUtils.getCheckCode(HEADERS+NEWS);
//            Log.e(TAG, "getDate: "+ SIGN+HEADERS+NEWS+CHECK_CODE +SIGN);
            return SIGN+HEADERS+NEWS+CHECK_CODE +SIGN;
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reset();
        unregisterReceiver(myReceiver);
    }
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            locationUtils.getMessage(mLocation);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
            locationUtils.getMessage(mLocationManager.getLastKnownLocation(s));
        }

        @Override
        public void onProviderDisabled(String s) {
            locationUtils.getMessage(null);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_GPSSERVICE)) {
                getdates();
                Log.e(TAG, "onStartCommand: " + "接收广播成功");
                if (mSocketLocation!=null){
                    try {
                        mSocketLocation.close();
                        startSocket(GPS_IP,GPS_PORT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        }
}
