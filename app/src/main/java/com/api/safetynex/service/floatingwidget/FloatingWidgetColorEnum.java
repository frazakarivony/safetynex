package com.api.safetynex.service.floatingwidget;

public enum FloatingWidgetColorEnum {
    //Objets directement construits
    LOW_OF_LOWLEVEL("LOW", "BLACK"),
    MEDIUM_OF_LOWLEVEL("MEDIUM", "BLACK"),
    HIGH_OF_LOWLEVEL("HIGH", "BLACK"),
    WARNING("WARNING", "BLACK"),
    WARNING_SPEED("WARNING_SPEED", "RED"),
    GPS_LOST("GPS_LOST", "GREY"),
    ALERT("ALERT", "RED");

    private String floatingWidgetBorderColor = "";
    private String floatingWidgetTxtColor = "";

    //Constructeurs
    FloatingWidgetColorEnum(String floatingWidgetBorderColor, String floatingWidgetTxtColor){
        this.floatingWidgetBorderColor = floatingWidgetBorderColor;
        this.floatingWidgetTxtColor = floatingWidgetTxtColor;
    }

    public String getFloatingWidgetBorderColor(){
        return floatingWidgetBorderColor;
    }

    public String getFloatingWidgetTxtColor(){
        return floatingWidgetTxtColor;
    }

}
