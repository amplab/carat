package edu.berkeley.cs.amplab.carat.android;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class CaratMainActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)

        // Initialize a TabSpec for each tab and add it to the TabHost
        intent = new Intent().setClass(this, CaratSuggestionsActivity.class);
        spec = tabHost.newTabSpec("actions").setIndicator(getString(R.string.tab_actions),
                res.getDrawable(R.drawable.ic_tab_actions))
            .setContent(intent);
tabHost.addTab(spec);

		intent = new Intent().setClass(this, CaratMyDeviceActivity.class);
        spec = tabHost.newTabSpec("mydevice").setIndicator(getString(R.string.tab_my_device),
                          res.getDrawable(R.drawable.ic_tab_mydevice))
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, CaratBugsActivity.class);
        spec = tabHost.newTabSpec("bugs").setIndicator(getString(R.string.tab_bugs),
                          res.getDrawable(R.drawable.ic_tab_bugs))
                      .setContent(intent);
        tabHost.addTab(spec);
        

        intent = new Intent().setClass(this, CaratHogsActivity.class);
        spec = tabHost.newTabSpec("hogs").setIndicator("Hogs",
                          res.getDrawable(R.drawable.ic_tab_hogs))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, CaratAboutActivity.class);
        spec = tabHost.newTabSpec("about").setIndicator("About",
                          res.getDrawable(R.drawable.ic_tab_about))
                      .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
}