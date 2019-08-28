package com.example.myapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.location.Location;
import android.location.LocationListener;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.nexiad.safetynexappsample.CNxDemoData;
import com.nexiad.safetynexappsample.CNxInputAPI;
import com.nexiad.safetynexappsample.CONSTANTS;

import java.util.Objects;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class FloatingWidgetService extends Service implements SensorEventListener{

    private final String TAG = "FloatingWidgetService";
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mOverlayView == null) {

            mOverlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);

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

            DoubleclickListenerPerso doubleclickListenerPerso = new DoubleclickListenerPerso(getApplicationContext(),params, mWindowManager, intent, ((MainApp)getApplication()),mOverlayView);
            mOverlayView.setOnTouchListener(doubleclickListenerPerso);

            addListenerSensor();
            addListenerLocation(doubleclickListenerPerso);
        }
            return super.onStartCommand(intent, flags, startId);
    }

    private void addListenerSensor() {

        mSensorManager = (SensorManager) getSystemService(getApplicationContext().SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                last_x = sensorEvent.values[CONSTANTS.ACCELOROMETRE_X_INDIXE];
                last_y = sensorEvent.values[CONSTANTS.ACCELOROMETRE_Y_INDIXE];
                last_z = sensorEvent.values[CONSTANTS.ACCELOROMETRE_Z_INDIXE];
            //    Log.v("SensorEventListener ", "change x : "+last_x+" y : "+last_y+" z : "+last_z);

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
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                CNxInputAPI mInpuAPI = new CNxInputAPI();

                if(CONSTANTS.DEMO_DATA_TEST){
                    String lineFull = mData.ReadNextData();
                    mInpuAPI.ParseData(lineFull);
                    mInpuAPI.setNbOfSat(7);
                }else{
                    mInpuAPI.setmLat((float)location.getLatitude());
                    mInpuAPI.setmLon((float)location.getLongitude());
                    mInpuAPI.setmTime(location.getTime());
                    mInpuAPI.setmTimeDiffGPS(System.currentTimeMillis() - location.getTime());
                    mInpuAPI.setNbOfSat(location.getExtras().getInt("satellites"));
                    mInpuAPI.setmCap(location.getBearing());
                    mInpuAPI.setmSpeed(location.getSpeed()*CONSTANTS.SPEED_MS_TO_KH);
                    mInpuAPI.setmAccelZ(last_x);
                    mInpuAPI.setmAccelY(last_y);
                    mInpuAPI.setmAccelX(last_x);
                }



                final TextView text = mOverlayView.findViewById(R.id.textView2);
                text.setText(doubleclickListenerPerso.safetyNexAppiService.getRisk(mInpuAPI));

                ((LinearLayout)text.getParent()).setBackground(getApplicationContext().getDrawable(getDrawableColor(doubleclickListenerPerso.safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetBorderColor())));
                // text.setCompoundDrawablesWithIntrinsicBounds(getTextIconDrawable(doubleclickListenerPerso.safetyNexAppiService.getColorEnum().getFloatingWidgetBorderColor()) ,0,0,0);

                if(doubleclickListenerPerso.safetyNexAppiService.floatingWidgetAlertingInfos().getTextRounded() != null) {
                    text.setCompoundDrawablesWithIntrinsicBounds(TextDrawable.builder()
                            .beginConfig()
                            .width(60)  // width in px
                            .height(60) // height in px
                            .withBorder(5)
                            .textColor(getDrawableColor(doubleclickListenerPerso.safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetTxtColor()))
                            .fontSize(30)
                            .bold()
                            .endConfig()
                            .buildRound(doubleclickListenerPerso.safetyNexAppiService.floatingWidgetAlertingInfos().getTextRounded() , Color.WHITE), null, null, null);
                }else{
                    text.setCompoundDrawablesWithIntrinsicBounds(getTextIconDrawable(doubleclickListenerPerso.safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetBorderColor()) ,0,0,0);
                }

          /*      if(doubleclickListenerPerso.safetyNexAppiService.floatingWidgetAlertingInfos().imgId != null){
                    ImageView img = new ImageView(getApplicationContext());
                    int imageId = getResources().getIdentifier(doubleclickListenerPerso.safetyNexAppiService.floatingWidgetAlertingInfos().imgId ,"mipmap", getPackageName());
                    img.setImageResource(imageId);
                    text.setCompoundDrawablesWithIntrinsicBounds(img.getDrawable() ,null,null,null);
                }*/

                //text.setBackground(getApplicationContext().getDrawable(getDrawableColor(doubleclickListenerPerso.safetyNexAppiService.getColorEnum().getFloatingWidgetBorderColor())));
                text.setTextColor(getDrawableColor(doubleclickListenerPerso.safetyNexAppiService.floatingWidgetAlertingInfos().getFloatingWidgetColorEnum().getFloatingWidgetTxtColor()));
                //text.setText(String.valueOf(mInpuAPI.getmSpeed()));
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

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(12345678, getNotification());
        setTheme(R.style.AppTheme);
        Log.i(TAG, "onCreate");
        mData = new CNxDemoData(inputFile, outputFile);

        SensorManager senSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor senAccelerometer = Objects.requireNonNull(senSensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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

    }

    private Notification getNotification() {
        Intent snoozeIntent = new Intent(this, StopButton.class);
        snoozeIntent.setAction("dsffgds");
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);

        NotificationChannel channel = new NotificationChannel("channel_1","SafetyNext",NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
        Notification.Builder builder = new Notification.Builder(getApplicationContext(),"channel_1")
                .setContentTitle("SafetyNext")
                .setContentText("SafetyNext")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addAction(R.drawable.ic_check_box_black_24dp, getString(R.string.close_app),snoozePendingIntent);
        return builder.build();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
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

    private int getDrawableColor(String code){
        int drawableId;
        switch (code){
            case "LOW":
                drawableId = R.drawable.rounded_corner_low;
                break;
            case "MEDIUM":
                drawableId = R.drawable.rounded_corner_medium;
                break;
            case "HIGH":
                drawableId = R.drawable.rounded_corner_high;
                break;
            case "WARNING":
                drawableId = R.drawable.rounded_corner_warning;
                break;
            case "WARNING_SPEED":
                drawableId = R.drawable.rounded_corner_warning;
                break;
            case "GPS_LOST":
                drawableId = R.drawable.rounded_corner_gps_lost;
                break;
            case "ALERT":
                drawableId = R.drawable.rounded_corner_alert;
                break;
            case "BLACK":
                drawableId = R.color.colorNSXTxtBlack;
                break;
            case "ORANGE":
                drawableId = R.color.colorNSXBgWarningLevel;
                break;
            case "RED":
                drawableId = R.color.colorNSXBgAlertLevel;
                break;
            case "WHITE":
                drawableId = R.color.colorNSXTxtWhite;
                break;
            case "GREY":
                drawableId = R.color.colorNSXTxtGrey;
                break;
            default:
                drawableId = R.color.colorNSX;
                break;
        }
        return drawableId;
    }

    private int getTextIconDrawable(String code){
        int drawableId;
        switch (code){
            case "LOW":
                drawableId = R.drawable.ic_check_box_black_24dp;
                break;
            case "MEDIUM":
                drawableId = R.drawable.ic_medium_level_warning_24dp;
                break;
            case "HIGH":
                drawableId = R.drawable.ic_high_level_warning_24dp;
                break;
            case "WARNING":
                drawableId = R.drawable.ic_warning_24dp;
                break;
            case "ALERT":
                drawableId = R.drawable.ic_error_black_24dp;
                break;
            default:
                drawableId = R.drawable.ic_check_box_black_24dp;
                break;
        }
        return drawableId;
    }

}