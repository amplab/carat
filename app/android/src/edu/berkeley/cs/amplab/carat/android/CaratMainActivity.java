package edu.berkeley.cs.amplab.carat.android;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class CaratMainActivity extends TabActivity {
	
	static TabHost tabHost = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources(); // Resource object to get Drawables
        tabHost = getTabHost();  // The activity TabHost
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
        
        // Bind animations to tab changes:
        tabHost.setOnTabChangedListener(new OnTabChangeListener(){
        	int oldTab = tabHost.getCurrentTab();
        	
			@Override
			public void onTabChanged(String tabId) {
				int newTab = tabHost.getCurrentTab();
				View old = tabHost.getTabContentView().getChildAt(oldTab);
				View newView = tabHost.getTabContentView().getChildAt(newTab);
				Log.i("onTabChanged", "oldTab="+oldTab+" old="+old+" newTabId="+tabId+" newTab="+newTab+" newView="+newView);
				
				if (old != null && newView != null) {
					if (oldTab < newTab) {
						old.startAnimation(outtoLeft);
						newView.startAnimation(inFromRight);
					} else {
						newView.startAnimation(inFromLeft);
						old.startAnimation(outtoRight);
					}
				}
				oldTab = newTab;
			}
        });

        tabHost.setCurrentTab(0);
    }
    
    public static void changeTab(int tab){
    	tabHost.setCurrentTab(tab);
    }
    
    // 250 ms
    public static final long ANIMATION_DURATION = 250;
    
    
    /**
     * Animation for sliding a screen in from the right.
     * @return
     */
    public static Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * Animation for sliding a screen out to the left.
     * @return
     */
    public static Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        outtoLeft.setDuration(ANIMATION_DURATION);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
    }
    
    /**
     * Animation for sliding a screen in from the left.
     * @return
     */
    public static Animation inFromLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        inFromLeft.setDuration(ANIMATION_DURATION);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * Animation for sliding a screen out to the right.
     * @return
     */
    
    public static Animation outtoRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
    {
    	outtoRight.setDuration(ANIMATION_DURATION);
        outtoRight.setInterpolator(new AccelerateInterpolator());
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