package edu.berkeley.cs.amplab.carat.android;

import java.util.List;

import edu.berkeley.cs.amplab.carat.android.lists.BugsAdapter;
import edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity;
import edu.berkeley.cs.amplab.carat.android.ui.DrawView;
import edu.berkeley.cs.amplab.carat.android.ui.FlipperBackListener;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

public class CaratBugsActivity extends BaseVFActivity{
	private DrawView w = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bugs);
		vf = (ViewFlipper) findViewById(R.id.bugsFlipper);
		View baseView = findViewById(R.id.bugsList);
		baseView.setOnTouchListener(
				SwipeListener.instance);
		vf.setOnTouchListener(SwipeListener.instance);
		baseViewIndex = vf.indexOfChild(baseView);
		//initBugsView();
		//initGraphView();
		initGraphChart();
		
		Object o = getLastNonConfigurationInstance();
        if (o != null){
            CaratBugsActivity previous = (CaratBugsActivity) o;
            List<Double> xVals = previous.w.getXVals();
            List<Double> yVals = previous.w.getYVals();
            List<Double> xValsWithout = previous.w.getXValsWithout();
            List<Double> yValsWithout = previous.w.getYValsWithout();
            String appName = previous.w.getAppName();
            w.setParams(DrawView.Type.BUG, appName, xVals, yVals, xValsWithout, yValsWithout);
            w.postInvalidate();
        }
		
		if (viewIndex == 0)
		    vf.setDisplayedChild(baseViewIndex);
		else
		    vf.setDisplayedChild(viewIndex);
	}

	 private void initGraphChart() {
	        w = new DrawView(getApplicationContext());
	        vf.addView(w);
	        w.setOnTouchListener(new FlipperBackListener(this, vf, vf
	                .indexOfChild(findViewById(R.id.bugsList))));
	        
	        final ListView lv = (ListView) findViewById(R.id.bugsList);
	        lv.setOnItemClickListener(new OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> a, View v, int position,
	                    long id) {
	                Object o = lv.getItemAtPosition(position);
	                HogsBugs fullObject = (HogsBugs) o;
	                // View target = findViewById(R.id.hogsGraphView);
	                View target = w;
	                CaratApplication app = (CaratApplication) getApplication();
	                String label = app.labelForApp(fullObject.getAppName());
	                w.setHogsBugs(fullObject, label, true);
	                w.postInvalidate();
	                switchView(target);
	                /*
	                Toast.makeText(CaratBugsActivity.this,
	                        "You have chosen: " + " " + fullObject.getAppName(),
	                        Toast.LENGTH_SHORT).show();*/
	            }
	        });
	    }
	 
	 public void refresh(){
	     CaratApplication app = (CaratApplication) getApplication();
	     final ListView lv = (ListView) findViewById(R.id.bugsList);
	        lv.setAdapter(new BugsAdapter(app, app.s.getBugReport()));
	 }
	
	/**
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume(){
	    CaratApplication.setBugs(this);
		refresh();
		super.onResume(); 
	}
}
