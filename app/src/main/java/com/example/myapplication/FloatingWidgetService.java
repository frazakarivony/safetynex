package com.example.myapplication;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
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
import android.widget.RelativeLayout;
import android.location.Location;
import android.location.LocationListener;
import android.widget.TextView;


public class FloatingWidgetService extends Service {

    private final String TAG = "FloatingWidgetService";
    private WindowManager mWindowManager;
    private View mOverlayView;
    int mWidth;
    private static final long LOCATION_REFRESH_TIME = 0;
    private static final float LOCATION_REFRESH_DISTANCE = 0;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;


    public FloatingWidgetService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        addListenerLocation();

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

        }
            return super.onStartCommand(intent, flags, startId);
        }

    private void addListenerLocation() {
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
                Log.i("Position Latitude : ", String.valueOf(location.getLatitude()));
                Log.i("Position Longitude : ", String.valueOf(location.getLongitude()));
                Log.i("Position Speed : ", String.valueOf(location.getSpeed()));
                Log.i("Position Accuracy : ", String.valueOf(location.getAccuracy()));

                final TextView text = mOverlayView.findViewById(R.id.textView2);
                text.setText(String.valueOf(location.getLatitude()));
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mOverlayView != null){
            mWindowManager.removeView(mOverlayView);
        }
        mLocationManager.removeUpdates(mLocationListener);
    }

    private Notification getNotification() {
        NotificationChannel channel = new NotificationChannel("test","test2",NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(),"test")
                .setContentTitle("SafetyNext")
                .setContentText("SafetyNext")
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        return builder.build();
    }



}