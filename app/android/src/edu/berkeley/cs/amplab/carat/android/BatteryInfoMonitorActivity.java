package edu.berkeley.cs.amplab.carat.android;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.thrift.TException;

import edu.berkeley.cs.amplab.carat.android.storage.CaratDataStorage;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.view.KeyEvent;
import android.widget.TextView;

public class BatteryInfoMonitorActivity extends Activity {
   
  /** Called when the activity is first created. */

	TextView  batteryResult;
	TextView  CpuResult;
	TextView  MemoryResult;
	TextView  RunProcResult;
	private int  year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int second;
	long totalCpuTime;
	long totalIdleTime; 
	long totalCpuUsage; 
	/*Running process*/
	List<RunningAppProcessInfo> runningProcess;


	/*Memory Info*/
	String MemoryTotalInfo;

	/*battery Variable*/
	double Batterylevel = 0;
	String Batterystatus = null;
	String Batteryhealth= null;
	StringBuilder sbattery = new StringBuilder();
	
	/*Storage*/
	//Thread myStorageThread;
	Sample mySample = new Sample();
	
	@Override
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample);
        
	TextView uuid = (TextView) findViewById(R.id.uuid);
	mySample.setUuId(SamplingLibrary.getUuid(getApplicationContext()));
	uuid.setText("UUID: " + mySample.getUuId());

    CpuResult =(TextView)findViewById(R.id.CpuResult);
    MemoryResult = (TextView)findViewById(R.id.MemoryResult);
    batteryResult= (TextView)findViewById(R.id.Result);
    RunProcResult =(TextView)findViewById(R.id.RuningProcResult); 
        
 //   new Thread() {
	//	public void run() {
    BatteryInfoMonitorActivity.this.runOnUiThread(new Runnable(){
		public void run() {
        		IntentFilter tIntentFilter = new IntentFilter();
        		tIntentFilter.addAction(Intent.ACTION_TIME_TICK);
       			tIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
           
        		IntentFilter bIntentFilter = new IntentFilter();
        		bIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        		bIntentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        		bIntentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        		
        		registerReceiver(memoryBroadcastRecv, tIntentFilter);
        		registerReceiver(batteryBroadcastRecv, bIntentFilter);
		}
	});//	}
//	}.start();
	
	// Allow swipe to change tabs
	findViewById(R.id.sampleScroll).setOnTouchListener(SwipeListener.instance);

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0){
		    System.exit(0);
		}
		    return true;
		}

	
	protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryBroadcastRecv);
        unregisterReceiver(memoryBroadcastRecv);
        System.exit(0);
    }
 
	private BroadcastReceiver memoryBroadcastRecv = 
			new BroadcastReceiver() {  
			 @Override  
			 public void onReceive(Context context, Intent intent) { 
				 
				final String ac=intent.getAction();
			  if (ac.equals(Intent.ACTION_TIME_TICK)||ac.equals(Intent.ACTION_TIMEZONE_CHANGED)) {  
					/* The tick interval is ONE MINUTE only and can not be changed. */ 	  
				 MemoryTotalInfo = SamplingLibrary.getMemoryInfo();
				 
					
				 try {
					totalCpuTime = SamplingLibrary.getTotalCpuTime();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 try {
					totalIdleTime = SamplingLibrary.getTotalIdleTime();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					totalCpuUsage = SamplingLibrary.getTotalCpuUsage();

				 runningProcess = SamplingLibrary.getRunningProcessInfo(context);
				 

				 BatteryInfoMonitorActivity.this.runOnUiThread(new Runnable(){
						
					 public void run() {
								String tmp1 = "CPU Use Details:  \n" +"Total cpu usage time:" +String.valueOf(totalCpuTime) + 
				 				" Sec\n" +"Total cpu Idle time:"+ String.valueOf(totalIdleTime) + " Sec\nTotal cpu usage:" +
				 				String.valueOf(totalCpuUsage)+"%\n" + SamplingLibrary.getUptime();	
				 				CpuResult.setText(tmp1);

								MemoryResult.setText(MemoryTotalInfo);
								
							    //TotalRunProcNum.setText("Current total number of running processes:" + runningProcess.size());
								
								List<HashMap<String,String>> runningAppProcInfoList = new ArrayList<HashMap<String,String>>();
								ActivityManager pActivityManager = (ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE);	  
								for (Iterator<RunningAppProcessInfo> iterator = runningProcess.iterator(); iterator.hasNext();) {
						           
									RunningAppProcessInfo RunningProcList = iterator.next();
						            HashMap<String, String> map = new HashMap<String, String>();
						            map.put("processPID","The process PID is:"+String.valueOf(RunningProcList.pid)+"\n");
						            map.put("userid", "The user ID is:"+String.valueOf(RunningProcList.uid)+"\n");
						            map.put("processName", "The process name is:"+RunningProcList.processName+"\n");
						            map.put("importance", "The importance level is:"+String.valueOf(RunningProcList.importance)+"\n");
						            map.put("lru", "The lru is:"+String.valueOf(RunningProcList.lru)+"\n");
						            int[] procMem = new int[] { RunningProcList.pid };
									Debug.MemoryInfo[] memoryInfo = pActivityManager.getProcessMemoryInfo(procMem);  
									int memory = memoryInfo[0].dalvikPrivateDirty;
									map.put("memory", "The occupied memory size is:"+String.valueOf(memory+"\n"));
									runningAppProcInfoList.add(map);
									String ProcList=runningAppProcInfoList.toString();
									RunProcResult.setText("Running Process Details:\n"+ ProcList);
						        }								
						    }
					});


			/*new Thread() {
							public void run() {
									mySample.setBatteryLevel(Batterylevel); 
									mySample.setBatteryState(Batterystatus); 
									mySample.setMemoryFree(Integer.parseInt(SamplingLibrary.getMemoryFree()));
									CaratApplication app = (CaratApplication) getApplication();
									app.s.writeObject(mySample, CaratDataStorage.SAMPLE_FILE);
									try {
										app.c.uploadSample(mySample);
									} catch (TException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									}

					}.start();*/
			  }
			 }
			 };
					
	
	private BroadcastReceiver batteryBroadcastRecv= 
			new BroadcastReceiver(){
		
		public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				
			final String ac = intent.getAction();
			
			if(ac.equals(Intent.ACTION_BATTERY_CHANGED)) {
					
				double initiallevel = intent.getIntExtra("level", 0);
				int health= intent.getIntExtra("health", 0);
				double scale = intent.getIntExtra("scale", 100);
				int status = intent.getIntExtra("status", 0);
				int voltage= intent.getIntExtra("voltage", 0);
				int temperature = intent.getIntExtra("temperature", 0);
				int plugged = intent.getIntExtra("plugged", 0);

					
					if (initiallevel>0 && scale >0){
						Batterylevel=(initiallevel*100/scale);
					}
					
					sbattery.append("The battery level is:").append(Batterylevel).append("%");
					
					
						switch(health){
					
						case BatteryManager.BATTERY_HEALTH_DEAD:
							Batteryhealth="Dead";
							break;
						case BatteryManager.BATTERY_HEALTH_GOOD:
							Batteryhealth="Good";
							break;
						case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
							Batteryhealth="Over voltage";
							break;
						case BatteryManager.BATTERY_HEALTH_OVERHEAT:
							Batteryhealth="Overheat";
							break;
						case BatteryManager.BATTERY_HEALTH_UNKNOWN:
							Batteryhealth="Unknown";
							break;
						case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
							Batteryhealth="Unspecified failure";
							break;
						}
					
					sbattery.append("\nThe Battery health is:").append(Batteryhealth);
					sbattery.append("\nThe battery scale is:").append(scale);
					
						switch(status){
					
						case BatteryManager.BATTERY_STATUS_CHARGING:
							Batterystatus="Charging";
							break;
						case BatteryManager.BATTERY_STATUS_DISCHARGING:
							Batterystatus="Discharging";
							break;
						case BatteryManager.BATTERY_STATUS_FULL:
							Batterystatus="Full";
							break;
						case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
							Batterystatus="Not charging";
							break;
						case BatteryManager.BATTERY_STATUS_UNKNOWN:
							Batterystatus="Unknown";
							break;
						}
					
					sbattery.append("\nThe battery status is:").append(Batterystatus);
					sbattery.append("\nThe battery voltage is:").append(voltage);
					sbattery.append("\nThe battery temperature is:").append(temperature);
					
					String Batteryplugged = null;
						switch(plugged){
					
						case BatteryManager.BATTERY_PLUGGED_AC:
							Batteryplugged="Plugged AC";
							break;
						case BatteryManager.BATTERY_PLUGGED_USB:
							Batteryplugged="Plugged USB";
							break;
						}
						
					sbattery.append("\nThe Battery plugged is:").append(Batteryplugged);
					
					
					Calendar date=Calendar.getInstance();
					
					year=date.get(Calendar.YEAR);
					month = date.get(Calendar.MONTH);
					day = date.get(Calendar.DAY_OF_MONTH);
					hour = date.get(Calendar.HOUR_OF_DAY);
					minute = date.get(Calendar.MINUTE);
				    second = date.get(Calendar.SECOND);
				   
				    BatteryInfoMonitorActivity.this.runOnUiThread(new Runnable(){
						public void run() {
								String tmp = "Last Updated Time:"+Integer.toString(year)+"."+Integer.toString(month)+"."+
								Integer.toString(day)+","+Integer.toString(hour)+":"+Integer.toString(minute)+ ":" +
								Integer.toString(second)+ "\n"+ sbattery + "\n";
								batteryResult.setText(tmp);
						}
					});
				    	/*new Thread() {
							public void run() {
									mySample.setBatteryLevel(Batterylevel); 
									mySample.setBatteryState(Batterystatus); 
									mySample.setMemoryFree(Integer.parseInt(SamplingLibrary.getMemoryFree()));
									CaratApplication app = (CaratApplication) getApplication();
									app.s.writeObject(mySample, CaratDataStorage.SAMPLE_FILE);
									try {
										app.c.uploadSample(mySample);
									} catch (TException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									};

					}.start();*/
		}		
	  }	
	};
	}

	
	

	
