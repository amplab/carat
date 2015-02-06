package edu.berkeley.cs.amplab.carat.android;

import android.view.LayoutInflater;
import android.view.View;

public interface Item {
	public int getViewType();
    public View getView(LayoutInflater inflater, View convertView);
}
