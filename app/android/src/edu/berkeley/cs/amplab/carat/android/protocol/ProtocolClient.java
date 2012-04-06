package edu.berkeley.cs.amplab.carat.android.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

import android.R;
import android.content.Context;
import android.util.Log;

import edu.berkeley.cs.amplab.carat.thrift.CaratService;

/**
 * Client for the Carat Protocol.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class ProtocolClient {
  public static final String SERVER_PROPERTIES = "caratserver.properties";
  public static int SERVER_PORT = 0;
  public static String SERVER_ADDRESS = null;

  private static CaratService.Client instance = null;

  public static CaratService.Client getInstance(Context c) {
    if (SERVER_ADDRESS == null) {
      Properties properties = new Properties();
      try {
        InputStream raw = c.getAssets().open(SERVER_PROPERTIES);
        if (raw != null) {
          properties.load(raw);
          if (properties.containsKey("PORT"))
            SERVER_PORT = Integer.parseInt(properties.getProperty("PORT", "8080"));
          if (properties.containsKey("ADDRESS"))
            SERVER_ADDRESS = properties.getProperty("ADDRESS", "server.caratproject.com");

          Log.d("CaratProtocol", "Set address=" + SERVER_ADDRESS + " port=" + SERVER_PORT);
        } else
          Log.e("CaratProtocol", "Could not open server property file!");
      } catch (IOException e) {
        Log.e("CaratProtocol", "Could not open server property file: " + e.toString());
      }
    }

    if (instance == null) {
      // Initialize server port and address
      try {
        TSocket s = new TSocket(SERVER_ADDRESS, SERVER_PORT);
        s.open();
        TProtocol p = new TBinaryProtocol(s, true, true);
        instance = new CaratService.Client(p);
      } catch (Exception e) {
        Log.e("CaratProtocol", "Could not create instance!");
        e.printStackTrace();
      }
    }
    return instance;
  }

  /**
   * Test program.
   * 
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
  }

}
