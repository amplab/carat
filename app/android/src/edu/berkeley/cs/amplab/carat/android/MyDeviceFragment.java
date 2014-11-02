package edu.berkeley.cs.amplab.carat.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.subscreens.AppDetailsFragment;
import edu.berkeley.cs.amplab.carat.android.subscreens.ProcessListFragment;
import edu.berkeley.cs.amplab.carat.android.ui.DrawView;

/**
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class MyDeviceFragment extends Fragment {

	AppDetailsFragment detailsFragment;
	
    // TODO: FIXME: These should be gone.
    int viewIndex = 0;
    int baseViewIndex = 0;
    ViewFlipper vf = null;
    
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
        vf = new ViewFlipper(getActivity());
        View root = inflater.inflate(R.layout.mydevice, container, false);

        setModelAndVersion(root);

//        if (savedInstanceState != null){
//        Object o = savedInstanceState.get("savedInstance");
//        if (o != null) {
//            MyDeviceFragment previous = (MyDeviceFragment) o;
//            if (previous.osViewPage != null
//                    && previous.osViewPage == previous.vf.getChildAt(viewIndex)) {
//                DrawView v = previous.osView;
//                View[] viewAndPage = construct();
//                osView = (DrawView) viewAndPage[0];
//                osViewPage = viewAndPage[1];
//                Type t = v.getType();
//                String appName = v.getAppName();
//                osView.setParams(t, appName, 
//                        v.getEv(), v.getEvWithout(), v.getSampleCount(), v.getSampleCountWithout(), v.getError(), v.getErrorWithout(), osViewPage);
//                restorePage(osViewPage, previous.osViewPage);
//                viewIndex = vf.indexOfChild(osViewPage);
//            }else if (previous.modelViewPage != null
//                    && previous.modelViewPage == previous.vf.getChildAt(viewIndex)) {
//                View[] viewAndPage = construct();
//                modelView = (DrawView) viewAndPage[0];
//                modelViewPage = viewAndPage[1];
//                DrawView v = previous.modelView;
//                Type t = v.getType();
//                String appName = v.getAppName();
//                modelView.setParams(t, appName, 
//                        v.getEv(), v.getEvWithout(), v.getSampleCount(), v.getSampleCountWithout(), v.getError(), v.getErrorWithout(), modelViewPage);
//                restorePage(modelViewPage, previous.modelViewPage);
//                viewIndex = vf.indexOfChild(modelViewPage);
//            }
//        }
//        }
        
        if (viewIndex == 0)
            vf.setDisplayedChild(baseViewIndex);
        else
            vf.setDisplayedChild(viewIndex);
        
        root.findViewById(R.id.jscore_value).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CaratApplication.showHTMLFile("jscoreinfo");
			}
		});
        root.findViewById(R.id.jscore).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CaratApplication.showHTMLFile("jscoreinfo");
			}
		});
        
        root.findViewById(R.id.batterylife_value).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CaratApplication.showHTMLFile("batterylifeinfo");
			}
		});
        root.findViewById(R.id.batterylife).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CaratApplication.showHTMLFile("batterylifeinfo");
			}
		});
        
        root.findViewById(R.id.memoryInfo).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CaratApplication.showHTMLFile("memoryinfo");
			}
		});
        root.findViewById(R.id.progressBar1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CaratApplication.showHTMLFile("memoryinfo");
			}
		});
        root.findViewById(R.id.progressBar2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CaratApplication.showHTMLFile("memoryinfo");
			}
		});
        
        // shows the list of currently running processes.
        // called when the "View Process List" button is clicked/tapped.
        root.findViewById(R.id.viewProcessButton).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	ProcessListFragment fragment = ProcessListFragment.getInstance(); 
            	CaratApplication.replaceFragment(fragment, "ProcessList");
            }            
        });
        
        // called when the "more OS info" arrow (image) is clicked/tapped
        root.findViewById(R.id.osInfo).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	showOsInfo();
            }            
        });
        
        // called when the OS version is clicked/tapped
        root.findViewById(R.id.os_ver_value).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	showOsInfo();
            }            
        });
        
        // called when the "more device info" arrow (image) is clicked/tapped
        root.findViewById(R.id.devInfo).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	showDeviceInfo();
            }            
        });
        
        // called when the device model is clicked/tapped
        root.findViewById(R.id.dev_value).setOnClickListener(new View.OnClickListener(){
        	@Override
            public void onClick(View v) {
            	showDeviceInfo();
            }            
        });
        
        return root;
    }

    private void restorePage(View thisPage, View oldPage){
        TextView pn = (TextView) oldPage.findViewById(R.id.name);
        ImageView pi = (ImageView) oldPage.findViewById(R.id.appIcon);
        TextView pp = (TextView) oldPage.findViewById(R.id.benefit);
        
        ((TextView) thisPage.findViewById(R.id.name)).setText(pn.getText());
        ((ImageView) thisPage.findViewById(R.id.appIcon))
                .setImageDrawable(pi.getDrawable());
        ((TextView) thisPage.findViewById(R.id.benefit))
                .setText(pp.getText());
    }

	long[] lastPoint = null;

    /**
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
        CaratApplication.setMyDevice(this);
        CaratApplication.setReportData();
        /*UiRefreshThread.setReportData();
        new Thread() {
            public void run() {
                synchronized (UiRefreshThread.getInstance()) {
                    UiRefreshThread.getInstance().appResumed();
                }
            }
        }.start();*/

        setMemory();
        super.onResume();
    }
    
    @Override
    public void onDetach() {
    	CaratApplication.setMyDevice(null);
        super.onDetach();
    }
    
    private View[] construct() {
        View[] result = new View[2];
        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View detailPage = inflater.inflate(R.layout.graph, null);
        //  ViewGroup g = (ViewGroup) detailPage;
        DrawView w = new DrawView(getActivity());
        //g.addView(w);
        
//        vf.addView(detailPage);

        /*g.setOnTouchListener(new FlipperBackListener(this, vf, baseViewIndex,
                false));*/
        result[0] = w;
        result[1] = detailPage;

        return result;
    }
    
    /**
     * Called when OS additional info button is clicked.
     */
    public void showOsInfo() {
    	detailsFragment = AppDetailsFragment.getInstance(Type.OS, null, false); 
    	CaratApplication.replaceFragment(detailsFragment, "OsDetails");
    }

    /**
     * Called when Device additional info button is clicked.
     */
    public void showDeviceInfo() {
    	detailsFragment = AppDetailsFragment.getInstance(Type.MODEL, null, false); 
    	CaratApplication.replaceFragment(detailsFragment, "DeviceDetails");
    }

    private void setModelAndVersion(View root) {
        // Device model
        String model = SamplingLibrary.getModel();

        // Android version
        String version = SamplingLibrary.getOsVersion();

        // The info icon needs to change from dark to light.
        TextView mText = (TextView) root.findViewById(R.id.dev_value);
        mText.setText(model);
        mText = (TextView) root.findViewById(R.id.os_ver_value);
        mText.setText(version);
    }

    private void setMemory() {
        final Activity a = getActivity();
        // Set memory values to the progress bar.
        ProgressBar mText = (ProgressBar) a.findViewById(R.id.progressBar1);
        int[] totalAndUsed = SamplingLibrary.readMeminfo();
        mText.setMax(totalAndUsed[0] + totalAndUsed[1]);
        mText.setProgress(totalAndUsed[0]);
        mText = (ProgressBar) a.findViewById(R.id.progressBar2);

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
                ProgressBar mText = (ProgressBar) a.findViewById(R.id.cpubar);
                mText.setMax(100);
                mText.setProgress((int) (cpu * 100));
            }
        });
    }
    
    // FIXME: use this method in a listener, but first get the ClipboardManager working
    /**
     * Called when Carat ID is clicked.
     * 
     * @param v
     *            The source of the click.
     */
    public void copyCaratId(View v) {
        TextView tv = (TextView) getView().findViewById(R.id.carat_id_value);
        String copied = tv.getText().toString();
//        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
//        clipboard.setText(copied);
        Toast.makeText(getActivity(), getString(R.string.copied) +" "+copied, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
//    public void showAppInfo(View v) {
//      Reports r = CaratApplication.s.getReports();
//      if (r != null) {
//          DetailScreenReport similar = r.getSimilarApps();
//          DetailScreenReport similarWithout = r.getSimilarAppsWithout();
//
//          String label = getString(R.string.similarapps);
//          Drawable icon = CaratApplication.iconForApp(getActivity(), "Carat");
//          ((TextView) appsViewPage.findViewById(R.id.name)).setText(label);
//          ((ImageView) appsViewPage.findViewById(R.id.appIcon))
//                  .setImageDrawable(icon);
//
//          Log.v("SimilarInfo", "Similar score: " + similar.getScore());
//
//          double ev = similar.getExpectedValue();
//          double error = similar.getError();
//          double evWithout = similarWithout.getExpectedValue();
//          double errorWo = similarWithout.getError();
//          String benefitText = SimpleHogBug.textBenefit(ev, error, evWithout, errorWo);
//          if (benefitText == null)
//              benefitText = getString(R.string.jsna);
//          ((TextView) osViewPage.findViewById(R.id.benefit))
//                  .setText(benefitText);
//          
//          appsView.setParams(Type.SIMILAR, SamplingLibrary.getModel(),
//                  similar.getExpectedValue(), similarWithout.getExpectedValue(), (int) similar.getSamples(), (int) similarWithout.getSamples(), similar.getError(), similarWithout.getError(), appsViewPage);
//      }
//      switchView(appsViewPage);
//  }
    
	//  public void showMemoryInfo() {
	//	showHTMLFile("memoryinfo");
	//}
	
	//public void showBatteryLifeInfo() {
	//	showHTMLFile("batterylifeinfo");
	//}
	//
	//public void viewJscoreInfo() {
	//	showHTMLFile("jscoreinfo");
	//}
}
