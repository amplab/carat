package edu.berkeley.cs.amplab.carat.android.suggestions;

import java.util.ArrayList;

import edu.berkeley.cs.amplab.carat.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HogAdapter extends BaseAdapter {
	private static ArrayList<Hog> searchArrayList;

	private LayoutInflater mInflater;

	public HogAdapter(Context context, ArrayList<Hog> results) {
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

		holder.txtName.setText(searchArrayList.get(position).getName());
		holder.appIcon.setImageResource(searchArrayList.get(position).getIconResource());
		holder.progConfidence.setProgress((int) (searchArrayList.get(position)
				.getConfidence() * 100));
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