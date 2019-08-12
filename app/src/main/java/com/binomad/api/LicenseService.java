package com.binomad.api;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nexiad.safetynexappsample.CONSTANTS;

import org.json.JSONException;
import org.json.JSONObject;
import org.kobjects.base64.Base64;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpsTransportSE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LicenseService extends AsyncTask {

    private static final String NAMESPACE = "bnd:licenseWSDL";
    private static final String METHOD_NAME = "NewLicenseEx";
    private static final String SOAP_ACTION = "bnd:licenseWSDL#NewLicenseEx";
    private OnEventListener<String> mCallBack;
    private Context mContext;
    public Exception mException;
    private String licenseBndPathFile;
    private String licenseNxPathFile;
    private String certificateNxtPathFile;
    private RequestQueue queue;
    private String imei;

    public LicenseService(Context context, OnEventListener callback, String imei) {
        this.mCallBack = callback;
        this.mContext = context;
        this.licenseBndPathFile = CONSTANTS.DEMO_WORKING_PATH.concat(CONSTANTS.DEMO_LICENSE_FILE);
        this.licenseNxPathFile = CONSTANTS.DEMO_WORKING_PATH.concat(CONSTANTS.DEMO_LICENSE_FILE_NEXYAD);
        this.certificateNxtPathFile = CONSTANTS.DEMO_WORKING_PATH.concat(CONSTANTS.DEMO_CERTIFICATE_FILE_NEXYAD);
        this.queue = Volley.newRequestQueue(context);
        this.imei = imei;
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        try {
            processLicenseBnd();
            processLicenseNx();
            return null;

        } catch (Exception e) {
            mException = e;
        }

        return null;
    }

    private void processLicenseNx() {
        if(!fileExist(licenseNxPathFile)){
            writeToFile(licenseBndPathFile, Objects.requireNonNull(newLicenseNx(this.imei)));
        }
    }

    private void processLicenseBnd(){
        if(!fileExist(licenseBndPathFile)){
            writeToFile(licenseBndPathFile, Objects.requireNonNull(newLicenseBnd(3550, this.imei)));
        }
    }

    private boolean fileExist(String filePath){
        File file = new File(filePath);
        return file.exists();
    }

    private String newLicenseNx(String imei){

        // Instantiate the RequestQueue.
        String url =CONSTANTS.NEXYAD_LICENSING_URI+BinomadLicensingParams.IMEI.getRequestParam().concat(this.imei).concat("&").concat(BinomadLicensingParams.OREDERID.getRequestParam()).concat(this.imei);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject= new JSONObject(response);
                            writeToFile(licenseNxPathFile, jsonObject.getString("Content"));
                            //TODO vérifier la validité du certificat. si le certificat n'existe pas ou s'il est périmé
                            Log.e("NxLicense", jsonObject.getString("Content") + " uri : "+jsonObject.getString("KeyStoreFileUri"));
                            if(!fileExist(certificateNxtPathFile)){
                                getCertificate(jsonObject.getString("KeyStoreFileUri"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("NxLicense", "That didn't work!");
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Basic c29mdGVhbTpTRUZ6azY0ODlB");

                return params;
            }
        };
        queue.add(stringRequest);

        return null;
    }

    private void getCertificate(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        byte[] data = response.getBytes(StandardCharsets.UTF_8);
                        //String base64 = android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT);
                        writeToFile(certificateNxtPathFile, android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT));
                        // todo find the good encoding or another methode to store the certificate
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("NxCertificate", "That didn't work!");
            }
        });
        this.queue.add(stringRequest);

    }

    private String newLicenseBnd(int orderId, String imei) {
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("orderId", String.valueOf(orderId));
            request.addProperty("HSUID", imei);
            request.addProperty("purchaseId", imei);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();
            headers.add(new HeaderProperty("Authorization", "Basic "+ Base64.encode("softeam-data:vgHHmV4hXai".getBytes())));


            HttpsTransportSE androidHttpsTransport = new HttpsTransportSE("licensing.benomad.com", 443, "/licensing/licenseService.php", 10000);
            androidHttpsTransport.call(SOAP_ACTION, envelope, headers);
            SoapObject objetSOAP = (SoapObject)envelope.getResponse();
            return this.parserObjet(objetSOAP);

        } catch (Exception e) {
            Log.e("NewLicenseEx", "", e);
            return null;
        }
    }

    private String parserObjet(SoapObject objet) {
        if(objet.getProperty("errorCode").toString().equals("0")){
            return objet.getProperty("licenseContent").toString();
        }
        else{
            return null;
        }
    }


    private void writeToFile(String filepath, String content) {
        try {
            Log.i("writing file : ", filepath);
            FileOutputStream fileOutputStream = new FileOutputStream(filepath);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(content);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}