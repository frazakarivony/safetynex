package com.binomad.api;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.exceptions.BenomadException;
import com.nexiad.safetynexappsample.CONSTANTS;

import org.json.JSONObject;
import org.kobjects.base64.Base64;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpsTransportSE;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class LicenseServiceFred extends AsyncTask{

    private static final String NAMESPACE = "bnd:licenseWSDL";
    private static final String METHOD_NAME = "NewLicenseEx";
    private static final String SOAP_ACTION = "bnd:licenseWSDL#NewLicenseEx";
    private OnEventListener<String> mCallBack;
    private Context mContext;
    public Exception mException;
    private String licenseBndPathFile;
    private String licenseNxPathFile;
    private String certificateNxtPathFile;
    private String imei;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36";

    public LicenseServiceFred(Context context, OnEventListener callback, String imei) {
        this.mCallBack = callback;
        this.mContext = context;
        this.licenseBndPathFile = CONSTANTS.DEMO_WORKING_PATH.concat(CONSTANTS.DEMO_LICENSE_FILE);
        this.licenseNxPathFile = CONSTANTS.DEMO_WORKING_PATH.concat(CONSTANTS.DEMO_LICENSE_FILE_NEXYAD);
        this.certificateNxtPathFile = CONSTANTS.DEMO_WORKING_PATH.concat(CONSTANTS.DEMO_CERTIFICATE_FILE_NEXYAD);
        this.imei = imei;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        // Instantiate the RequestQueue.
        String surl =CONSTANTS.NEXYAD_LICENSING_URI+BinomadLicensingParams.IMEI.getRequestParam().concat(this.imei).concat("&").concat(BinomadLicensingParams.OREDERID.getRequestParam()).concat(this.imei);
        // Request a string response from the provided URL.
        try {

            Map<String, String> properties = new HashMap<String, String>();
            properties.put("Content-Type", "application/json");
            properties.put("Authorization", "Basic "+ Base64.encode("softeam:SEFzk6489A".getBytes()));
            getNexiadLicenceAndGetCertificate(surl, "GET",properties);
            getBenomadLicence(3550, this.imei);

            this.mCallBack.onSuccess(new String("ok"));

        }catch (Exception e){
            e.printStackTrace();
            this.mCallBack.onFailure(e);
        }
        return null;
    }

    private void getCertificate(String surl) throws Exception {
        HttpsURLConnection urlConnectionHttps = generateHttpsURLConnection(surl,"GET",null);
        if (urlConnectionHttps.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            System.out.println("****** getCertificate Content of the URL ********");
            writeStreamToFile(this.certificateNxtPathFile, urlConnectionHttps.getInputStream());
        }
    }

    private void setHttpUrlRequestProperty(HttpsURLConnection urlConnection, Map<String, String> properties){
        if(properties != null) {
            for (Map.Entry m : properties.entrySet()) {
                urlConnection.setRequestProperty(m.getKey().toString(), m.getValue().toString());
            }
        }
    }

    private HttpsURLConnection generateHttpsURLConnection(String surl, String methode, Map<String, String> properties) throws Exception{

        HttpsURLConnection urlConnectionHttps = null;
        URL url = new URL(surl);
        urlConnectionHttps = (HttpsURLConnection)url.openConnection();

        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        urlConnectionHttps.setSSLSocketFactory(sf);

        urlConnectionHttps.setRequestMethod(methode);
        setHttpUrlRequestProperty(urlConnectionHttps, properties);

        return urlConnectionHttps;
    }

    private void getBenomadLicence(int orderId, String imei) throws Exception {
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
        if(objetSOAP != null && objetSOAP.getProperty("errorCode").toString().equals("0")) {
            writeStreamToFile(this.licenseBndPathFile, new ByteArrayInputStream(Base64.decode(objetSOAP.getProperty("licenseContent").toString())));
        }else{
            throw new BenomadException("ERREUR de récupération de la licence Binomad");
        }
    }

    private void getNexiadLicenceAndGetCertificate(String surl,String methode , Map<String, String> params) throws Exception{

        HttpsURLConnection httpsURLConnection = generateHttpsURLConnection(surl,methode,params);
        JSONObject jsonObject = null;
        if(httpsURLConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            Log.v("getNexiadLicenceAndGetCertificateUri", String.valueOf(httpsURLConnection.getResponseCode()));
            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(httpsURLConnection.getInputStream()));

            String input;
            StringBuffer stringBuffer = new StringBuffer();

            while ((input = br.readLine()) != null) {
                stringBuffer.append(input);
                System.out.println(input);
            }
            br.close();
            jsonObject = new JSONObject(stringBuffer.toString());
            writeToFile(licenseNxPathFile, jsonObject.getString("Content"));
            getCertificate(jsonObject.getString("KeyStoreFileUri"));
        }
    }

    private void writeToFile(String filepath, String content) {
        try {
            Log.i("writing file : ", filepath);
            File f = new File(filepath);
            if (f.exists()){
                f.delete();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(filepath);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(content);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void writeStreamToFile(String filepath, InputStream content) {
        try {
            Log.i("writing file : ", filepath);
            File f = new File(filepath);
            if (f.exists()){
                f.delete();
                f.createNewFile();
            }
            java.nio.file.Files.copy(
                    content,
                    f.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


}