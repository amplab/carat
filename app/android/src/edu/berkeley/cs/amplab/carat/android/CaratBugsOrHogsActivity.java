package edu.berkeley.cs.amplab.carat.android;

import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;
import edu.berkeley.cs.amplab.carat.android.lists.HogsBugsAdapter;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity;
import edu.berkeley.cs.amplab.carat.android.ui.DrawView;
import edu.berkeley.cs.amplab.carat.android.ui.FlipperBackListener;
import edu.berkeley.cs.amplab.carat.android.ui.LocalizedWebView;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

public class CaratBugsOrHogsActivity extends BaseVFActivity {

	protected boolean isBugsActivity = false;
	protected Type activityType = Type.HOG;
	private DrawView w = null;
	private View detailPage = null;
	private View tv = null;
	private int emptyIndex = -1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		if (i != null) {
			String a = i.getAction();
			if (a.equals(CaratMainActivity.ACTION_BUGS)) {
				activityType = Type.BUG;
				isBugsActivity = true;
			} else {
				activityType = Type.HOG;
				isBugsActivity = false;
			}
		}
		setContentView(R.layout.hogs);
		vf = (ViewFlipper) findViewById(R.id.flipper);
		View baseView = findViewById(android.R.id.list);
		baseView.setOnTouchListener(SwipeListener.instance);
		vf.setOnTouchListener(SwipeListener.instance);
		baseViewIndex = vf.indexOfChild(baseView);

		LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
		tv = mInflater.inflate(R.layout.emptyactions, null);
		if (tv != null) {
			vf.addView(tv);
			emptyIndex = vf.indexOfChild(tv);
		}
		// initBugsView();
		// initGraphView();
		initGraphChart();
		initDetailView();

		Object o = getLastNonConfigurationInstance();
		if (o != null) {
			CaratBugsOrHogsActivity previous = (CaratBugsOrHogsActivity) o;
			TextView pn = (TextView) previous.detailPage
					.findViewById(R.id.name);
			ImageView pi = (ImageView) previous.detailPage
					.findViewById(R.id.appIcon);
			TextView pp = (TextView) previous.detailPage.findViewById(R.id.benefit);
			
			DrawView w = previous.w;

			((TextView) detailPage.findViewById(R.id.name)).setText(pn
					.getText());
			((ImageView) detailPage.findViewById(R.id.appIcon))
					.setImageDrawable(pi.getDrawable());
			((TextView) detailPage.findViewById(R.id.benefit))
					.setText(pp.getText());

			String appName = w.getAppName();
			
			w.setParams(activityType, appName,
					w.getEv(), w.getEvWithout(), w.getSampleCount(), w.getSampleCountWithout(), w.getError(), w.getErrorWithout(), (ViewGroup) detailPage);
			//w.postInvalidate();
		}

		if (viewIndex == 0)
			vf.setDisplayedChild(baseViewIndex);
		else
			vf.setDisplayedChild(viewIndex);
	}

	private void initGraphChart() {
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		detailPage = inflater.inflate(R.layout.graph, null);
		w = new DrawView(getApplicationContext());
		//g.addView(w);
		vf.addView(detailPage);

		OnClickListener detailViewer = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				switchView(R.id.detailView);
			}
		};

		View moreinfo = detailPage.findViewById(R.id.moreinfo);
		moreinfo.setOnClickListener(detailViewer);

		View item = detailPage.findViewById(R.id.benefit);
		item.setClickable(true);
		item.setOnClickListener(detailViewer);
		item = detailPage.findViewById(R.id.benefit);
		item.setClickable(true);
		item.setOnClickListener(detailViewer);
		item = detailPage.findViewById(R.id.name);
		item.setClickable(true);
		item.setOnClickListener(detailViewer);
		item = detailPage.findViewById(R.id.appIcon);
		item.setClickable(true);
		item.setOnClickListener(detailViewer);

		detailPage.setOnTouchListener(new FlipperBackListener(this, vf,
				baseViewIndex, false));

		final ListView lv = (ListView) findViewById(android.R.id.list);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Object o = lv.getItemAtPosition(position);
				SimpleHogBug fullObject = (SimpleHogBug) o;
				// View target = findViewById(R.id.hogsGraphView);
				View target = detailPage;
				String label = CaratApplication.labelForApp(
						getApplicationContext(), fullObject.getAppName());
				Drawable icon = CaratApplication.iconForApp(
						getApplicationContext(), fullObject.getAppName());
				PackageInfo pak = SamplingLibrary.getPackageInfo(
						getApplicationContext(), fullObject.getAppName());
				String ver = "";
				if (pak != null){
					ver = pak.versionName;
					if (ver == null)
					    ver = pak.versionCode+"";
				}
				final String s = label + " " + ver;
				((TextView) detailPage.findViewById(R.id.name)).setText(s);
				((ImageView) detailPage.findViewById(R.id.appIcon))
						.setImageDrawable(icon);
				((TextView) detailPage.findViewById(R.id.benefit))
						.setText(fullObject.textBenefit());
				w.setHogsBugs(fullObject, s, isBugsActivity, target);
				//detailPage.postInvalidate();
				switchView(target);
			}
		});
	}

	private void initDetailView() {
		LocalizedWebView webview = (LocalizedWebView) findViewById(R.id.detailView);

		webview.loadUrl("file:///android_asset/detailinfo.html");
		webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
				.indexOfChild(detailPage), false));
	}

	public void refresh() {
		CaratApplication app = (CaratApplication) getApplication();
		final ListView lv = (ListView) findViewById(android.R.id.list);
		if (isBugsActivity)
			lv.setAdapter(new HogsBugsAdapter(app, CaratApplication.s
					.getBugReport()));
		else
			lv.setAdapter(new HogsBugsAdapter(app, CaratApplication.s
					.getHogReport()));
		emptyCheck(lv);
	}

	private void emptyCheck(ListView lv) {
		if (lv.getAdapter().isEmpty()) {
			if (vf.getDisplayedChild() == baseViewIndex)
				vf.setDisplayedChild(emptyIndex);
		} else {
			if (vf.getDisplayedChild() == emptyIndex) {
				vf.setDisplayedChild(baseViewIndex);
			}
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		if (isBugsActivity)
			CaratApplication.setBugs(this);
		else
			CaratApplication.setHogs(this);
		refresh();
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (vf.getDisplayedChild() != baseViewIndex
				&& vf.getDisplayedChild() != emptyIndex) {
			vf.setOutAnimation(CaratMainActivity.outtoRight);
			vf.setInAnimation(CaratMainActivity.inFromLeft);
			vf.setDisplayedChild(baseViewIndex);
			viewIndex = baseViewIndex;
		} else
			finish();
	}
}
