package edu.berkeley.cs.amplab.carat.android;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import edu.berkeley.cs.amplab.carat.android.TextArrayAdapter.RowType;

public class ListItem implements Item {
	private final String text;

	public ListItem(String text) {
		this.text = text;
	}

	@Override
	public int getViewType() {
		return RowType.LIST_ITEM.ordinal();
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view;
		if (convertView == null) {
			view = (View) inflater.inflate(R.layout.nav_drawer_list_item, null);
		} else {
			view = convertView;
		}

		TextView textView = (TextView) view.findViewById(R.id.list_content);
		textView.setText(text);

		return view;
	}

}
