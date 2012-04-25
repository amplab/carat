package edu.berkeley.cs.amplab.carat.android.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

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

					Log.d("CaratProtocol", "Set address=" + SERVER_ADDRESS
							+ " port=" + SERVER_PORT);
				} else
					Log.e("CaratProtocol",
							"Could not open server property file!");
			} catch (IOException e) {
				Log.e("CaratProtocol", "Could not open server property file: "
						+ e.toString());
			}
		}

		// Initialize server port and address
		boolean tryagain = true;
		while (tryagain) {
			try {
				if (soc == null)
					soc = new TSocket(SERVER_ADDRESS, SERVER_PORT);
				else
					soc.close();
				soc.open();
				TProtocol p = new TBinaryProtocol(soc, true, true);
				instance = new CaratService.Client(p);
				tryagain = false;
			} catch (Exception e) {
				Log.e("CaratProtocol", "Could not create instance!");
				e.printStackTrace();
				tryagain = false;
			} catch (Throwable th) {
				// Try again if get an unknown error.
				Log.e("CaratProtocol",
						"Socket error? Creating new soc and protocol.");
				th.printStackTrace();
				soc = new TSocket(SERVER_ADDRESS, SERVER_PORT);
				TProtocol p = new TBinaryProtocol(soc, true, true);
				instance = new CaratService.Client(p);
				tryagain = true;
			}
		}
		return instance;
	}

	public static void close() {
		soc.close();
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
