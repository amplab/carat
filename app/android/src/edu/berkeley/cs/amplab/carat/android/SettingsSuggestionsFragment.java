package edu.berkeley.cs.amplab.carat.android;

import java.io.Serializable;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import edu.berkeley.cs.amplab.carat.android.lists.SettingsSuggestionAdapter;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.android.ui.LocalizedWebView;

public class SettingsSuggestionsFragment extends Fragment implements Serializable{
    
	// TODO: serialVersionUID is used for serialization. see onSaveInstanceState(). 
	// should be the same as what expected at de-serialization time
    private static final long serialVersionUID = -6034269327947014085L; 
    private static final String TAG = "SettingsSuggestions";
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.suggestions, container, false);
        
        final ListView lv = (ListView) rootView.findViewById(android.R.id.list);
        lv.setCacheColorHint(0);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				Object o = lv.getItemAtPosition(position);
				SimpleHogBug fullObject = (SimpleHogBug) o;
				final String actionName = fullObject.getAppName();
				
				Log.v(TAG, "Showing view for " + actionName);
				
				if (actionName.equals("OsUpgrade"))
					CaratApplication.showHTMLFile("upgradeos");
				else if (actionName.equals(getString(R.string.dimscreen)))
					GoToDisplayScreen();
				else if (actionName.equals(getString(R.string.disablewifi)))
					GoToWifiScreen();
				else if (actionName.equals(getString(R.string.disablegps)))
					GoToLocSevScreen();
				else if (actionName.equals(getString(R.string.disablebluetooth)))
					GoToBluetoothScreen();
				else if (actionName.equals(getString(R.string.disablehapticfeedback)))
					GoToSoundScreen();
				else if (actionName.equals(getString(R.string.automaticbrightness)))
					GoToDisplayScreen();
				else if (actionName.equals(getString(R.string.disablenetwork)))
					GoToMobileNetworkScreen();
				else if (actionName.equals(getString(R.string.disablevibration)))
					GoToSoundScreen();
				else if (actionName.equals(getString(R.string.shortenscreentimeout)))
					GoToDisplayScreen();
				else if (actionName.equals(getString(R.string.disableautomaticsync)))
					GoToSyncScreen();
				else if (actionName.equals(getString(R.string.helpcarat)))
					CaratApplication.showHTMLFile("collectdata");
				else if (actionName.equals(getString(R.string.questionnaire)))
					openQuestionnaire();
			}

		});

        initUpgradeOsView(rootView);

        return rootView;
    }
    
    private void initUpgradeOsView(View root) {
        LocalizedWebView webview = (LocalizedWebView) root.findViewById(R.id.upgradeOsView);
        webview.loadUrl("file:///android_asset/upgradeos.html");
        //webview.setOnTouchListener(new FlipperBackListener(this, vf, vf.indexOfChild(findViewById(android.R.id.list))));
    }

    /* Show the bluetooth setting */
    public void GoToBluetoothScreen() {
        safeStart(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS, getString(R.string.bluetoothsettings));
    }

    /* Show the wifi setting */
    public void GoToWifiScreen() {
        safeStart(android.provider.Settings.ACTION_WIFI_SETTINGS, getString(R.string.wifisettings));
    }

    /*
     * Show the display setting including screen brightness setting, sleep mode
     */
    public void GoToDisplayScreen() {
        safeStart(android.provider.Settings.ACTION_DISPLAY_SETTINGS, getString(R.string.screensettings));
    }

    /*
     * Show the sound setting including phone ringer mode, vibration mode, haptic feedback setting and other sound options
     */
    public void GoToSoundScreen() {
        safeStart(android.provider.Settings.ACTION_SOUND_SETTINGS, getString(R.string.soundsettings));
    }

    /*
     * Show the location service setting including configuring gps provider, network provider
     */
    public void GoToLocSevScreen() {
        safeStart(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS, getString(R.string.locationsettings));
    }

    /* Show the synchronization setting */
    public void GoToSyncScreen() {
        safeStart(android.provider.Settings.ACTION_SYNC_SETTINGS, getString(R.string.syncsettings));
    }

    /*
     * Show the mobile network setting including configuring 3G/2G, network operators
     */
    public void GoToMobileNetworkScreen() {
        safeStart(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS, getString(R.string.mobilenetworksettings));
    }

    /* Show the application setting */
    public void GoToAppScreen() {
        safeStart(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS, getString(R.string.appsettings));
    }

    private void safeStart(String intentString, String thing) {
        Intent intent = null;
        try {
            intent = new Intent(intentString);
            startActivity(intent);
        } catch (Throwable th) {
            Log.e(TAG, "Could not start activity: " + intent, th);
            if (thing != null) {
                Toast t = Toast.makeText(getActivity(), getString(R.string.opening) + thing + getString(R.string.notsupported),
                        Toast.LENGTH_SHORT);
                t.show();
            }
        }
    }

    /**
     * Open a Carat-related questionnaire.
     */
    public void openQuestionnaire() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String caratId = Uri.encode(p.getString(CaratApplication.REGISTERED_UUID, ""));
        String os = Uri.encode(SamplingLibrary.getOsVersion());
        String model = Uri.encode(SamplingLibrary.getModel());
        String url = CaratApplication.storage.getQuestionnaireUrl();
        if (url != null && url.length() > 7 && url.startsWith("http")) { // http://
            url = url.replace("caratid", caratId).replace("caratos", os).replace("caratmodel", model);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
    	// TODO: The following method call (setActionList) and the original method should be removed.
    	// we no longer manipulate fragments directly.
        //  CaratApplication.setActionList(this); 
        refresh();
        super.onResume();
    }

    public void refresh() {
        CaratApplication caratAppllication = (CaratApplication) CaratApplication.getMainActivity().getApplication();
        final ListView lv = (ListView) rootView.findViewById(android.R.id.list);
        lv.setAdapter(new SettingsSuggestionAdapter(caratAppllication, CaratApplication.storage.getSettingsReport()));
        // emptyCheck(lv);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    	// FIXME: disabled until fixing serialization (appropriate serialVersionUID)
        //  outState.putSerializable("savedInstance", this);
        super.onSaveInstanceState(outState);
    }

    /*
     * Needs to happen in Activity
     */
    /*
    @Override
    public void onBackPressed() {
        if (vf.getDisplayedChild() != baseViewIndex && vf.getDisplayedChild() != emptyIndex) {
            SamplingLibrary.resetRunningProcessInfo();
            refresh();
            vf.setOutAnimation(MainActivity.outtoRight);
            vf.setInAnimation(MainActivity.inFromLeft);
            vf.setDisplayedChild(baseViewIndex);
            viewIndex = baseViewIndex;
        } else
            finish();
    }*/
}
