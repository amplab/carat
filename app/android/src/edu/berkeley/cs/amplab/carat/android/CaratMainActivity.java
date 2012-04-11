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
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources(); // Resource object to get Drawables
        final TabHost tabHost = getTabHost();  // The activity TabHost
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
        
        final OnTouchListener touchListener = new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				int action = arg1.getActionMasked();
						if (action == MotionEvent.ACTION_MOVE){
							int hs = arg1.getHistorySize();
							float oldX  = arg1.getHistoricalX(hs);
							float oldY  = arg1.getHistoricalY(hs);
							float x  = arg1.getX();
							float y  = arg1.getY();
							
							boolean left = false;
							float xDiff = x - oldX;
							if (x < oldX){
								xDiff = oldX - x;
								left = true;
							}
							
							float yDiff = y - oldY;
							if (y < oldY)
								yDiff = oldY - y;
							
							if (xDiff > yDiff){
								// moved horizontally. Lets change tabs to the right direction:
								int c = tabHost.getCurrentTab();
								if (left && c > 0)
									tabHost.setCurrentTab(c-1);
								else if (!left && c+1 < tabHost.getChildCount())
									tabHost.setCurrentTab(c+1);
							}
						}
				return false;
			}
        	
        };
        
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
						old.setAnimation(outToLeftAnimation());
						newView.setAnimation(inFromRightAnimation());
					} else {
						newView.setAnimation(inFromLeftAnimation());
						old.setAnimation(outToRightAnimation());
					}
				}
				oldTab = newTab;
			}
        });

        tabHost.setCurrentTab(0);
    }
    
    // 100 ms
    public static final long ANIMATION_DURATION = 250;
    
    public Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    public Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(ANIMATION_DURATION);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }
    
    public Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromLeft.setDuration(ANIMATION_DURATION);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
        return inFromLeft;
    }

    public Animation outToRightAnimation() {
        Animation outtoRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoRight.setDuration(ANIMATION_DURATION);
        outtoRight.setInterpolator(new AccelerateInterpolator());
        return outtoRight;
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