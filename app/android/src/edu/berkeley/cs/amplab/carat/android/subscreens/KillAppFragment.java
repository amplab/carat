package edu.berkeley.cs.amplab.carat.android.subscreens;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.fragments.ExtendedTitleFragment;
import edu.berkeley.cs.amplab.carat.android.protocol.ClickTracking;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.ui.LocalizedWebView;

public class KillAppFragment extends ExtendedTitleFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.killlayout, container, false);
		LocalizedWebView webview = (LocalizedWebView) view.findViewById(R.id.killView);
		String osVer = SamplingLibrary.getOsVersion();
		// FIXME: KLUDGE. Should be smarter with the version number.
		if (osVer.startsWith("2."))
			webview.loadUrl("file:///android_asset/killapp-2.2.html");
		else
			webview.loadUrl("file:///android_asset/killapp.html");

		final Context c = getActivity();
		final String raw = getArguments().getString("raw");

		Boolean isBug = getArguments().getBoolean("isBug");
		Boolean isHog = getArguments().getBoolean("isHog");

		ImageView icon = (ImageView) view.findViewById(R.id.suggestion_app_icon);
		TextView txtName = (TextView) view.findViewById(R.id.actionName);
		TextView txtType = (TextView) view.findViewById(R.id.suggestion_type);
		final TextView txtBenefit = (TextView) view.findViewById(R.id.expectedBenefit);
		final Button killButton = (Button) view.findViewById(R.id.killButton);

		final String label = CaratApplication.labelForApp(c, raw);
		icon.setImageDrawable(CaratApplication.iconForApp(c, raw));

		if (isBug || isHog) {
			txtName.setText(label);
			final PackageInfo pak = SamplingLibrary.getPackageInfo(c, raw);
			String ver = "";
			if (pak != null)
				ver = pak.versionName;
			if (ver == null)
				ver = "";
			// Log.d("killApp", pak.packageName);
			final String s = label + " " + ver;
			killButton.setText(getString(R.string.kill) + " " + s);
			killButton.setEnabled(true);
			killButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					/* killButton clicked. Track click: */

					SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
					if (p != null) {
						String uuId = p.getString(CaratApplication.getRegisteredUuid(), "UNKNOWN");
						HashMap<String, String> options = new HashMap<String, String>();
						if (pak != null) {
							options.put("app", pak.packageName);
							options.put("version", pak.versionName);
							options.put("versionCode", pak.versionCode + "");
							options.put("label", label);
						}
						options.put("benefit", txtBenefit.getText().toString().replace('\u00B1', '+'));
						ClickTracking.track(uuId, "killbutton", options, getActivity());
					}
					/*
					 * Change kill button text and make it unclickable until
					 * screen is exited
					 */
					killButton.setEnabled(false);
					killButton.setText(s + " " + getString(R.string.killed));
					// FIXME: sometimes this method doesn't kill the app, 
					// check and if needed, fix it
					SamplingLibrary.killApp(c, raw, label);
				}
			});
			Button AppManagerButton = (Button) view.findViewById(R.id.appManager);
			AppManagerButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
					if (p != null) {
						String uuId = p.getString(CaratApplication.getRegisteredUuid(), "UNKNOWN");
						HashMap<String, String> options = new HashMap<String, String>();
						if (pak != null) {
							options.put("app", pak.packageName);
							options.put("version", pak.versionName);
							options.put("versionCode", pak.versionCode + "");
							options.put("label", label);
						}
						options.put("benefit", txtBenefit.getText().toString().replace('\u00B1', '+'));
						ClickTracking.track(uuId, "appmanagerbutton", options, getActivity());
					}
					
					// FIXME: implement this method (show AppManager in a fragment/screen)
					// GoToAppScreen();
				}
			});
		} else { // Other action
			txtName.setText(label);
			killButton.setText(label);
		}

		txtType.setText(getArguments().getString("appPriority"));
		String benefit = getArguments().getString("benefit");
		txtBenefit.setText(benefit);

		// onCreateView() should return the view resulting from inflating the
		// layout file
		return view;
	}
}
