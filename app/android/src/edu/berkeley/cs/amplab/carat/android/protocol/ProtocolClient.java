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

    /**
     * FIXME: this needs to come from a factory, so that connections are not
     * kept open unnecessarily, and that they do not become stale, and that we
     * handle disconnections gracefully.
     * 
     * @param c
     * @return
     * @throws NumberFormatException 
     * @throws TTransportException 
     */
    public static CaratService.Client getInstance(Context c) throws NumberFormatException, TTransportException {
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

        TSocket soc = new TSocket(SERVER_ADDRESS, SERVER_PORT, 60000);
        TProtocol p = new TBinaryProtocol(soc, true, true);
        CaratService.Client instance = new CaratService.Client(p);

        if (soc != null && !soc.isOpen())
            soc.open();

        return instance;
    }
    
    public static CaratService.Client open(Context c) throws NumberFormatException, TTransportException {
    	Log.d("ProtocolClient", "trying to get an instance of CaratProtocol.");
        return getInstance(c);
    }
    
}
