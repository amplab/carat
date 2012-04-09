package edu.berkeley.cs.amplab.carat.android;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class CaratSuggestionsActivity extends TabActivity {
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
        intent = new Intent().setClass(this, CaratHomeScreenActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("home").setIndicator(getString(R.string.tab_my_device),
                          res.getDrawable(R.drawable.ic_tab_home))
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, CaratHogsActivity.class);
        spec = tabHost.newTabSpec("hogs").setIndicator("Hogs",
                          res.getDrawable(R.drawable.ic_tab_hog))
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