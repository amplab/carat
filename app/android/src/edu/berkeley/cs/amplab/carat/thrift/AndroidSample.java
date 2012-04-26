package edu.berkeley.cs.amplab.carat.thrift;

import java.io.Serializable;
import java.util.HashMap;

public class  AndroidSample implements Serializable{
    
    HashMap<String, String> androidSampleExtras = new HashMap<String,String>();
    
    public void setCPUTotalTime(double TotalTime){
        androidSampleExtras.put("cpuTotalTime", String.valueOf(TotalTime));
    }
    public String getCPUTotalTime(){
       return androidSampleExtras.get("cpuTotalTime");
    }
    public void setCPUIdleTime(double IdleTime){
        androidSampleExtras.put("cpuIdleTime", String.valueOf(IdleTime));
    }
    public String getCPUIdleTime(){
      return androidSampleExtras.get("cpuIdleTime");
    }
    public void setCPUUsage(double CpuUsage){
        androidSampleExtras.put("cpuUsage", String.valueOf(CpuUsage));
    }
    public String getCPUUsage(){
        return androidSampleExtras.get("cpuUsage");
    }
    public void setMemoryTotalInfo(String MemoryTotalInfo){
        androidSampleExtras.put("memoryTotalInfo", MemoryTotalInfo);
    }
    public String getMemoryTotalInfo(){
        return androidSampleExtras.get("memoryTotalInfo");
    }
    public void setBatteryVoltage(double voltage){
        androidSampleExtras.put("batteryVoltage", String.valueOf(voltage));
    }
    public String getBatteryVoltage(){
        return androidSampleExtras.get("batteryVoltage");
    }
    public void setBatteryTemperature(int BatteryTemperature){
        androidSampleExtras.put("batteryTemperature", String.valueOf

(BatteryTemperature));
    }
    public String getBatteryTemperature(){
        return androidSampleExtras.get("batteryTemperature");
    }
    public void setBatteryStatus(String BatteryStatus){
        androidSampleExtras.put("batteryStatus", BatteryStatus);
    }
    public String getBatteryStatus(){
        return androidSampleExtras.get("batteryStatus");
    }
    public void setBatteryHealth(String BatteryHealth){
        androidSampleExtras.put("batteryHealth", BatteryHealth);
    }
    public String getBatteryHealth(){
        return androidSampleExtras.get("batteryHealth");
    }
    public void setBatteryPlugged(String BatteryPlugged){
        androidSampleExtras.put("batteryPlugged", BatteryPlugged);
    }
    public String getBatteryPlugged(){
        return androidSampleExtras.get("batteryPlugged");
    }
    
    public void setBatteryTechnology(String batteryTechnology){
        androidSampleExtras.put("batteryTechnology", batteryTechnology);
    }
    public String getBatteryTechnology(){
        return androidSampleExtras.get("batteryTechnology");
    }
   
    public void setWifiSignalStrength(int WifiSignalStrength){
        androidSampleExtras.put("wifiSignalStrength", String.valueOf

(WifiSignalStrength));
    }
    public String getWifiSignalStrength(){
        return androidSampleExtras.get("wifiSignalStrength");
    }
    public void setWifiLinkSpeed(int WifiLinkSpeed){
        androidSampleExtras.put("wifiLinkSpeed", String.valueOf

(WifiLinkSpeed));
    }
    public String getWifiLinkSpeed(){
        return androidSampleExtras.get("wifiLinkSpeed");
    }
    public void setWifiState(int WifiState){
        androidSampleExtras.put("wifiLinkSpeed", String.valueOf
(WifiState));
    }
    public String getWifiState(){
        return androidSampleExtras.get("wifiState");
    }
    
}