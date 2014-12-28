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

public class SettingsSuggestionAdapter extends BaseAdapter {

	private SimpleHogBug[] indexes = null;
	
	private boolean addFakeItem = false;

	private LayoutInflater mInflater;
	private CaratApplication a = null;
	
	private String FAKE_ITEM = "OsUpgrade";
	private String action = "";

	public SettingsSuggestionAdapter(CaratApplication a, SimpleHogBug[] settings) {
		this.a = a;

		ArrayList<SimpleHogBug> result = new ArrayList<SimpleHogBug>();
		acceptSettings(settings, result); // fill in the result array list
		
		addFeatureActions(result);

		if (addFakeItem){
//		    SimpleHogBug fake = new SimpleHogBug(FAKE_ITEM, Type.BUG);
//            fake.setExpectedValue(0.0);
//            fake.setExpectedValueWithout(0.0);
//            temp.add(fake);
		}
//		Collections.sort(result, new HogsBugsComparator());

		indexes = result.toArray(new SimpleHogBug[result.size()]);

		mInflater = LayoutInflater.from(a.getApplicationContext());
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
					.findViewById(R.id.jscore_info);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (indexes == null || position < 0 || position >= indexes.length)
			return convertView;
		
		SimpleHogBug item = indexes[position];
		if (item == null)
		    return convertView;
		
		final String appName = item.getAppName();
		Drawable icon = CaratApplication.iconForApp(a.getApplicationContext(), appName); 
		// TODO: modify the iconForApp() method to accommodate returning correct icon based on appNames for system settings

		if (appName.equals(FAKE_ITEM)){
            holder.txtName.setText(a.getString(R.string.osupgrade));
            holder.txtType.setText(a.getString(R.string.information));
            holder.txtBenefit.setText(a.getString(R.string.unknown));
        } else {
            
            String label = CaratApplication.labelForApp(a.getApplicationContext(), appName);
            // TODO: modify the labelForApp() method to accommodate returning correct label based on appNames for system settings
            if (label == null)
                label = a.getString(R.string.unknown);
            
            holder.icon.setImageDrawable(icon);
            Type type = item.getType();
            
            // TODO: add strings for other types
            
            if (type == Type.BRIGHTNESS)
            	action = "dim screen brightness";
            else if (type == Type.WIFI)
            	action = "turn off wifi";
            else if (type == Type.MOBILEDATA)
            	action = "turn off mobile data";
            
            holder.txtName.setText(action + " " + label);
            
            if (type == Type.OTHER)
                holder.txtType.setText(item.getAppPriority());
            else
            holder.txtType.setText(CaratApplication.translatedPriority(item.getAppPriority()));
            // TODO: check the method translatedPriority() to see if needs to be modified
            
            // TODO: (IMPORTANT) set the expected benefit
            
            /*if (raw.equals(a.getString(R.string.disablebluetooth))){
                double benefitOther=SamplingLibrary.bluetoothBenefit(a.getApplicationContext());
                hours = (int) (benefitOther);
                min= (int) ((benefitOther- hours)*60);
            }
            else if(raw.equals(a.getString(R.string.disablewifi))){
                double benefitOther=SamplingLibrary.wifiBenefit(a.getApplicationContext());
                hours = (int) (benefitOther);
                min= (int) ((benefitOther- hours)*60); 
            }
            else if(raw.equals(a.getString(R.string.dimscreen))){
                double benefitOther=SamplingLibrary.screenBrightnessBenefit(a.getApplicationContext());
                hours = (int) (benefitOther);
                min = (int) ((benefitOther- hours)*60); 
            }*/
            // Do not show a benefit for things that have none.
            if (item.getExpectedValue() == 0 && item.getExpectedValueWithout() == 0){
                holder.txtBenefit.setText("");
                TextView bl = (TextView) convertView
                        .findViewById(R.id.benefitLegend);
                bl.setText("");
            }else
            holder.txtBenefit.setText(item.getBenefitText());

            // TODO: set holder.moreInfos and their click listener ...
            }
       // }
        return convertView;
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
	
	
	private void acceptSettings(SimpleHogBug[] inputArrayList, ArrayList<SimpleHogBug> resultArrayList) {
		if (inputArrayList == null)
			return;
		for (SimpleHogBug item : inputArrayList) {
			if (item == null)
				continue;
			// TODO: can we use the same formula for calculating the expected improvement?
			// if not, embed the formula here or fetch the value from the server
			// double benefit = 100.0 / item.getExpectedValueWithout() - 100.0
			// 		/ item.getExpectedValue();
			
			// TODO other filter conditions?
			// Limit max number of items?
			
			String appName = item.getAppName();
			if (appName == null) 
				appName = a.getString(R.string.unknown);

			
			// TODO: skip (leave out) special cases
			boolean someCondition = true;
			if (someCondition)
			    continue;
			
			// TODO: fill in the list if it's empty (for non focus-group users)
			// if (addFakeItem && appName.equals(FAKE_ITEM))
			//    resultArrayList.add(item);
			
			// Filter out if benefit is too small
			// TODO: disabled this check for the time being, until we have a calculated benefit
			// if (SamplingLibrary.isSettingsSuggestion(a.getApplicationContext(), appName) && benefit > 60 ) {
			resultArrayList.add(item);
			// }
		}
	}
	
	
	private void addFeatureActions(ArrayList<SimpleHogBug> results) {
		// TODO: These need benefits
		acceptDisableLocSev(results);
		acceptDisableHapticFb(results);
		acceptDimScreen(results);
		acceptDisableWifi(results);
		acceptDisableNetwork(results);
		acceptDisableGps(results);
		acceptDisableBluetooth(results);
		acceptDisableVibration(results);
		acceptSetScreenTimeout(results);
		acceptDisableAutoSync(results);
		
		if (results.isEmpty())
			helpCaratCollectMoreData(results);
		String url = CaratApplication.storage.getQuestionnaireUrl();
		boolean questionnaireEnabled = url != null && url.length() > 7; // http://
		if (questionnaireEnabled)
			questionnaire(results);
	}

  
	private void acceptDimScreen(ArrayList<SimpleHogBug> result) {
	    // set the screen threshold to be 50
	    if(!SamplingLibrary.isAutoBrightness(a.getApplicationContext()) && SamplingLibrary.getScreenBrightness(a.getApplicationContext())>50){
	 	        
	        SimpleHogBug item=new SimpleHogBug(a.getString(R.string.dimscreen), Type.OS);
	        result.add(item);
	    }   
	}
	
	private void acceptDisableWifi(ArrayList<SimpleHogBug> result) {
        if(SamplingLibrary.getWifiEnabled(a.getApplicationContext())){
            SimpleHogBug item=new SimpleHogBug(a.getString(R.string.disablewifi), Type.OS);
            result.add(item);
        }   
    }
	
	private void acceptDisableLocSev(ArrayList<SimpleHogBug> result) {
	    List<String> providers = SamplingLibrary.getEnabledLocationProviders(a.getApplicationContext());
	    if (providers != null && providers.size() > 1){
	        // Always has 1 provider
	        SimpleHogBug item=new SimpleHogBug(a.getString(R.string.disablelocation), Type.OS);
	        result.add(item);
	    }
	        
	}
	
	private void acceptDisableGps(ArrayList<SimpleHogBug> result) {
        if(SamplingLibrary.getGpsEnabled(a.getApplicationContext())==true){
            SimpleHogBug item=new SimpleHogBug(a.getString(R.string.disablegps), Type.OS);
            result.add(item);
        }   
    }
	
	private void acceptDisableBluetooth(ArrayList<SimpleHogBug> result) {
	    BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
        if (myBluetoothAdapter.isEnabled()==true) {               
            SimpleHogBug item=new SimpleHogBug(a.getString(R.string.disablebluetooth), Type.OS);
            result.add(item);
        }   
    }
	
	private void acceptDisableHapticFb(ArrayList<SimpleHogBug> result) {
	    try {
            if(Settings.System.getInt(
                    a.getApplicationContext().getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED)== 1){               
                SimpleHogBug item=new SimpleHogBug(a.getString(R.string.disablehapticfeedback), Type.OS);
                // TODO Get expected benefit
                result.add(item);
            }
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
    }
	
	private void acceptDisableNetwork(ArrayList<SimpleHogBug> result) {
		if (SamplingLibrary.networkAvailable(a.getApplicationContext()) == true) {
			SimpleHogBug item = new SimpleHogBug(a.getString(R.string.disablenetwork), Type.OS);
			// TODO Get expected benefit
			result.add(item);
		}
	}

	private void acceptDisableVibration(ArrayList<SimpleHogBug> result) {
		AudioManager myAudioManager = (AudioManager) a.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		if (myAudioManager.getVibrateSetting(1) == 1 || myAudioManager.getVibrateSetting(0) == 1) {
			SimpleHogBug item = new SimpleHogBug(a.getString(R.string.disablevibration), Type.OS);
			// TODO Get expected benefit
			result.add(item);
		}
	}

	private void acceptSetScreenTimeout(ArrayList<SimpleHogBug> result) {

		try {
			if (Settings.System.getInt(a.getApplicationContext().getContentResolver(),
					Settings.System.SCREEN_OFF_TIMEOUT) > 30000) {
				SimpleHogBug item = new SimpleHogBug(a.getString(R.string.shortenscreentimeout), Type.OS);
				// TODO Get expected benefit
				result.add(item);
			}
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void acceptDisableAutoSync(ArrayList<SimpleHogBug> result) {

		if (ContentResolver.getMasterSyncAutomatically() == true) {
			SimpleHogBug item = new SimpleHogBug(a.getString(R.string.disableautomaticsync), Type.OS);
			// TODO Get expected benefit
			result.add(item);
		}
	}

	private void helpCaratCollectMoreData(ArrayList<SimpleHogBug> result) {
		SimpleHogBug item = new SimpleHogBug(a.getString(R.string.helpcarat), Type.OS);
		result.add(item);
	}

	private void questionnaire(ArrayList<SimpleHogBug> result) {
		SimpleHogBug item = new SimpleHogBug(a.getString(R.string.questionnaire), Type.OTHER,
				a.getString(R.string.questionnaire2));
		result.add(item);
	}
	
    static class ViewHolder {
		ImageView icon;
		TextView txtName;
		TextView txtType;
		TextView txtBenefit;
		ImageView moreInfo;
	}
}

// TODO: disabled till we have expected benefit 
//class SettingsComparator implements Comparator<SimpleHogBug> {
//
//	@Override
//	public int compare(SimpleHogBug lhs, SimpleHogBug rhs) {
////		double benefitL = 100.0 / lhs.getExpectedValueWithout() - 100.0
////				/ lhs.getExpectedValue();
////		double benefitR = 100.0 / rhs.getExpectedValueWithout() - 100.0
////				/ rhs.getExpectedValue();
////		if (benefitL > benefitR)
////			return -1;
////		else if (benefitL < benefitR)
////			return 1;
//		return 0;
//	}
//}
