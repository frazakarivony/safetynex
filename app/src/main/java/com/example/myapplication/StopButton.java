package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StopButton extends BroadcastReceiver {

    String TAG ="StopButton";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"onReceive");
        System.exit(0);
    }

}