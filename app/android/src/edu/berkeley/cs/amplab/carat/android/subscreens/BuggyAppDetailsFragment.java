package edu.berkeley.cs.amplab.carat.android.subscreens;

import java.util.HashMap;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.protocol.ClickTracking;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;

public class BuggyAppDetailsFragment extends Fragment {

	private SimpleHogBug fullObject;
	private boolean isBugs;
	
	public static BuggyAppDetailsFragment newInstance(SimpleHogBug fullObject, boolean isBugs) {
		BuggyAppDetailsFragment fragment = new BuggyAppDetailsFragment();
		fragment.setFullObject(fullObject);
		fragment.isBugs = isBugs;
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View detailsPage = inflater.inflate(R.layout.graph, container, false);
		
		String label = CaratApplication.labelForApp(getActivity(), fullObject.getAppName());
        Drawable icon = CaratApplication.iconForApp(getActivity(), fullObject.getAppName());
        PackageInfo pak = SamplingLibrary.getPackageInfo(getActivity(), fullObject.getAppName());
        
        String ver = "";
        if (pak != null) {
            ver = pak.versionName;
            if (ver == null)
                ver = pak.versionCode + "";
        }
        final String s = label + " " + ver;
        
        ((TextView) detailsPage.findViewById(R.id.name)).setText(s);
        ((ImageView) detailsPage.findViewById(R.id.appIcon)).setImageDrawable(icon);
        ((TextView) detailsPage.findViewById(R.id.benefit)).setText(fullObject.textBenefit());
        
        ((TextView) detailsPage.findViewById(R.id.killBenefit)).setText(fullObject.textBenefit());
        ((TextView) detailsPage.findViewById(R.id.samples)).setText(String.valueOf(fullObject.getSamples()));
        ((TextView) detailsPage.findViewById(R.id.error)).setText(String.valueOf(fullObject.getError()));
        ((TextView) detailsPage.findViewById(R.id.samplesWo)).setText(String.valueOf(fullObject.getSamplesWithout()));
		
        trackUser(label, pak);
        
		// onCreateView() should return the view resulting from inflating the
		// layout file
		return detailsPage;
	}

	private void trackUser(String label, PackageInfo pak) {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (p != null) {
            String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("status", getActivity().getTitle().toString());
            options.put("type", isBugs ? "Bugs" : "Hogs");
            if (pak != null) {
                options.put("app", pak.packageName);
                options.put("version", pak.versionName);
                options.put("versionCode", pak.versionCode + "");
                options.put("label", label);
            }
            options.put("benefit", fullObject.textBenefit().replace('\u00B1', '+'));
            ClickTracking.track(uuId, "samplesview", options, getActivity());
        }
	}
	
	public void setFullObject(SimpleHogBug fullObject) {
		this.fullObject = fullObject;
	}
}
