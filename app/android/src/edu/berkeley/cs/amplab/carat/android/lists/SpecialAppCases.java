package edu.berkeley.cs.amplab.carat.android.lists;

import edu.berkeley.cs.amplab.carat.android.Constants;

/**
 * 
 * @author Javad Sadeqzadeh
 *
 */
public class SpecialAppCases {

	/**
	 * Checking if an app is a special app (Carat or a system app)
	 * @param appName
	 * @return
	 */
	public static boolean isSpecialApp(String appName) {
		// since using a "switch" statement on a string requires JDK 1.7, 
		// and also because only Android 4.4 ("Kitkat") and higher versions support JDK 1.7, 
		// we have to go with the plain "if" statement
		if (appName.equals(Constants.CARAT_PACKAGE_NAME) || appName.equals(Constants.CARAT_OLD))
		    return true;
		// Don't show system apps, not possible to kill.
		// Don't show "Dialer" app in HTC devices.
		else if (appName.equals("com.android.htcdialer"))
			return true;
		// Don't show "Wiper App". 
		else if (appName.equals("com.qualcomm.wiper"))
			return true;
		// otherwise return false
		else 
			return false;
	}
}
