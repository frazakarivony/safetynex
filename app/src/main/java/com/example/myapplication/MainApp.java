package com.example.myapplication;

import android.app.Activity;
import android.app.Application;

import java.io.Serializable;

public class MainApp extends Application implements Serializable {

    private boolean firstRun = true;
    private Activity mCurrentActivity = null;
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
    public boolean isFirstRun(){
        return this.firstRun;
    }
    public void setFirsRun(boolean firstRun){
        this.firstRun = firstRun;
    }
}
