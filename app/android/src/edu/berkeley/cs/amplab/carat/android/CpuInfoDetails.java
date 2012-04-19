package edu.berkeley.cs.amplab.carat.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CpuInfoDetails {

	public long getTotalCpuTime() throws IOException{
		long totalCpuTime;
		File file = new File("/proc/stat");   
		FileInputStream in= new FileInputStream(file);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));   
	    String str = br.readLine(); 
	    String[] cpuTotal = str.split(" ");
		 br.close();
        totalCpuTime= Long.parseLong(cpuTotal[2])+Long.parseLong(cpuTotal[3])+Long.parseLong(cpuTotal[4])+ Long.parseLong(cpuTotal[6])+Long.parseLong(cpuTotal[7])+Long.parseLong(cpuTotal[8]);
	      return totalCpuTime;
	}
	 
	public long getTotalIdleTime() throws IOException{
		long totalIdleTime;
		File file = new File("/proc/stat");   
		FileInputStream in= new FileInputStream(file);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));   
	    String str = br.readLine(); 
	    String[] idleTotal = str.split(" ");
	    br.close();
	     totalIdleTime=Long.parseLong(idleTotal[5]);
	     
		return totalIdleTime;
	}
	
	public long getTotalCpuUsage(){
		String[] cpuUsage;
		long totalCpuUsage;
		try { 
			File file = new File("/proc/stat");   
			FileInputStream in= new FileInputStream(file);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));   
		    String str = br.readLine(); 
		    cpuUsage = str.split(" ");
		    br.close();
		   
		    long idle1 =Long.parseLong(cpuUsage[5]);
		    long cpu1 = getTotalCpuTime();
		   
		    try {
		    	Thread.sleep(300);
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
		    	e.printStackTrace();
		    }
		
			File file2 = new File("/proc/stat");   
			FileInputStream in2= new FileInputStream(file2);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));   
			str = br2.readLine(); 
			cpuUsage = str.split(" ");
			br2.close();

			long idle2 = Long.parseLong(cpuUsage[5]);
			long cpu2 = getTotalCpuTime();
		   
			totalCpuUsage=(100*(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1)));
		
			return totalCpuUsage;
		} 
		catch (IOException ex) { ex.printStackTrace();
	    return -1;
		}   		
	}
	

		
}
	
	

