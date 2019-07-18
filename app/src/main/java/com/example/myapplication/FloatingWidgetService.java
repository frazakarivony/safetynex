package com.example.myapplication;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;


public class FloatingWidgetService extends Service {


    private WindowManager mWindowManager;
    private View mOverlayView;
    int mWidth;
    TextFab counterFab;
    boolean activity_background;


    public FloatingWidgetService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            activity_background = intent.getBooleanExtra("activity_background", false);
        }

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

            counterFab = mOverlayView.findViewById(R.id.fabHead);

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
            } else {
                counterFab.increase();
            }

            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            setTheme(R.style.AppTheme);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mOverlayView != null){
                mWindowManager.removeView(mOverlayView);
            }
        }
    }