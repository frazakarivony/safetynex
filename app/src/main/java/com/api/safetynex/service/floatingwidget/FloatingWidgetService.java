package com.api.safetynex.service.floatingwidget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.location.Location;
import android.location.LocationListener;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.api.binomad.LicenseAppiService;
import com.api.safetynex.SafetyStats;
import com.api.safetynex.listener.LicensingListener;
import com.api.safetynex.listener.interfaces.OnEventListener;
import com.api.safetynex.MainApp;
import com.api.safetynex.R;
import com.api.safetynex.listener.UserActionListener;
import com.api.safetynex.service.SafetyNexAppiService;
import com.api.utils.ConnectionUtils;
import com.api.safetynex.receiver.AppReceiver;
import com.api.safetynex.receiver.StopNotificationReceiver;
import com.api.utils.DrawableUtils;
import com.api.exceptions.NexiadException;
import com.nexiad.safetynexappsample.CNxDemoData;
import com.nexiad.safetynexappsample.CNxInputAPI;
import com.nexiad.safetynexappsample.CONSTANTS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FloatingWidgetService extends Service  {

    private final String TAG = CONSTANTS.LOGNAME.concat("FloatingWidgetService");
    private WindowManager mWindowManager;
    private View mOverlayView;
    int mWidth;
    private static final long LOCATION_REFRESH_TIME = 0;
    private static final float LOCATION_REFRESH_DISTANCE = 0;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    private CNxDemoData mData;
    private String workingPath = CONSTANTS.DEMO_WORKING_PATH;
    private String inputFile = workingPath + CONSTANTS.DEMO_IN_FILE_NAME;
    private String outputFile = workingPath + CONSTANTS.DEMO_OUT_FILE_NAME;
    private Boolean isPaused=false;
    private SafetyNexAppiService safetyNexAppiService;
    private LicenseAppiService licenseAppiService;
    private UserActionListener userActionListenerPerso;
    private OnEventListener onEventListener;
    private CNxInputAPI mInpuAPI;
    private Future timerRunnableFuture;
    private String lastSpeech ="init";
    private int speechRepetition = 1;
    private boolean stop=false;
    private String data="";
    private BufferedReader br;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        this.mInpuAPI= new CNxInputAPI();

        if (mOverlayView == null) {
            mOverlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);
        }
        if(CONSTANTS.USE_REAL_MOCK){
            readMockFile();
        }

        AppReceiver.getInstance().setFloatingWidgetService(this);
        startForeground(12345678, createFloatingWidgetSpecialNotification());
        setTheme(R.style.AppTheme);
        mData = new CNxDemoData(inputFile, outputFile);

        safetyNexAppiService = SafetyNexAppiService.getInstance(getApplication());
        AppReceiver.getInstance().setSafetyNexAppiService(safetyNexAppiService);

        String imei = "";
        if (ActivityCompat.checkSelfPermission(((MainApp)getApplication()), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            imei=telephonyManager.getImei();
        }
        else {
            imei = telephonyManager.getDeviceId();
        }
        this.generateFloatingListeners();

        this.licenseAppiService = new LicenseAppiService(getApplicationContext(), onEventListener, imei);
        try {
            this.safetyNexAppiService.initAPI();
        }catch (NexiadException e){
            e.printStackTrace();
            this.safetyNexAppiService.closeAPI();
            LicenseAppiService.removeAllLicences();
            this.licenseAppiService.execute();
        }
    }

    private void generateFloatingListeners(){

        SensorManager senSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor senAccelerometer = Objects.requireNonNull(senSensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this.mSensorListener, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        onEventListener = new LicensingListener(this.safetyNexAppiService.getApp());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if(CONSTANTS.DEMO_REAL_MOCK) {
            writeToFile();
        }

        if (mOverlayView != null){
            mWindowManager.removeView(mOverlayView);
        }
        mLocationManager.removeUpdates(mLocationListener);
        mSensorManager.unregisterListener(this.mSensorListener);
        unregisterReceiver(AppReceiver.getInstance());

        stop=true;
        timerRunnableFuture.cancel(true);

        if(CONSTANTS.USE_REAL_MOCK){
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

            //Specify the view position
            params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
            params.x = 0;
            params.y = 100;

            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            final Point size = new Point();
            if (mWindowManager != null) {
                if(mOverlayView.getWindowToken()==null){
                    mWindowManager.addView(mOverlayView, params);
                    Display display = mWindowManager.getDefaultDisplay();
                    display.getSize(size);
                }
            }

            final RelativeLayout layout = mOverlayView.findViewById(R.id.layout);

            ViewTreeObserver vto = layout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = layout.getMeasuredWidth();

                    //To get the accurate middle of the screen we subtract the width of the android floating widget.
                    mWidth = size.x - width;

                }
            });

            userActionListenerPerso = new UserActionListener(getApplicationContext(),params, mWindowManager, intent);
            mOverlayView.setOnTouchListener(userActionListenerPerso);

            addListenerSensor();
            addListenerLocation(userActionListenerPerso);

            TextView text = mOverlayView.findViewById(R.id.textView2);
            text.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        if(text.getCompoundDrawables()[DRAWABLE_RIGHT] != null){
                            if(event.getRawX() >= (text.getRight() - text.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                if(!isPaused){
                                    safetyNexAppiService.speechOut(getString(R.string.pause));
                                    text.setCompoundDrawablesWithIntrinsicBounds(
                                            null,
                                            null,
                                            getDrawable(R.drawable.ic_play_circle_outline_white_24dp),
                                            null);
                                }else{
                                    safetyNexAppiService.restartApi();
                                }
//                                CNxFullStat cNxFullStat[] = safetyNexAppiService.mJniFunction.GetCloudStat();
//                                float grade = safetyNexAppiService.mJniFunction.GetUserGrade();
//                                float position =safetyNexAppiService.mJniFunction.GetCarPosition();
//                                CNxFullStat cNxFullStat1[] = safetyNexAppiService.mJniFunction.GetCloudStat();
                                isPaused = !isPaused;
                                speechRepetition =0;
                                //safetyNexAppiService.restartApi();
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler timerHandler = new Handler();
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                if(!stop){
                    timerHandler.postDelayed(this, 1000);
                }
                Log.i(TAG, "loooppppp");
                if(mInpuAPI.getLocationUpdated() || CONSTANTS.USE_REAL_MOCK){
                    callSafetyApi();
                    mInpuAPI.setLocationUpdated(Boolean.FALSE);
                }
            }
        };

        //timerRunnable.run();
        timerRunnableFuture = executorService.submit(timerRunnable);

        int ret =  super.onStartCommand(intent, flags, startId);

        getApplicationContext().sendBroadcast(new Intent("FLOATING_OK"));
        return ret;
    }

    private void addListenerSensor() {
        Log.i(TAG, "addListenerSensor");

        mSensorManager = (SensorManager) getSystemService(getApplicationContext().SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if(!CONSTANTS.USE_REAL_MOCK) {
                    mInpuAPI.setmAccelZ(sensorEvent.values[CONSTANTS.ACCELOROMETRE_X_INDIXE]);
                    mInpuAPI.setmAccelY(sensorEvent.values[CONSTANTS.ACCELOROMETRE_Y_INDIXE]);
                    mInpuAPI.setmAccelX(sensorEvent.values[CONSTANTS.ACCELOROMETRE_Z_INDIXE]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        mSensorManager.registerListener(mSensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    private void addListenerLocation(final UserActionListener doubleclickListenerPerso) {
        Log.i(TAG, "addListenerLocation");
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {

                if(!CONSTANTS.USE_REAL_MOCK){
                    mInpuAPI.setmLat((float) location.getLatitude());
                    mInpuAPI.setmLon((float) location.getLongitude());
                    mInpuAPI.setGpsTimeLong(location.getTime());
                    mInpuAPI.setNbOfSat(location.getExtras().getInt("satellites"));
                    mInpuAPI.setmCap(location.getBearing());
                    mInpuAPI.setmSpeed(location.getSpeed() * CONSTANTS.SPEED_MS_TO_KH);
                    mInpuAPI.setLocationUpdated(Boolean.TRUE);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i("Position Latitude : ", "onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.i("Position Latitude : ", "onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.i("Position Latitude : ", "onProviderDisabled");

            }
        };
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

//    public void killAll(){
//        Log.i(TAG, "killllll");
//        this.safetyNexAppiService.closeAPI();
//        this.stopSelf();
//    }

    private Notification createFloatingWidgetSpecialNotification() {
        Log.i(TAG, "createFloatingWidgetSpecialNotification");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("KILL");
        registerReceiver(AppReceiver.getInstance(), intentFilter);

        Intent closeIntent = new Intent(this, StopNotificationReceiver.class);
        closeIntent.setAction("NOTIFKILLSIG");
        PendingIntent closePendingIntent =
                PendingIntent.getBroadcast(this, 158, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationChannel channel = new NotificationChannel("channel_1","SafetyNext",NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder = new Notification.Builder(getApplicationContext(),"channel_1")
                .setContentTitle("SafetyNext")
                .setContentText("SafetyNext")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addAction(R.drawable.ic_check_box_black_24dp, getString(R.string.close_app), closePendingIntent);
        return builder.build();
    }

    private void callSafetyApi(){
        final TextView text = mOverlayView.findViewById(R.id.textView2);

        if(!isPaused) {
            if (ConnectionUtils.isInternetConnection(getApplicationContext())) {
                if (CONSTANTS.DEMO_DATA_TEST) {
                    String lineFull = mData.ReadNextData();
                    mInpuAPI.ParseData(lineFull);
                    mInpuAPI.setNbOfSat(7);
                }
                if(CONSTANTS.DEMO_REAL_MOCK){
                    data = data + mInpuAPI.toCsv();
                }
                if(CONSTANTS.USE_REAL_MOCK){
                    String line = null;
                    try {
                        line = br.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mInpuAPI.getValueFromCsv(line);
                }else{
                    mInpuAPI.setmTimeDiffGPS((System.currentTimeMillis() - mInpuAPI.getGpsTimeLong())/1000);
                }

                String textResult=safetyNexAppiService.getRisk(mInpuAPI);
                Log.i(TAG,String.valueOf(speechRepetition));

                text.setText(textResult);
                updateInfos(text);

                if(safetyNexAppiService.floatingWidgetAlertingInfos().m_sTextToSpeech.equals(lastSpeech)){
                    if(speechRepetition %5 == 0){
                        Log.i(TAG, "%10");
                        safetyNexAppiService.speechOut(safetyNexAppiService.floatingWidgetAlertingInfos().m_sTextToSpeech);
                    }
                    speechRepetition++;
                    Log.i(TAG, "identique");
                }else{
                    Log.i(TAG, "different");
                    safetyNexAppiService.speechOut(safetyNexAppiService.floatingWidgetAlertingInfos().m_sTextToSpeech);
                    lastSpeech =safetyNexAppiService.floatingWidgetAlertingInfos().m_sTextToSpeech;
                    speechRepetition =1;
                }


            }   else {
                text.setText(R.string.no_connection);
                safetyNexAppiService.speechOut(getString(R.string.no_connection));
            }
        }
    }

    private void updateInfos(TextView text) {

        ((LinearLayout) text.getParent()).setBackground(getApplicationContext().getDrawable(DrawableUtils.getDrawableColor(safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetBorderColor())));


        if (safetyNexAppiService.floatingWidgetAlertingInfos().getTextRounded() != null || safetyNexAppiService.floatingWidgetAlertingInfos().imgId != null) {

            Drawable leftText;
            if (safetyNexAppiService.floatingWidgetAlertingInfos().imgId != null) {

                int ressourceId = getApplicationContext().getResources().getIdentifier(safetyNexAppiService.floatingWidgetAlertingInfos().imgId, "drawable", getApplicationContext().getPackageName());
                leftText = getResources().getDrawable(ressourceId, null);

            } else {
                leftText = TextDrawable.builder()
                        .beginConfig()
                        .width(100)  // width in px
                        .height(100) // height in px
                        .withBorder(5)
                        .textColor(DrawableUtils.getDrawableColor(safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetTxtColor()))
                        .fontSize(30)
                        .bold()
                        .endConfig()
                        .buildRound(safetyNexAppiService.floatingWidgetAlertingInfos().getTextRounded(), Color.WHITE);
            }

            text.setCompoundDrawablesWithIntrinsicBounds(leftText,
                    null,
                    null,
                    null);
        } else {
            text.setCompoundDrawablesWithIntrinsicBounds(DrawableUtils.getTextIconDrawable(safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetBorderColor()), 0, 0, 0);
        }

        text.setCompoundDrawablesRelativeWithIntrinsicBounds(
                text.getCompoundDrawables()[0],
                text.getCompoundDrawables()[1],
                getDrawable(R.drawable.ic_pause_circle_outline_white_24dp),
                text.getCompoundDrawables()[3]
        );
        text.setTextColor(DrawableUtils.getDrawableColor(safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetTxtColor()));
    }

    public void writeToFile()
    {
        // Get the directory for the user's public pictures directory.
        final File path =
            Environment.getExternalStoragePublicDirectory
                (
                    //Environment.DIRECTORY_PICTURES
                    Environment.DIRECTORY_DOWNLOADS + "/safetyNext/"
                );

        // Make sure the path directory exists.
        if(!path.exists())
        {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        final File file = new File(path, "save"+System.currentTimeMillis()+".csv");
        // Save your stream, don't forget to flush() it before closing it.

        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void readMockFile(){
        final File path =
            Environment.getExternalStoragePublicDirectory
                (
                    //Environment.DIRECTORY_PICTURES
                    Environment.DIRECTORY_DOWNLOADS + "/safetyNext/"
                );

        File file = new File(path,CONSTANTS.MOCK_NAME);
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            br = new BufferedReader(new FileReader(file));
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
    }
}