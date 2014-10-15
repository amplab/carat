package edu.berkeley.cs.amplab.carat.android;

import edu.berkeley.cs.amplab.carat.android.ui.LocalizedWebView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends Fragment {
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
	    View rootView = inflater.inflate(R.layout.about, container, false);
        LocalizedWebView webview = (LocalizedWebView) rootView.findViewById(R.id.aboutView);
        
        webview.loadUrl("file:///android_asset/about.html");
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }
}
