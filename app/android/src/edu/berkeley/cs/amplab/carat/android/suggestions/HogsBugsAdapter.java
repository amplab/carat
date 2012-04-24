package edu.berkeley.cs.amplab.carat.android.suggestions;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class HogsBugsAdapter extends BaseAdapter {
	private List<HogsBugs> allBugsOrHogs = null;

	private LayoutInflater mInflater;
	private CaratApplication a = null;

	public HogsBugsAdapter(CaratApplication a, List<HogsBugs> results) {
		this.a = a;
		allBugsOrHogs = results;
		if (allBugsOrHogs == null)
			allBugsOrHogs = new ArrayList<HogsBugs>();
		mInflater = LayoutInflater.from(a.getApplicationContext());
	}

	public int getCount() {
		return allBugsOrHogs.size();
	}

	public Object getItem(int position) {
		return allBugsOrHogs.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	
	protected abstract int getId();

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(getId(), null);
			holder = new ViewHolder();
			holder.appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
			holder.txtName = (TextView) convertView.findViewById(R.id.bugName);
			holder.progConfidence = (ProgressBar) convertView
					.findViewById(R.id.confidenceBar);
			holder.moreInfo = (ImageView) convertView
					.findViewById(R.id.moreinfo);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		HogsBugs item = allBugsOrHogs.get(position);
		Drawable icon = a.iconForApp(item.getAppName());

		holder.txtName.setText(item.getAppName());
		holder.appIcon.setImageDrawable(icon);
		holder.progConfidence.setProgress((int) (item.getWDistance() * 100));
		// holder.moreInfo...

		return convertView;
	}

	static class ViewHolder {
		ImageView appIcon;
		TextView txtName;
		ProgressBar progConfidence;
		ImageView moreInfo;
	}
}