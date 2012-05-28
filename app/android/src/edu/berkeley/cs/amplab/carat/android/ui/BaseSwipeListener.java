package edu.berkeley.cs.amplab.carat.android.ui;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import edu.berkeley.cs.amplab.carat.android.CaratMainActivity;

abstract class BaseSwipeListener implements OnTouchListener {

    float oldX = 0;
    float oldY = 0;
    
    int currentTab = 0;

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            currentTab = CaratMainActivity.tabHost.getCurrentTab();
            oldX = ev.getX();
            oldY = ev.getY();
            // Fix swipe not working on fake bugs/hogs screens:
            if (currentTab == 0 || currentTab == 2 || currentTab == 3)
                return true;
            return false;
        } else if (action == MotionEvent.ACTION_UP) {
            currentTab = CaratMainActivity.tabHost.getCurrentTab();
            return handleUp(v, ev);
        }
        return false;
    }

    public abstract boolean handleUp(View v, MotionEvent ev);
}
