package edu.berkeley.cs.amplab.carat.android.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.Constants;
import edu.berkeley.cs.amplab.carat.android.MainActivity;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.subscreens.AppDetailsFragment;
import edu.berkeley.cs.amplab.carat.android.subscreens.ProcessListFragment;

/**
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class MyDeviceFragment extends ExtendedTitleFragment {

	private final MainActivity mMainActivity = CaratApplication.getMainActivity();
	long[] lastPoint = null;  // related to the CPU usage bar
	AppDetailsFragment detailsFragment;
	// private final String TAG = "mydeviceFragment"; // for logging (debugging)
    
    /**
     * Set up fragment and sub-screens. Transformations from Fragment
     * to another should be handled at a higher level.
     */
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.mydevice, container, false);
        handleClicks(rootView);
        return rootView;
    }	
	    
    @Override
    public void onResume() {
        setTextViews();
        setMemoryBars();
        super.onResume();
    }	
    
    /**
	 * Handle user clicks on different views (TextViews, little arrow ImageViews, 
	 *  and the memory and CPU usage bars) 
	 * @param rootView The current fragment's view (inflated using a layout inflater)
	 */
	private void handleClicks(View rootView) {
		handleJscoreClick(rootView);
        handleBatteryLifeClick(rootView);
        handleCaratIdClick(rootView);
        handleMemoryInfoClick(rootView);
        handleOsClick(rootView);
        handleDeviceClick(rootView);
        handleProcessesButtonClick(rootView);
	}
    
    private void setTextViews() {
    	setModelAndVersion();
    	setJscoreTextView();
    	setLastUpdateTimeTextView();
    	setCaratIdTextView();
    	setBatteryLifeTextView();
    	setCondensedFontForBatteryLife();
	}

    /**
	 * copy Carat ID when Carat ID is tapped
	 * @param rootView the current fragment's view (inflated using a layout inflater)
	 */
	private void handleCaratIdClick(View rootView) {
		rootView.findViewById(R.id.carat_id).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	copyCaratId();
            }            
        });
	}

	/**
	 * Show the list of currently running processes 
	 * when the "view processes" button is clicked
	 * @param rootView the current fragment's view (inflated using a layout inflater)
	 */
	private void handleProcessesButtonClick(View rootView) {
		rootView.findViewById(R.id.view_processes_button).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	ProcessListFragment fragment = ProcessListFragment.getInstance(); 
            	CaratApplication.getMainActivity().replaceFragment(fragment, "ProcessList", false);
            }            
        });
	}

	/**
	 * show battery consumptions statistics of the current OS version
	 *  (average battery consumption [expected value])
	 * @param rootView the current fragment's view (inflated using a layout inflater)
	 */
	private void handleOsClick(View rootView) {
		rootView.findViewById(R.id.os_info).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	showOsInfo();
            }            
        });
        
        rootView.findViewById(R.id.os_version).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	showOsInfo();
            }            
        });
	}

	/**
	 * show device energy stats (average battery lifetime) the views pertaining to the device are tapped
	 * @param rootView the current fragment's view (inflated using a layout inflater)
	 */
	private void handleDeviceClick(View rootView) {
		rootView.findViewById(R.id.device_info).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	showDeviceInfo();
            }            
        });
        
        
        rootView.findViewById(R.id.device).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	showDeviceInfo();
            }            
        });
	}

	/**
	 * display memory explanation info
	 * @param root the current fragment's view (inflated using a layout inflater)
	 */
	private void handleMemoryInfoClick(View root) {
		Subscreen lis = new Subscreen("memoryinfo", getString(R.string.memoryinfo));
		root.findViewById(R.id.memory_info).setOnClickListener(lis);
        root.findViewById(R.id.memory_used_bar).setOnClickListener(lis);
        root.findViewById(R.id.memory_active_bar).setOnClickListener(lis);
	}

	/** 
	 * display battery life explanation info
	 * @param root the current fragment's view (inflated using a layout inflater)
	 */
	private void handleBatteryLifeClick(View root) {
		Subscreen lis = new Subscreen("batterylifeinfo", getString(R.string.batterylifeinfo));
		root.findViewById(R.id.battery_life_legend).setOnClickListener(lis);
        root.findViewById(R.id.battery_life).setOnClickListener(lis);
        root.findViewById(R.id.battery_life_info).setOnClickListener(lis);
	}

	/**
	 * display jscore explanation info
	 * @param root the current fragment's view (inflated using a layout inflater)
	 */
	private void handleJscoreClick(View root) {
		Subscreen lis = new Subscreen("jscoreinfo", getString(R.string.jscoreinfo));
		root.findViewById(R.id.jscore_info).setOnClickListener(lis);
        root.findViewById(R.id.jscore).setOnClickListener(lis);
        root.findViewById(R.id.jscore_legend).setOnClickListener(lis);
	}
	
	private class Subscreen implements OnClickListener{
		private String screenName = null;
		private String title = null;
		
		public Subscreen(String screenName, String title) {
			this.screenName = screenName;
			this.title  = title;
		}
		@Override
		public void onClick(View v) {
			mMainActivity.showHTMLFile(screenName, title, false);
		}
	}
    
    private void setJscoreTextView() {
    	TextView jscoreValue = (TextView) getView().findViewById(R.id.jscore);
    	int jscore = CaratApplication.getJscore();
    	
    	if (jscore == -1 || jscore == 0)
    		jscoreValue.setText("N/A");
    	else 
    		jscoreValue.setText(Integer.toString(jscore));
    }
	
	private void setLastUpdateTimeTextView() {
		long lastUpdateTime = CaratApplication.myDeviceData.getLastReportsTimeMillis();
    	long min = CaratApplication.myDeviceData.getFreshnessMinutes();
    	long hour = CaratApplication.myDeviceData.getFreshnessHours();
    	
    	TextView lastUpdateTimeView = (TextView) getView().findViewById(R.id.updated);
    	Activity mainActivity = getActivity();
    	
    	if (lastUpdateTime <= 0)
    		lastUpdateTimeView.setText(mainActivity.getString(R.string.neverupdated));
    	else if (min == 0)
    		lastUpdateTimeView.setText(mainActivity.getString(R.string.updatedjustnow));
    	else 
    		lastUpdateTimeView.setText(mainActivity.getString(R.string.updated) 
    				+ " " + hour + "h " + min + "m " + mainActivity.getString(R.string.ago));
	}

	private void setCaratIdTextView() {
		String caratId = CaratApplication.myDeviceData.getCaratId();
    	TextView caratIdView = (TextView) getView().findViewById(R.id.carat_id);
    	caratIdView.setText(caratId);
	}
    
    private void setBatteryLifeTextView() {
		String batteryLife = CaratApplication.myDeviceData.getBatteryLife();
    	TextView batteryLifeView = (TextView) getView().findViewById(R.id.battery_life);
    	batteryLifeView.setText(batteryLife);
	}
	
    /**
     * show battery consumptions statistics of the current OS (and OS version) (average battery consumption [expected value])
     */
    public void showOsInfo() {
    	detailsFragment = AppDetailsFragment.getInstance(Constants.Type.OS, null, false); 
    	CaratApplication.getMainActivity().replaceFragment(detailsFragment, "OsDetails", false);
    }

    /**
     * show battery consumptions statistics of the current device (average battery consumption [expected value])
     */
    public void showDeviceInfo() {
    	detailsFragment = AppDetailsFragment.getInstance(Constants.Type.MODEL, null, false); 
    	CaratApplication.getMainActivity().replaceFragment(detailsFragment, "DeviceDetails", false);
    }

    /**
     * Set the device model and OS version text views
     * @param root
     */
    private void setModelAndVersion() {
        // Device model
        String model = SamplingLibrary.getModel();
        // Android version
        String version = SamplingLibrary.getOsVersion();

        // The info icon needs to change from dark to light.
        TextView mText = (TextView) getView().findViewById(R.id.device);
        mText.setText(model);
        mText = (TextView) getView().findViewById(R.id.os_version);
        mText.setText(version);
    }

    /**
     * Set memory values to the progress bar
     */
    private void setMemoryBars() {
        final Activity a = getActivity();
        ProgressBar mText = (ProgressBar) a.findViewById(R.id.memory_used_bar);
        int[] totalAndUsed = SamplingLibrary.readMeminfo();
        mText.setMax(totalAndUsed[0] + totalAndUsed[1]);
        mText.setProgress(totalAndUsed[0]);
        mText = (ProgressBar) a.findViewById(R.id.memory_active_bar);

        if (totalAndUsed.length > 2) {
            mText.setMax(totalAndUsed[2] + totalAndUsed[3]);
            mText.setProgress(totalAndUsed[2]);
        }

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                long[] currentPoint = SamplingLibrary.readUsagePoint();
                
                double cpu = 0;
                if (lastPoint == null)
                    lastPoint = currentPoint;
                else
                    cpu = SamplingLibrary.getUsage(lastPoint, currentPoint);
                    
                /* CPU usage */
                ProgressBar mText = (ProgressBar) a.findViewById(R.id.cpu_bar);
                mText.setMax(100);
                mText.setProgress((int) (cpu * 100));
            }
        });
    }
    
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public void copyCaratId() {
    	TextView tv = (TextView) getView().findViewById(R.id.carat_id);
        String caratId = tv.getText().toString();
        
    	int sdk = android.os.Build.VERSION.SDK_INT;
    	// for Android API lower than 11
    	if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
    	    android.text.ClipboardManager clipboard = 
    	    		(android.text.ClipboardManager) getActivity()
    	    		.getSystemService(Context.CLIPBOARD_SERVICE);
    	    clipboard.setText(caratId);
    	} else {
    		// for Android API 11 and above
    	    android.content.ClipboardManager clipboard = 
    	    		(android.content.ClipboardManager) getActivity()
    	    		.getSystemService(Context.CLIPBOARD_SERVICE); 
    	    android.content.ClipData clip = android.content.ClipData
    	    		.newPlainText("Carat ID", caratId);
    	    clipboard.setPrimaryClip(clip);
    	}
    	
        Toast.makeText(getActivity(), getString(R.string.copied) + " " + caratId, 
        		Toast.LENGTH_LONG).show();
    }
    
    private void setCondensedFontForBatteryLife() {
		Typeface face = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Condensed.ttf");
        TextView tv = (TextView) getActivity().findViewById(R.id.battery_life);
        tv.setTypeface(face);
	}
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    } 
}