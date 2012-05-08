package edu.berkeley.cs.amplab.carat.ui;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ViewFlipper;
import edu.berkeley.cs.amplab.carat.CaratMainActivity;

public class FlipperBackListener extends BaseSwipeListener {

    ViewFlipper vf = null;
    // To be able to save the state of which view we are in
    VFActivity vfa = null;
    int backViewIndex = 0;
    boolean trueHack = false;

    public FlipperBackListener(VFActivity vfa, ViewFlipper vf, int backViewIndex) {
        this.vf = vf;
        this.vfa = vfa;
        this.backViewIndex = backViewIndex;
    }
    
    public FlipperBackListener(VFActivity vfa, ViewFlipper vf, int backViewIndex, boolean trueHack) {
        this.vf = vf;
        this.vfa = vfa;
        this.backViewIndex = backViewIndex;
        this.trueHack = trueHack;
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            currentTab = CaratMainActivity.tabHost.getCurrentTab();
            oldX = ev.getX();
            oldY = ev.getY();
            // Fix swipe not working on fake bugs/hogs screens:
            if (currentTab == 2 || currentTab == 3 || trueHack)
                return true;
            return false;
        } else if (action == MotionEvent.ACTION_UP) {
            currentTab = CaratMainActivity.tabHost.getCurrentTab();
            return handleUp(v, ev);
        }
        return false;
    }
    

    @Override
    public boolean handleUp(View v, MotionEvent ev) {
        int w = v.getWidth();
        float x = ev.getX();
        float y = ev.getY();

        boolean left = true;
        float xDiff = x - oldX;
        if (x < oldX) {
            xDiff = oldX - x;
            left = false;
        }
        
        float yDiff = y - oldY;
        if (y < oldY) {
            yDiff = oldY - y;
        }

        if (xDiff > w / 3.0 && xDiff > yDiff) {
            // moved horizontally. Lets change tabs to the right direction:
            if (left) {
                vf.setOutAnimation(CaratMainActivity.outtoRight);
                vf.setInAnimation(CaratMainActivity.inFromLeft);
                vf.setDisplayedChild(backViewIndex);
                vfa.setViewId(backViewIndex);
                return true;
            }
        }
        return false;
    }
}

