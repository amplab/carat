package edu.berkeley.cs.amplab.carat.android.sampling;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.protocol.CommsThread;
import edu.berkeley.cs.amplab.carat.thrift.CellInfo;

public class distanceThread extends Thread{
    private boolean isRunning = true;
    double latitude=0;
    double longitude=0;
    double distance=0;
    CaratApplication app = null;

    
    public distanceThread(CaratApplication app) {
        this.app = app;
    }
    public void stopRunning() {
        isRunning = false;
    }
    
    public void appResumed() {
        synchronized (distanceThread.this) {
            distanceThread.this.interrupt();
        }
    }
   
   /*Get the latitude and longitude of the current location*/ 
    public void run () {
        while (isRunning) {
            CellInfo curCell = new CellInfo();
            Context c = app.getApplicationContext();
            curCell=SamplingLibrary.getCellInfo(c);    
            
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost request = new HttpPost("http://www.google.com/loc/json");
        try {
            JSONObject holder = new JSONObject();
            holder.put("version", "1.1.0");
            holder.put("host", "maps.google.com");
            holder.put("address_language", "en_FI");
            holder.put("request_address", true);
            holder.put("radio_type", "gsm");
     
            JSONObject data = new JSONObject();
            data.put("mobile_country_code", curCell.MCC);
            data.put("mobile_network_code", curCell.MNC);
            data.put("location_area_code", curCell.LAC);
            data.put("cell_id", curCell.CID);
            data.put("age", 0);
     
            JSONArray dataArray = new JSONArray();
            dataArray.put(data);
            holder.put("cell_towers", dataArray);

            StringEntity se = new StringEntity(holder.toString());
            request.setEntity(se);
            
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            InputStreamReader stream = new InputStreamReader(entity.getContent());
            BufferedReader br = new BufferedReader(stream);
            StringBuffer sb = new StringBuffer();
            
            String info = br.readLine();

            while (info != null) {
                //Log.e("receive location",info);
                sb.append(info);
                info=br.readLine();
            }
            Log.v("ResultLocation","Location info:"+sb+sb.length());
            if(latitude==0 && longitude==0){
                if(sb.length()==0){
                    Log.v("Error", "Get location error!\n");
                }
                else{
                    JSONObject json = new JSONObject(sb.toString());
                    JSONObject subjosn = new JSONObject(json.getString("location"));
     
                    latitude=Double.parseDouble(subjosn.getString("latitude"));
                    longitude = Double.parseDouble(subjosn.getString("longitude"));
                    distance=0;
                    Log.v("Itude & distance", "latitude:"+latitude+"longitude:"+longitude+"Distance:"+distance);
                }            
            
            }
            else{
                if(sb.length()==0){
                    Log.v("Error", "Get location error!\n");
                }
                else{
                    JSONObject json = new JSONObject(sb.toString());
                    JSONObject subjosn = new JSONObject(json.getString("location"));
                    double endlatitude = Double.parseDouble(subjosn.getString("latitude"));
                    double endlongitude = Double.parseDouble(subjosn.getString("longitude")) ;
                    distance=distance+SamplingLibrary.getDistance(latitude, longitude, endlatitude, endlongitude);
                    
                    Log.v("Itude & distance", "latitude:"+latitude+"longitude:"+longitude+"Distance:"+distance);
                }
                
            }
            try {
                sleep(CaratApplication.COMMS_INTERVAL);
            } catch (InterruptedException e) {
                
                try {
                    sleep(CaratApplication.COMMS_WIFI_WAIT);
                } catch (InterruptedException e1) {
                
                }
            }
        } catch (Exception e) {
            Log.e(e.getMessage(), e.toString());
        } 
        finally{
            request.abort();
            httpclient = null;
        }
        
    }
   }
}
