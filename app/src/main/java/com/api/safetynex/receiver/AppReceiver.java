package com.api.safetynex.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.api.safetynex.service.SafetyNexAppiService;
import com.api.safetynex.service.floatingwidget.FloatingWidgetService;
import com.api.safetynex.MainActivity;

public class AppReceiver extends BroadcastReceiver {

    private static AppReceiver appReceiver;

    private MainActivity mainActivity;
    private FloatingWidgetService floatingWidgetService;
    private SafetyNexAppiService safetyNexAppiService;
    private boolean restartOnlyActivity = false;

    public static AppReceiver getInstance(){
        if(appReceiver == null){
            appReceiver = new AppReceiver();
        }
        return appReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("AppReceiver", intent.getAction());
        String action = intent.getAction();

        if(action != null){
            switch (action){
                case "android.intent.action.BOOT_COMPLETED" :
                    Intent i = new Intent(context, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                    break;
                case  "FLOATING_OK":
                    if(mainActivity != null){
                        mainActivity.finish();
                    }
                    break;
                case "KILL":
                    safetyNexAppiService.mJniFunction.Death();
                    if(mainActivity != null){
                        mainActivity.finish();
                    }
                    if(floatingWidgetService != null){
                        floatingWidgetService.stopSelf();
                    }
                    break;
                case "RESTARTACTIVITY":
                    setRestartOnlyActivity(true);
                    Intent openMainActivity = new Intent(context, MainActivity.class);
                    openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(openMainActivity);
                    if(floatingWidgetService != null){
                        floatingWidgetService.stopSelf();
                    }
                    break;
//                case "STAT":
//                    Log.i("STAT", "STAT "+floatingWidgetService+" "+mainActivity);
//                    if(floatingWidgetService != null){
//                        floatingWidgetService.killAll();
//                    }
//                    if(mainActivity != null){
//                        mainActivity.restartMainActivity();
//                    }
//                    break;
                default:
                    break;
            }
        }
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setFloatingWidgetService(FloatingWidgetService floatingWidgetService) {
        this.floatingWidgetService = floatingWidgetService;
    }

    public void setSafetyNexAppiService(SafetyNexAppiService safetyNexAppiService) {
        this.safetyNexAppiService = safetyNexAppiService;
    }

    public boolean isRestartOnlyActivity() {
        return restartOnlyActivity;
    }

    public void setRestartOnlyActivity(boolean restartOnlyActivity) {
        this.restartOnlyActivity = restartOnlyActivity;
    }
}
