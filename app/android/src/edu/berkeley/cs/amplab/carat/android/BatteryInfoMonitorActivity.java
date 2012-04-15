package edu.berkeley.cs.amplab.carat.android;


import java.io.IOException;
import java.util.Calendar;
import android.app.Activity;
//import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class BatteryInfoMonitorActivity extends Activity {
   
  /** Called when the activity is first created. */

	Button batteryInfoDetails;
	TextView  batteryResult;
	TextView  CpuResult;
	TextView  MemoryResult;
	private int  year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int second;
	private long ut;
	long totalCpuTime = 0;
	long totalIdleTime=0;
	long totalCpuUsage=0;
	long myProcessCpuUsage=0;
	
	// used for Carat data Storage?
	//private Application app = getApplication();
	@Override
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample);
     
        CpuResult =(TextView)findViewById(R.id.CpuResult);
        MemoryResult = (TextView)findViewById(R.id.MemoryResult);
        batteryResult= (TextView)findViewById(R.id.Result); 
        
        IntentFilter tIntentFilter = new IntentFilter();
        tIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        tIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        tIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
           
        IntentFilter bIntentFilter = new IntentFilter();
        bIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        bIntentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        bIntentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        bIntentFilter.addAction(Intent.ACTION_TIME_TICK);
            
        registerReceiver(memoryBroadcastRecv, tIntentFilter);
        registerReceiver(batteryBroadcastRecv, bIntentFilter);
            
	
       /* batteryInfoDetails.setOnClickListener(new OnClickListener(){	
		@Override
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
		   registerReceiver(batteryBroadcastRecv, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		   batteryRecver();
		}
		});*/
	} 
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0){
		    System.exit(0);
		}
		    return true;
		}
	
	protected void onResume() {
        super.onResume();
        cpuInfo();
        memoryInfo();
    }
	
	protected void onRestart() {
        super.onRestart();
        cpuInfo();
        memoryInfo();
    }
	
	protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryBroadcastRecv);
        unregisterReceiver(memoryBroadcastRecv);
        System.exit(0);
    }
	
	protected long getUptime(){
		long uptime=SystemClock.elapsedRealtime();
		return uptime;
		
	}
	
	private BroadcastReceiver memoryBroadcastRecv = 
			new BroadcastReceiver() {  
			 @Override  
			 public void onReceive(Context context, Intent intent) { 
				 
				 final String ac=intent.getAction();
				 
			  if (ac.equals(Intent.ACTION_TIME_TICK)||ac.equals(Intent.ACTION_TIMEZONE_CHANGED)||ac.equals(Intent.ACTION_SCREEN_ON)) {  
					/* The tick interval is ONE MINUTE only and can not be changed. */
				  memoryInfo();
				  cpuInfo();
			  }  
			 }  
			};  
	
	private void memoryInfo(){		
		MemoryInfoDetails memory = new MemoryInfoDetails();
		String tmp = memory.getMemoryInfo();
		MemoryResult.setText(tmp);
	}

	private void cpuInfo(){
		
		CpuInfoDetails cpuInfoUsage = new CpuInfoDetails();
		   
		try {
			totalCpuTime =  cpuInfoUsage.getTotalCpuTime();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		try {
			totalIdleTime = cpuInfoUsage.getTotalIdleTime();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		totalCpuUsage =cpuInfoUsage.getTotalCpuUsage();
	   
		 ut= getUptime();
		 int seconds = (int)(ut /1000) % 60;
		 int minutes = (int)(ut /(1000*60) % 60);
		 int hours   = (int)(ut /(1000*60*60) % 24);
	 
		String tmp="CPU Use Details:  \n" +"Total cpu usage time:" +String.valueOf(totalCpuTime) + "\n" +"Total cpu Idle time:"+ 
		String.valueOf(totalIdleTime) +"\nTotal cpu usage:" +String.valueOf(totalCpuUsage) +"%\n" + "The uptime is :" + hours +"hr:"+minutes+"mins:"+seconds+"sec.\n";
 	    CpuResult.setText(tmp);	

	}
	
	private BroadcastReceiver batteryBroadcastRecv= 
			new BroadcastReceiver(){
		
		public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				
			String ac = intent.getAction();
			
			if(ac.equals(Intent.ACTION_BATTERY_CHANGED)||ac.equals(Intent.ACTION_TIME_TICK)) {
					
				StringBuilder sbattery = new StringBuilder();
					
				int initiallevel = intent.getIntExtra("level", 0);
				int health= intent.getIntExtra("health", 0);
				int scale = intent.getIntExtra("scale", 100);
				int status = intent.getIntExtra("status", 0);
				int voltage= intent.getIntExtra("voltage", 0);
				int temperature = intent.getIntExtra("temperature", 0);
				int plugged = intent.getIntExtra("plugged", 0);
				int level = 0;
					
					if (initiallevel>0 && scale >0){
						level=(initiallevel*100/scale);
						}
					
					sbattery.append("The battery level is:").append(level);
					
					String Batteryhealth = null;
					
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
					
					String Batterystatus = null;
					
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
				   
				    String tmp = "Current Time:"+Integer.toString(year)+"."+Integer.toString(month)+"."+
				    Integer.toString(day)+","+Integer.toString(hour)+":"+Integer.toString(minute)+ ":" +
				    Integer.toString(second)+ "\n"+ sbattery + "\n";
					batteryResult.setText(tmp);
				}
				
	  }	
	};	
		}

	

	