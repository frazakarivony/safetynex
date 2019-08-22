package com.example.myapplication;

public class FloatingWidgetAlertingInfos {

    private FloatingWidgetColorEnum floatingWidgetColorEnum;
    private String textRounded = null;

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

}
