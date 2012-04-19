package edu.berkeley.cs.amplab.carat.android;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;



public class MemoryInfoDetails {
	String tmp;
	BufferedReader br;
	public String getMemoryInfo() {
	
	try {
	File file = new File("/proc/meminfo");   
	FileInputStream in=new FileInputStream(file);
  	br = new BufferedReader(new InputStreamReader(in));  
    
	} catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    try {
		tmp = br.readLine();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    StringBuilder sMemory = new StringBuilder();
    sMemory.append(tmp);
    try {
		tmp=br.readLine();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    sMemory.append("\n").append(tmp).append("\n");
    String result="Memory Status:\n"+sMemory;
    return result;
	}
	
}