package xxq.com.locationdemo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText ipEt , portEt , idEt ,timeEt;
    private static SharedPreferences mSharedPreferences;
    private static final String Date="date";
    private static final String IP="IP";
    private static final String PORT="PORT";
    private static final String ID="ID";
    private static final String TIMES="TIMES";
    private TextView test;
    private   Intent intent;
    private String ip;
    private String id;
    private String port;
    private String return_time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences(Date,MODE_PRIVATE);
        test = (TextView) findViewById(R.id.test);
        ipEt = (EditText) findViewById(R.id.ip);
        portEt = (EditText) findViewById(R.id.port);
        idEt = (EditText) findViewById(R.id.id);
        timeEt = (EditText) findViewById(R.id.time);
        getDataTime();
        intent = new Intent();
        intent.setClass(this,GpsService.class);
        if (!isServiceExisted(this,GpsService.class.getName())){
            startService(intent);
        }
        test.setText("端口："+mSharedPreferences.getInt(PORT, 19000)+"回调时间："+mSharedPreferences.getInt(TIMES, 30000));
    }
    public void getDataTime(){
        String ip = mSharedPreferences.getString(IP, "");
        int port = mSharedPreferences.getInt(PORT, 19000);
        int time = mSharedPreferences.getInt(TIMES, 30000);
        String id = mSharedPreferences.getString(ID, "");
        ipEt.setText(ip);
        portEt.setText(port+"");
        idEt.setText(id);
        timeEt.setText(time+"");
    }
    public void saveUpdateData(){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        ip = ipEt.getText().toString();
        id = idEt.getText().toString();
        port = portEt.getText().toString();
        return_time = timeEt.getText().toString();
        editor.putString(IP, ip);
        editor.putInt(PORT, Integer.parseInt(port));
        editor.putString(ID, id);
        editor.putInt(TIMES, Integer.parseInt(return_time));
        editor.commit();
    }

    public void OnSave(View view){
        saveUpdateData();
        if (id.length()<11){
            Toast.makeText(this,"请输入正确的手机号码",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.setAction(GpsService.ACTION_GPSSERVICE);
        sendBroadcast(intent);
    }
    public static boolean isServiceExisted(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
