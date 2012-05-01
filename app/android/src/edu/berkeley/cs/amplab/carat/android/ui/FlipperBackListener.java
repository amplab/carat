package edu.berkeley.cs.amplab.carat.android.ui;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ViewFlipper;
import edu.berkeley.cs.amplab.carat.android.CaratMainActivity;

public class FlipperBackListener extends BaseSwipeListener {

    ViewFlipper vf = null;
    // To be able to save the state of which view we are in
    VFActivity vfa = null;
    int backViewIndex = 0;

    public FlipperBackListener(VFActivity vfa, ViewFlipper vf, int backViewIndex) {
        this.vf = vf;
        this.vfa = vfa;
        this.backViewIndex = backViewIndex;
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

