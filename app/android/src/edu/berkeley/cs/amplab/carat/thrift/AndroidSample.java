package edu.berkeley.cs.amplab.carat.thrift;

import java.io.Serializable;
import java.util.HashMap;

public class  AndroidSample implements Serializable{
    
    HashMap<String, String> androidSampleExtras = new HashMap<String,String>();
    
    public void setCPUTotalTime(long TotalTime){
        androidSampleExtras.put("cpuTotalTime", String.valueOf(TotalTime));
    }
    public String getCPUTotalTime(){
       return androidSampleExtras.get("cpuTotalTime");
    }
    public void setCPUIdleTime(long IdleTime){
        androidSampleExtras.put("cpuIdleTime", String.valueOf(IdleTime));
    }
    public String getCPUIdleTime(){
      return androidSampleExtras.get("cpuIdleTime");
    }
    public void setCPUUsage(long CpuUsage){
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
    public void setBatteryVoltage(int BatteryVoltage){
        androidSampleExtras.put("batteryVoltage", String.valueOf(BatteryVoltage));
    }
    public String getBatteryVoltage(){
        return androidSampleExtras.get("batteryVoltage");
    }
    public void setBatteryTemperature(int BatteryTemperature){
        androidSampleExtras.put("batteryTemperature", String.valueOf(BatteryTemperature));
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
    public void setWifiSignalStrength(int WifiSignalStrength){
        androidSampleExtras.put("wifiSignalStrength", String.valueOf(WifiSignalStrength));
    }
    public String getWifiSignalStrength(){
        return androidSampleExtras.get("wifiSignalStrength");
    }
    public void setWifiLinkSpeed(int WifiLinkSpeed){
        androidSampleExtras.put("wifiLinkSpeed", String.valueOf(WifiLinkSpeed));
    }
    public String getWifiLinkSpeed(){
        return androidSampleExtras.get("wifiLinkSpeed");
    }
}