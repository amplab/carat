package edu.berkeley.cs.amplab.carat.android;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.List;

import edu.berkeley.cs.amplab.carat.android.lists.HogBugSuggestionsAdapter;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity;
import edu.berkeley.cs.amplab.carat.android.ui.FlipperBackListener;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;

public class CaratSuggestionsActivity extends BaseVFActivity {

    View tv = null;
    
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
        vf.addView(tv);

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
                else
                    switchView(R.id.killAppView);
            }
        });
//        ContentResolver aContentResolver=this.getContentResolver();
        initKillView();
        initUpgradeOsView();
        DimScreen(getApplicationContext());
        //SetAutoBrightness(getApplicationContext());
        DisableWifi(getApplicationContext());
        DisableBluetooth(); 
        DisableHapticFb(getApplicationContext());
        
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
    public void DisableVibration(){
        AudioManager audioManager=(AudioManager) getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
        
        if(audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION)){        
        }        
    }
    
  */  
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
        lv.setAdapter(new HogBugSuggestionsAdapter(app, app.s.getHogReport(),
                app.s.getBugReport()));
        emptyCheck(lv);
    }
    
    
    private void emptyCheck(ListView lv) {
        if (lv.getAdapter().isEmpty()) {
            if (vf.getDisplayedChild() == baseViewIndex)
                vf.setDisplayedChild(vf.indexOfChild(tv));
        } else {
            if (vf.getDisplayedChild() == vf.indexOfChild(tv)) {
                vf.setDisplayedChild(baseViewIndex);
            }
        }
    }
    

    public void killApp(String appName) {
        List<ActivityManager.RunningAppProcessInfo> list = SamplingLibrary
                .getRunningProcessInfo(getApplicationContext());
        if (list != null) {
            for (int i = 0; i < list.size(); ++i) {
                ActivityManager.RunningAppProcessInfo pi = list.get(i);
                if (appName.matches(pi.processName)) {
                    android.os.Process.killProcess(pi.pid);
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        if (vf.getDisplayedChild() != baseViewIndex && vf.getDisplayedChild() != vf.indexOfChild(tv)) {
            vf.setOutAnimation(CaratMainActivity.outtoRight);
            vf.setInAnimation(CaratMainActivity.inFromLeft);
            vf.setDisplayedChild(baseViewIndex);
            viewIndex = baseViewIndex;
        } else
            finish();
    }
}
