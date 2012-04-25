package edu.berkeley.cs.amplab.carat.android.suggestions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.R;
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

	private LayoutInflater mInflater;
	private CaratApplication a = null;

	public HogBugSuggestionsAdapter(CaratApplication a, List<HogsBugs> hogs,
			List<HogsBugs> bugs) {
		this.a = a;

		ArrayList<HogsBugs> temp = new ArrayList<HogsBugs>();
		acceptHogsOrBugs(hogs, temp);
		acceptHogsOrBugs(bugs, temp);

		Collections.sort(temp, new HogsBugsComparator());

		indexes = new HogsBugs[temp.size()];
		isBug = new boolean[temp.size()];
		int i = 0;
		for (HogsBugs b : temp) {
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
			// TODO: Filter out stuff not running on this device in the final
			// version

			// Filter out if benefit is too small
			if (benefit > 60) {
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
