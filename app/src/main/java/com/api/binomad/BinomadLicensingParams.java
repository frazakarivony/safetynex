package com.api.binomad;

public enum BinomadLicensingParams {
    //Objets directement construits
    IMEI("imei", "imei="),
    OREDERID("orderId", "orderId=");

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
