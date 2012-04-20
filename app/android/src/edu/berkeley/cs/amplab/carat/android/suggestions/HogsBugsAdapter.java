package edu.berkeley.cs.amplab.carat.android.suggestions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HogsBugsAdapter extends BaseAdapter {
	private List<HogsBugs> allBugsOrHogs = null;
	private Map<String, Drawable> appToIcon = null;

	private LayoutInflater mInflater;

	public HogsBugsAdapter(Context context, List<HogsBugs> results, Map<String, Drawable> icons) {
		allBugsOrHogs = results;
		if (allBugsOrHogs == null)
			allBugsOrHogs = new ArrayList<HogsBugs>();
		appToIcon = icons;
		mInflater = LayoutInflater.from(context);
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

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.bughog, null);
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
		Drawable icon = appToIcon.get(item.getAppName());
		if (icon == null)
			icon = appToIcon.get("edu.berkeley.cs.amplab.carat.android");

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