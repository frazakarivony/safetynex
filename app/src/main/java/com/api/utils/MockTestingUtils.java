package com.api.utils;

import com.api.safetynex.service.floatingwidget.FloatingWidgetAlertingInfos;
import com.api.safetynex.service.floatingwidget.FloatingWidgetColorEnum;

import java.util.ArrayList;
import java.util.List;

public class MockTestingUtils {

    public static ArrayList<FloatingWidgetAlertingInfos> generate(){
        ArrayList<FloatingWidgetAlertingInfos> mock = new ArrayList<FloatingWidgetAlertingInfos>();
        List<String> vitesses = new ArrayList<String>();
        vitesses.add("20");
        vitesses.add("30");
        vitesses.add("50");
        vitesses.add("80");
        vitesses.add("90");
        vitesses.add("110");
        vitesses.add("130");

        Integer indexMockVitesse = (int)(Math.random() * (vitesses.size() - 1));

        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.LOW_OF_LOWLEVEL, null));
        /*mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.MEDIUM_OF_LOWLEVEL, null));
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.HIGH_OF_LOWLEVEL, null));*/
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.WARNING, null));
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.ALERT, null));
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.WARNING_SPEED, vitesses.get(indexMockVitesse)));
        mock.add(FloatingWidgetAlertingInfos.generateFakeFloating(FloatingWidgetColorEnum.GPS_LOST, "GPS"));
        return mock;
    }
}
