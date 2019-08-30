package com.example.myapplication;

import android.app.Application;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.nexyad.jndksafetynex.CNxRisk.CNxAlert.TONE_ALERT;

class SafetyNexAppiService implements TextToSpeech.OnInitListener {

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
    private TextToSpeech mTts;
    private int previousM_iSafetyNexEngineState = 99;
    private String lastTTS = "";

    private List<FloatingWidgetAlertingInfos> mokFloatingAlertingInfos;

    private FloatingWidgetAlertingInfos alertingTypeEnum;
    private ToneGenerator toneGenerator;
    private FloatingWidgetAlertingInfos currentAlertingTypeEnum;
    private int rank = 0;

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
        Language = 1;
        this.toneGenerator  = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        this.mTts = new TextToSpeech(this.app.getApplicationContext(), this);
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

        this.mokFloatingAlertingInfos = new ArrayList<FloatingWidgetAlertingInfos>();
        this.mokFloatingAlertingInfos.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.LOW_OF_LOWLEVEL, null));
        this.mokFloatingAlertingInfos.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.MEDIUM_OF_LOWLEVEL, null));
        this.mokFloatingAlertingInfos.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.HIGH_OF_LOWLEVEL, null));
        this.mokFloatingAlertingInfos.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.WARNING, null));
        this.mokFloatingAlertingInfos.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.ALERT, null));
        this.mokFloatingAlertingInfos.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.WARNING_SPEED, "90"));
        this.mokFloatingAlertingInfos.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.GPS_LOST, "GPS"));
    }

    public FloatingWidgetAlertingInfos floatingWidgetAlertingInfos() {
        return this.alertingTypeEnum;
    }

    void initAPI() {
        Log.v(TAG, "initAPI");
        this.mIsRunning = true;
        mTimerHandler.postDelayed(mTimerRunnable, CONSTANTS.DEMO_FIRST_DELAY);
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
        String TempMessage ;
        float risque = Math.round(this.mNxRisk.m_fRisk * 100);
        risque = (risque < 0 ? 0 : risque);
        if (prmCurrEhorizon != null && prmCurrEhorizon.length > 4) {
            TempMessage = "Vitesse : " + Math.round(mInpuAPI.getmSpeed()) + "km/h"
                    + " | Risque :" + risque + "%";
        } else {
            TempMessage = "Vitesse : " + Math.round(mInpuAPI.getmSpeed()) + "km/h"
                    + " | Risque : " +risque + "%";
        }
        return TempMessage;
    }

    private void closeAPI() {
        Log.v(TAG, "closeAPI");
        if(this.mTts != null){
            this.mTts.stop();
            this.mTts.shutdown();
        }
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
    }

    public String getRisk(CNxInputAPI cNxInputAPI){
        Log.v(TAG, "getRisk "+printCNxInputAPI(cNxInputAPI));
        //Set GPS
        this.mJniFunction.SetGPSData(cNxInputAPI.getmLat(), cNxInputAPI.getmLon(), cNxInputAPI.getNbOfSat(), cNxInputAPI.getmCap(), cNxInputAPI.getmSpeed(), cNxInputAPI.getmTimeDiffGPS());
        //Set Accel and get Risk
        this.mJniFunction.GetAccelDataWithRisk(cNxInputAPI.getmAccelX(), cNxInputAPI.getmAccelY(), cNxInputAPI.getmAccelZ(), this.mNxRisk);
        long [] CurrEhorizon = this.mJniFunction.GetCurrEHorizon();
        Long speed = null;
        //Update Output
        if (CurrEhorizon != null) {
            mMessage = getMessageCustomer(CurrEhorizon, cNxInputAPI);
            if(CurrEhorizon.length > 6) {
                     speed = CurrEhorizon[6];
            }
            this.writeDatas(mMessage);
        } else {

            mMessage = "Count " + (this.mCount+1)
                    + "; No e-Horizon";
        }
        this.mCount++;
        this.alertingTypeEnum = updateRiskInfo(speed);
        this.previousM_iSafetyNexEngineState = mNxRisk.m_iSafetyNexEngineState;
        return mMessage;
    }

    private String printCNxInputAPI(CNxInputAPI cNxInputAPI){
        return "NB Sat : "+cNxInputAPI.getNbOfSat()+" X: "+cNxInputAPI.getmAccelX()+" Y: "+cNxInputAPI.getmAccelY()+" Z: "+cNxInputAPI.getmAccelZ()+" state : "+mNxRisk.m_iSafetyNexEngineState+" speed: "+cNxInputAPI.getmSpeed()*3.6;
    }

    void stop(){
        this.mIsRunning=false;
    }

    private FloatingWidgetAlertingInfos updateRiskInfo(Long speedLimitSegment){
        FloatingWidgetAlertingInfos alertingTypeEnum;

        Log.i(TAG, String.valueOf(rank));
        if(rank%10==0){
            alertingTypeEnum = this.mokFloatingAlertingInfos.get(0 + (int)(Math.random() * ((this.mokFloatingAlertingInfos.size()))));
            currentAlertingTypeEnum =alertingTypeEnum;
        }else{
            alertingTypeEnum = currentAlertingTypeEnum;
        }
        rank++;

        switch (alertingTypeEnum.m_iSafetyNexEngineState) {
            case CNxRisk.RISK_AVAILABLE:
              if (alertingTypeEnum.m_iTonesRiskAlert == TONE_ALERT){
    //                this.toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 1000);
                }
                if (alertingTypeEnum.m_sTextToSpeech != null && alertingTypeEnum.m_sTextToSpeech != ""){
                    speechOut(alertingTypeEnum.m_sTextToSpeech);
                    this.mMessage+=" \n\n"+alertingTypeEnum.m_sTextToSpeech;
                }
                if (alertingTypeEnum.m_iSpeedLimitTone == CNxRisk.CNxSpeedAlert.SPEED_TONE){
      //              this.toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 1000);
                    speechOut("Portion limitée à "+alertingTypeEnum.getTextRounded()+" kilomètres par heure.");
                }
            break;
            case CNxRisk.UPDATING_HORIZ:
                break;
            case CNxRisk.CAR_STOPPED:
              break;
            case CNxRisk.GPS_LOST:
                speechOut("Perte du GPS.");

                break;
            case CNxRisk.RISK_BUG:
                break;
            case CNxRisk.STOP_PROLOG:
                break;
            default:
                break;
        }
     return alertingTypeEnum;
    }


   /* private FloatingWidgetAlertingInfos updateRiskInfo(Long speedLimitSegment){

        FloatingWidgetAlertingInfos alertingTypeEnum = new FloatingWidgetAlertingInfos(FloatingWidgetColorEnum.LOW_OF_LOWLEVEL, null);
        switch (mNxRisk.m_iSafetyNexEngineState) {
            case CNxRisk.RISK_AVAILABLE
                    :
                    // risque faible à moyen
                    //  0 à 30% ok vert
                    //  30 à 50% ok orange claire
                    //  50 à 70% ok mais orange ++
                if (mNxRisk.m_TAlert.m_iVisualAlert == CNxRisk.CNxAlert.VISUAL_ALERT_1 ){
                    Integer risk = manageLowRiskLevel(mNxRisk.m_fRisk * 100);
                    switch (risk){
                        case 0 :
                            alertingTypeEnum = new FloatingWidgetAlertingInfos(FloatingWidgetColorEnum.LOW_OF_LOWLEVEL, null);
                            break;
                        case 1 :
                            alertingTypeEnum = new FloatingWidgetAlertingInfos(FloatingWidgetColorEnum.MEDIUM_OF_LOWLEVEL, null);
                            break;
                        default:
                            alertingTypeEnum = new FloatingWidgetAlertingInfos(FloatingWidgetColorEnum.HIGH_OF_LOWLEVEL, null);
                            break;
                    }
                }
                //Risk higher than THRESHOLD_ALERT1 70 à 90% warning
                if (mNxRisk.m_TAlert.m_iVisualAlert == CNxRisk.CNxAlert.VISUAL_ALERT_2){
                    alertingTypeEnum = new FloatingWidgetAlertingInfos(FloatingWidgetColorEnum.WARNING, null);
                }
                //Risk higher than THRESHOLD_ALERT2 > 90% Danger
                if (mNxRisk.m_TAlert.m_iVisualAlert == CNxRisk.CNxAlert.VISUAL_ALERT_3){
                    alertingTypeEnum = new FloatingWidgetAlertingInfos(FloatingWidgetColorEnum.ALERT, null);
                }
                if (mNxRisk.m_TAlert.m_iTonesRiskAlert == TONE_ALERT){
                    this.toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 1000);
                }
                if (mNxRisk.m_TAlert.m_sTextToSpeech != null && mNxRisk.m_TAlert.m_sTextToSpeech != ""){

                    if(mNxRisk.m_TAlert.m_iNxAlertValue != -1){
                        alertingTypeEnum.imgId = "icon_"+ String.valueOf(mNxRisk.m_TAlert.m_iNxAlertValue);
                    }
                    this.mMessage+=" \n\n"+mNxRisk.m_TAlert.m_sTextToSpeech;
                    speechOut(mNxRisk.m_TAlert.m_sTextToSpeech);
                }
                if (mNxRisk.m_SpeedAlert.m_iSpeedLimitPanel <=20){
                }
                if (mNxRisk.m_SpeedAlert.m_iSpeedLimitTone == CNxRisk.CNxSpeedAlert.SPEED_TONE){
                    //this.toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 1000);
                    speechOut("Veuillez ralentir portion limitée à "+speedLimitSegment.toString()+" kilomètres par heure.");
                    alertingTypeEnum = new FloatingWidgetAlertingInfos(FloatingWidgetColorEnum.WARNING_SPEED, speedLimitSegment.toString());

                }
                break;
            case CNxRisk.UPDATING_HORIZ:
                break;
            case CNxRisk.CAR_STOPPED:
                break;
            case CNxRisk.GPS_LOST:
                speechOut("Perte du GPS.");
                alertingTypeEnum = new FloatingWidgetAlertingInfos(FloatingWidgetColorEnum.GPS_LOST, "GPS");

                break;
            case CNxRisk.RISK_BUG:
                break;
            case CNxRisk.STOP_PROLOG:
                break;
            default:
                this.lastTTS = "";
                break;
        }
        return alertingTypeEnum;
    }*/

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

    @Override
    public void onInit(int status) {
        if(status != TextToSpeech.ERROR) {
            mTts.setLanguage(Locale.FRANCE);
            mTts.setSpeechRate(1); // 1 est la valeur par défaut. Une valeur inférieure rendra l'énonciation plus lente, une valeur supérieure la rendra plus rapide.
            mTts.setPitch(1); // 1 est la valeur par défaut. Une valeur inférieure rendra l'énonciation plus grave, une valeur supérieure la rendra plus aigue.
            speechOut("Démarrage de l'application");
        }
    }

    private void speechOut(String txt){
        if(!this.lastTTS.equals(txt)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTts.speak(txt, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                mTts.speak(txt, TextToSpeech.QUEUE_FLUSH, null);
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTts.speak(txt, TextToSpeech.QUEUE_ADD, null, null);
            } else {
                mTts.speak(txt, TextToSpeech.QUEUE_ADD, null);
            }
        }
        this.lastTTS = txt;

    }
}
