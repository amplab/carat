package edu.berkeley.cs.amplab.carat.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;


public class JsonParser {
	
	public String getJSONFromUrl(String url) {
		InputStream inputStream = null;
	    String result = null; 
	    
		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
		
		try {
            // Set up HTTP post

            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(param));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            // Read content & Log
            inputStream = httpEntity.getContent();
        } catch (UnknownHostException e0) {
        	Log.d("JsonParser", "Unable to connect to the statstics server (no Internet on the device! is Wifi or mobile data on?), " + e0.toString());
        	return "";
        } catch (UnsupportedEncodingException e1) {
            Log.e("UnsupportedEncodingException", e1.toString());
            return "";
        } catch (ClientProtocolException e2) {
            Log.e("ClientProtocolException", e2.toString());
            return "";
        } catch (IllegalStateException e3) {
            Log.e("IllegalStateException", e3.toString());
            return "";
        } catch (IOException e4) {
            Log.e("IOException", e4.toString());
            return "";
        }
        // Convert response to string using String Builder
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
            StringBuilder sBuilder = new StringBuilder();

            String line = null;
            while ((line = bReader.readLine()) != null) {
                sBuilder.append(line + "\n");
            }

            inputStream.close();
            result = sBuilder.toString();

        } catch (Exception e) {
            Log.e("StringBuilding & BufferedReader", "Error converting result " + e.toString());
        }
        
        return result;
	}
}
