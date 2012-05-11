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

import android.util.Log;
import edu.berkeley.cs.amplab.carat.thrift.CellInfo;

public class distanceThread extends Thread{
    String latitude=null;
    String longitude=null;
    CellInfo curCell;
    
    public distanceThread(CellInfo curCell) {
        this.curCell = curCell;
    }
    /*Get the latitude and longitude of the current location*/ 
    public void run () {
        
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
            Log.v("hello","hell"+sb+sb.length());
            if(sb.length()==0){
                latitude =null;
                longitude = null;
                Log.v("Itude", latitude + longitude);
            }
            else{
                JSONObject json = new JSONObject(sb.toString());
                JSONObject subjosn = new JSONObject(json.getString("location"));
     
                latitude=subjosn.getString("latitude");
                longitude = subjosn.getString("longitude");
             
            Log.v("Itude", "latitude:"+latitude+"longitude:"+longitude);
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
