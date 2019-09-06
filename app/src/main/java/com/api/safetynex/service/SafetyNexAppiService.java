package com.api.safetynex.service;

import android.app.Application;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.api.exceptions.NexiadException;
import com.api.safetynex.service.floatingwidget.FloatingWidgetAlertingInfos;
import com.api.safetynex.service.floatingwidget.FloatingWidgetColorEnum;
import com.api.safetynex.MainApp;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.nexyad.jndksafetynex.CNxRisk.CNxAlert.TONE_ALERT;

public class SafetyNexAppiService implements TextToSpeech.OnInitListener {

    private CNxDemoData mData;
    private int mCount;
    private JNDKSafetyNex mJniFunction;
    public CNxRisk mNxRisk;
    private String LicenseFileBnd;
    private String LicenseFileNx;
    private String MapSubPath;
    private String UnlockKey;
    private int language;
    private String TAG;
    private String mMessage;
    private MainApp app;
    private static final Integer LOW_LOWLEVEL_RISK = 0;
    private static final Integer MEDIUM_LOWLEVEL_RISK = 1;
    private static final Integer HIGH_LOWLEVEL_RISK = 2;
    private TextToSpeech mTts;
    private String lastTTS = "";
    private static final String NEXIAD_LICENCE_EXCEPTION = "Nexiad License exception.";

    private List<FloatingWidgetAlertingInfos> mokFloatingAlertingInfos;

    private FloatingWidgetAlertingInfos alertingTypeEnum;
    private ToneGenerator toneGenerator;
    private FloatingWidgetAlertingInfos currentAlertingTypeEnum;
    private int rank = 0;

    private static SafetyNexAppiService safetyNexAppiService;

    public static SafetyNexAppiService getInstance(Application app){
        if(safetyNexAppiService == null){
            safetyNexAppiService = new SafetyNexAppiService(app);
        }
        return safetyNexAppiService;
    }

    private SafetyNexAppiService(Application app) {
        this.TAG  = CONSTANTS.LOGNAME.concat("SafetyNexService");
        this.app = (MainApp)app;
        String workingPath = CONSTANTS.DEMO_WORKING_PATH;
        String inputFile = workingPath + CONSTANTS.DEMO_IN_FILE_NAME;
        String outputFile = workingPath + CONSTANTS.DEMO_OUT_FILE_NAME;
        LicenseFileBnd = workingPath + CONSTANTS.DEMO_LICENSE_FILE;
        LicenseFileNx = workingPath + CONSTANTS.DEMO_LICENSE_FILE_NEXYAD;
        MapSubPath = workingPath + CONSTANTS.MAP_SUB_PATH;
        UnlockKey = CONSTANTS.DEMO_UNLOCK_KEY;
        language = 1;
        this.toneGenerator  = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        this.mTts = new TextToSpeech(this.app.getApplicationContext(), this);
        try {
            language = Integer.parseInt(String.valueOf(CONSTANTS.DEMO_LANGUAGE));
        }catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }

        mCount = 0;
        mData = new CNxDemoData(inputFile, outputFile);
        mJniFunction = JNDKSafetyNex.GetInstance(this.app.getApplicationContext());
        mNxRisk = new CNxRisk();
        this.mokFloatingAlertingInfos = new ArrayList<>();
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

    public void initAPI() throws NexiadException {
        Log.v(TAG, "initAPI");
        CNxLicenseInfo tempLicInfo = new CNxLicenseInfo();
        this.copyMapsOnDeviceStorage();
        boolean isLicOK = this.mJniFunction.Birth(this.LicenseFileBnd, this.MapSubPath, this.UnlockKey, this.language, this.LicenseFileNx, tempLicInfo);
        if(!isLicOK) {
           /* new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                }
            }, CONSTANTS.DEMO_EXIT_DELAY);*/
            throw new NexiadException(NEXIAD_LICENCE_EXCEPTION);
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

    public void closeAPI() {
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
                for(String f : Objects.requireNonNull(li)){
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

    public MainApp getApp() {
        return app;
    }

    private void writeDatas(String mMessage){
        this.mData.WriteData(mMessage);
    }

    public String getRisk(CNxInputAPI cNxInputAPI){
        Log.v(TAG, "getRisk "+printCNxInputAPI(cNxInputAPI));
        //Set GPS
        this.mJniFunction.SetGPSData(cNxInputAPI.getmLat(), cNxInputAPI.getmLon(), cNxInputAPI.getNbOfSat(), cNxInputAPI.getmCap(), cNxInputAPI.getmSpeed(), cNxInputAPI.getmTimeDiffGPS());
        Log.i(TAG,"gps : "+cNxInputAPI.getmLat()+"/"+cNxInputAPI.getmLon()+"/"+cNxInputAPI.getmTimeDiffGPS()+"/"+cNxInputAPI.getmCap());
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
        return mMessage;
    }

    private String printCNxInputAPI(CNxInputAPI cNxInputAPI){
        return "NB Sat : "+cNxInputAPI.getNbOfSat()+" X: "+cNxInputAPI.getmAccelX()+" Y: "+cNxInputAPI.getmAccelY()+" Z: "+cNxInputAPI.getmAccelZ()+" state : "+mNxRisk.m_iSafetyNexEngineState+" speed: "+cNxInputAPI.getmSpeed()*3.6;
    }

   private FloatingWidgetAlertingInfos updateRiskInfo(Long speedLimitSegment) {

       FloatingWidgetAlertingInfos alertingTypeEnum;
       String speech = "";
       if (CONSTANTS.DEMO_DATA_TEST) {
            if (rank % 5 == 0) {
                Log.i(TAG, "new random");
                alertingTypeEnum = this.mokFloatingAlertingInfos.get((int) (Math.random() * ((this.mokFloatingAlertingInfos.size()))));
                currentAlertingTypeEnum = alertingTypeEnum;
            } else {
                alertingTypeEnum = currentAlertingTypeEnum;
           }
            speech = alertingTypeEnum.m_sTextToSpeech;
            rank++;
       }else{
            alertingTypeEnum = new FloatingWidgetAlertingInfos(FloatingWidgetColorEnum.LOW_OF_LOWLEVEL, null);

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
                if (mNxRisk.m_TAlert.m_sTextToSpeech != null && !mNxRisk.m_TAlert.m_sTextToSpeech.equals("")){

                       if(mNxRisk.m_TAlert.m_iNxAlertValue != -1){
                           alertingTypeEnum.imgId = "ic_icon_"+ mNxRisk.m_TAlert.m_iNxAlertValue;
                       }
                       this.mMessage+=" \n\n"+mNxRisk.m_TAlert.m_sTextToSpeech;
                       speech = mNxRisk.m_TAlert.m_sTextToSpeech;
                   }
                   if (mNxRisk.m_SpeedAlert.m_iSpeedLimitPanel <=20){
                   }
                   if (mNxRisk.m_SpeedAlert.m_iSpeedLimitTone == CNxRisk.CNxSpeedAlert.SPEED_TONE){
                       //this.toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 1000);
                       speech = "Veuillez ralentir portion limitée à "+speedLimitSegment.toString()+" kilomètres par heure.";
                       alertingTypeEnum = new FloatingWidgetAlertingInfos(FloatingWidgetColorEnum.WARNING_SPEED, speedLimitSegment.toString());

                   }
                   break;
               case CNxRisk.UPDATING_HORIZ:
                   break;
               case CNxRisk.CAR_STOPPED:
                   break;
               case CNxRisk.GPS_LOST:
                   speech = "Perte du GPS";
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
       }
        alertingTypeEnum.m_sTextToSpeech = speech;
        return alertingTypeEnum;
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

    @Override
    public void onInit(int status) {
        if(status != TextToSpeech.ERROR) {
            mTts.setLanguage(Locale.FRANCE);
            mTts.setSpeechRate(1); // 1 est la valeur par défaut. Une valeur inférieure rendra l'énonciation plus lente, une valeur supérieure la rendra plus rapide.
            mTts.setPitch(1); // 1 est la valeur par défaut. Une valeur inférieure rendra l'énonciation plus grave, une valeur supérieure la rendra plus aigue.
        }
    }

    public void speechOut(String txt){
        HashMap<String,String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0.5");

        Bundle paramss = new Bundle();
        paramss.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.5f);

        if(!this.lastTTS.equals(txt)) {
            mTts.speak(txt, TextToSpeech.QUEUE_FLUSH, paramss, null);
        }else{
            mTts.speak(txt, TextToSpeech.QUEUE_ADD, paramss, null);
        }
        this.lastTTS = txt;

    }
}
