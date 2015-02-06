package edu.berkeley.cs.amplab.carat.android;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import edu.berkeley.cs.amplab.carat.android.TextArrayAdapter.RowType;

public class NavDrawerListHeader implements Item {

	private final String name;

	public NavDrawerListHeader(String name) {
		this.name = name;
	}

	@Override
	public int getViewType() {
		return RowType.HEADER_ITEM.ordinal();
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view;
		if (convertView == null) {
			view = (View) inflater.inflate(R.layout.nav_drawer_list_header, null);
		} else {
			view = convertView;
		}

		TextView textView = (TextView) view.findViewById(R.id.header_textview);
		textView.setText(name);

		return view;
	}

}
