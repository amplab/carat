package edu.berkeley.cs.amplab.carat.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;
import edu.berkeley.cs.amplab.carat.android.lists.HogBugSuggestionsAdapter;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity;
import edu.berkeley.cs.amplab.carat.android.ui.FlipperBackListener;
import edu.berkeley.cs.amplab.carat.android.ui.LocalizedWebView;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;

public class CaratSuggestionsActivity extends BaseVFActivity {

    private static final String TAG = "CaratSuggestions";
    private View tv = null;
    private View killView = null;
    private View collectDataView = null;
    private int emptyIndex = -1;
    private LocalizedWebView webview = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestions);
        final Context c = getApplicationContext();
        LayoutInflater mInflater = LayoutInflater.from(c);
        vf = (ViewFlipper) findViewById(R.id.suggestionsFlipper);
        View baseView = findViewById(android.R.id.list);
        baseView.setOnTouchListener(SwipeListener.instance);
        vf.setOnTouchListener(SwipeListener.instance);
        baseViewIndex = vf.indexOfChild(baseView);

        tv = mInflater.inflate(R.layout.emptyactions, null);
        if (tv != null) {
            vf.addView(tv);
            emptyIndex = vf.indexOfChild(tv);
        }

        final ListView lv = (ListView) findViewById(android.R.id.list);
        lv.setCacheColorHint(0);

        //initKillView();
        //initCollectDataView();

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                    long id) {
                Object o = lv.getItemAtPosition(position);
                SimpleHogBug fullObject = (SimpleHogBug) o;
                final String raw = fullObject.getAppName();
                Log.v(TAG, "Showing kill view for " + raw);
                if (raw.equals("OsUpgrade"))
                    switchView(R.id.upgradeOsView);
                else if (raw.equals(getString(R.string.dimscreen)))
                    GoToDisplayScreen();
                else if (raw.equals(getString(R.string.disablewifi)))
                    GoToWifiScreen();
                else if (raw.equals(getString(R.string.disablegps)))
                    GoToLocSevScreen();
                else if (raw.equals(getString(R.string.disablebluetooth)))
                    GoToBluetoothScreen();
                else if (raw.equals(getString(R.string.disablehapticfeedback)))
                    GoToSoundScreen();
                else if (raw.equals(getString(R.string.automaticbrightness)))
                    GoToDisplayScreen();
                else if (raw.equals(getString(R.string.disablenetwork)))
                    GoToMobileNetworkScreen();
                else if (raw.equals(getString(R.string.disablevibration)))
                    GoToSoundScreen();
                else if (raw.equals(getString(R.string.shortenscreentimeout)))
                    GoToDisplayScreen();
                else if (raw.equals(getString(R.string.disableautomaticsync)))
                    GoToSyncScreen();
                else if (raw.equals(getString(R.string.helpcarat))){
                    initCollectDataView();
                    switchView(collectDataView);
                }else if (raw.equals(getString(R.string.questionnaire))){
                    openQuestionnaire();
                } else {
                    initKillView();
                    ImageView icon = (ImageView) killView
                            .findViewById(R.id.suggestion_app_icon);
                    TextView txtName = (TextView) killView
                            .findViewById(R.id.actionName);
                    TextView txtType = (TextView) killView
                            .findViewById(R.id.suggestion_type);
                    TextView txtBenefit = (TextView) killView
                            .findViewById(R.id.expectedBenefit);
                    final Button killButton = (Button) killView
                            .findViewById(R.id.killButton);

                    final String label = CaratApplication.labelForApp(c, raw);

                    icon.setImageDrawable(CaratApplication.iconForApp(c, raw));

                    Type type = fullObject.getType();
                    if (type == Type.BUG || type == Type.HOG) {
                        txtName.setText(label);
                        PackageInfo pak = SamplingLibrary
                                .getPackageInfo(c, raw);
                        String ver = "";
                        if (pak != null)
                            ver = pak.versionName;
                        if (ver == null)
                            ver = "";
                        final String s = label + " " + ver;
                        killButton.setText(getString(R.string.kill) + " " + s);
                        killButton.setEnabled(true);
                        killButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                killButton.setEnabled(false);
                                killButton.setText(s + " "
                                        + getString(R.string.killed));
                                SamplingLibrary.killApp(c, raw, label);
                                // onBackPressed();
                            }
                        });
                    } else { // Other action
                        txtName.setText(label);
                        killButton.setText(label);
                    }
                    if (fullObject.getType() == Type.OTHER){
                        // Already translated
                        txtType.setText(fullObject.getAppPriority());
                    }else
                    txtType.setText(CaratApplication
                            .translatedPriority(fullObject.getAppPriority()));

                    /*
                     * if (raw.equals("Disable bluetooth")) { double
                     * benefitOther = PowerProfileHelper. bluetoothBenefit(c);
                     * hours = (int) (benefitOther); min = (int) (benefitOther *
                     * 60); min -= hours * 60; } else if
                     * (raw.equals("Disable Wifi")) { double benefitOther =
                     * PowerProfileHelper.wifiBenefit(c); hours = (int)
                     * (benefitOther); min = (int) (benefitOther * 60); min -=
                     * hours * 60; } else if (raw.equals("Dim the Screen")) {
                     * double benefitOther = PowerProfileHelper.
                     * screenBrightnessBenefit(c); hours = (int) (benefitOther);
                     * min = (int) (benefitOther * 60); min -= hours * 60; }
                     */

                    txtBenefit.setText(fullObject.textBenefit());

                    switchView(killView);
                }
            }
        });

        initUpgradeOsView();

        Object o = getLastNonConfigurationInstance();
        if (o != null) {
            CaratSuggestionsActivity previous = (CaratSuggestionsActivity) o;
            viewIndex = previous.viewIndex;
            if (previous.killView != null
                    && previous.killView == previous.vf.getChildAt(viewIndex)) {
                restoreKillView(previous.killView);
            }
        }

        if (viewIndex == 0)
            vf.setDisplayedChild(baseViewIndex);
        else
            vf.setDisplayedChild(viewIndex);
    }
    
    private void restoreKillView(View previous) {
        initKillView();
        ImageView icon = (ImageView) killView.findViewById(R.id.suggestion_app_icon);

        icon.setImageDrawable(((ImageView) previous
                .findViewById(R.id.suggestion_app_icon)).getDrawable());
        TextView txtName = (TextView) killView.findViewById(R.id.actionName);
        txtName.setText(((TextView) previous.findViewById(R.id.actionName))
                .getText());
        TextView txtType = (TextView) killView
                .findViewById(R.id.suggestion_type);
        txtType.setText(((TextView) previous.findViewById(R.id.suggestion_type))
                .getText());
        TextView txtBenefit = (TextView) killView
                .findViewById(R.id.expectedBenefit);
        txtBenefit.setText(((TextView) previous
                .findViewById(R.id.expectedBenefit)).getText());
        Button killButton = (Button) killView.findViewById(R.id.killButton);
        killButton.setText(((Button) previous.findViewById(R.id.killButton))
                .getText());
    }

    private void initKillView() {
        if (killView == null){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View killPage = inflater.inflate(R.layout.killlayout, null);
        LocalizedWebView webview = (LocalizedWebView) killPage.findViewById(R.id.killView);
        String osVer = SamplingLibrary.getOsVersion();
        // FIXME: KLUDGE. Should be smarter with the version number.
        if (osVer.startsWith("2."))
            webview.loadUrl("file:///android_asset/killapp-2.2.html");
        else
            webview.loadUrl("file:///android_asset/killapp.html");
        // FIXME: This does not work with the embedded WebView (randomly goes back); no idea why
        killPage.setOnTouchListener(new FlipperBackListener(this, vf, baseViewIndex, true));
        webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
                .indexOfChild(findViewById(android.R.id.list)), false));
        Button AppManagerButton = (Button) killPage.findViewById(R.id.appManager);
        AppManagerButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0) {
                GoToAppScreen();
            } 
        });
        killView = killPage;
        vf.addView(killView);
        }
    }
    
    private void initCollectDataView() {
        if (webview == null){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View collectPage = inflater.inflate(R.layout.collectdata, null);
        webview = (LocalizedWebView) collectPage.findViewById(R.id.collectDataView);
        webview.loadUrl("file:///android_asset/collectdata.html");
        webview.setOnTouchListener(new FlipperBackListener(this, vf, baseViewIndex));
        vf.addView(webview);
        }
    }

    private void initUpgradeOsView() {
        LocalizedWebView webview = (LocalizedWebView) findViewById(R.id.upgradeOsView);
        webview.loadUrl("file:///android_asset/upgradeos.html");
        webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
                .indexOfChild(findViewById(android.R.id.list))));
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
     * Show the sound setting including phone ringer mode, vibration mode,
     * haptic feedback setting and other sound options
     */
    public void GoToSoundScreen() {
        safeStart(android.provider.Settings.ACTION_SOUND_SETTINGS, getString(R.string.soundsettings));
    }

    /*
     * Show the location service setting including configuring gps provider,
     * network provider
     */
    public void GoToLocSevScreen() {
        safeStart(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS, getString(R.string.locationsettings));
    }

    /* Show the synchronization setting */
    public void GoToSyncScreen() {
        safeStart(android.provider.Settings.ACTION_SYNC_SETTINGS, getString(R.string.syncsettings));
    }

    /*
     * Show the mobile network setting including configuring 3G/2G, network
     * operators
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
                Toast t = Toast.makeText(getApplicationContext(), getString(R.string.opening)
                        + thing + getString(R.string.notsupported),
                        Toast.LENGTH_SHORT);
                t.show();
            }
        }
    }
    
    /**
     * Open a Carat-related questionnaire.
     */
    public void openQuestionnaire(){
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String caratId = Uri.encode(p.getString(CaratApplication.REGISTERED_UUID, ""));
        String os = Uri.encode(SamplingLibrary.getOsVersion());
        String model = Uri.encode(SamplingLibrary.getModel());
        String url = CaratApplication.s.getQuestionnaireUrl();
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
    protected void onResume() {
        CaratApplication.setActionList(this);
        refresh();
        super.onResume();
    }

    public void refresh() {
        CaratApplication app = (CaratApplication) getApplication();
        final ListView lv = (ListView) findViewById(android.R.id.list);
        lv.setAdapter(new HogBugSuggestionsAdapter(app, CaratApplication.s
                .getHogReport(), CaratApplication.s.getBugReport()));
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
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        if (vf.getDisplayedChild() != baseViewIndex
                && vf.getDisplayedChild() != emptyIndex) {
            SamplingLibrary.resetRunningProcessInfo();
            refresh();
            vf.setOutAnimation(CaratMainActivity.outtoRight);
            vf.setInAnimation(CaratMainActivity.inFromLeft);
            vf.setDisplayedChild(baseViewIndex);
            viewIndex = baseViewIndex;
        } else
            finish();
    }
}
