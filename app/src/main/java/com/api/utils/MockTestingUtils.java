package com.api.utils;

import com.api.safetynex.service.floatingwidget.FloatingWidgetAlertingInfos;
import com.api.safetynex.service.floatingwidget.FloatingWidgetColorEnum;

import java.util.ArrayList;

public class MockTestingUtils {

    public static ArrayList<FloatingWidgetAlertingInfos> generate(){
        ArrayList<FloatingWidgetAlertingInfos> mock = new ArrayList<FloatingWidgetAlertingInfos>();
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.LOW_OF_LOWLEVEL, null));
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.MEDIUM_OF_LOWLEVEL, null));
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.HIGH_OF_LOWLEVEL, null));
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.WARNING, null));
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.ALERT, null));
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.WARNING_SPEED, "90"));
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.GPS_LOST, "GPS"));
        return mock;
    }
}
