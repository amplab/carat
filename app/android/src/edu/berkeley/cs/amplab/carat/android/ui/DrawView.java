package edu.berkeley.cs.amplab.carat.android.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.Constants;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.android.utils.Tracker;

public class DrawView extends View {

	private Context c = null;

	private Constants.Type type = null;

	private double ev = 0.0;
	private double evWithout = 0.0;
	private int sampleCount = 0;
	private int sampleCountWo = 0;
	private double error = 0.0;
	private double errorWo = 0.0;
	private String textBenefit = null;
	private String appName = null;
	private Drawable icon;

	public String getAppName() {
		return this.appName;
	}

	public DrawView(Context context) {
		super(context);
		this.c = context;
	}

	public Constants.Type getType() {
		return this.type;
	}

	public double getEvWithout() {
		return evWithout;
	}

	public double getEv() {
		return ev;
	}

	public double getError() {
		return error;
	}

	public double getErrorWithout() {
		return errorWo;
	}

	public int getSampleCount() {
		return sampleCount;
	}

	public int getSampleCountWithout() {
		return sampleCountWo;
	}

	public void setHogsBugs(SimpleHogBug bugOrHog, String appName, boolean isBug, View parent) {
		this.ev = bugOrHog.getExpectedValue();
		this.evWithout = bugOrHog.getExpectedValueWithout();
		this.textBenefit = bugOrHog.getBenefitText();
		this.error = bugOrHog.getError();
		this.errorWo = bugOrHog.getErrorWithout();
		this.sampleCount = (int) bugOrHog.getSamples();
		this.sampleCountWo = (int) bugOrHog.getSamplesWithout();
		this.type = isBug ? Constants.Type.BUG : Constants.Type.HOG;
		this.appName = appName;
		setFields(parent, true);
	}

	public void setParams(Constants.Type type, String appName, double ev, double evWithout, int sampleCount, int sampleCountWo,
			double error, double errorWo, View parent) {
		this.ev = ev;
		this.evWithout = evWithout;
		this.sampleCount = sampleCount;
		this.error = error;
		this.errorWo = errorWo;
		this.sampleCount = (int) sampleCount;
		this.sampleCountWo = (int) sampleCountWo;

		this.type = type;
		this.appName = appName;
		this.icon = CaratApplication.iconForApp(CaratApplication.getMainActivity(), "Carat");
		setFields(parent, false);
	}
	
	public void setParams(SimpleHogBug fullObject, View parent) {
		String ver = "";
		Context activity = CaratApplication.getMainActivity();
		String label = CaratApplication.labelForApp(activity, fullObject.getAppName());
		PackageInfo pak = SamplingLibrary.getPackageInfo(activity, fullObject.getAppName());
		if (pak != null) {
			ver = pak.versionName;
			if (ver == null)
				ver = pak.versionCode + "";
		}
		this.appName = label + " " + ver;
		this.icon = CaratApplication.iconForApp(activity, fullObject.getAppName());
		this.type = fullObject.getType();
		
		this.textBenefit = fullObject.getBenefitText();
		this.sampleCount = fullObject.getSamples();
		this.sampleCountWo = fullObject.getSamplesWithout();
		this.error = fullObject.getError();
		this.ev = fullObject.getExpectedValue();
		this.evWithout = fullObject.getExpectedValueWithout();
		this.errorWo = fullObject.getErrorWithout();

		Tracker tracker = Tracker.getInstance();
		// the field "type" should be set BEFORE calling this tracking method
		tracker.trackUser(label, fullObject);
		
		setFields(parent, true);
	}

	private void setFields(View parent, boolean isApp) {
		TextView samples = (TextView) parent.findViewById(R.id.samples);
//		TextView killBenefit = (TextView) parent.findViewById(R.id.killBenefit);
//		TextView benefitTopTextView = (TextView) parent.findViewById(R.id.benefit);
		TextView samplesWoT = (TextView) parent.findViewById(R.id.samplesWo);
		TextView errorText = (TextView) parent.findViewById(R.id.error);
		TextView appName = (TextView) parent.findViewById(R.id.name);
		ImageView iconImageView = (ImageView) parent.findViewById(R.id.appIcon);

		appName.setText(this.appName);
		iconImageView.setImageDrawable(icon);
		
		if (sampleCount > 0) {
			samplesWoT.setText(sampleCountWo + "");
			samples.setText(sampleCount + "");
		} else {
			samples.setText("0");
			samplesWoT.setText("0");
		}
		
		if (ev > 0 || isApp) {
			errorText.setText(SimpleHogBug.getErrorText(ev, error, evWithout, errorWo));
		}

//		if (textBenefit != null) {
//			killBenefit.setText(textBenefit);
//			benefitTopTextView.setText(textBenefit);
//		} else {
//			double benefit = 100.0 / evWithout - 100.0 / ev;
//			if (benefit < 0) {
//				killBenefit.setText(c.getString(R.string.best));
//			} else {
//				killBenefit.setText(SimpleHogBug.getBenefitText(ev, error, evWithout, errorWo));
//			}
//		}
	}
}
