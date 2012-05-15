package edu.berkeley.cs.amplab.carat.android.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

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
    public static final String TAG = "ProtocolClient";
    public static final String SERVER_PROPERTIES = "caratserver.properties";
    public static int SERVER_PORT = 0;
    public static String SERVER_ADDRESS = null;

    private static CaratService.Client instance = null;
    private static TSocket soc = null;

    /**
     * FIXME: this needs to come from a factory, so that connections are not
     * kept open unnecessarily, and that they do not become stale, and that we
     * handle disconnections gracefully.
     * 
     * @param c
     * @return
     */
    public static CaratService.Client getInstance(Context c) {
        if (SERVER_ADDRESS == null) {
            Properties properties = new Properties();
            try {
                InputStream raw = c.getAssets().open(SERVER_PROPERTIES);
                if (raw != null) {
                    properties.load(raw);
                    if (properties.containsKey("PORT"))
                        SERVER_PORT = Integer.parseInt(properties.getProperty(
                                "PORT", "8080"));
                    if (properties.containsKey("ADDRESS"))
                        SERVER_ADDRESS = properties.getProperty("ADDRESS",
                                "server.caratproject.com");

                    Log.d(TAG, "Set address=" + SERVER_ADDRESS + " port="
                            + SERVER_PORT);
                } else
                    Log.e(TAG, "Could not open server property file!");
            } catch (IOException e) {
                Log.e(TAG,
                        "Could not open server property file: " + e.toString());
            }
        }
        if (SERVER_ADDRESS == null || SERVER_PORT == 0)
            return null;

        if (soc == null) {
            soc = new TSocket(SERVER_ADDRESS, SERVER_PORT);
            TProtocol p = new TBinaryProtocol(soc, true, true);
            instance = new CaratService.Client(p);
        }
        
        return instance;
    }

    /**
     * Unknown error, next time build connection from scratch
     */
    public static void resetConnection() {
        if (soc != null)
            close();
        soc = null;
    }

    public static void close() {
        if (soc != null)
            soc.close();
    }
    
    public static void open() throws TTransportException{
        if (soc != null && !soc.isOpen())
            soc.open();
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
