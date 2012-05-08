package edu.berkeley.cs.amplab.carat.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.berkeley.cs.amplab.carat.CaratApplication;
import edu.berkeley.cs.amplab.carat.R;
import edu.berkeley.cs.amplab.carat.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HogBugSuggestionsAdapter extends BaseAdapter {

	private boolean[] isBug = null;
	private HogsBugs[] indexes = null;
	
	private boolean addFakeItem = true;

	private LayoutInflater mInflater;
	private CaratApplication a = null;
	
	private String FAKE_ITEM = "OsUpgrade";

	public HogBugSuggestionsAdapter(CaratApplication a, List<HogsBugs> hogs,
			List<HogsBugs> bugs) {
		this.a = a;

		ArrayList<HogsBugs> temp = new ArrayList<HogsBugs>();
		acceptHogsOrBugs(hogs, temp);
		acceptHogsOrBugs(bugs, temp);
		if (addFakeItem){
		    HogsBugs fake = new HogsBugs();
            fake.setAppName(FAKE_ITEM);
            fake.setExpectedValue(0.0);
            fake.setExpectedValueWithout(0.0);
            temp.add(fake);
		}
		Collections.sort(temp, new HogsBugsComparator());

		indexes = new HogsBugs[temp.size()];
		isBug = new boolean[temp.size()];
		int i = 0;
		for (HogsBugs b : temp) {
		    if (bugs != null)
		        isBug[i] = bugs.contains(b);
			indexes[i] = b;
			i++;
		}

		mInflater = LayoutInflater.from(a.getApplicationContext());
	}

	private void acceptHogsOrBugs(List<HogsBugs> input, List<HogsBugs> result) {
		if (input == null)
			return;
		for (HogsBugs item : input) {
			double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
					/ item.getExpectedValue();
			// TODO other filter conditions?
			// Limit max number of items?
			
			// Skip system apps
			if (SamplingLibrary.isSystem(a.getApplicationContext(), item.getAppName()))
			    continue;
			if (addFakeItem && item.getAppName().equals(FAKE_ITEM))
			    result.add(item);
			// Filter out if benefit is too small
			if (SamplingLibrary.isRunning(a.getApplicationContext(), item.getAppName()) && benefit > 60) {
				result.add(item);
			}
		}
	}

	public int getCount() {
		return indexes.length;
	}

	public Object getItem(int position) {
		return indexes[position];
	}

	public long getItemId(int position) {
		return position;
	}

	protected int getId() {
		return R.layout.suggestion;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.suggestion, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView
					.findViewById(R.id.suggestion_app_icon);
			holder.txtName = (TextView) convertView
					.findViewById(R.id.actionName);
			holder.txtType = (TextView) convertView
					.findViewById(R.id.suggestion_type);
			holder.txtBenefit = (TextView) convertView
					.findViewById(R.id.expectedBenefit);
			holder.moreInfo = (ImageView) convertView
					.findViewById(R.id.moreinfo);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		HogsBugs item = indexes[position];
		boolean bug = isBug[position];

		Drawable icon = a.iconForApp(item.getAppName());

		if (item.getAppName().equals(FAKE_ITEM)){
            holder.txtName.setText("OS Upgrade");
            // TODO: Include process type=priority in Sample?
            holder.txtType.setText("information");
            holder.txtBenefit.setText("Unknown");
        } else {
            double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
                    / item.getExpectedValue();

            int min = (int) (benefit / 60);
            int hours = (int) (min / 60);
            min -= hours * 60;

            holder.icon.setImageDrawable(icon);
            holder.txtName.setText((bug ? "Restart" : "Kill") + " "
                    + a.labelForApp(item.getAppName()));
            // TODO: Include process type=priority in Sample?
            // holder.txtType.setText(item.getType());
            holder.txtBenefit.setText(hours + "h " + min + "m");

            // holder.moreInfo...
        }
        return convertView;
    }

	static class ViewHolder {
		ImageView icon;
		TextView txtName;
		TextView txtType;
		TextView txtBenefit;
		ImageView moreInfo;
	}
}

class HogsBugsComparator implements Comparator<HogsBugs> {

	@Override
	public int compare(HogsBugs lhs, HogsBugs rhs) {
		double benefitL = 100.0 / lhs.getExpectedValueWithout() - 100.0
				/ lhs.getExpectedValue();
		double benefitR = 100.0 / rhs.getExpectedValueWithout() - 100.0
				/ rhs.getExpectedValue();
		if (benefitL > benefitR)
			return -1;
		else if (benefitL < benefitR)
			return 1;
		return 0;
	}
}
