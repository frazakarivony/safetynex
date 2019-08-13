package com.example.myapplication;

public enum MainActivityColorEnum {
    //Objets directement construits
    LOW_OF_LOWLEVEL("LOW", "BLACK"),
    MEDIUM_OF_LOWLEVEL("MEDIUM", "BLACK"),
    HIGH_OF_LOWLEVEL("HIGH", "BLACK"),
    WARNING("WARNING", "BLACK"),
    ALERT("ALERT", "WHITE");

    private String bg = "";
    private String txt = "";

    //Constructeur
    MainActivityColorEnum(String bg, String txt){
        this.bg = bg;
        this.txt = txt;
    }

    public String getBg(){
        return bg;
    }

    public String getTxt(){
        return txt;
    }
}
