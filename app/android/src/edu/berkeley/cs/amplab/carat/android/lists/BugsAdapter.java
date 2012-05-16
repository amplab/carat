package edu.berkeley.cs.amplab.carat.android.lists;


import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;

public class BugsAdapter extends HogsBugsAdapter {

	public BugsAdapter(CaratApplication a, SimpleHogBug[] results) {
		super(a, results);
	}

	@Override
	protected int getId() {
		return R.layout.bug;
	}
}