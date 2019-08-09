package com.example.myapplication;

import android.app.Application;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.nexiad.safetynexappsample.CNxDemoData;
import com.nexiad.safetynexappsample.CNxInputAPI;
import com.nexiad.safetynexappsample.CONSTANTS;
import com.nexyad.jndksafetynex.CNxFullStat;
import com.nexyad.jndksafetynex.CNxLicenseInfo;
import com.nexyad.jndksafetynex.CNxRisk;
import com.nexyad.jndksafetynex.CNxUserStat;
import com.nexyad.jndksafetynex.JNDKSafetyNex;

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
    private String UnlockKey;
    private int Language;
    private String TAG;
    private String mMessage;
    private Handler mTimerHandler;
    private Runnable mTimerRunnable;
    private MainApp app;
    private View mView;

    SafetyNexAppiService(Application app, View view) {
        this.TAG  = "SafetyNexService";
        this.app = (MainApp)app;
        this.mView = view;
        String workingPath = CONSTANTS.DEMO_WORKING_PATH;
        String inputFile = workingPath + CONSTANTS.DEMO_IN_FILE_NAME;
        String outputFile = workingPath + CONSTANTS.DEMO_OUT_FILE_NAME;
        LicenseFileBnd = workingPath + CONSTANTS.DEMO_LICENSE_FILE;
        LicenseFileNx = workingPath + CONSTANTS.DEMO_LICENSE_FILE_NEXYAD;
        MapSubPath = "app/src/main/maps/EU_CARDIn/";//workingPath + CONSTANTS.MAP_SUB_PATH;
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
        if (prmCurrEhorizon != null && prmCurrEhorizon.length > 4) {
            TempMessage = "Count " + this.mCount
                    + "; Speed " + Math.round(mInpuAPI.getmSpeed()) + "km/h"
                    + "; State " + this.mNxRisk.m_iSafetyNexEngineState
                    + "; Risk " + Math.round(this.mNxRisk.m_fRisk * 100) + "%"
                    + "; TTS:" +this.mNxRisk.m_TAlert.m_sTextToSpeech
                    + "; NxAlert :" + this.mNxRisk.m_TAlert.m_iNxAlertValue
                    + "; len :" +  prmCurrEhorizon[1];
        } else {
            TempMessage = "Count " + this.mCount
                    + "; Speed " + Math.round(mInpuAPI.getmSpeed()) + "km/h"
                    + "; State " + this.mNxRisk.m_iSafetyNexEngineState
                    + "; Risk " + Math.round(this.mNxRisk.m_fRisk * 100) + "%"
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
        Log.v(TAG, "getRisk");
        //Set GPS
        this.mJniFunction.SetGPSData(cNxInputAPI.getmLat(), cNxInputAPI.getmLon(), cNxInputAPI.getNbOfSat(), cNxInputAPI.getmCap(), cNxInputAPI.getmSpeed(), cNxInputAPI.getmTimeDiffGPS());
        //Set Accel and get Risk
        this.mJniFunction.GetAccelDataWithRisk(cNxInputAPI.getmAccelX(), cNxInputAPI.getmAccelY(), cNxInputAPI.getmAccelZ(), this.mNxRisk);
        updateRiskInfo();
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

    void stop(){
        this.mIsRunning=false;
    }

    private void updateRiskInfo(){
        switch (mNxRisk.m_iSafetyNexEngineState) {
            case CNxRisk.RISK_AVAILABLE:
                if (mNxRisk.m_TAlert.m_iVisualAlert == CNxRisk.CNxAlert.VISUAL_ALERT_1 ){

                }
                /*Risk higher than THRESHOLD_ALERT1*/
                if (mNxRisk.m_TAlert.m_iVisualAlert == CNxRisk.CNxAlert.VISUAL_ALERT_2){
                    /*Do SomeThing*/
                }
                /*Risk higher than THRESHOLD_ALERT2*/
                if (mNxRisk.m_TAlert.m_iVisualAlert == CNxRisk.CNxAlert.VISUAL_ALERT_3){
                    /*Do SomeThing*/
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
    }
}
