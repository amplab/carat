package edu.berkeley.cs.amplab.carat.android;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ViewFlipper;

import edu.berkeley.cs.amplab.carat.android.lists.HogBugSuggestionsAdapter;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity;
import edu.berkeley.cs.amplab.carat.android.ui.FlipperBackListener;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;

public class CaratSuggestionsActivity extends BaseVFActivity {

    private View tv = null;
    private int emptyIndex = -1;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestions);
        LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
        vf = (ViewFlipper) findViewById(R.id.suggestionsFlipper);
        View baseView = findViewById(android.R.id.list);
        baseView.setOnTouchListener(SwipeListener.instance);
        vf.setOnTouchListener(SwipeListener.instance);
        baseViewIndex = vf.indexOfChild(baseView);
        
        tv = mInflater.inflate(R.layout.emptyactions, null);
        if (tv != null){
            vf.addView(tv);
            emptyIndex = vf.indexOfChild(tv);
        }

        final ListView lv = (ListView) findViewById(android.R.id.list);
        lv.setCacheColorHint(0);

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                    long id) {
                Object o = lv.getItemAtPosition(position);
                SimpleHogBug fullObject = (SimpleHogBug) o;
                if (fullObject.getAppName().equals("OsUpgrade"))
                    switchView(R.id.upgradeOsView);
                else if (fullObject.getAppName().equals("Dim the Screen"))
                    GoToDisplayScreen();
                else if (fullObject.getAppName().equals("Disable Wifi"))
                    GoToWifiScreen();
                else if (fullObject.getAppName().equals("Disable gps"))
                    GoToLocSevScreen();
                else if (fullObject.getAppName().equals("Disable bluetooth"))
                    GoToBluetoothScreen();
                else if (fullObject.getAppName().equals("Disable haptic feedback"))
                    GoToSoundScreen();
                else if (fullObject.getAppName().equals("Set brightness to automatic"))
                    GoToDisplayScreen();
                else if (fullObject.getAppName().equals("Disable network"))
                    GoToMobileNetworkScreen();
                else if (fullObject.getAppName().equals("Disable vibration"))
                    GoToSoundScreen();
                else if (fullObject.getAppName().equals("Shorten screen timeout"))
                    GoToDisplayScreen();
                else if (fullObject.getAppName().equals("Disable automatic sync"))
                    GoToSyncScreen();
                else
                    switchView(R.id.killAppView);
                }
        });
        
        initKillView();
        initUpgradeOsView();

        if (viewIndex == 0)
            vf.setDisplayedChild(baseViewIndex);
        else
            vf.setDisplayedChild(viewIndex);
    }

    private void initKillView() {
        WebView webview = (WebView) findViewById(R.id.killAppView);
        // Fixes the white flash when showing the page for the first time.
        if (getString(R.string.blackBackground).equals("true"))
            webview.setBackgroundColor(0);
        String osVer = SamplingLibrary.getOsVersion();
        // FIXME: KLUDGE. Should be smarter with the version number.
        if (osVer.startsWith("2."))
            webview.loadUrl("file:///android_asset/killapp-2.2.html");
        else
            webview.loadUrl("file:///android_asset/killapp.html");
        webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
                .indexOfChild(findViewById(android.R.id.list))));
    }
    private void initUpgradeOsView() {
        WebView webview = (WebView) findViewById(R.id.upgradeOsView);
        // Fixes the white flash when showing the page for the first time.
        if (getString(R.string.blackBackground).equals("true"))
            webview.setBackgroundColor(0);
        webview.loadUrl("file:///android_asset/upgradeos.html");
        webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
                .indexOfChild(findViewById(android.R.id.list))));
    }
    
    /*Dim the screen if current screen brightness value is larger than 30*/
    public void DimScreen(Context context){
        boolean isAutoBrightness= SamplingLibrary.isAutoBrightness(context);
        if (isAutoBrightness==true) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
        int brightnessValue=30;
        int curBrightValue=SamplingLibrary.getScreenBrightness(context);
        if(curBrightValue>brightnessValue)
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 30);
    }

    /*Set automatic brightness mode*/
    public void SetAutoBrightness(Context context){
        boolean isAutoBrightness= SamplingLibrary.isAutoBrightness(context);
        if (isAutoBrightness==false) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);   
        }
        else{
            Log.v("Screen","Automatic Brightness already on");
            
        }
    }
    
    /*Disable Wifi if Wifi is on*/
    public void DisableWifi(Context context){
        boolean wifiEnabled=SamplingLibrary.getWifiEnabled(context);
        if(wifiEnabled==true){
            WifiManager myWifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            myWifiManager.setWifiEnabled(false);            
        }
    }
    
    /*Show the bluetooth setting*/
    public void GoToBluetoothScreen(){
        Intent startIntent= new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(startIntent);  
    }
    /*Show the wifi setting*/
    public void GoToWifiScreen(){
        Intent startIntent= new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
        startActivity(startIntent);        
    }
    /*Show the display setting
     * including screen brightness setting, sleep mode*/
    public void GoToDisplayScreen(){
        Intent startIntent= new Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS);
        startActivity(startIntent);
    }
    /*Show the sound setting
     * including phone ringer mode, vibration mode, haptic feedback setting and other sound options*/
    public void GoToSoundScreen(){
        Intent startIntent= new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);
        startActivity(startIntent);
    }
    /*Show the location service setting
     * including configuring gps provider, network provider*/
    public void GoToLocSevScreen(){
        Intent startIntent= new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(startIntent);
    }
    /*Show the synchronization setting*/
    public void GoToSyncScreen(){
        Intent startIntent= new Intent(android.provider.Settings.ACTION_SYNC_SETTINGS);
        startActivity(startIntent);
    }
    /*Show the mobile network setting
     * including configuring 3G/2G, network operators*/
    public void GoToMobileNetworkScreen(){
        Intent startIntent= new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
        startActivity(startIntent);
    }
    /*Show the application setting*/
    public void GoToAppScreen(){
        Intent startIntent= new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
        startActivity(startIntent);
    }
    
    /*Disable bluetooth if bluetooth is on*/
    public void DisableBluetooth(){
        BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
        if (myBluetoothAdapter.isEnabled()==true) {
            myBluetoothAdapter.disable(); 
        }                 
    }
    
    /*Disable haptic feedback if it is on*/
    public void DisableHapticFb(Context context){
        try {
            if(Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED)== 1)
            {
            Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
            }
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
     }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        CaratApplication.setActionList(this);
        refresh();
        super.onResume();
    }

    public void refresh() {
        CaratApplication app = (CaratApplication) getApplication();
        final ListView lv = (ListView) findViewById(android.R.id.list);
        lv.setAdapter(new HogBugSuggestionsAdapter(app, CaratApplication.s.getHogReport(),
                CaratApplication.s.getBugReport()));
        emptyCheck(lv);
    }
    
    
    private void emptyCheck(ListView lv) {
        if (lv.getAdapter().isEmpty()) {
            if (vf.getDisplayedChild() == baseViewIndex)
                vf.setDisplayedChild(emptyIndex);
        } else {
            if (vf.getDisplayedChild() == emptyIndex) {
                vf.setDisplayedChild(baseViewIndex);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        if (vf.getDisplayedChild() != baseViewIndex && vf.getDisplayedChild() != emptyIndex) {
            vf.setOutAnimation(CaratMainActivity.outtoRight);
            vf.setInAnimation(CaratMainActivity.inFromLeft);
            vf.setDisplayedChild(baseViewIndex);
            viewIndex = baseViewIndex;
        } else
            finish();
    }
}
