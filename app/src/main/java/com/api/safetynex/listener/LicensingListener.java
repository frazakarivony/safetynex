package com.api.safetynex.listener;

import android.app.Application;
import android.content.Context;
import android.widget.Button;

import com.api.exceptions.NexiadException;
import com.api.safetynex.MainApp;
import com.api.safetynex.R;
import com.api.safetynex.listener.interfaces.OnEventListener;
import com.api.safetynex.service.SafetyNexAppiService;

public class LicensingListener implements OnEventListener {

    private Application application;

    public LicensingListener(Application application){
        this.application = application;
    }

    @Override
    public void onSuccess(Object object) {
        if("ok".equals(object)) {
            if (((MainApp)this.application).isFirstRun()) {
                   /*     Button button = mOverlayView.findViewById(R.id.button);
                        button.setClickable(true);
                        button.callOnClick();*/
                try {
                    SafetyNexAppiService.getInstance(this.application).initAPI();
                }catch (NexiadException e){
                    e.printStackTrace();
                    //  licenseAppiService.execute();
                }
                ((MainApp)this.application).setFirsRun(false);
            }
        }
    }

    public void onFailure(Exception e) {
        Button close = ((MainApp)this.application).getCurrentActivity().findViewById(R.id.close);
        close.callOnClick();
    }
}
