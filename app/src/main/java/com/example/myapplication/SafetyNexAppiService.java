package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nexiad.safetynexappsample.CNxDemoData;
import com.nexiad.safetynexappsample.CNxInputAPI;
import com.nexiad.safetynexappsample.CONSTANTS;
import com.nexyad.jndksafetynex.CNxFullStat;
import com.nexyad.jndksafetynex.CNxLicenseInfo;
import com.nexyad.jndksafetynex.CNxRisk;
import com.nexyad.jndksafetynex.CNxUserStat;
import com.nexyad.jndksafetynex.JNDKSafetyNex;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import static com.nexyad.jndksafetynex.CNxRisk.CNxAlert.TONE_ALERT;

class SafetyNexAppiService {

    private boolean mIsRunning;
    private CNxDemoData mData;
    private CNxInputAPI mInpuAPI;
    private int mCount;
    private JNDKSafetyNex mJniFunction;
    private CNxRisk mNxRisk;
    private String LicenseFileBnd;
    private String LicenseFileNx;
    private String MapSubPath;
    private String ResMapSubPath;
    private String UnlockKey;
    private int Language;
    private String TAG;
    private String mMessage;
    private Handler mTimerHandler;
    private Runnable mTimerRunnable;
    private MainApp app;
    private View mView;
    private static final Integer LOW_LOWLEVEL_RISK = 0;
    private static final Integer MEDIUM_LOWLEVEL_RISK = 1;
    private static final Integer HIGH_LOWLEVEL_RISK = 2;

    public MainActivityColorEnum getColorEnum() {
        return colorEnum;
    }

    private MainActivityColorEnum colorEnum;

    SafetyNexAppiService(Application app, View view) {
        this.TAG  = "SafetyNexService";
        this.app = (MainApp)app;
        this.mView = view;
        String workingPath = CONSTANTS.DEMO_WORKING_PATH;
        String resPath = CONSTANTS.APP_ASSETS_MAPS_PATH;
        String inputFile = workingPath + CONSTANTS.DEMO_IN_FILE_NAME;
        String outputFile = workingPath + CONSTANTS.DEMO_OUT_FILE_NAME;
        LicenseFileBnd = workingPath + CONSTANTS.DEMO_LICENSE_FILE;
        LicenseFileNx = workingPath + CONSTANTS.DEMO_LICENSE_FILE_NEXYAD;
        MapSubPath = workingPath + CONSTANTS.MAP_SUB_PATH;
        ResMapSubPath = resPath + CONSTANTS.MAP_SUB_PATH;
        UnlockKey = CONSTANTS.DEMO_UNLOCK_KEY;
        Language = 0;

        try {
            Language = Integer.parseInt(String.valueOf(CONSTANTS.DEMO_LANGUAGE));
        }catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }

        mCount = 0;

        mIsRunning = true;
        mData = new CNxDemoData(inputFile, outputFile);
        mJniFunction = JNDKSafetyNex.GetInstance(this.app.getApplicationContext());
        mInpuAPI = new CNxInputAPI();
        mNxRisk = new CNxRisk();
        mTimerHandler = new Handler();
        /*mTimerRunnable = new Runnable() {
            @Override
            public void run() {
                updateRiskFromFile();
                doNextStep();
            }
        };*/
    }

    void initAPI() {
        Log.v(TAG, "initAPI");

        this.mIsRunning = true;
        mTimerHandler.postDelayed(mTimerRunnable, CONSTANTS.DEMO_FIRST_DELAY);
        this.mJniFunction = JNDKSafetyNex.GetInstance(this.app.getApplicationContext());
        CNxLicenseInfo tempLicInfo = new CNxLicenseInfo();
        this.copyMapsOnDeviceStorage();
        boolean isLicOK = this.mJniFunction.Birth(this.LicenseFileBnd, this.MapSubPath, this.UnlockKey, this.Language, this.LicenseFileNx, tempLicInfo);
        if(!isLicOK) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                }
            }, CONSTANTS.DEMO_EXIT_DELAY);
        }
        this.mJniFunction.SetTreshMin(20);
        this.mJniFunction.UserStart();
    }

    private String getMessageCustomer(long [] prmCurrEhorizon, CNxInputAPI mInpuAPI) {
        String TempMessage;
        float risque = Math.round(this.mNxRisk.m_fRisk * 100);
        risque = (risque < 0 ? 0 : risque);
        if (prmCurrEhorizon != null && prmCurrEhorizon.length > 4) {
            TempMessage = "Count " + this.mCount
                    + "; Speed " + Math.round(mInpuAPI.getmSpeed()) + "km/h"
                    + "; State " + this.mNxRisk.m_iSafetyNexEngineState
                    + "; Risk " + risque + "%"
                    + "; TTS:" +this.mNxRisk.m_TAlert.m_sTextToSpeech
                    + "; NxAlert :" + this.mNxRisk.m_TAlert.m_iNxAlertValue
                    + "; len :" +  prmCurrEhorizon[1];
        } else {
            TempMessage = "Count " + this.mCount
                    + "; Speed " + Math.round(mInpuAPI.getmSpeed()) + "km/h"
                    + "; State " + this.mNxRisk.m_iSafetyNexEngineState
                    + "; Risk " +risque + "%"
                    + "; TTS:" +this.mNxRisk.m_TAlert.m_sTextToSpeech
                    + "; NxAlert :" + this.mNxRisk.m_TAlert.m_iNxAlertValue;
        }
        return TempMessage;
    }

    private void closeAPI() {
        Log.v(TAG, "closeAPI");
        this.mJniFunction.UserStop();
        float grade = this.mJniFunction.GetUserGrade();
        CNxUserStat InputUserStat = new CNxUserStat();
        CNxUserStat OutUserStat = new CNxUserStat();
        this.mJniFunction.GetSIUserStat(InputUserStat );
        this.mJniFunction.GetLocalUserStat(OutUserStat, InputUserStat);
        float duration = 0;
        float distance = 0;
        CNxFullStat[] FullStat = this.mJniFunction.GetCloudStat();
        for (CNxFullStat cNxFullStat : FullStat) {
            duration += cNxFullStat.m_fDuration;
            distance += cNxFullStat.m_fDistance;
        }
        this.mJniFunction.StoreCloudStatToMemory(CONSTANTS.DEMO_WORKING_PATH);
        mMessage = "Grade = " + grade
                + "; duration =" + duration
                + "; distance =" + distance;
        this.mJniFunction.Death();
    }

    private void copyMapsOnDeviceStorage(){
       File externalFilesDir =  new File(MapSubPath);
        AssetManager assetManager = this.app.getAssets();

        try {
            if(!externalFilesDir.exists()){
                externalFilesDir.mkdir();

                String[] li = assetManager.list(CONSTANTS.MAP_SUB_PATH);
                for(String f : li){
                  InputStream in = assetManager.open(CONSTANTS.MAP_SUB_PATH+File.separator+f);
                  String path = externalFilesDir.getAbsolutePath()+File.separator+f;
                  File fil = new File(path);
                  fil.createNewFile();
                    OutputStream out = new FileOutputStream(path);

                    // Copy the bits from instream to outstream
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doNextStep() {
        upDateHMI();
        if(this.mIsRunning) {
            if(this.mData.isEOF()) {
                closeAPI();
                this.mIsRunning = false;
                upDateHMI();
            } else {
                mTimerHandler.postDelayed(mTimerRunnable, CONSTANTS.DEMO_RUN_DELAY);
            }
        } else {
            closeAPI();
            upDateHMI();
        }
    }

    private void upDateHMI() {
        Log.v(TAG, mMessage);
    }

    private void writeDatas(String mMessage){
        this.mData.WriteData(mMessage);
//        ((TextView)this.mView.findViewById(R.id.fabHeadMsg)).setText(mMessage);
    }

    public String getRisk(CNxInputAPI cNxInputAPI){
        Log.v(TAG, "getRisk "+printCNxInputAPI(cNxInputAPI));
        //Set GPS
        this.mJniFunction.SetGPSData(cNxInputAPI.getmLat(), cNxInputAPI.getmLon(), cNxInputAPI.getNbOfSat(), cNxInputAPI.getmCap(), cNxInputAPI.getmSpeed(), cNxInputAPI.getmTimeDiffGPS());
        //Set Accel and get Risk
        this.mJniFunction.GetAccelDataWithRisk(cNxInputAPI.getmAccelX(), cNxInputAPI.getmAccelY(), cNxInputAPI.getmAccelZ(), this.mNxRisk);
        this.colorEnum = updateRiskInfo();
        long [] CurrEhorizon = this.mJniFunction.GetCurrEHorizon();
        //Update Output
        if (CurrEhorizon != null) {
            mMessage = getMessageCustomer(CurrEhorizon, cNxInputAPI);
            this.writeDatas(mMessage);
        } else {

            mMessage = "Count " + (this.mCount+1)
                    + "; No e-Horizon";
        }
        this.mCount++;
        return mMessage;
    }

    private String printCNxInputAPI(CNxInputAPI cNxInputAPI){
        return "NB Sat : "+cNxInputAPI.getNbOfSat()+" X: "+cNxInputAPI.getmAccelX()+" Y: "+cNxInputAPI.getmAccelY()+" Z: "+cNxInputAPI.getmAccelZ()+" speed: "+cNxInputAPI.getmSpeed()*3.6;
    }

    void stop(){
        this.mIsRunning=false;
    }

    private MainActivityColorEnum updateRiskInfo(){
        MainActivityColorEnum color = MainActivityColorEnum.LOW_OF_LOWLEVEL;
        switch (mNxRisk.m_iSafetyNexEngineState) {
            case CNxRisk.RISK_AVAILABLE:
                /* risque faible à moyen
                  0 à 30% ok vert
                  30 à 50% ok orange claire
                  50 à 70% ok mais orange ++
                * */
                if (mNxRisk.m_TAlert.m_iVisualAlert == CNxRisk.CNxAlert.VISUAL_ALERT_1 ){
                    Integer risk = manageLowRiskLevel(mNxRisk.m_fRisk * 100);
                    switch (risk){
                        case 0 :
                            color = MainActivityColorEnum.LOW_OF_LOWLEVEL;
                            break;
                        case 1 :
                            color = MainActivityColorEnum.MEDIUM_OF_LOWLEVEL;
                            break;
                        default:
                            color = MainActivityColorEnum.HIGH_OF_LOWLEVEL;
                            break;
                    }
                }
                /*Risk higher than THRESHOLD_ALERT1 70 à 90% warning*/
                if (mNxRisk.m_TAlert.m_iVisualAlert == CNxRisk.CNxAlert.VISUAL_ALERT_2){
                    /*Do SomeThing*/
                    color = MainActivityColorEnum.WARNING;
                }
                /*Risk higher than THRESHOLD_ALERT2 > 90% Danger*/
                if (mNxRisk.m_TAlert.m_iVisualAlert == CNxRisk.CNxAlert.VISUAL_ALERT_3){
                    /*Do SomeThing*/
                    color = MainActivityColorEnum.ALERT;
                }
                if (mNxRisk.m_TAlert.m_iTonesRiskAlert == TONE_ALERT){
                    /*Do SomeThing*/
                }
                if (mNxRisk.m_TAlert.m_sTextToSpeech != ""){
                    /*Do SomeThing*/
                }
                if (mNxRisk.m_SpeedAlert.m_iSpeedLimitPanel <=20){
                    /*Do SomeThing*/
                }
                if (mNxRisk.m_SpeedAlert.m_iSpeedLimitTone == CNxRisk.CNxSpeedAlert.SPEED_TONE){
                    /*Do SomeThing*/
                }
            break;
            case CNxRisk.UPDATING_HORIZ:
                /*Do SomeThing*/
                break;
            case CNxRisk.CAR_STOPPED:
                /*Do SomeThing*/
                break;
            case CNxRisk.GPS_LOST:
                /*Do SomeThing*/
                break;
            case CNxRisk.RISK_BUG:
                /*Do SomeThing*/
                break;
            case CNxRisk.STOP_PROLOG:
                /*Do SomeThing*/
                break;
            default:
                /*default*/
                break;
        };
        return color;
    }

    private Integer manageLowRiskLevel(float percentOfRisk){
        Integer levelOflowlevelRisk = LOW_LOWLEVEL_RISK;

        if(30 < percentOfRisk && percentOfRisk <= 50){
           levelOflowlevelRisk  = MEDIUM_LOWLEVEL_RISK;
        }
        if( 50 < percentOfRisk){
            levelOflowlevelRisk = HIGH_LOWLEVEL_RISK;
        }

        return levelOflowlevelRisk;
    }
}
