package edu.berkeley.cs.amplab.carat.ui;

import edu.berkeley.cs.amplab.carat.CaratMainActivity;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

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
			if (currentTab == 2 || currentTab == 3)
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