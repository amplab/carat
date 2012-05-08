package edu.berkeley.cs.amplab.carat.lists;

import java.util.List;

import edu.berkeley.cs.amplab.carat.CaratApplication;
import edu.berkeley.cs.amplab.carat.R;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

public class BugsAdapter extends HogsBugsAdapter {

	public BugsAdapter(CaratApplication a, List<HogsBugs> results) {
		super(a, results);
	}

	@Override
	protected int getId() {
		return R.layout.bug;
	}
}