package com.example.myapplication;

import com.nexyad.jndksafetynex.CNxRisk;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class FloatingWidgetAlertingInfos {

    private FloatingWidgetColorEnum floatingWidgetColorEnum;
    private String textRounded = null;

    public int m_iVisualAlert =   CNxRisk.CNxAlert.VISUAL_ALERT_1;
    public int m_iSafetyNexEngineState = CNxRisk.RISK_AVAILABLE;
    public int m_iTonesRiskAlert = CNxRisk.CNxAlert.TONE_ALERT;
    public String m_sTextToSpeech = "";
    public int m_iSpeedLimitTone = CNxRisk.CNxSpeedAlert.NO_SPEED_TONE;
    public int m_iNxAlertValue = -1;
    public int m_fRisk = 0;
    public String imgId = null;

    private static Map<Integer, String> textToSpeechMock;

    public FloatingWidgetAlertingInfos(FloatingWidgetColorEnum floatingWidgetColorEnum, String textRounded){
        this.floatingWidgetColorEnum = floatingWidgetColorEnum;
        this.textRounded = textRounded;
        this.generateTextToSpeachMock();
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

    private void generateTextToSpeachMock()
    {
        textToSpeechMock = new HashMap<Integer, String>();
        textToSpeechMock.put(1,"Accident");
        textToSpeechMock.put(2,"Passage d'animaux sauvages");
        textToSpeechMock.put(3,"Attention");
        textToSpeechMock.put(4,"Embouteillage");
        textToSpeechMock.put(5,"Virage dangereux");
        textToSpeechMock.put(6,"Eboulement possible");
        textToSpeechMock.put(7,"Forte descente");
        textToSpeechMock.put(8,"Forte montée");
        textToSpeechMock.put(9,"Risque de neige");
        textToSpeechMock.put(10,"Croisement");
        textToSpeechMock.put(11,"Réduction à une voie");
        textToSpeechMock.put(12,"Voie réservée aux véhicules lents");
        textToSpeechMock.put(13,"Rétressissement de la chaussée");
        textToSpeechMock.put(14,"Dépassement interdit");
        textToSpeechMock.put(15,"Dépassement interdit pour les poids lourds");
        textToSpeechMock.put(16,"Passage piéton");
        textToSpeechMock.put(17,"Vous avez la piorité");
        textToSpeechMock.put(18,"Priorité sens opposé");
        textToSpeechMock.put(19,"Passage à niveau");
        textToSpeechMock.put(20,"Dos d'âne");
        textToSpeechMock.put(21,"Zone scolaire");
        textToSpeechMock.put(22,"Verglas");
        textToSpeechMock.put(23,"Panneau stop");
        textToSpeechMock.put(24,"Feu tricolore");
        textToSpeechMock.put(25,"Passage tramway");
        textToSpeechMock.put(26,"Vent violent");
        textToSpeechMock.put(27,"Succession de virages");
        textToSpeechMock.put(28,"Cédez le passage");
    }

    public static FloatingWidgetAlertingInfos generateFakeFloating(FloatingWidgetColorEnum enumF, String txt){
        FloatingWidgetAlertingInfos toReturn = new FloatingWidgetAlertingInfos(enumF, txt);
        Integer indexMock = 1 + (int)(Math.random() * (textToSpeechMock.size() - 1));
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
                toReturn.m_sTextToSpeech = textToSpeechMock.get(3);
                toReturn.m_iNxAlertValue = 3;
                break;
            case ALERT:
                toReturn.m_iSafetyNexEngineState = CNxRisk.RISK_AVAILABLE;
                toReturn.m_iVisualAlert = CNxRisk.CNxAlert.VISUAL_ALERT_3;

                toReturn.m_sTextToSpeech = textToSpeechMock.get(indexMock);
                toReturn.m_iNxAlertValue = indexMock;
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
