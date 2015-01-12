package edu.berkeley.cs.amplab.carat.android.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
//important: the following import command imports the class from a library project, not from android.preference.PreferenceFragment
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.Constants;
import edu.berkeley.cs.amplab.carat.android.MainActivity;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.utils.Tracker;


public class CaratSettingsFragment extends PreferenceFragment {
	
	Tracker tracker = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Using a PreferenceFragment and its addPreferencesFromResource() method is a convenient way to create a 
		 * preference/settings fragment. 
		 * The addPreferencesFromResource() method of the PreferenceFragment class does all of the hard work for us: 
		 * it reads the values from our defaultSharedPreferences, 
		 * inflates a ListView as our fragment's root view, 
		 * then inside the getView() method of an ArrayAdapter it creates and attaches to our ListView,
		 * it inflates each individual Preference widget we have in our specified xml file (whose resource ID we pass as an argument to this method),
		 * sets the values of those inflated view objects to the values just read from our defaultSharedPreferences,
		 * adds those inflated view objects to our rootView (ListView view object) using the mentioned adapter, 
		 * and sets the resulting view as our fragment's view.
		 * 
		 * In addition, whenever the value of a preference widget changes in our PreferenceFragment's inflated view, 
		 * the PreferenceFragment updates the value of the corresponding (tied) item in our defaultSharedPreferences
		 * (if the item doesn't exist in there, PreferenceFragment creates an item with this key and sets its value to the just read value from the view/widget)
		 * 
		 * sets them to the last value of each corresponding preference (in the
		 * XML file). basically it loads the preferences from an XML resource
		 */
		
		addPreferencesFromResource(R.xml.preferences);

		/*
		 * Both of the "Switch" Preference widget (view) and the "PreferenceFragment" classes can only be used in Android 3.0+.
		 * We use two external libraries to backport these handy classes (provide them for Android version < 3.0):
		 * 1. The "switch widget backport" library, check out the readme: https://github.com/BoD/android-switch-backport
		 * 2. The "android support preferencefragment" (check out the readme in the library's directory)
		 */
		
		// we use the tracker in the following two methods, so instantiate it here
		tracker = Tracker.getInstance();
		
		setSharePreferenceIntent();
		setFeedbackPreferenceIntent();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        getActivity().setTitle(getResources().getString(R.string.tab_carat_settings));
        
        return super.onCreateView(inflater, container, savedInstanceState);
    }

	/**
	 * Set an intent for our "share" preference widget
	 */
	private void setSharePreferenceIntent() {
		// grab a reference to our Preference widget object (Preference is a subclass of View). 
		// (we have several preference widget objects in our res/xml/preferences.xml), 
		// the name of the preference object we are trying to grab here is specified in CaratApplication.SHARE_PREFERENCE_KEY. 
		// each preference widget in our xml file (preferences.xml) corresponds to an item/entry in our preference fragment's view		
		Preference preference = findPreference(Constants.SHARE_PREFERENCE_KEY);
		
		// create an intent that we want to perform whenever this item is clicked (in this case, a send intent)
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		int jscore = CaratApplication.getJscore();
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.myjscoreis) + " " + jscore);
		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharetext1) + " " + jscore
				+ getString(R.string.sharetext2));
		
		// set the created intent as our preference (view) object's intent
		preference.setIntent(intent);
		
		tracker.trackSharing();
	}

	
	/**
	 * Set an intent for our "share" preference widget
	 */
	private void setFeedbackPreferenceIntent() {
		Preference preference = findPreference(Constants.FEEDBACK_PREFERENCE_KEY);
		
		Intent intent = new Intent(Intent.ACTION_SEND);
		
		MainActivity mainActivity = CaratApplication.getMainActivity();
		Context context = mainActivity.getApplicationContext();
		SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		String os = SamplingLibrary.getOsVersion();
		String model = SamplingLibrary.getModel();
		String uuId = defaultSharedPreferences.getString(CaratApplication.getRegisteredUuid(), "UNKNOWN");
		int jscore = CaratApplication.getJscore();
		
		// Emulator does not support message/rfc822
		if (model.equals("sdk"))
			intent.setType("text/plain");
		else
			intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "carat@eecs.berkeley.edu" });
		intent.putExtra(Intent.EXTRA_SUBJECT, "[carat] [Android] " + getString(R.string.feedbackfrom) + " "
				+ model);
		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.os) + ": " + os + "\n"
				+ getString(R.string.model) + ": " + model + "\nCarat ID: " + uuId + "\nJ-Score: " + jscore + "\n"
				+ mainActivity.getFulVersion() + "\n");
		
		preference.setIntent(intent);
		
		tracker.trackFeedback(os, model);
	}
	
	/**
	 * If you would like to do something FURTHER than changing "wifiOnly" preference value, do it in this listener.
	 * 
	 * Note 1: preferences value DO change automatically, no need for manual action.
	 * (when we use a preference widget in an xml preference file (res/xml/*.xml))
	 * 
	 * Note 2: if you fill in this listener, do not forget to set it as our preference widget's listener, like this:
	 * findPreference(CaratApplication.WIFI_ONLY_PREFERENCE_KEY).setOnPreferenceChangeListener(mOnPreferenceChangeListener);
	 * (using Preference.setOnPreferenceChangeListener(listener))
	 * (attach the listener in onCreate() method of the current fragment (which is a subclass of a specialized fragment, i.e. PreferenceFragment))
	 */
	/* private OnPreferenceChangeListener mOnPreferenceChangeListener = new OnPreferenceChangeListener() {
          @Override
          public boolean onPreferenceChange(Preference preference, Object o) {
  	        Log.d("settings-fragment", "New value is: " + o.toString());
  	        // return true to update the state of the Preference with the new value.
  	        return true;
  	    }
      }; */

	/**
     * This is a onCLICK listener method (rather than onCHANGE listener)
     * usually not needed, but leave it here just in case.
     * if you want to start an activity or open a custom dialog, this is not the correct way to go
     * See Preference.SetIntent() for example for starting an activity/intent
     * Or if your "extra" (the data you want to pass together with your intent) is merely some constants, 
     * see here for embedding your intent into your preference's xml element:
     * http://developer.android.com/guide/topics/ui/settings.html#Intents
     * If you want to display a custom dialog, see "Building a Custom Preference" in the above article.
     * There already are some ready-made dialogs (ListPreference, EditTextPreference, CheckBoxPreference), 
     * see the Preference class for a list of all other subclasses and their corresponding properties. 
     */
	/* private OnPreferenceClickListener mOnPreferenceClickListener = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
	        Log.d("settings-fragment", preference.getKey() + " was click");
	        // return true to update the state of the Preference with the new value.
	        return true;
	    }
    }; */

}