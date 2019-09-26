package com.api.safetynex.listener;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.api.safetynex.receiver.AppReceiver;

public class UserActionListener implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private GestureDetector gestureDetector;
    private WindowManager mWindowManager;
    private Context context;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private WindowManager.LayoutParams params;
    private Intent intentFloatingService;

    public UserActionListener(Context context, WindowManager.LayoutParams params, WindowManager windowManager, Intent intent){
        this.context = context;
        this.params = params;
        this.mWindowManager = windowManager;
        this.intentFloatingService = intent;
        gestureDetector = new GestureDetector(context, this);

        IntentFilter intentFilter = new IntentFilter();
        // Add network connectivity change action.
        intentFilter.addAction("RESTARTACTIVITY");
        // Set broadcast receiver priority.
        context.registerReceiver(AppReceiver.getInstance(), intentFilter);
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
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        //context.stopService(this.intentFloatingService);
        Log.i("DoubleClickListener", "onSingleTapUp");

        AppReceiver appReceiver = AppReceiver.getInstance();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("RESTARTACTIVITY");
        intentFilter.setPriority(100);
        context.registerReceiver(appReceiver, intentFilter);
        context.getApplicationContext().sendBroadcast(new Intent("RESTARTACTIVITY"));
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        String msg = "Mouvement vers : ";
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
