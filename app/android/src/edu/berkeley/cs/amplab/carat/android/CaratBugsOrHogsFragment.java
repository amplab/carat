package edu.berkeley.cs.amplab.carat.android;

import java.util.HashMap;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;
import edu.berkeley.cs.amplab.carat.android.lists.HogsBugsAdapter;
import edu.berkeley.cs.amplab.carat.android.protocol.ClickTracking;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.android.ui.DrawView;
import edu.berkeley.cs.amplab.carat.android.ui.LocalizedWebView;

public class CaratBugsOrHogsFragment extends Fragment {

    public static final String IS_BUGS = "IS_BUGS";
    protected boolean isBugs = false;

    private DrawView w = null;
    private View detailPage = null;
    private View tv = null;
    private int emptyIndex = -1;
    private View bv = null;
    private int emptyBugsIndex = -1;

    int viewIndex = 0;
    int baseViewIndex = 0;
    ViewFlipper vf = null;

    private void switchView(View v) {
    }

    private void switchView(int id) {
    }

    /**
     * When creating Hogs or Bugs, set which one this is here:
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (savedInstanceState != null) {
//            isBugs = savedInstanceState.getBoolean(IS_BUGS);
//        } else {
//        	Log.d("CaratBugsOrHogsFragment", "savedInstanceState=null");
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	Tab tab = ((ActionBarActivity)getActivity()).getSupportActionBar().getSelectedTab();
    	String bugsOrHogs = tab.getTag().toString();
    	if (bugsOrHogs.equals("bugs"))
            isBugs = true;
        else
        	isBugs = false;
    	
    	String isBugsStr = isBugs? "IS_BUGS=true" : "IS_BUGS=false";
    	Log.d("BugsOrHogs", isBugsStr);
        
        vf = new ViewFlipper(getActivity());
        View root = inflater.inflate(R.layout.hogs, container, false);
        tv = inflater.inflate(R.layout.emptyactions, null);
        if (tv != null) {
            vf.addView(tv);
            emptyIndex = vf.indexOfChild(tv);
        }

        if (isBugs) {
            bv = inflater.inflate(R.layout.emptybugsonly, null);
            if (bv != null) {
                vf.addView(bv);
                emptyBugsIndex = vf.indexOfChild(bv);
            }
        }

        // initBugsView();
        // initGraphView();
        initGraphChart(root);
        initDetailView(root);

        if (savedInstanceState != null) {
            Object o = savedInstanceState.get("savedInstance");
            if (o != null) {
                CaratBugsOrHogsFragment previous = (CaratBugsOrHogsFragment) o;
                TextView pn = (TextView) previous.detailPage.findViewById(R.id.name);
                ImageView pi = (ImageView) previous.detailPage.findViewById(R.id.appIcon);
                TextView pp = (TextView) previous.detailPage.findViewById(R.id.benefit);

                DrawView w = previous.w;

                ((TextView) detailPage.findViewById(R.id.name)).setText(pn.getText());
                ((ImageView) detailPage.findViewById(R.id.appIcon)).setImageDrawable(pi.getDrawable());
                ((TextView) detailPage.findViewById(R.id.benefit)).setText(pp.getText());

                String appName = w.getAppName();

                w.setParams(isBugs ? Type.BUG : Type.HOG, appName, w.getEv(), w.getEvWithout(), w.getSampleCount(),
                        w.getSampleCountWithout(), w.getError(), w.getErrorWithout(), (ViewGroup) detailPage);
                // w.postInvalidate();
            }
        }
        if (viewIndex == 0)
            vf.setDisplayedChild(baseViewIndex);
        else
            vf.setDisplayedChild(viewIndex);
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    private void initGraphChart(View root) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        detailPage = inflater.inflate(R.layout.graph, null);
        w = new DrawView(getActivity());
        // g.addView(w);
        vf.addView(detailPage);

        OnClickListener detailViewer = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (p != null) {
                    String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
                    HashMap<String, String> options = new HashMap<String, String>();
                    options.put("status", getActivity().getTitle().toString());
                    options.put("type", isBugs ? "Bugs" : "Hogs");
                    options.put("app", ((TextView) detailPage.findViewById(R.id.name)).getText() + "");
                    options.put("benefit", ((TextView) detailPage.findViewById(R.id.benefit)).getText().toString()
                            .replace('\u00B1', '+'));
                    ClickTracking.track(uuId, "whatnumbers", options, getActivity());
                }
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

        /*
         * detailPage.setOnTouchListener(new FlipperBackListener(this, vf,
         * baseViewIndex, false));
         */

        final ListView lv = (ListView) root.findViewById(android.R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = lv.getItemAtPosition(position);
                SimpleHogBug fullObject = (SimpleHogBug) o;
                // View target = findViewById(R.id.hogsGraphView);
                View target = detailPage;
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
                ((TextView) detailPage.findViewById(R.id.name)).setText(s);
                ((ImageView) detailPage.findViewById(R.id.appIcon)).setImageDrawable(icon);
                ((TextView) detailPage.findViewById(R.id.benefit)).setText(fullObject.textBenefit());
                w.setHogsBugs(fullObject, s, isBugs, target);
                // detailPage.postInvalidate();

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

                switchView(target);
            }
        });
    }

    private void initDetailView(View root) {
        LocalizedWebView webview = (LocalizedWebView) root.findViewById(R.id.detailView);

        webview.loadUrl("file:///android_asset/detailinfo.html");
        /*
         * webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
         * .indexOfChild(detailPage), false));
         */
    }

    public void refresh() {
        CaratApplication app = (CaratApplication) getActivity().getApplication();
        final ListView lv = (ListView) getActivity().findViewById(android.R.id.list);
        if (isBugs)
            lv.setAdapter(new HogsBugsAdapter(app, CaratApplication.s.getBugReport()));
        else
            lv.setAdapter(new HogsBugsAdapter(app, CaratApplication.s.getHogReport()));
        emptyCheck(lv);
    }

    private void emptyCheck(ListView lv) {
        if (lv.getAdapter().isEmpty() && isBugs && CaratApplication.s.getHogReport() != null
                && CaratApplication.s.getHogReport().length > 0) {
            if (vf.getDisplayedChild() == baseViewIndex || vf.getDisplayedChild() == emptyIndex)
                vf.setDisplayedChild(emptyBugsIndex);
        } else if (lv.getAdapter().isEmpty()) {
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
    public void onResume() {    	
        if (isBugs)
            CaratApplication.setBugs(this);
        else
            CaratApplication.setHogs(this);
        refresh();
        super.onResume();
    }

    /*
     * Needs to take place in Activity
     */
    /*
     * @Override public void onBackPressed() { if (vf.getDisplayedChild() !=
     * baseViewIndex && vf.getDisplayedChild() != emptyIndex &&
     * vf.getDisplayedChild() != emptyBugsIndex) {
     * vf.setOutAnimation(CaratMainActivity.outtoRight);
     * vf.setInAnimation(CaratMainActivity.inFromLeft);
     * vf.setDisplayedChild(baseViewIndex); viewIndex = baseViewIndex; } else
     * finish(); }
     */
}
