package edu.berkeley.cs.amplab.carat.android.suggestions;

import java.util.ArrayList;

import edu.berkeley.cs.amplab.carat.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class SuggestionAdapter extends BaseAdapter {
	 private static ArrayList<Suggestion> searchArrayList;
	 
	 private LayoutInflater mInflater;

	 public SuggestionAdapter(Context context, ArrayList<Suggestion> results) {
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
	   convertView = mInflater.inflate(R.layout.suggestion, null);
	   holder = new ViewHolder();
	   holder.txtName = (TextView) convertView.findViewById(R.id.actionName);
	   holder.txtBenefit = (TextView) convertView.findViewById(R.id.expectedBenefit);
	   holder.moreInfo = (ImageView) convertView.findViewById(R.id.moreinfo);

	   convertView.setTag(holder);
	  } else {
	   holder = (ViewHolder) convertView.getTag();
	  }
	  
	  holder.txtName.setText(searchArrayList.get(position).getName());
	  holder.txtBenefit.setText(searchArrayList.get(position).getBenefit());
	  //holder.moreInfo...

	  return convertView;
	 }

	 static class ViewHolder {
	  TextView txtName;
	  TextView txtBenefit;
	  ImageView moreInfo;
	 }
	}