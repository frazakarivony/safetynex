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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
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
import com.api.safetynex.listener.LicensingListener;
import com.api.safetynex.listener.interfaces.OnEventListener;
import com.api.safetynex.MainApp;
import com.api.safetynex.R;
import com.api.safetynex.listener.DoubleclickListenerPerso;
import com.api.safetynex.service.SafetyNexAppiService;
import com.api.utils.ConnectionUtils;
import com.api.safetynex.receiver.AppReceiver;
import com.api.safetynex.receiver.StopNotificationReceiver;
import com.api.utils.DrawableUtils;
import com.api.exceptions.NexiadException;
import com.nexiad.safetynexappsample.CNxDemoData;
import com.nexiad.safetynexappsample.CNxInputAPI;
import com.nexiad.safetynexappsample.CONSTANTS;

import java.util.Objects;

public class FloatingWidgetService extends Service implements SensorEventListener{

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
    private long lastUpdate = 0;
    private float last_x, last_y, last_z=0;
    private static final int SHAKE_THRESHOLD = 600;
    private CNxDemoData mData;
    private String workingPath = CONSTANTS.DEMO_WORKING_PATH;
    private String inputFile = workingPath + CONSTANTS.DEMO_IN_FILE_NAME;
    private String outputFile = workingPath + CONSTANTS.DEMO_OUT_FILE_NAME;
    private Boolean isPaused=false;
    private SafetyNexAppiService safetyNexAppiService;
    private LicenseAppiService licenseAppiService;
    private DoubleclickListenerPerso doubleclickListenerPerso;
    private OnEventListener onEventListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
   /*
        récuparation du layout
         */
        if (mOverlayView == null) {
            mOverlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);
        }

        AppReceiver.getInstance().setFloatingWidgetService(this);
        startForeground(12345678, createFloatingWidgetSpecialNotification());
        setTheme(R.style.AppTheme);
        mData = new CNxDemoData(inputFile, outputFile);

        safetyNexAppiService = SafetyNexAppiService.getInstance(getApplication());

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
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        onEventListener = new LicensingListener(this.safetyNexAppiService.getApp());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mOverlayView != null){
            mWindowManager.removeView(mOverlayView);
        }
        mLocationManager.removeUpdates(mLocationListener);
        mSensorManager.unregisterListener(this.mSensorListener);
        unregisterReceiver(AppReceiver.getInstance());

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
                mWindowManager.addView(mOverlayView, params);
                Display display = mWindowManager.getDefaultDisplay();
                display.getSize(size);
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

            doubleclickListenerPerso = new DoubleclickListenerPerso(getApplicationContext(),params, mWindowManager, intent);
            mOverlayView.setOnTouchListener(doubleclickListenerPerso);

            addListenerSensor();
            addListenerLocation(doubleclickListenerPerso);

            TextView text = mOverlayView.findViewById(R.id.textView2);
            text.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        if(event.getRawX() >= (text.getRight() - text.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            if(!isPaused){
                                safetyNexAppiService.speechOut(getString(R.string.pause));
                                text.setCompoundDrawablesWithIntrinsicBounds(
                                        null,
                                        null,
                                        getDrawable(R.drawable.ic_play_circle_outline_white_24dp),
                                        null);
                            }
                            isPaused = !isPaused;
                            return true;
                        }
                    }
                    return false;
                }
            });
       // }

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
                last_x = sensorEvent.values[CONSTANTS.ACCELOROMETRE_X_INDIXE];
                last_y = sensorEvent.values[CONSTANTS.ACCELOROMETRE_Y_INDIXE];
                last_z = sensorEvent.values[CONSTANTS.ACCELOROMETRE_Z_INDIXE];

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        mSensorManager.registerListener(mSensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    private void addListenerLocation(final DoubleclickListenerPerso doubleclickListenerPerso) {
        Log.i(TAG, "addListenerLocation");
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {

                final TextView text = mOverlayView.findViewById(R.id.textView2);

                if(!isPaused) {
                    if (ConnectionUtils.isInternetConnection(getApplicationContext())) {

                        CNxInputAPI mInpuAPI = new CNxInputAPI();

                        if (CONSTANTS.DEMO_DATA_TEST) {
                            String lineFull = mData.ReadNextData();
                            mInpuAPI.ParseData(lineFull);
                            mInpuAPI.setNbOfSat(7);
                        } else {
                            mInpuAPI.setmLat((float) location.getLatitude());
                            mInpuAPI.setmLon((float) location.getLongitude());
                            mInpuAPI.setmTime(location.getTime());
                            mInpuAPI.setmTimeDiffGPS(System.currentTimeMillis() - location.getTime());
                            mInpuAPI.setNbOfSat(location.getExtras().getInt("satellites"));
                            mInpuAPI.setmCap(location.getBearing());
                            mInpuAPI.setmSpeed(location.getSpeed() * CONSTANTS.SPEED_MS_TO_KH);
                            mInpuAPI.setmAccelZ(last_x);
                            mInpuAPI.setmAccelY(last_y);
                            mInpuAPI.setmAccelX(last_x);
                        }

                    text.setText(safetyNexAppiService.getRisk(mInpuAPI));

                    ((LinearLayout)text.getParent()).setBackground(getApplicationContext().getDrawable(DrawableUtils.getDrawableColor(safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetBorderColor())));
                    // text.setCompoundDrawablesWithIntrinsicBounds(getTextIconDrawable(doubleclickListenerPerso.safetyNexAppiService.getColorEnum().getFloatingWidgetBorderColor()) ,0,0,0);


                    if(safetyNexAppiService.floatingWidgetAlertingInfos().getTextRounded() != null) {
                        text.setCompoundDrawablesWithIntrinsicBounds(TextDrawable.builder()
                                .beginConfig()
                                .width(60)  // width in px
                                .height(60) // height in px
                                .withBorder(5)
                                .textColor(DrawableUtils.getDrawableColor(safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetTxtColor()))
                                .fontSize(30)
                                .bold()
                                .endConfig()
                                .buildRound(safetyNexAppiService.floatingWidgetAlertingInfos().getTextRounded() , Color.WHITE),
                                null,
                                null,
                                null);
                    }else{
                        text.setCompoundDrawablesWithIntrinsicBounds(DrawableUtils.getTextIconDrawable(safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetBorderColor()) ,0,0,0);
                    }

                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            text.getCompoundDrawables()[0],
                            text.getCompoundDrawables()[1],
                            getDrawable(R.drawable.ic_pause_circle_outline_white_24dp),
                            text.getCompoundDrawables()[3]
                    );
                    text.setTextColor(DrawableUtils.getDrawableColor(safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetTxtColor()));
                }   else {
                        text.setText(R.string.no_connection);
                        safetyNexAppiService.speechOut(getString(R.string.no_connection));
                    }
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        Log.i(TAG, "onSensorChanged");

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {

                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}