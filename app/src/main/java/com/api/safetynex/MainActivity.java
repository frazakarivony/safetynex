package com.api.safetynex;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.api.safetynex.receiver.AppReceiver;
import com.api.safetynex.service.SafetyNexAppiService;
import com.api.safetynex.service.floatingwidget.FloatingWidgetService;
import com.nexiad.safetynexappsample.CONSTANTS;
import com.nexyad.jndksafetynex.CNxFullStat;


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
            this.finish();
        });

        close.setOnClickListener(v -> {
            MainActivity.this.finishAffinity();
            ((MainApp)getApplication()).setCurrentActivity(null);
            this.sendBroadcast(new Intent("KILL"));
        });


        if(appReceiver.isRestartOnlyActivity()){
            SafetyNexAppiService safetyNexAppiService = SafetyNexAppiService.getInstance(getApplication());
            SafetyStats safetyStats = safetyNexAppiService.getStat();

            ProgressBar p = (ProgressBar) findViewById(R.id.profress);
            p.setVisibility(View.INVISIBLE);
            SafetyStats stats =  safetyNexAppiService.getStat();
            String stattttt = "";
            LinearLayoutCompat layout = (LinearLayoutCompat) findViewById(R.id.rl);
            textView.setText("");
//            ConstraintSet set = new ConstraintSet();

  //          SpannableStringBuilder ssb = new SpannableStringBuilder();

            //TODO filtre infos + affichage
            //ssb.append(stats.getInputStat().toString());
            int idx = 0;
            int v = 50;
            ImageView img = new ImageView(this);
            img.setImageResource(R.drawable.ic_icon_6);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            img.setLayoutParams(params);
            layout.addView(img,idx);
            idx++;
            LinearLayoutCompat.LayoutParams paramsTxtView = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
            paramsTxtView.gravity = Gravity.CENTER;
            paramsTxtView.setMargins(50,10,0,10);
            while (v-->0){
                TextView tv = new TextView(this);
                tv.setLayoutParams(paramsTxtView);
                tv.setText("Niveau de risque : " + String.valueOf(v) + " environnement : " + v);
                tv.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.ic_icon_1), null, null, null);
                tv.setId(View.generateViewId());

                layout.addView(tv,idx);

                idx++;
            }
         /*   for(CNxFullStat stat : stats.getStats()){
                TextView tv = new TextView(this);
                tv.setLayoutParams(paramsTxtView);
                tv.setText("Niveau de risque : " + String.valueOf(stat.m_iRiskSlice) + " environnement : " + stat.m_iEnvConf);
                tv.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.ic_icon_1), null, null, null);

                tv.setId(View.generateViewId());
                layout.addView(tv,idx);

                idx++;
                // if(stat.m_iEnvConf != 3) {
//                    ssb.append("Niveau de risque : " + String.valueOf(stat.m_iRiskSlice) + " environnement : " + stat.m_iEnvConf + "\n", new ImageSpan(getApplicationContext(), R.drawable.ic_icon_1), 0);
                    //ssb.setSpan(new ImageSpan(getApplicationContext(), R.drawable.ic_icon_1), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//                                        text.setText(String.valueOf(stat.m_iRiskSlice));
  //                  stattttt += "Niveau de risque : " + String.valueOf(stat.m_iRiskSlice) + " environnement : " + stat.m_iEnvConf;
                //}
            }*/
    //        textView.setText(ssb, TextView.BufferType.SPANNABLE);
        }else{
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
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onReStart");
       // unregisterReceiver(appReceiver);
        super.onRestart();
        ((MainApp)getApplication()).setFirsRun(false);
        this.startActivity(this.getIntent());
        //initializeView();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        try {
            unregisterReceiver(appReceiver);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
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

//    public void restartMainActivity(){
//        Log.i(TAG, "Restart");
//        ((MainApp)this.getApplication()).setFirsRun(false);
//        this.onRestart();
//        //initializeView();
//    }

    private void continueRun(){
        Log.i(TAG, "continueRun");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

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
        if(!checkServiceRunning(FloatingWidgetService.class.getName())) {
            startService(new Intent(MainActivity.this, FloatingWidgetService.class));
        }else{
            this.finish();
        }
    }

    public boolean checkServiceRunning(String serv){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serv.equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}

