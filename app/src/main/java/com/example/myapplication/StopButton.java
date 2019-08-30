package com.example.myapplication;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

public class StopButton extends BroadcastReceiver {

    String TAG ="StopButton";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"onReceive");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}