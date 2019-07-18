package com.binomad.api;
import android.os.AsyncTask;
import android.util.Log;

import org.kobjects.base64.Base64;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpsTransportSE;

import java.util.ArrayList;
import java.util.List;

public class AppelService extends AsyncTask {

    private static final String NAMESPACE = "bnd:licenseWSDL";
    private static final String METHOD_NAME = "NewLicenseEx";
    private static final String SOAP_ACTION = "bnd:licenseWSDL#NewLicenseEx";

    private String licenseEx;

    @Override
    protected Object doInBackground(Object[] objects) {
        this.licenseEx = newLicenseEx(3550, "357330074843194" );
        return null;
    }

    private String newLicenseEx(int orderId, String imei) {
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("orderId", String.valueOf(orderId));
            request.addProperty("HSUID", imei);
            request.addProperty("purchaseId", imei);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();
            headers.add(new HeaderProperty("Authorization", "Basic "+ Base64.encode("softeam-data:vgHHmV4hXai".getBytes())));


            HttpsTransportSE androidHttpTransport = new HttpsTransportSE("licensing.benomad.com", 443, "/licensing/licenseService.php", 10000);
            androidHttpTransport.call(SOAP_ACTION, envelope, headers);
            SoapObject objetSOAP = (SoapObject)envelope.getResponse();
            return this.parserObjet(objetSOAP);

        } catch (Exception e) {
            Log.e("NewLicenseEx", "", e);
            return null;
        }
    }

    private String parserObjet(SoapObject objet) {
        SoapObject licenseExObjet = (SoapObject)objet.getProperty("TBRetLicenseRequest");
        return licenseExObjet.getProperty("return").toString();
    }

    public String getLicenseEx() {
        return licenseEx;
    }

}