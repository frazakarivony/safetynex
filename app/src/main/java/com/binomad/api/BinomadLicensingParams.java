package com.binomad.api;

public enum BinomadLicensingParams {
    //Objets directement construits
    IMEI("imei", "imei="),
    OREDERID("orderId", "orederId=");

    private String name = "";
    private String requestParam = "";

    //Constructeur
    BinomadLicensingParams(String name, String requestParam){
        this.name = name;
        this.requestParam = requestParam;
    }

    public String getRequestParam(){
        return requestParam;
    }

    public String toString(){
        return name;
    }
}
