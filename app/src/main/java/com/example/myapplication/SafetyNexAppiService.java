package com.example.myapplication;

import android.app.Application;
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

import java.util.Timer;
import java.util.TimerTask;

public class SafetyNexAppiService {

    public boolean mIsRunning;
    public CNxDemoData mData;
    public CNxInputAPI mInpuAPI;
    public int mCount;
    public JNDKSafetyNex mJniFunction;
    public CNxRisk mNxRisk;
    public String WorkingPath;
    public  String InputFile;
    public String OutputFile;
    public String LicenseFileBnd;
    public String LicenseFileNx;
    public String MapSubPath;
    public String UnlockKey;
    public int Language;
    public int previousAlertValue;
    private TextView textView;


    private String TAG;
    private String mMessage;

    public View.OnClickListener getmRunListener() {
        return mRunListener;
    }

    public void setmRunListener(View.OnClickListener mRunListener) {
        this.mRunListener = mRunListener;
    }

    private View.OnClickListener mRunListener;

    public Handler getmTimerHandler() {
        return mTimerHandler;
    }

    public void setmTimerHandler(Handler mTimerHandler) {
        this.mTimerHandler = mTimerHandler;
    }

    private Handler mTimerHandler;
    private Runnable mTimerRunnable;
    private MainApp app;
    private View mView;

    public SafetyNexAppiService(Application app, View view) {
        this.TAG  = "SafetyNexService";
        this.app = (MainApp)app;
        this.mView = view;
        WorkingPath = CONSTANTS.DEMO_WORKING_PATH;
        InputFile = WorkingPath + CONSTANTS.DEMO_IN_FILE_NAME;
        OutputFile = WorkingPath + CONSTANTS.DEMO_OUT_FILE_NAME;
        LicenseFileBnd = WorkingPath + CONSTANTS.DEMO_LICENSE_FILE;
        LicenseFileNx = WorkingPath + CONSTANTS.DEMO_LICENSE_FILE_NEXYAD;
        MapSubPath = WorkingPath + CONSTANTS.DEMO_MAP_SUB_PATH;
        UnlockKey = CONSTANTS.DEMO_UNLOCK_KEY;
        previousAlertValue = -1;

        Language = 0;

        try {
            Language = Integer.parseInt(String.valueOf(CONSTANTS.DEMO_LANGUAGE));
        }catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }

        mCount = 0;

        mIsRunning = true;
        mData = new CNxDemoData(InputFile, OutputFile);
        mJniFunction = JNDKSafetyNex.GetInstance(this.app.getApplicationContext());
        mInpuAPI = new CNxInputAPI();
        mNxRisk = new CNxRisk();
        mTimerHandler = new Handler();
        mTimerRunnable = new Runnable() {
            @Override
            public void run() {
                updateRisk();
                doNextStep();
            }
        };
    }

    public void initAPI() {
        Log.v(TAG, "initAPI");

        //File tempPath[] =  getExternalFilesDirs(null); //A call to this function seems needed to grant access to externals directories



        this.mIsRunning = true;
        mTimerHandler.postDelayed(mTimerRunnable, CONSTANTS.DEMO_FIRST_DELAY);
        //mData = new CNxDemoData(null, OutputFile);
        this.mJniFunction = JNDKSafetyNex.GetInstance(this.app.getApplicationContext());
        //boolean isLicOK = mJniFunction.Birth(LicenseFileBnd, MapSubPath, UnlockKey, Language, LicenseFileNx);
        CNxLicenseInfo tempLicInfo = new CNxLicenseInfo();
        boolean isLicOK = this.mJniFunction.Birth(this.LicenseFileBnd, this.MapSubPath, this.UnlockKey, this.Language, this.LicenseFileNx, tempLicInfo);
        if(!isLicOK) {
            //TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //String deviceid = manager.getDeviceId();
            String deviceidBirth = tempLicInfo.m_sRunningDeviceId;
            String deviceid = this.mJniFunction.GetDeviceId();
            //alertMessage("Your license is not valid","Please communicate the IMEI " + "("+deviceid+")" + " to Nexyad in order to generate a license.");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    //finish(); // this code will be executed after 2 seconds
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
        for (int i = 0; i < FullStat.length; i++){
            duration += FullStat[i].m_fDuration;
            distance += FullStat[i].m_fDistance;
        }
        this.mJniFunction.StoreCloudStatToMemory(CONSTANTS.DEMO_WORKING_PATH);
        mMessage = "Grade = " + String.valueOf(grade)
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
            //if (mCount%20 == 0)
            this.mJniFunction.SetGPSData(this.mInpuAPI.mLat, this.mInpuAPI.mLon, 7, this.mInpuAPI.mCap, this.mInpuAPI.mSpeed, this.mInpuAPI.mTimeDiffGPS);
            //Set Accel and get Risk
            this.mJniFunction.GetAccelDataWithRisk(this.mInpuAPI.mAccelX, this.mInpuAPI.mAccelY, this.mInpuAPI.mAccelZ, this.mNxRisk);
            long [] CurrEhorizon = this.mJniFunction.GetCurrEHorizon();
            //Update Output
            if (CurrEhorizon != null) {
                mMessage = getMessageCustomer(CurrEhorizon);
                /*if(this.previousAlertValue != this.mNxRisk.m_TAlert.m_iNxAlertValue) {
                    this.previousAlertValue = this.mNxRisk.m_TAlert.m_iNxAlertValue;
                    Toast.makeText(SafetyNexAppiService.this, mMessage, Toast.LENGTH_LONG).show();
                }*/
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

    public void writeDatas(String mMessage){
        this.mData.WriteData(mMessage);
        ((TextView)this.mView.findViewById(R.id.fabHeadMsg)).setText(mMessage);
    }
/*
    public void writeMessageAlert(String mMessage, TextView view){
        view.setText(mMessage);
    }*/

    public void stop(){
        this.mIsRunning=false;
    }
}
