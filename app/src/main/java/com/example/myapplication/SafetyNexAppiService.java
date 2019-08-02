package com.example.myapplication;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
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

import java.util.Timer;
import java.util.TimerTask;

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
    private static final long LOCATION_REFRESH_TIME = 0;
    private static final float LOCATION_REFRESH_DISTANCE = 0;

    SafetyNexAppiService(Application app, View view) {
        this.TAG  = "SafetyNexService";
        this.app = (MainApp)app;
        this.mView = view;
        String workingPath = CONSTANTS.DEMO_WORKING_PATH;
        String inputFile = workingPath + CONSTANTS.DEMO_IN_FILE_NAME;
        String outputFile = workingPath + CONSTANTS.DEMO_OUT_FILE_NAME;
        LicenseFileBnd = workingPath + CONSTANTS.DEMO_LICENSE_FILE;
        LicenseFileNx = workingPath + CONSTANTS.DEMO_LICENSE_FILE_NEXYAD;
        MapSubPath = workingPath + CONSTANTS.MAP_SUB_PATH;
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
                updateRisk();
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

    private String getMessageCustomer(long [] prmCurrEhorizon) {
        String TempMessage;
        if (prmCurrEhorizon != null && prmCurrEhorizon.length > 4) {
            TempMessage = "Count " + this.mCount
                    + "; Speed " + Math.round(this.mInpuAPI.mSpeed) + "km/h"
                    + "; State " + this.mNxRisk.m_iSafetyNexEngineState
                    + "; Risk " + Math.round(this.mNxRisk.m_fRisk * 100) + "%"
                    + "; TTS:" +this.mNxRisk.m_TAlert.m_sTextToSpeech
                    + "; NxAlert :" + this.mNxRisk.m_TAlert.m_iNxAlertValue
                    + "; len :" +  prmCurrEhorizon[1];
        } else {
            TempMessage = "Count " + this.mCount
                    + "; Speed " + Math.round(this.mInpuAPI.mSpeed) + "km/h"
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

    private void updateRisk() {
        Log.v(TAG, "upDateRisk");
        //Read data

        String lineFull = this.mData.ReadNextData();
        if(this.mInpuAPI.ParseData(lineFull)) {
            //Set GPS
            this.mJniFunction.SetGPSData(this.mInpuAPI.mLat, this.mInpuAPI.mLon, 7, this.mInpuAPI.mCap, this.mInpuAPI.mSpeed, this.mInpuAPI.mTimeDiffGPS);
            //Set Accel and get Risk
            this.mJniFunction.GetAccelDataWithRisk(this.mInpuAPI.mAccelX, this.mInpuAPI.mAccelY, this.mInpuAPI.mAccelZ, this.mNxRisk);
            long [] CurrEhorizon = this.mJniFunction.GetCurrEHorizon();
            //Update Output
            if (CurrEhorizon != null) {
                mMessage = getMessageCustomer(CurrEhorizon);
                 this.writeDatas(mMessage);
            } else {
                mMessage = "Count " + (this.mCount+1)
                        + "; No e-Horizon";
            }
        } else {
            mMessage = "Count " + (this.mCount+1)
                    + "; Invalid line of data";
        }
        this.mCount++;
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

    void stop(){
        this.mIsRunning=false;
    }
}
