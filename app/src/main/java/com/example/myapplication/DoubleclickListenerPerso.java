package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

class DoubleclickListenerPerso implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private GestureDetector gestureDetector;
    private WindowManager mWindowManager;
    private Context context;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private WindowManager.LayoutParams params;
    private Intent intentFloatingService;
    private MainApp mainApp;
    private SafetyNexAppiService safetyNexAppiService;

    DoubleclickListenerPerso(Context context, WindowManager.LayoutParams params, WindowManager windowManager, Intent intent, MainApp mainApp, View view){
        this.context = context;
        this.mainApp = mainApp;
        this.params = params;
        this.mWindowManager = windowManager;
        this.intentFloatingService = intent;
        gestureDetector = new GestureDetector(context, this);
        this.safetyNexAppiService = new SafetyNexAppiService(this.mainApp, view);
        this.safetyNexAppiService.initAPI();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        context.stopService(this.intentFloatingService);
        this.safetyNexAppiService.stop();
        context.startActivity(this.mainApp.getCurrentActivity().getIntent());
        Log.v("DoubleClickListener", "onSingleTapUp");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        context.stopService(this.intentFloatingService);
        Log.v("DoubleClickListener", "onSingleTapUp");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        String msg = "";
        msg = "Mouvement vers : ";
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    msg += "bas detecté " ;
                    //remember the initial position.
                    initialX = params.x;
                    initialY = params.y;

                    //get the touch location
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();

                    break;
                case MotionEvent.ACTION_MOVE:
                    msg += "indéterminé detecté ";
                    //Calculate the X and Y coordinates of the view.
                    params.x = initialX + (int) (event.getRawX() - initialTouchX);
                    params.y = initialY + (int) (event.getRawY() - initialTouchY);

                    //Update the layout with new X & Y coordinates
                    mWindowManager.updateViewLayout(v, params);
                    break;
                case MotionEvent.ACTION_UP:
                    msg="";
                    break;
            }
            Log.v("DoubleClickListener" , msg);
        return this.gestureDetector.onTouchEvent(event);
    }


}
