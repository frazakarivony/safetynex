package com.example.myapplication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

public class MainApp extends MultiDexApplication {

    private boolean firstRun = true;

    private Activity mCurrentActivity = null;

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }

    @Override
    protected void attachBaseContext(Context newBase){
        super.attachBaseContext(newBase);
        MultiDex.install(newBase);
    }

    public boolean isFirstRun(){
        return this.firstRun;
    }

    public void setFirsRun(boolean firstRun){
        this.firstRun = firstRun;
    }

}
