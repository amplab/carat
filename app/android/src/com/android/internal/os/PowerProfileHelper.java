import android.content.Context;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.android.sampling.PowerProfile;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;


public static class PowerProfileHelper {
    public static PowerProfile powCal = null;
    
    public static double getBatteryCapacity(Context context){        
         if (powCal == null)
             powCal=new PowerProfile(context);
        double batteryCap=powCal.getBatteryCapacity();
        return batteryCap;
    }

  public static double getAverageBluetoothPower(Context context){
      if (powCal == null)
          powCal=new PowerProfile(context);
        
        double bluetoothOnCost=powCal.getAveragePower(powCal.POWER_BLUETOOTH_ON);
        Log.i("bluetoothOnCost", "Bluetooth on cost is:"+bluetoothOnCost); 
        double bluetoothActiveCost=powCal.getAveragePower(powCal.POWER_BLUETOOTH_ACTIVE);
       // double bluetoothAtcmdCost=powCal.getAveragePower(powCal.POWER_BLUETOOTH_AT_CMD);
        Log.i("bluetoothActiveCost", "Bluetooth active cost is:"+bluetoothActiveCost);
        double alpha = 0.5;
        double bluetoothPowerCost=bluetoothOnCost*alpha+bluetoothActiveCost*(1-alpha);
        Log.i("bluetoothPowerConsumption", "Bluetooth power consumption is:"+bluetoothPowerCost); 
        return bluetoothPowerCost;   
    }
    
    public static double getAverageWifiPower(Context context){
        if (powCal == null)
            powCal=new PowerProfile(context);
        
        //double wifiScanCost=powCal.getAveragePower(powCal.POWER_WIFI_SCAN);
        double wifiOnCost=powCal.getAveragePower(powCal.POWER_WIFI_ON);
        Log.i("wifiOnCost", "Wifi on cost is:"+wifiOnCost);
        double wifiActiveCost=powCal.getAveragePower(powCal.POWER_WIFI_ACTIVE);
        Log.i("wifiActiveCost", "Wifi active cost is:"+wifiActiveCost);
        
        double alpha = 0.5;
        double wifiPowerCost=wifiOnCost*alpha+wifiActiveCost*(1-alpha);
        Log.i("wifiPowerConsumption", "Wifi power consumption is:"+wifiPowerCost); 
        return wifiPowerCost;   
    }
    
    public static double getAverageGpsPower(Context context){
        if (powCal == null)
            powCal=new PowerProfile(context);
        double gpsOnCost=powCal.getAveragePower(powCal.POWER_GPS_ON);
        Log.i("gpsPowerConsumption", "Gps power consumption is:"+gpsOnCost);
        return gpsOnCost;   
    }
    
    public static double [] getAverageCpuPower(Context context){
        
        double result[]=new double[3];
        if (powCal == null)
            powCal=new PowerProfile(context);
        
        double cpuActiveCost=powCal.getAveragePower(powCal.POWER_CPU_ACTIVE);
        double cpuIdleCost=powCal.getAveragePower(powCal.POWER_CPU_IDLE);
        double cpuAwakeCost=powCal.getAveragePower(powCal.POWER_CPU_AWAKE);
        
        result[0]=cpuActiveCost;
        result[1]=cpuIdleCost;
        result[2]=cpuAwakeCost;
        Log.i("cpuPowerConsumption", "When cpu is active:\n"+cpuActiveCost);
        Log.i("cpuPowerConsumption", "When cpu is idle:\n"+cpuIdleCost);
        Log.i("cpuPowerConsumption", "When cpu is awake:\n"+cpuAwakeCost);
        return result;   
    }

    public static double getAverageScreenPower(Context context){
        if (powCal == null)
            powCal=new PowerProfile(context);
        double screenCost=0;
        //double wifiScanCost=powCal.getAveragePower(powCal.POWER_WIFI_SCAN);
        double screenOnCost=powCal.getAveragePower(powCal.POWER_SCREEN_ON);
        
        if(SamplingLibrary.getScreenBrightness(context)==255){
            screenCost=powCal.getAveragePower(powCal.POWER_SCREEN_FULL);
        }
        else{
            double curBrightness=SamplingLibrary.getScreenBrightness(context);
            screenCost=curBrightness/255.0*powCal.getAveragePower(powCal.POWER_SCREEN_FULL);
        }
        
        double screenPowerCost=screenOnCost+screenCost;
        
        Log.i("screenPowerConsumption", "Screen power consumption is:"+screenPowerCost); 
        return screenPowerCost;   
    }
    
    public static double getAverageScreenOnPower(Context context){
        if (powCal == null)
            powCal=new PowerProfile(context);
        double screenOnCost=powCal.getAveragePower(powCal.POWER_SCREEN_ON);
        return screenOnCost;   
    }
    
    public static double getAverageVedioPower(Context context){
        if (powCal == null)
            powCal=new PowerProfile(context);
        double vedioOnCost=powCal.getAveragePower(powCal.POWER_VIDEO);
        Log.i("vedioPowerConsumption", "Vedio power consumption is:"+vedioOnCost);
        return vedioOnCost;   
    }
    
    public static double getAverageAudioPower(Context context){
        if (powCal == null)
            powCal=new PowerProfile(context);
        double audioOnCost=powCal.getAveragePower(powCal.POWER_AUDIO);
        Log.i("audioPowerConsumption", "Audio power consumption is:"+audioOnCost);
        return audioOnCost;   
    }
    
    public static double getAverageRadioPower(Context context){
        if (powCal == null)
            powCal=new PowerProfile(context);
        
        //double radioScanCost=powCal.getAveragePower(powCal.POWER_RADIO_SCANNING);
        double radioOnCost=powCal.getAveragePower(powCal.POWER_RADIO_ON);
        double radioActiveCost=powCal.getAveragePower(powCal.POWER_RADIO_ACTIVE);
        
        double radioPowerCost=radioOnCost*0.05+radioActiveCost*0.05;
        Log.i("radioPowerConsumption", "Radio power consumption is:"+radioPowerCost); 
        return radioPowerCost;   
    }
    
    public static void printAverageFeaturePower(Context context){
        if (powCal == null)
            powCal=new PowerProfile(context);
        
        /**
         * Power consumption when CPU is in power collapse mode.
         */
        double powerCpuActive=powCal.getAveragePower(powCal.POWER_CPU_ACTIVE);
        /**
         * Power consumption when CPU is awake (when a wake lock is held).  This
         * should be 0 on devices that can go into full CPU power collapse even
         * when a wake lock is held.  Otherwise, this is the power consumption in
         * addition to POWERR_CPU_IDLE due to a wake lock being held but with no
         * CPU activity.
         */
        double powerCpuAwake=powCal.getAveragePower(powCal.POWER_CPU_AWAKE);
        /**
         * Power consumption when CPU is in power collapse mode.
         */
        double powerCpuIdle=powCal.getAveragePower(powCal.POWER_CPU_IDLE);
        /**
         * Power consumption when Bluetooth driver is on.
         */
        double powerBluetoothOn=powCal.getAveragePower(powCal.POWER_BLUETOOTH_ON);
        /**
         * Power consumption when Bluetooth driver is transmitting/receiving.
         */
        double powerBluetoothActive=powCal.getAveragePower(powCal.POWER_BLUETOOTH_ACTIVE);
        /**
         * Power consumption when Bluetooth driver gets an AT command.
         */
        double powerBluetoothAtCommand=powCal.getAveragePower(powCal.POWER_BLUETOOTH_AT_CMD);
        /**
         * Power consumption when cell radio is on but not on a call.
         */
        double powerRadioOn=powCal.getAveragePower(powCal.POWER_RADIO_ON);
        /**
         * Power consumption when talking on the phone.
         */
        double powerRadioActive=powCal.getAveragePower(powCal.POWER_RADIO_ACTIVE);
        /**
         * Power consumption when cell radio is hunting for a signal.
         */
        double powerRadioScanning=powCal.getAveragePower(powCal.POWER_RADIO_SCANNING);
        /**
         * Power consumption when screen is on, not including the backlight power.
         */
        double powerScreenOn=powCal.getAveragePower(powCal.POWER_SCREEN_ON);
        /**
         * Power consumption at full backlight brightness. If the backlight is at
         * 50% brightness, then this should be multiplied by 0.5
         */
        double powerScreenFull=powCal.getAveragePower(powCal.POWER_SCREEN_FULL);
        /**
         * Power consumption when GPS is on.
         */
        double powerGpsOn=powCal.getAveragePower(powCal.POWER_GPS_ON);
        /**
         * Power consumption when WiFi driver is on.
         */
        double powerWifiOn=powCal.getAveragePower(powCal.POWER_WIFI_ON);
        /**
         * Power consumption when WiFi driver is transmitting/receiving.
         */
        double powerWifiActive=powCal.getAveragePower(powCal.POWER_WIFI_ACTIVE);
        /**
         * Power consumption when WiFi driver is scanning for networks.
         */
        double powerWifiScan=powCal.getAveragePower(powCal.POWER_WIFI_SCAN);
        /**
         * Power consumed by any media hardware when playing back video content. This is in addition
         * to the CPU power, probably due to a DSP.
         */
        double powerVedioOn=powCal.getAveragePower(powCal.POWER_VIDEO);
        /**
         * Power consumed by the audio hardware when playing back audio content. This is in addition
         * to the CPU power, probably due to a DSP and / or amplifier.
         */
        double powerAudioOn=powCal.getAveragePower(powCal.POWER_AUDIO);
        /**
         * Battery capacity in milliAmpHour (mAh).
         */
        double batteryCapacity=powCal.getBatteryCapacity();
        
        Log.i(TAG, "Power consumption when CPU is active"+powerCpuActive);
        Log.i(TAG, "Power consumption when CPU is awake"+powerCpuAwake);
        Log.i(TAG, "Power consumption when CPU is idle"+powerCpuIdle);
        Log.i(TAG, "Power consumption when bluetooth is on "+powerBluetoothOn);
        Log.i(TAG, "Power consumption when bluetooth is active "+powerBluetoothActive);
        Log.i(TAG, "Power consumption when bluetooth is at command "+powerBluetoothAtCommand);
        Log.i(TAG, "Power consumption when radio is on "+powerRadioOn);
        Log.i(TAG, "Power consumption when radio is active "+powerRadioActive);
        Log.i(TAG, "Power consumption when radio is scanning"+powerRadioScanning);
        Log.i(TAG, "Power consumption when screen is on "+powerScreenOn);
        Log.i(TAG, "Power consumption when screen is full "+powerScreenFull);
        Log.i(TAG, "Power consumption when Gps is on "+powerGpsOn);
        Log.i(TAG, "Power consumption when wifi is on "+powerWifiOn);
        Log.i(TAG, "Power consumption when wifi is active "+powerWifiActive);
        Log.i(TAG, "Power consumption when wifi is scanning "+powerWifiScan);
        Log.i(TAG, "Power consumption when vedio is on "+powerVedioOn);
        Log.i(TAG, "Power consumption when audio is on "+powerAudioOn);
        Log.i(TAG, "Battery capacity is "+batteryCapacity);
    }
    
    public static double bluetoothBenefit(Context context){
        double bluetoothPowerCost=SamplingLibrary.getAverageBluetoothPower(context);
        Log.d("bluetoothPowerCost", "Bluetooth power cost: " + bluetoothPowerCost);
        double batteryCapacity=SamplingLibrary.getBatteryCapacity(context);
        Log.d("batteryCapacity", "Battery capacity: " + batteryCapacity);
        
        double benefit=batteryCapacity/bluetoothPowerCost;
        Log.d("BluetoothPowerBenefit", "Bluetooth power benefit: " + benefit);
        return benefit;
        }
    
    public static double wifiBenefit(Context context){
        double wifiPowerCost= getAverageWifiPower(context);
        Log.d("wifiPowerCost", "wifi power cost: " + wifiPowerCost);
        double batteryCapacity= getBatteryCapacity(context);
        Log.d("batteryCapacity", "Battery capacity: " + batteryCapacity);
        
        // This is not that simple. We have to compare with Carat battery life or power profile battery life -- without wifi. --Eemil
        
        double benefit=(batteryCapacity/wifiPowerCost);
        Log.d("wifiPowerBenefit", "wifi power benefit: " + benefit);
        return benefit;
        }
    
    public static double gpsBenefit(Context context){
        double gpsPowerCost= getAverageGpsPower(context);
        Log.d("gpsPowerCost", "gps power cost: " + gpsPowerCost);
        double batteryCapacity= getBatteryCapacity(context);
        Log.d("batteryCapacity", "Battery capacity: " + batteryCapacity);
        double benefit=batteryCapacity/gpsPowerCost;
        Log.d("gpsPowerBenefit", "gps power benefit: " + benefit);
        return benefit;
       }
       
    public static double screenBrightnessBenefit(Context context){
         double screenPowerCost= getAverageScreenPower(context);
         Log.d("screenPowerCost", "screen power cost: " + screenPowerCost);
         double batteryCapacity= getBatteryCapacity(context);
         Log.d("batteryCapacity", "Battery capacity: " + batteryCapacity);
         double benefit=batteryCapacity/screenPowerCost;
         Log.d("screenPowerBenefit", "screen power benefit: " + benefit);
         return benefit;
       }
}
