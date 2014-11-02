package edu.berkeley.cs.amplab.carat.android.subscreens;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.ui.LocalizedWebView;
import edu.berkeley.cs.amplab.carat.android.utils.Tracker;

public class WebViewFragment extends Fragment {

	private String fileName;
	
	public static WebViewFragment newInstance(String fileName) {
		WebViewFragment instance = new WebViewFragment();
		instance.fileName = fileName;
		return instance;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.webview, container, false);
		LocalizedWebView webview = (LocalizedWebView) view.findViewById(R.id.webView);
        webview.loadUrl("file:///android_asset/" + fileName + ".html");
        
        Tracker tracker = Tracker.getInstance();
		tracker.trackUser(fileName);
		// onCreateView() should return the view resulting from inflating the
		// layout file
		return view;
	}
}
