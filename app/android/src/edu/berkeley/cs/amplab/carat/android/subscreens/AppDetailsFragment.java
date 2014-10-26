package edu.berkeley.cs.amplab.carat.android.subscreens;

import java.util.HashMap;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;
import edu.berkeley.cs.amplab.carat.android.protocol.ClickTracking;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.android.ui.DrawView;
import edu.berkeley.cs.amplab.carat.thrift.DetailScreenReport;
import edu.berkeley.cs.amplab.carat.thrift.Reports;

public class AppDetailsFragment extends Fragment {

	private SimpleHogBug fullObject;
	private boolean isBugs;
	private boolean isOs;
	private boolean isModel;
	private double ev, error, evWithout, errorWo;
	private int samplesCount, samplesCountWithout;
	
	/*
	 * call this factory method when you want to display the details of a regular app
	 */
	public static AppDetailsFragment newInstance(SimpleHogBug fullObject, boolean isBugs) {
		AppDetailsFragment fragment = new AppDetailsFragment();
		fragment.setFullObject(fullObject);
		fragment.isBugs = isBugs;
		fragment.isOs = false;
		fragment.isModel = false;
		return fragment;
	}

	/*
	 * call this factory method when you want to display the OS details or the device details
	 * @param isModel set this to true if you want to show the device info rather than OS info
	 */
	public static AppDetailsFragment newInstance(boolean isModel) {
		AppDetailsFragment fragment = new AppDetailsFragment();
		if (!isModel) {
			fragment.isOs = true;
			fragment.isModel = false;
		} else {
			fragment.isOs = false;
			fragment.isModel = true;
		}
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View detailsPage = inflater.inflate(R.layout.graph, container, false);
		
		if (!isOs && ! isModel) {
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
			
//			setDetails(fullObject.getExpectedValue(), fullObject.getError(), osWithout.getExpectedValue(), osWithout.getError(), (int) fullObject.getSamples(), (int) fullObject.getSamplesWithout());
			
			((TextView) detailsPage.findViewById(R.id.benefit)).setText(fullObject.textBenefit());
			((TextView) detailsPage.findViewById(R.id.killBenefit)).setText(fullObject.textBenefit());
			((TextView) detailsPage.findViewById(R.id.samples)).setText(String.valueOf(fullObject.getSamples()));
			((TextView) detailsPage.findViewById(R.id.error)).setText(String.valueOf(fullObject.getError()));
			((TextView) detailsPage.findViewById(R.id.samplesWo))
					.setText(String.valueOf(fullObject.getSamplesWithout()));

			View moreinfo = detailsPage.findViewById(R.id.moreinfo);
			moreinfo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					CaratApplication.showHTMLFile("detailinfo");
				}
			});
			trackUser(label, pak);
		} else {
			DrawView drawView = new DrawView(getActivity());
			Reports r = CaratApplication.s.getReports();
			String label;
			
	        if (r != null) {
	        	if (isOs) {
	        		DetailScreenReport os = r.getOs();
	        		DetailScreenReport osWithout = r.getOsWithout();
	        		label = getString(R.string.os) +": " + SamplingLibrary.getOsVersion();
	        		Log.v("OsInfo", "Os score: " + os.getScore());
	        		setDetails(os.getExpectedValue(), os.getError(), osWithout.getExpectedValue(), osWithout.getError(), (int) os.getSamples(), (int) osWithout.getSamples());
	        		drawView.setParams(Type.OS, SamplingLibrary.getOsVersion(),
		                    ev, evWithout, samplesCount, samplesCountWithout, error, errorWo, detailsPage);
	        		CaratApplication.trackUser("osInfo");
	        	} else {
	        		DetailScreenReport model = r.getModel();
	                DetailScreenReport modelWithout = r.getModelWithout();
	                label = getString(R.string.model) +": " + SamplingLibrary.getModel();
	                Log.v("ModelInfo", "Model score: " + model.getScore());
	                setDetails(model.getExpectedValue(), model.getError(), modelWithout.getExpectedValue(), modelWithout.getError(), (int) model.getSamples(), (int) modelWithout.getSamples());
	                drawView.setParams(Type.MODEL, SamplingLibrary.getModel(),
		                    ev, evWithout, samplesCount, samplesCountWithout, error, errorWo, detailsPage);
		            CaratApplication.trackUser("deviceInfo");
	        	}
	        	
	            Drawable icon = CaratApplication.iconForApp(getActivity(), "Carat");
	            ((TextView) detailsPage.findViewById(R.id.name)).setText(label);
	            ((ImageView) detailsPage.findViewById(R.id.appIcon))
	                    .setImageDrawable(icon);
	            
	            String benefitText = SimpleHogBug.textBenefit(ev, error, evWithout, errorWo);
	            if (benefitText == null)
	                benefitText = getString(R.string.jsna);
	            
	            ((TextView) detailsPage.findViewById(R.id.benefit))
	                    .setText(benefitText);
	        }
		}
		
		// onCreateView() should return the view resulting from inflating the
		// layout file
		return detailsPage;
	}
	
	private void setDetails(double ev, double error, double evWithout, double errorWithout, int samplesCount, int samplesCountWithout) {
		this.ev = ev;
		this.error = error;
		this.evWithout = errorWithout;
		this.errorWo = errorWithout;
		this.samplesCount = samplesCount;
		this.samplesCountWithout = samplesCountWithout;
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
