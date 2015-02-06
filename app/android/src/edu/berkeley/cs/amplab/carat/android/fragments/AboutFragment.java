package edu.berkeley.cs.amplab.carat.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.ui.LocalizedWebView;

public class AboutFragment extends ExtendedTitleFragment {
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View rootView = inflater.inflate(R.layout.about, container, false);
        LocalizedWebView webview = (LocalizedWebView) rootView.findViewById(R.id.aboutView);
        webview.loadUrl("file:///android_asset/about.html");
        //getActivity().setTitle(CaratApplication.);
        
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
