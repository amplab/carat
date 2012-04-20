package edu.berkeley.cs.amplab.carat.android.suggestions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.cs.amplab.carat.android.R;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ProcessInfoAdapter extends BaseAdapter {
	private static List<RunningAppProcessInfo> searchArrayList;

	private LayoutInflater mInflater;

	private static final Map<Integer, String> importanceToString = new HashMap<Integer, String>();
	{
		importanceToString.put(RunningAppProcessInfo.IMPORTANCE_EMPTY,
				"Not running");
		importanceToString.put(RunningAppProcessInfo.IMPORTANCE_BACKGROUND,
				"Background process");
		importanceToString.put(RunningAppProcessInfo.IMPORTANCE_SERVICE,
				"Service");
		importanceToString.put(RunningAppProcessInfo.IMPORTANCE_VISIBLE,
				"Visible task");
		importanceToString.put(RunningAppProcessInfo.IMPORTANCE_FOREGROUND,
				"Foreground app");
	}

	public ProcessInfoAdapter(Context context,
			List<RunningAppProcessInfo> results) {
		searchArrayList = results;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return searchArrayList.size();
	}

	public Object getItem(int position) {
		return searchArrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.process, null);
			holder = new ViewHolder();
			holder.txtName = (TextView) convertView
					.findViewById(R.id.processName);
			holder.txtBenefit = (TextView) convertView
					.findViewById(R.id.processPriority);
			// holder.moreInfo = (ImageView)
			// convertView.findViewById(R.id.moreinfo);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		RunningAppProcessInfo x = searchArrayList.get(position);
		holder.txtName.setText(x.processName);
		holder.txtBenefit.setText(importanceToString.get(x.importance));
		// holder.moreInfo...

		return convertView;
	}

	static class ViewHolder {
		TextView txtName;
		TextView txtBenefit;
		// ImageView moreInfo;
	}
}
