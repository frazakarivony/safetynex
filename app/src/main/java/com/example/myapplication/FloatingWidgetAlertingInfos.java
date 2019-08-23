package com.example.myapplication;

import com.nexyad.jndksafetynex.CNxRisk;

public class FloatingWidgetAlertingInfos {

    private FloatingWidgetColorEnum floatingWidgetColorEnum;
    private String textRounded = null;

    public int m_iVisualAlert =   CNxRisk.CNxAlert.VISUAL_ALERT_1;
    public int m_iSafetyNexEngineState = CNxRisk.RISK_AVAILABLE;
    public int m_iTonesRiskAlert = CNxRisk.CNxAlert.TONE_ALERT;
    public String m_sTextToSpeech = "";
    public int m_iSpeedLimitTone = CNxRisk.CNxSpeedAlert.SPEED_TONE;
    public int m_fRisk = 0;

    public FloatingWidgetAlertingInfos(FloatingWidgetColorEnum floatingWidgetColorEnum, String textRounded){
        this.floatingWidgetColorEnum = floatingWidgetColorEnum;
        this.textRounded = textRounded;
    }

    public FloatingWidgetColorEnum getFloatingWidgetColorEnum() {
        return floatingWidgetColorEnum;
    }

    public void setFloatingWidgetColorEnum(FloatingWidgetColorEnum floatingWidgetColorEnum) {
        this.floatingWidgetColorEnum = floatingWidgetColorEnum;
    }

    public String getTextRounded() {
        return textRounded;
    }

    public void setTextRounded(String textRounded) {
        this.textRounded = textRounded;
    }

    public static FloatingWidgetAlertingInfos generateFakeFloating(FloatingWidgetColorEnum enumF, String txt){
        FloatingWidgetAlertingInfos toReturn = new FloatingWidgetAlertingInfos(enumF, txt);

        switch (enumF){
            case LOW_OF_LOWLEVEL:
                toReturn.m_iSafetyNexEngineState = CNxRisk.RISK_AVAILABLE;
                toReturn.m_fRisk = 0;
                toReturn.m_iVisualAlert = CNxRisk.CNxAlert.VISUAL_ALERT_1;
                break;
            case MEDIUM_OF_LOWLEVEL:
                toReturn.m_iSafetyNexEngineState = CNxRisk.RISK_AVAILABLE;
                toReturn.m_fRisk = 1;
                toReturn.m_iVisualAlert = CNxRisk.CNxAlert.VISUAL_ALERT_1;
                break;
            case HIGH_OF_LOWLEVEL:
                toReturn.m_iSafetyNexEngineState = CNxRisk.RISK_AVAILABLE;
                toReturn.m_fRisk = 2;
                toReturn.m_iVisualAlert = CNxRisk.CNxAlert.VISUAL_ALERT_1;
                break;
            case WARNING:
                toReturn.m_iSafetyNexEngineState = CNxRisk.RISK_AVAILABLE;
                toReturn.m_iVisualAlert = CNxRisk.CNxAlert.VISUAL_ALERT_2;
                toReturn.m_sTextToSpeech = "Warning";
                break;
            case ALERT:
                toReturn.m_iSafetyNexEngineState = CNxRisk.RISK_AVAILABLE;
                toReturn.m_iVisualAlert = CNxRisk.CNxAlert.VISUAL_ALERT_3;
                toReturn.m_sTextToSpeech = "Alert alert";
                toReturn.m_iTonesRiskAlert = CNxRisk.CNxAlert.TONE_ALERT;
                break;
            case WARNING_SPEED:
                toReturn.m_iSafetyNexEngineState = CNxRisk.RISK_AVAILABLE;
                toReturn.m_iSpeedLimitTone = CNxRisk.CNxSpeedAlert.SPEED_TONE;
                break;
            case GPS_LOST:
                toReturn.m_iSafetyNexEngineState = CNxRisk.GPS_LOST;
                toReturn.m_sTextToSpeech = "GPS lost";
                break;
        }
        return toReturn;
    }
}
