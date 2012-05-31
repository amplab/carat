package edu.berkeley.cs.amplab.carat.android.ui;

import edu.berkeley.cs.amplab.carat.android.CaratMainActivity;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

public class SwipeListener extends BaseSwipeListener {

	public static SwipeListener instance = new SwipeListener();

	public boolean handleUp(View v, MotionEvent ev) {
		float w = v.getWidth();
		float x = ev.getX();
		float y = ev.getY();

		boolean left = true;
		float xDiff = x - oldX;
		if (x < oldX) {
			// finger moves left, ui moves right
			xDiff = oldX - x;
			left = false;
		}
		
		float yDiff = y - oldY;
		if (y < oldY) {
			yDiff = oldY - y;
		}

		if (xDiff > w / 3.0 && xDiff > yDiff) {
			// moved horizontally. Lets change tabs to the right direction:
			if (left && currentTab > 0) {
				CaratMainActivity.changeTab(currentTab - 1);
				return true;
			} else if (!left && currentTab + 1 < CaratMainActivity.tabHost.getTabWidget().getTabCount()) {
				CaratMainActivity.changeTab(currentTab + 1);
				return true;
			} else
				return false;
		}
		return false;
	}
}

class BackSwipeListener extends BaseSwipeListener {

	Activity a = null;

	public BackSwipeListener(Activity a) {
		this.a = a;
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
				a.finish();
				return true;
			}
		}
		return false;
	}
}