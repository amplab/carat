package edu.berkeley.cs.amplab.carat.android.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HogBugSuggestionsAdapter extends BaseAdapter {

	private SimpleHogBug[] indexes = null;
	
	private boolean addFakeItem = false;

	private LayoutInflater mInflater;
	private CaratApplication a = null;
	
	private String FAKE_ITEM = "OsUpgrade";

	public HogBugSuggestionsAdapter(CaratApplication a, SimpleHogBug[] hogs,
	        SimpleHogBug[] bugs) {
		this.a = a;

		ArrayList<SimpleHogBug> temp = new ArrayList<SimpleHogBug>();
		acceptHogsOrBugs(hogs, temp);
		acceptHogsOrBugs(bugs, temp);
		addFeatureActions(temp);

		if (addFakeItem){
		    SimpleHogBug fake = new SimpleHogBug(FAKE_ITEM, Type.BUG);
            fake.setExpectedValue(0.0);
            fake.setExpectedValueWithout(0.0);
            temp.add(fake);
		}
		Collections.sort(temp, new HogsBugsComparator());

		indexes = temp.toArray(new SimpleHogBug[temp.size()]);

		mInflater = LayoutInflater.from(a.getApplicationContext());
	}

	private void acceptHogsOrBugs(SimpleHogBug[] input, ArrayList<SimpleHogBug> result) {
		if (input == null)
			return;
		for (SimpleHogBug item : input) {
			double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
					/ item.getExpectedValue();
			// TODO other filter conditions?
			// Limit max number of items?
			// Skip system apps
			String appName = item.getAppName();
			if (appName == null) appName = "unknown";
			
			if (SamplingLibrary.isHidden(a.getApplicationContext(), appName))
			    continue;
			if (appName.equals(CaratApplication.CARAT_PACKAGE) || appName.equals(CaratApplication.CARAT_OLD))
			    continue;
			if (addFakeItem && appName.equals(FAKE_ITEM))
			    result.add(item);
			// Filter out if benefit is too small
			if (SamplingLibrary.isRunning(a.getApplicationContext(), appName) && benefit > 60) {
				result.add(item);
			}
		}
	}
	
	private void addFeatureActions(ArrayList<SimpleHogBug> results){
	    acceptDimScreen(results);
        acceptDisableWifi(results);
        acceptDisableLocSev(results);
        acceptDisableBluetooth(results);
        acceptDisableHapticFb(results);
        acceptSetAutoBrightness(results);
        acceptDisableNetwork(results);
        acceptDisableVibration(results);
        acceptSetScreenTimeout(results);
        acceptDisableAutoSync(results);
	}

	private void acceptDimScreen(ArrayList<SimpleHogBug> result) {
	    /*set the screen threshold to be 50 */
	    if(!SamplingLibrary.isAutoBrightness(a.getApplicationContext()) && SamplingLibrary.getScreenBrightness(a.getApplicationContext())>50){
	 	        
	        SimpleHogBug item=new SimpleHogBug("Dim the Screen", Type.OS);
	     // TODO Get expected benefit
	       // PowerProfile powerCalculate= new PowerProfile(a.getApplicationContext());
	       // powerCalculate.getAveragePower(arg0);
	        /*item.setExpectedValueWithout(1);
	        item.setExpectedValue(4);
	        double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
	                    / item.getExpectedValue();
	        if(benefit > 60)*/
	        result.add(item);
	    }   
	}
	
	private void acceptDisableWifi(ArrayList<SimpleHogBug> result) {
        if(SamplingLibrary.getWifiEnabled(a.getApplicationContext())){
            SimpleHogBug item=new SimpleHogBug("Disable Wifi", Type.OS);
            // TODO Get expected benefit
            /*item.setExpectedValueWithout(1);
            item.setExpectedValue(4);
            double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
                        / item.getExpectedValue();
            if(benefit > 60)*/
            result.add(item);
        }   
    }
	
	private void acceptDisableLocSev(ArrayList<SimpleHogBug> result) {
	    List<String> providers = SamplingLibrary.getEnabledLocationProviders(a.getApplicationContext());
	    if (providers != null && providers.size() > 1){
	        // Always has 1 provider
	        SimpleHogBug item=new SimpleHogBug("Disable location services", Type.OS);
	        result.add(item);
	    }
	        
	}
	
	private void acceptDisableGps(ArrayList<SimpleHogBug> result) {
        if(SamplingLibrary.getGpsEnabled(a.getApplicationContext())==true){
            SimpleHogBug item=new SimpleHogBug("Disable gps", Type.OS);
            // TODO Get expected benefit
            /*item.setExpectedValueWithout(1);
            item.setExpectedValue(4);
            double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
                        / item.getExpectedValue();
            if(benefit > 60)*/
            result.add(item);
        }   
    }
	
	private void acceptDisableBluetooth(ArrayList<SimpleHogBug> result) {
	    BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
        if (myBluetoothAdapter.isEnabled()==true) {               
            SimpleHogBug item=new SimpleHogBug("Disable bluetooth", Type.OS);
            // TODO Get expected benefit
            /*item.setExpectedValueWithout(1);
            item.setExpectedValue(4);
            double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
                        / item.getExpectedValue();
            if(benefit > 60)*/
            result.add(item);
        }   
    }
	
	private void acceptDisableHapticFb(ArrayList<SimpleHogBug> result) {
	    try {
            if(Settings.System.getInt(
                    a.getApplicationContext().getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED)== 1){               
                SimpleHogBug item=new SimpleHogBug("Disable haptic feedback", Type.OS);
                // TODO Get expected benefit
                /*item.setExpectedValueWithout(1);
                item.setExpectedValue(4);
                double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
                            / item.getExpectedValue();
                if(benefit > 60)*/
                result.add(item);
            }
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
    }
	
	   private void acceptSetAutoBrightness(ArrayList<SimpleHogBug> result) {
	        if(SamplingLibrary.isAutoBrightness(a.getApplicationContext())){
	            SimpleHogBug item=new SimpleHogBug("Set brightness to automatic", Type.OS);
	            // TODO Get expected benefit
	            /*item.setExpectedValueWithout(1);
	            item.setExpectedValue(4);
	            double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
	                        / item.getExpectedValue();
	            if(benefit > 60)*/
	            result.add(item);
	        }   
	    }
	
	   private void acceptDisableNetwork(ArrayList<SimpleHogBug> result) {
	        if(SamplingLibrary.networkAvailable(a.getApplicationContext())==true){
	            SimpleHogBug item=new SimpleHogBug("Disable network", Type.OS);
	            // TODO Get expected benefit
	            /*item.setExpectedValueWithout(1);
	            item.setExpectedValue(4);
	            double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
	                        / item.getExpectedValue();
	            if(benefit > 60)*/
	            result.add(item);
	        }   
	    }
	   
	   private void acceptDisableVibration(ArrayList<SimpleHogBug> result) {
	       AudioManager myAudioManager = (AudioManager) a.getApplicationContext()
	                .getSystemService(Context.AUDIO_SERVICE);
	       if(myAudioManager.getVibrateSetting(1)==1||myAudioManager.getVibrateSetting(0)==1){
               SimpleHogBug item=new SimpleHogBug("Disable vibration", Type.OS);
               // TODO Get expected benefit
               /*item.setExpectedValueWithout(1);
               item.setExpectedValue(4);
               double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
                           / item.getExpectedValue();
               if(benefit > 60)*/
               result.add(item);
           }   
       }
	   
       private void acceptSetScreenTimeout(ArrayList<SimpleHogBug> result) {
           
           try {
               if(Settings.System.getInt(
                       a.getApplicationContext().getContentResolver(),
                       Settings.System.SCREEN_OFF_TIMEOUT)>30000){               
                   SimpleHogBug item=new SimpleHogBug("Shorten screen timeout", Type.OS);
                   // TODO Get expected benefit
                   /*item.setExpectedValueWithout(1);
                   item.setExpectedValue(4);
                   double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
                               / item.getExpectedValue();
                   if(benefit > 60)*/
                   result.add(item);
               }
           } catch (SettingNotFoundException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
       } 
       
       private void acceptDisableAutoSync(ArrayList<SimpleHogBug> result) {
           
               if(ContentResolver.getMasterSyncAutomatically()==true){               
                   SimpleHogBug item=new SimpleHogBug("Disable automatic sync", Type.OS);
                   // TODO Get expected benefit
                   /*item.setExpectedValueWithout(1);
                   item.setExpectedValue(4);
                   double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
                               / item.getExpectedValue();
                   if(benefit > 60)*/
                   result.add(item);
               }
           } 

	public int getCount() {
		return indexes.length;
	}

	public Object getItem(int position) {
	    if (position >= 0 && position < indexes.length)
	        return indexes[position];
	    else
	        return null;
	}

	public long getItemId(int position) {
		return position;
	}

	protected int getId() {
		return R.layout.suggestion;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.suggestion, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView
					.findViewById(R.id.suggestion_app_icon);
			holder.txtName = (TextView) convertView
					.findViewById(R.id.actionName);
			holder.txtType = (TextView) convertView
					.findViewById(R.id.suggestion_type);
			holder.txtBenefit = (TextView) convertView
					.findViewById(R.id.expectedBenefit);
			holder.moreInfo = (ImageView) convertView
					.findViewById(R.id.moreinfo);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		SimpleHogBug item = indexes[position];
		if (item == null)
		    return convertView;
		
		Drawable icon = CaratApplication.iconForApp(a.getApplicationContext(), item.getAppName());

		if (item.getAppName().equals(FAKE_ITEM)){
            holder.txtName.setText("OS Upgrade");
            // TODO: Include process type=priority in Sample?
            holder.txtType.setText("information");
            holder.txtBenefit.setText("Unknown");
        } else {
            double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
                    / item.getExpectedValue();

            int min = (int) (benefit / 60);
            int hours = (int) (min / 60);
            min -= hours * 60;
            
            String label = CaratApplication.labelForApp(a.getApplicationContext(), item.getAppName());
            if (label == null)
                label = "Unknown";
            
            holder.icon.setImageDrawable(icon);
            Type type = item.getType();
            if (type == Type.BUG)
                holder.txtName.setText("Restart "+label);
            else if (type == Type.HOG)
                holder.txtName.setText("Kill "+label);
            else{ // Other action
                holder.txtName.setText(label);
                holder.txtType.setText(item.getAppPriority());
            }
            
            holder.txtBenefit.setText(hours + "h " + min + "m");

            // holder.moreInfo...
            }
       // }
        return convertView;
    }

	static class ViewHolder {
		ImageView icon;
		TextView txtName;
		TextView txtType;
		TextView txtBenefit;
		ImageView moreInfo;
	}
}

class HogsBugsComparator implements Comparator<SimpleHogBug> {

	@Override
	public int compare(SimpleHogBug lhs, SimpleHogBug rhs) {
		double benefitL = 100.0 / lhs.getExpectedValueWithout() - 100.0
				/ lhs.getExpectedValue();
		double benefitR = 100.0 / rhs.getExpectedValueWithout() - 100.0
				/ rhs.getExpectedValue();
		if (benefitL > benefitR)
			return -1;
		else if (benefitL < benefitR)
			return 1;
		return 0;
	}
}
