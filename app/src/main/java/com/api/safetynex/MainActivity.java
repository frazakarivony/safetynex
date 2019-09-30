package com.api.safetynex;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.api.safetynex.receiver.AppReceiver;
import com.api.safetynex.service.SafetyNexAppiService;
import com.api.safetynex.service.floatingwidget.FloatingWidgetService;
import com.nexiad.safetynexappsample.CONSTANTS;
import com.nexyad.jndksafetynex.CNxFullStat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.components.Description;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

            ProgressBar p = (ProgressBar) findViewById(R.id.profress);
            p.setVisibility(View.INVISIBLE);
            LinearLayoutCompat layout = (LinearLayoutCompat) findViewById(R.id.rl);
            textView.setText("");

            //TODO filtre infos + affichage
            SafetyNexAppiService safetyNexAppiService = SafetyNexAppiService.getInstance(getApplication());
            SafetyStats stats = safetyNexAppiService.getStat();
            BarChart barChart = findViewById(R.id.barchart);

            ArrayList<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(1f, stats.getOutputStat().m_fRiskHist[0]));
            entries.add(new BarEntry(2f, stats.getOutputStat().m_fRiskHist[1]));
            entries.add(new BarEntry(3f, stats.getOutputStat().m_fRiskHist[2]));
            entries.add(new BarEntry(4f, stats.getOutputStat().m_fRiskHist[3]));
            entries.add(new BarEntry(5f, stats.getOutputStat().m_fRiskHist[4]));

            BarDataSet bardataset = new BarDataSet(entries, "Risque");

            // load data and animate it

            String[] labels = new String[5];
            labels[0]="0-20%";
            labels[1]="20-40%";
            labels[2]="40-60%";
            labels[3]="60-80%";
            labels[4]="80-100%";

            bardataset.setStackLabels(labels);

            BarData data = new BarData(bardataset);
            barChart.setData(data); // set the data and list of lables into chart

            Description description = new Description();
            description.setText("Bilan de la conduite");
            barChart.setDescription(description);  // set the description

            bardataset.setColors(getColor(R.color.greenCol), getColor(R.color.yellowCol),getColor(R.color.orCol), getColor(R.color.orangeCol),getColor(R.color.redCol) );
            barChart.animateY(5000);

            int idx = 0;
            int v = 50;

            LinearLayoutCompat.LayoutParams paramsTxtView = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
            paramsTxtView.gravity = Gravity.CENTER;
            paramsTxtView.setMargins(50,10,0,10);
            List<CNxFullStat> cNxFullStatSorted = Arrays.stream(stats.getStats()).sorted((o1,o2)->Float.compare(o1.m_fDuration ,o2.m_fDuration)).collect(Collectors.toList());

            for(CNxFullStat stat : cNxFullStatSorted){
                if(stat.m_iEnvConf != 3 && stat.m_iRiskSlice != 0) {
                    TextView tv = new TextView(this);
                    tv.setLayoutParams(paramsTxtView);
                    tv.setText(this.pointEnvironnement(stat.m_iEnvConf)+ "prise de risque : " + this.getPercentOfRisk(stat.m_iRiskSlice));
                    tv.setCompoundDrawablesRelativeWithIntrinsicBounds(getPuceColor(stat.m_iRiskSlice), null, null, null);

                    tv.setId(View.generateViewId());
                    layout.addView(tv, idx);

                    idx++;
                }
            }
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

    private String getPercentOfRisk(int riskRange){

        String retour = "";
        switch (riskRange){
            case 0:
                retour = "0 et 10%";
                break;
            case 1:
                retour = "10 et 20%";
                break;
            case 2:
                retour = "20 et 30%";
                break;
            case 3:
                retour = "30 et 40%";
                break;
            case 4:
                retour = "40 et 50%";
                break;
            case 5:
                retour = "50 et 60%";
                break;
            case 6:
                retour = "60 et 70%";
                break;
            case 7:
                retour = "70 et 80%";
                break;
            case 8:
                retour = "80 et 90%";
                break;
            case 9:
                retour = "90 et 100%";
                break;

        }
        return retour;
    }

    private Drawable getPuceColor(int percentRisk){
        Drawable retour = null;
        if(0 <= percentRisk && percentRisk < 2){
            retour = getDrawable(R.drawable.ic_0_20dp);
        }
        if(2 <= percentRisk && percentRisk < 4){
            retour = getDrawable(R.drawable.ic_40_60dp);
        }
        if(4 <= percentRisk && percentRisk < 6){
            retour = getDrawable(R.drawable.ic_20_40dp);
        }
        if(6 <= percentRisk && percentRisk < 8){
            retour = getDrawable(R.drawable.ic_60_80dp);
        }
        if(8 <= percentRisk && percentRisk <= 9){
            retour = getDrawable(R.drawable.ic_80_100dp);
        }
        return retour;
    }

    private String pointEnvironnement(int env){
        String environnement = "Rien ";
        if(env == 0){
            environnement = "Signalisation, ";
        }
        if(env == 1){
            environnement = "Virage dangereux, ";
        }
        if(env == 2){
            environnement = "Intersection, ";
        }
        return environnement;
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

