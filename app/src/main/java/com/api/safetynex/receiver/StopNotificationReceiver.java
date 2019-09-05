package com.api.safetynex.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StopNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("AppReceiver", intent.getAction());
        String action = intent.getAction();

        if(action != null){
            switch (action){
                case "NOTIFKILLSIG" :
                    context.sendBroadcast(new Intent("KILL"));
                    break;
                default:
                    break;
            }
        }
    }

}
