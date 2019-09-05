package com.api.safetynex;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.api.safetynex.receiver.AppReceiver;
import com.api.safetynex.service.floatingwidget.FloatingWidgetService;
import com.nexiad.safetynexappsample.CONSTANTS;


public class MainActivity extends AppCompatActivity {

    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;
    private static final int REQUEST_PERMISSIONS= 126;
    private static final String TAG =  CONSTANTS.LOGNAME.concat("MainActivityLog");
    private AppReceiver appReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        appReceiver =  AppReceiver.getInstance();
        appReceiver.setMainActivity(this);

        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter();
        // Add network connectivity change action.
        intentFilter.addAction("FLOATING_OK");
        // Set broadcast receiver priority.
        intentFilter.setPriority(100);

        Log.i(TAG, "register appReceiver FLOATING_OK");
        registerReceiver(appReceiver, intentFilter);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSIONS);

    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onReStart");
        unregisterReceiver(appReceiver);
        super.onRestart();
        Log.i(TAG, "onReStart");
        initializeView();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        unregisterReceiver(appReceiver);
        ((MainApp)getApplication()).setFirsRun(true);
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult");
        continueRun();
    }

    private void continueRun(){
        Log.i(TAG, "continueRun");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        TextView textView = findViewById(R.id.textView);
        Button close = findViewById(R.id.close);


        if(((MainApp) getApplication()).isFirstRun()){
            textView.setText(getResources().getString(R.string.init_app));
        };

        ((MainApp)getApplication()).setCurrentActivity(this);
        button.setOnClickListener(v -> {
            this.launchFloatingWidget();
        });

        close.setOnClickListener(v -> {
            MainActivity.this.finishAffinity();
            ((MainApp)getApplication()).setCurrentActivity(null);
            this.sendBroadcast(new Intent("KILL"));
        });

        this.launchFloatingWidget();
    }

    private void launchFloatingWidget(){
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && !Settings.canDrawOverlays(MainActivity.this)) {

            //If the draw over permission is not available to open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }else{
            initializeView();
        }
    }
    private void initializeView() {
       //mTts.speak(getResources().getString(R.string.loading), TextToSpeech.QUEUE_FLUSH,null,null);
        Log.i(TAG, "initializeView");
        startService(new Intent(MainActivity.this, FloatingWidgetService.class));
    }
}

